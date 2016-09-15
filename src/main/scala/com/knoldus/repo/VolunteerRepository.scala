package com.knoldus.repo

import java.sql.{Date, Timestamp}

import com.knoldus.connection.{DBComponent, H2DBImpl}

import scala.concurrent.Future


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


  def delete(id: Int): Future[Int] = db.run {
    volunteerTableQuery.filter(_.id === id).delete
  }

  def createTable =  {
    volunteerTableQuery.schema.create
  }

  def dropTable =  {
    volunteerTableQuery.schema.drop
  }


  def ddl = db.run {
    createTable
  }

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
    val eventDate = column[Date]("event_date")
    val creationDate = column[Timestamp]("creation_date")

    def * = (firstname, surname, telephone, email, eventDate, creationDate, id.?) <>(Volunteer.tupled, Volunteer.unapply)
  }

}

//for demo(connected to H2 in memory database )
trait VolunteerRepositoryImpl extends VolunteerRepository with H2DBImpl

//use this for production, but change for postgres
//trait VolunteerRepositoryImpl extends BankRepository with MySQLDBImpl

case class Volunteer(firstname: String, surname: String, telephone: String, email: String, `event_date`: Date, `creation_date`: Timestamp, id: Option[Int] = None)
