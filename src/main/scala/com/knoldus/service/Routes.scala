package com.knoldus.service


import java.sql.{Date, Timestamp}
import java.time.{LocalDate, LocalDateTime, Month}

import akka.actor.ActorRef
import akka.http.scaladsl.model.{ContentTypes, HttpEntity, HttpResponse}
import akka.http.scaladsl.server.Directives._
import com.knoldus.actor.SendTypeformReceiveThankYouEmail
import com.knoldus.json.JsonHelper
import com.knoldus.repo.{Volunteer, VolunteerRepository}
import com.knoldus.typeform.{TypeformService, TypeformUtils}
import com.knoldus.ui.{DateUtils, StaticPageUtil}
import org.slf4j.LoggerFactory

import scala.concurrent.{ExecutionContextExecutor, Future}
import scala.util.{Failure, Success}

trait Routes extends JsonHelper {
  this: VolunteerRepository =>

  implicit val dispatcher: ExecutionContextExecutor
  implicit val mailgunActor: ActorRef

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
            val typeformResult = TypeformUtils.processWebhook(json)

            log.info(typeformResult.toString)

            val volunteerPerEvents = typeformResult.dates.map { date =>
              Volunteer(
                firstname = typeformResult.firstname,
                surname = typeformResult.lastname,
                telephone = typeformResult.mobile,
                email = typeformResult.email,
                `event_date` = date,
                `creation_date` = Timestamp.valueOf(LocalDateTime.now())
              )
            }

            onSuccess(Future.sequence(volunteerPerEvents.map(create))){ result =>
              complete {
                mailgunActor ! SendTypeformReceiveThankYouEmail(typeformResult)
                HttpResponse(entity = "New volunteer dates saved successfully")
              }
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
      } ~
      path("who") {
        get {
          complete {
            val Seq(thisWeekDate, nextWeekDate) = DateUtils.findNextDays(weeks = 2)

            val html = for {
              thisWeekVolunteers <- getAllForEvent(Date.valueOf(thisWeekDate))
              nextWeekVolunteers <- getAllForEvent(Date.valueOf(nextWeekDate))
            } yield StaticPageUtil.generatePublicWhosComingHtml(
              thisWeekVolunteers = thisWeekVolunteers,
              thisWeekDate = DateUtils.formatHumanDate(thisWeekDate, includeDayOfTheWeek = true),
              nextWeekVolunteers = nextWeekVolunteers,
              nextWeekDate = DateUtils.formatHumanDate(nextWeekDate, includeDayOfTheWeek = true)
            )

            html.map(x => HttpResponse(entity = HttpEntity(ContentTypes.`text/html(UTF-8)`, x)))

          }
        }
      }

  }
}
