package com.feastwithus.repo

import java.sql.{Date, Timestamp}
import java.util.concurrent.TimeUnit

import com.feastwithus.connection.{DBComponent, PostgresDBImpl}

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, ExecutionContext, Future}


trait VolunteerRepository extends VolunteerTable {
  this: DBComponent =>

  import driver.api._

  def create(volunteer: Volunteer): Future[Int] = db.run {
    volunteerTableAutoInc += volunteer
  }


  def update(bank: Volunteer): Future[Int] = db.run {
    volunteerTableQuery.filter(_.id === bank.id.get).update(bank)
  }


  def getById(id: Int): Future[Option[Volunteer]] = db.run {
    volunteerTableQuery.filter(_.id === id).result.headOption
  }


  def getAll(): Future[List[Volunteer]] = db.run {
    volunteerTableQuery.to[List].result
  }

  def getAllForEvent(eventDate: Date): Future[List[Volunteer]] = db.run {
    volunteerTableQuery.filter(x => x.eventDate.? === eventDate).to[List].result
  }

  def getAllEventDates(): Future[List[Date]] = db.run {
    volunteerTableQuery.map(_.eventDate).distinct.sorted.to[List].result
  }

  def getNameForEmail(email: String): Future[String] = db.run {
    volunteerTableQuery.filter(x => x.email === email).map(x => x.firstname ++ " " ++ x.surname).result.head
  }

  def getTopRegistratedAttendeesEmails(num: Int): Future[List[(String, Int)]] = db.run {
    volunteerTableQuery.groupBy(_.email)
      .map { case (email, group) => email -> group.map(_.email).length }
      .sortBy(x=> x._2.desc)
      .take(num)
      .to[List]
      .result
  }

  def getTopRegisteredAttendeesNames(num: Int)(implicit ec: ExecutionContext): Future[List[(String, Int)]] = {
    getTopRegistratedAttendeesEmails(num).map { emailCountList =>
      emailCountList.map { case (email, count) =>
        Await.result(getNameForEmail(email), Duration(5, TimeUnit.SECONDS)) -> count
      }
    }
  }

  def deleteById(id: Int): Future[Int] = db.run {
    volunteerTableQuery.filter(_.id === id).delete
  }

  def publicSynchronousDelete(email: String, id: Int): Volunteer = {
    val volunteerFuture = db.run { volunteerTableQuery.filter(_.email === email).filter(_.id === id).result.head }
    val volunteer = Await.result(volunteerFuture, Duration(5, TimeUnit.SECONDS))
    val deleteFuture = db.run { volunteerTableQuery.filter(_.email === email).filter(_.id === id).delete }
    Await.result(volunteerFuture, Duration(5, TimeUnit.SECONDS))

    volunteer
  }

  def createTable =  {
    volunteerTableQuery.schema.create
  }

  def dropTable =  {
    volunteerTableQuery.schema.drop
  }

  def ddl = db.run(createTable)

}

trait VolunteerTable {
  this: DBComponent =>

  import driver.api._

  protected val volunteerTableQuery = TableQuery[VolunteerTable]

  protected def volunteerTableAutoInc = volunteerTableQuery returning volunteerTableQuery.map(_.id)

  class VolunteerTable(tag: Tag) extends Table[Volunteer](tag, "volunteer") {
    val id = column[Int]("id", O.PrimaryKey, O.AutoInc)
    val firstname = column[String]("firstname")
    val surname = column[String]("surname")
    val telephone = column[String]("telephone")
    val email = column[String]("email")
    val facilitator = column[Boolean]("is_facilitator")
    val eventDate = column[Date]("event_date")
    val creationDate = column[Timestamp]("creation_date")

    def * = (firstname, surname, telephone, email, facilitator, eventDate, creationDate, id.?) <>(Volunteer.tupled, Volunteer.unapply)
  }

}

//for demo(connected to H2 in memory database )
//trait VolunteerRepositoryImpl extends VolunteerRepository with H2DBImpl

//use this for production
trait VolunteerRepositoryImpl extends VolunteerRepository with PostgresDBImpl

case class Volunteer(firstname: String, surname: String, telephone: String, email: String, facilitator: Boolean, `event_date`: Date, `creation_date`: Timestamp, id: Option[Int] = None)
