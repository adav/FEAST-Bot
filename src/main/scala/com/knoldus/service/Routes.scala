package com.knoldus.service


import java.sql.Date
import java.time.{LocalDate, Month}

import akka.http.scaladsl.model.HttpResponse
import akka.http.scaladsl.server.Directives._
import com.knoldus.json.JsonHelper
import com.knoldus.repo.{Volunteer, VolunteerRepository}

import scala.concurrent.ExecutionContextExecutor

trait Routes extends JsonHelper {
  this: VolunteerRepository =>

  implicit val dispatcher: ExecutionContextExecutor

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
          entity(as[String]) { bankJson =>
            complete {
              val volunteer = parse(bankJson).extract[Volunteer]
              create(volunteer).map { result => HttpResponse(entity = "New volunteer has been saved successfully") }
            }
          }
        }
      }

  }
}

//    path("bank" / IntNumber) { id =>
//      get {
//        complete {
//          getById(id).map {
//            _ match {
//              case Some(result) => HttpResponse(entity = write(result))
//              case None => HttpResponse(entity = "This bank does not exist")
//            }
//          }
//        }
//      }
//    } ~
//      path("bank" / "all") {
//        get {
//          complete {
//            getAll().map { result => HttpResponse(entity = write(result)) }
//          }
//        }
//      } ~
//      path("bank" / "save") {
//        post {
//          entity(as[String]) { bankJson =>
//            complete {
//              val bank = parse(bankJson).extract[Bank]
//              create(bank).map { result => HttpResponse(entity = "Bank has  been saved successfully") }
//            }
//          }
//        }
//      } ~
//      path("bank" / "update") {
//        post {
//          entity(as[String]) { bankJson =>
//            complete {
//              val bank = parse(bankJson).extract[Bank]
//              update(bank).map { result => HttpResponse(entity = "Bank has  been updated successfully") }
//            }
//          }
//        }
//      } ~
//      path("bank" / "delete" / IntNumber) { id =>
//        post {
//          complete {
//            delete(id).map { result => HttpResponse(entity = "Bank has been deleted successfully") }
//
//          }
//        }
//      }
//  }
//
//}



