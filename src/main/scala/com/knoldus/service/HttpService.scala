package com.knoldus.service

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.stream.ActorMaterializer
import com.knoldus.repo.VolunteerRepositoryImpl

import scala.concurrent.ExecutionContextExecutor


object HttpService extends App with Routes with VolunteerRepositoryImpl {

  implicit val system: ActorSystem = ActorSystem()

  implicit val materializer = ActorMaterializer()

  implicit val dispatcher: ExecutionContextExecutor = system.dispatcher

  ddl.onComplete {
    _ =>
      Http().bindAndHandle(routes, "localhost", 9000)
  }

}

