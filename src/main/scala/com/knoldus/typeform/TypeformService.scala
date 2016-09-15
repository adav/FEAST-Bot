package com.knoldus.typeform

import akka.http.scaladsl.Http
import akka.http.scaladsl.client.RequestBuilding
import akka.http.scaladsl.model._
import akka.http.scaladsl.model.headers.RawHeader
import akka.http.scaladsl.unmarshalling.Unmarshal
import akka.stream.scaladsl.{Flow, Sink, Source}
import akka.util.ByteString

import scala.concurrent.Future
import scala.util.{Failure, Success, Try}

/**
  * Created by adav on 15/09/2016.
  */
object TypeformService {
  lazy val typeformConnectionFlow: Flow[HttpRequest, HttpResponse, Any] =
    Http().outgoingConnectionHttps("api.typeform.io")

  def typeformRequest(request: HttpRequest): Future[HttpResponse] =
    Source.single(request).via(typeformConnectionFlow).runWith(Sink.head)

  def createNewTypeform(body: String): Future[Try[String]] = {
    val entity = ByteString.fromString(body) //Marshal(FormData()).to[RequestEntity]

    typeformRequest(
      RequestBuilding.Post("/v0.4/forms", entity)
      .withHeaders(RawHeader("X-API-TOKEN",""))
    ).flatMap {
      case HttpResponse(StatusCodes.OK, _, e, _) => Unmarshal(e).to[TypeformPostResult].map { result =>
        val url = result._links.filter(_.rel equals "url").head.href
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

