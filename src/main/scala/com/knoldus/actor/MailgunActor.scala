package com.knoldus.actor

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
import com.knoldus.mailgun.MailgunUtil
import com.knoldus.repo.Volunteer
import com.knoldus.typeform.TypeformResult

import scala.concurrent.{ExecutionContextExecutor, Future}
import scala.util.{Failure, Success, Try}

/**
  * Created by adav on 16/09/2016.
  */
class MailgunActor extends Actor with ActorLogging {

  def receive = {
    case SendTypeformReceiveThankYouEmail(volunteer) => {
      val randEmailIndentifier = UUID.randomUUID.toString

      log.info(s"Sending thank you email to: ${volunteer.firstname} ${volunteer.lastname} for dates=${volunteer.dates.mkString(",")} $randEmailIndentifier")

      sendEmail(volunteer.firstname, volunteer.email, "FEAST! Thank you!", MailgunUtil.thankYouEmailBody(volunteer.firstname, volunteer.dates.toVector.map(_.toString)))
        .onSuccess {
          case Success(Done) => log.info(s"Email sent $randEmailIndentifier")
          case Failure(e) => log.error(s"Failed to send email $randEmailIndentifier", e)
        }
    }
    case SendReminderEmail(volunteer) => {
      log.info(s"Sending reminder email to: ${volunteer.firstname} ${volunteer.surname} for ${volunteer.`event_date`.toString}")
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
      "from" -> "Mailgun Sandbox <postmaster@sandboxc22a6adf73294704ac7c29e0dd5e48ae.mailgun.org>",
      "to" -> address,
      "subject" -> subject,
      "text" -> messageBody
    )

    mailgunRequest(
      RequestBuilding.Post("/v3/sandboxc22a6adf73294704ac7c29e0dd5e48ae.mailgun.org/messages", entity)
        .withHeaders(Authorization(BasicHttpCredentials("api", token)))
    ).flatMap {
      case HttpResponse(OK, _, _, _) => Future(Success(Done))
      case HttpResponse(statusCode, _, e, _) =>
        Future.successful(Failure(new Exception(s"Sent email=${entity.toString} received response=${e.toString()}")))
      case mystery@_ =>
        Future.successful(Failure(new Exception(s"Failed to create Typeform with mystery=${mystery.toString()}")))
    }
  }

}

case class SendTypeformReceiveThankYouEmail(volunteer: TypeformResult)
case class SendReminderEmail(volunteer: Volunteer)
