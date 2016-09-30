package com.feastwithus.actor

import java.util.UUID

import akka.Done
import akka.actor.{Actor, ActorLogging, ActorSystem}
import akka.http.scaladsl.Http
import akka.http.scaladsl.client.RequestBuilding
import akka.http.scaladsl.model.StatusCodes._
import akka.http.scaladsl.model.headers.{Authorization, BasicHttpCredentials}
import akka.http.scaladsl.model.{FormData, HttpRequest, HttpResponse}
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{Flow, Sink, Source}
import com.feastwithus.mailgun.MailgunUtils
import com.feastwithus.repo.Volunteer
import com.feastwithus.typeform.TypeformResult
import com.feastwithus.ui.{DateUtils, StaticPageUtil}

import scala.concurrent.{ExecutionContextExecutor, Future}
import scala.util.{Failure, Success, Try}

/**
  * Created by adav on 16/09/2016.
  */
class MailgunActor extends Actor with ActorLogging {

  def receive = {
    case SendTypeformReceiveThankYouEmail(volunteer) => {
      val id = UUID.randomUUID.toString

      log.info(s"Sending thank you email to: ${volunteer.firstname} ${volunteer.lastname} for dates=${volunteer.dates.mkString(",")} $id")

      val datesFormatted = volunteer.dates.toVector.map( d => DateUtils.formatHumanDate(d.toLocalDate, includeDayOfTheWeek = true))

      sendEmail(volunteer.firstname, volunteer.email, "FEAST! Thank you!", MailgunUtils.thankYouEmailBody(volunteer.firstname, datesFormatted, volunteer.facilitator))
        .onSuccess {
          case Success(Done) => log.info(s"Email sent $id")
          case Failure(e) => log.error(s"Failed to send email $id", e)
        }
    }
    case SendReminderEmail(volunteer) => {
      val id = UUID.randomUUID.toString

      log.info(s"Sending reminder email to: ${volunteer.firstname} ${volunteer.surname} for ${volunteer.`event_date`.toString} $id")

      val dateFormatted = DateUtils.formatHumanDate(volunteer.`event_date`.toLocalDate, includeDayOfTheWeek = true)

      sendEmail(volunteer.firstname, volunteer.email, "Reminder: FEAST! this Thursday",
        MailgunUtils.reminderEmailBody(volunteer.firstname, dateFormatted) + formatUnregisterLink(volunteer))
        .onSuccess {
          case Success(Done) => log.info(s"Email sent $id")
          case Failure(e) => log.error(s"Failed to send email $id", e)
        }
    }

    case SendDeleteEventEmail(volunteer) => {
      val id = UUID.randomUUID.toString

      log.info(s"Sending delete email to: ${volunteer.firstname} ${volunteer.surname} for ${volunteer.`event_date`.toString} $id")

      val dateFormatted = DateUtils.formatHumanDate(volunteer.`event_date`.toLocalDate, includeDayOfTheWeek = true)

      sendEmail(volunteer.firstname, volunteer.email, "FEAST! Notice", MailgunUtils.deleteEmailBody(volunteer.firstname, dateFormatted))
        .onSuccess {
          case Success(Done) => log.info(s"Email sent $id")
          case Failure(e) => log.error(s"Failed to send email $id", e)
        }
    }

    case _ => log.error("MailgunActor received unknown message")
  }

  implicit val system: ActorSystem = ActorSystem()
  implicit val materializer = ActorMaterializer()
  implicit val dispatcher: ExecutionContextExecutor = system.dispatcher

  lazy val mailgunConnectionFlow: Flow[HttpRequest, HttpResponse, Any] =
    Http().outgoingConnectionHttps("api.mailgun.net")

  def sendEmail(name: String, address: String, subject: String, messageBody: String): Future[Try[Done]] = {

    def mailgunRequest(request: HttpRequest): Future[HttpResponse] =
      Source.single(request).via(mailgunConnectionFlow).runWith(Sink.head)

    val token = sys.env.getOrElse("MAILGUN", "")
    val entity = FormData(
      "from" -> "FEAST! Team <postmaster@feastwithus.org.uk>",
      "to" -> address,
      "subject" -> subject,
      "text" -> messageBody
    )

    mailgunRequest(
      RequestBuilding.Post("/v3/feastwithus.org.uk/messages", entity)
        .withHeaders(Authorization(BasicHttpCredentials("api", token)))
    ).flatMap {
      case HttpResponse(OK, _, _, _) => Future(Success(Done))
      case HttpResponse(statusCode, _, e, _) =>
        Future.successful(Failure(new Exception(s"Sent email=${entity.toString} received response=${e.toString}")))
      case mystery@_ =>
        Future.successful(Failure(new Exception(s"Failed to create Typeform with mystery=${mystery.toString()}")))
    }
  }

  def formatUnregisterLink(volunteer: Volunteer): String = {
    "Can't make it? Unregister " + DateUtils.formatHumanDate(volunteer.`event_date`.toLocalDate) + " here: " + StaticPageUtil.makeUnregisterLink(volunteer)
  }

}

case class SendTypeformReceiveThankYouEmail(volunteer: TypeformResult)
case class SendReminderEmail(volunteer: Volunteer)
case class SendDeleteEventEmail(volunteer: Volunteer)
