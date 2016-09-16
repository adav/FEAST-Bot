package com.knoldus.service


import java.sql.{Date, Timestamp}
import java.time.{LocalDate, LocalDateTime, Month}

import akka.http.scaladsl.model.{ContentTypes, HttpEntity, HttpResponse}
import akka.http.scaladsl.server.Directives._
import com.knoldus.json.JsonHelper
import com.knoldus.mailgun.MailgunService
import com.knoldus.repo.{Volunteer, VolunteerRepository}
import com.knoldus.typeform.{TypeformService, TypeformUtils}
import org.slf4j.LoggerFactory

import scala.concurrent.{ExecutionContextExecutor, Future}
import scala.util.{Failure, Success}

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

                MailgunService.sendEmail(result.firstname, result.email, "Thanks for volunteering with FEAST!",
                  s"Dear ${result.firstname}, You're awesome! Love from FEAST!")
              }

              Future.sequence(futureCreates).map( result => HttpResponse(entity = "New volunteer dates saved successfully") )
            }
          }
        }
      } ~
    path("") {
      get {
        complete {
          TypeformService.createNewTypeform(TypeformUtils.createTypeformJsonRequest).map {
            case Success(url) =>
              HttpResponse(entity = HttpEntity(ContentTypes.`text/html(UTF-8)`, TypeformUtils.landingHtml(url)))
            case Failure(e) => HttpResponse(entity = HttpEntity(ContentTypes.`text/html(UTF-8)`, s"Failed: ${e.getMessage}"))
          }
        }
      }
    }

  }
}