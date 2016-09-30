package com.feastwithus.service

import akka.actor.{ActorRef, ActorSystem, Props}
import akka.http.scaladsl.Http
import akka.stream.ActorMaterializer
import com.feastwithus.actor.MailgunActor
import com.feastwithus.repo.VolunteerRepositoryImpl

import scala.concurrent.ExecutionContextExecutor
import scala.util.Properties


object HttpService extends App with Routes with VolunteerRepositoryImpl {

  implicit val system: ActorSystem = ActorSystem()

  implicit val materializer = ActorMaterializer()

  implicit val dispatcher: ExecutionContextExecutor = system.dispatcher

  implicit val mailgunActor: ActorRef = system.actorOf(Props[MailgunActor], "mailgun-actor")


//  Await.result(db.run(dropTable), Duration.Inf)

  ddl.onComplete {
    _ =>
      Http().bindAndHandle(
        handler = routes,
        interface = "0.0.0.0",
        port = Properties.envOrElse("PORT", "9000").toInt
      )
  }

}

