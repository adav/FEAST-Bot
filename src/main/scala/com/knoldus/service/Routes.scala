package com.knoldus.service


import java.sql.{Date, Timestamp}
import java.time.{LocalDate, LocalDateTime, Month}

import akka.http.scaladsl.model.{ContentTypes, HttpEntity, HttpResponse}
import akka.http.scaladsl.server.Directives._
import com.knoldus.json.JsonHelper
import com.knoldus.repo.{Volunteer, VolunteerRepository}
import com.knoldus.typeform.TypeformUtils
import org.slf4j.LoggerFactory

import scala.concurrent.{ExecutionContextExecutor, Future}

trait Routes extends JsonHelper {
  this: VolunteerRepository =>

  implicit val dispatcher: ExecutionContextExecutor
  val log = LoggerFactory.getLogger(this.getClass)

  val routes = {
    path("volunteers") {
      get {
        complete {
          getAll().map { result => HttpResponse(entity = write(result)) }
        }
      }
    } ~
      path("volunteers" / IntNumber / IntNumber / IntNumber) { (year, month, day) =>
        get {
          complete {
            getAllForEvent(Date.valueOf(LocalDate.of(year, Month.of(month), day))).map { result => HttpResponse(entity = write(result)) }
          }
        }
      } ~
      path("volunteers" / "save") {
        post {
          entity(as[String]) { json =>
            complete {
              val volunteer = parse(json).extract[Volunteer]
              create(volunteer).map { result => HttpResponse(entity = "New volunteer has been saved successfully") }
            }
          }
        }
      } ~
      path("volunteers" / "typeform") {
        post {
          entity(as[String]) { json =>
            complete {
              val result = TypeformUtils.processWebhook(json)

              log.info(result.toString)

              val futureCreates = result.dates map { date =>
                val newVolunteer = Volunteer(
                  firstname = result.firstname,
                  surname = result.lastname,
                  telephone = result.mobile,
                  email = result.email,
                  `event_date` = date,
                  `creation_date` = Timestamp.valueOf(LocalDateTime.now())
                )
                log.info("Adding " + newVolunteer)
                create(newVolunteer)
              }

              Future.sequence(futureCreates).map( result => HttpResponse(entity = "New volunteer dates saved successfully") )
            }
          }
        }
      } ~
    path("") {
      get {
        complete {
          val html =
            s"""
              |<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
              |<html>
              |<head>
              |  <!--Add the title of your typeform below-->
              |  <title>All fields</title>
              |
              |  <!--CSS styles that ensure your typeform takes up all the available screen space (DO NOT EDIT!)-->
              |<style type="text/css">
              |    html{
              |      margin: 0;
              |      height: 100%;
              |      overflow: hidden;
              |    }
              |    iframe{
              |      position: absolute;
              |      left:0;
              |      right:0;
              |      bottom:0;
              |      top:0;
              |      border:0;
              |    }
              |  </style>
              |</head>
              |<body>
              |  <iframe id="typeform-full" width="100%" height="100%" frameborder="0" src="YOUR TYPEFORM URL HERE"></iframe>
              |  <script type="text/javascript" src="https://s3-eu-west-1.amazonaws.com/share.typeform.com/embed.js"></script>
              |</body>
              |</html>
            """.stripMargin

          HttpResponse(entity = HttpEntity(ContentTypes.`text/html(UTF-8)`, html))
        }
      }
    }

  }
}