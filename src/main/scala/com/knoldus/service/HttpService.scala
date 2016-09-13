package com.knoldus.service

import java.sql.{Date, Timestamp}
import java.time.{LocalDate, LocalDateTime, Month}

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.stream.ActorMaterializer
import com.knoldus.repo.{Volunteer, VolunteerRepositoryImpl}

import scala.concurrent.ExecutionContextExecutor


object HttpService extends App with Routes with VolunteerRepositoryImpl {

  implicit val system: ActorSystem = ActorSystem()

  implicit val materializer = ActorMaterializer()

  implicit val dispatcher: ExecutionContextExecutor = system.dispatcher

  ddl.onComplete {
    _ =>
      val testVolunteer1 = Volunteer(
        firstname = "Andrew",
        surname = "Davidson",
        telephone = "07123456789",
        email = "a@b.com",
        eventDay = 10,
        eventMonth = 1,
        eventYear = 1991,
        eventDate = Date.valueOf(LocalDate.of(1991, Month.JANUARY, 10)),
        creationDate = Timestamp.valueOf(LocalDateTime.now())
      )

      val testVolunteer2 = Volunteer(
        firstname = "Zac",
        surname = "Kenton",
        telephone = "07987654321",
        email = "c@d.com",
        eventDay = 2,
        eventMonth = 2,
        eventYear = 1992,
        eventDate = Date.valueOf(LocalDate.of(1992, Month.FEBRUARY, 2)),
        creationDate = Timestamp.valueOf(LocalDateTime.now())
      )

      create(testVolunteer1)
      create(testVolunteer2)
      Http().bindAndHandle(routes, "localhost", 9000)
  }

}

