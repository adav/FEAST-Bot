package com.feastwithus.typeform

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.client.RequestBuilding
import akka.http.scaladsl.model.StatusCodes._
import akka.http.scaladsl.model._
import akka.http.scaladsl.model.headers.RawHeader
import akka.http.scaladsl.unmarshalling.Unmarshal
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{Flow, Sink, Source}
import akka.util.ByteString
import org.json4s.DefaultFormats
import org.json4s._
import org.json4s.native.JsonMethods._


import scala.concurrent.{ExecutionContextExecutor, Future}
import scala.util.{Failure, Success, Try}

/**
  * Created by adav on 15/09/2016.
  */
object TypeformService {

  implicit val system: ActorSystem = ActorSystem()

  implicit val materializer = ActorMaterializer()

  implicit val dispatcher: ExecutionContextExecutor = system.dispatcher

  implicit val formats = DefaultFormats


  lazy val typeformConnectionFlow: Flow[HttpRequest, HttpResponse, Any] =
    Http().outgoingConnectionHttps("api.typeform.io")

  def typeformRequest(request: HttpRequest): Future[HttpResponse] =
    Source.single(request).via(typeformConnectionFlow).runWith(Sink.head)

  def createNewTypeform(body: String): Future[Try[String]] = {

    val entity = ByteString.fromString(body)
    val token = sys.env.getOrElse("TYPEFORMIO", "")

    typeformRequest(
      RequestBuilding.Post("/v0.4/forms", entity)
      .withHeaders(RawHeader("X-API-TOKEN", token))
    ).flatMap {
      case HttpResponse(Created, _, e, _) => Unmarshal(e).to[String].map(parse(_).extract[TypeformPostResult]).map { result =>
        val url = result._links.filter(_.rel equals "form_render").head.href
        Success(url)
      }
      case HttpResponse(statusCode, _, _, _) =>
        Future.successful(Failure(new Exception(s"Failed to create Typeform with statuscode=$statusCode")))
      case mystery @ _ =>
        Future.successful(Failure(new Exception(s"Failed to create Typeform with mystery=${mystery.toString()}")))
    }
  }

  case class TypeformPostResult(_links: List[TypeformPostResultLinks])
  case class TypeformPostResultLinks(rel: String, href: String)

}

