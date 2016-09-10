package com.knoldus.repo

import java.sql.{Date, Timestamp}
import java.time.{LocalDate, LocalDateTime, Month}
import java.util.concurrent.TimeUnit

import org.scalatest.concurrent.ScalaFutures
import org.scalatest.time.{Millis, Seconds, Span}
import org.scalatest.{FunSuite, Matchers}

import scala.concurrent.Await
import scala.concurrent.duration.Duration


class VolunteerRepositoryTest extends FunSuite with VolunteerRepository with TestH2DBImpl with ScalaFutures with Matchers {

  implicit val defaultPatience = PatienceConfig(timeout = Span(5, Seconds), interval = Span(500, Millis))

  import scala.concurrent.ExecutionContext.Implicits.global

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

  test("Add new vol ") {
    Await.ready(ddl, Duration(5, TimeUnit.SECONDS))

    val givenWhen = for {
      createdVol1 <- create(testVolunteer1)

      results <- getAll()
    } yield (createdVol1, results)
    whenReady(givenWhen) { case (createdVol1, results) =>
      createdVol1 should be(1)
      results.map(_.firstname) should be(List(testVolunteer1.firstname))
    }
  }

  test("Get vol list") {

    Await.ready(ddl, Duration(5, TimeUnit.SECONDS))

    val givenWhen = for {
      createdVol1 <- create(testVolunteer1)
      createdVol2 <- create(testVolunteer2)

      results <- getAll()
    } yield results

    whenReady(givenWhen) { results =>
      results.map(_.firstname) should be(List(testVolunteer1.firstname, testVolunteer2.firstname))
    }
  }

  test("Get vol list for a date") {
    Await.ready(ddl, Duration(5, TimeUnit.SECONDS))


    val givenWhen = for {
      createdVol1 <- create(testVolunteer1)
      createdVol2 <- create(testVolunteer2)

      results <- getAllForEvent(Date.valueOf(LocalDate.of(1991, Month.JANUARY, 10)))
    } yield results

    whenReady(givenWhen) { results =>
      logger.info(results.mkString(","))
      results.map(_.firstname) should be(List(testVolunteer1.firstname))
    }
  }

}