package com.knoldus.mailgun

import akka.actor.ActorSystem
import akka.actor.Status.Success
import akka.http.scaladsl.Http
import akka.http.scaladsl.client.RequestBuilding
import akka.http.scaladsl.model.headers.{Authorization, BasicHttpCredentials}
import akka.http.scaladsl.model.{FormData, HttpRequest, HttpResponse, StatusCodes}
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{Flow, Sink, Source}
import org.json4s.DefaultFormats

import scala.concurrent.{ExecutionContextExecutor, Future}
import scala.util.Failure

/**
  * Created by adav on 16/09/2016.
  */
object MailgunService {
  implicit val system: ActorSystem = ActorSystem()

  implicit val materializer = ActorMaterializer()

  implicit val dispatcher: ExecutionContextExecutor = system.dispatcher

  implicit val formats = DefaultFormats


  lazy val mailgunConnectionFlow: Flow[HttpRequest, HttpResponse, Any] =
    Http().outgoingConnectionHttps("api.mailgun.net")

  def mailgunRequest(request: HttpRequest): Future[HttpResponse] =
    Source.single(request).via(mailgunConnectionFlow).runWith(Sink.head)

  def sendEmail(name: String, address: String, subject: String, messageBody: String) = {
    val token = sys.env.getOrElse("MAILGUN", "")
    val entity = FormData(
      "from" -> "Mailgun Sandbox <postmaster@sandboxc22a6adf73294704ac7c29e0dd5e48ae.mailgun.org>",
      "to" -> address,
      "subject" -> subject,
      "text" -> messageBody
    )

    mailgunRequest(
      RequestBuilding.Post("/v0.4/forms", entity)
        .withHeaders(Authorization(BasicHttpCredentials("api", token)))
    ).flatMap { response =>
      response.status match {
        case StatusCodes.OK => Future.successful(Success(""))
        case status => Future.successful(Failure(new Exception(s"Error code=$status")))
      }
    }

  }
}