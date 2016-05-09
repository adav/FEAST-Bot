package com.knoldus.repo


import com.knoldus.connection.{DBComponent, H2DBImpl}

import scala.concurrent.Future


trait BankRepository extends BankTable {
  this: DBComponent =>

  import driver.api._

  def create(bank: Bank): Future[Int] = db.run {
    bankTableAutoInc += bank
  }


  def update(bank: Bank): Future[Int] = db.run {
    bankTableQuery.filter(_.id === bank.id.get).update(bank)
  }


  def getById(id: Int): Future[Option[Bank]] = db.run {
    bankTableQuery.filter(_.id === id).result.headOption
  }


  def getAll(): Future[List[Bank]] = db.run {
    bankTableQuery.to[List].result
  }


  def delete(id: Int): Future[Int] = db.run {
    bankTableQuery.filter(_.id === id).delete
  }

  def ddl = db.run {
    bankTableQuery.schema.create
  }

}

trait BankTable {
  this: DBComponent =>

  import driver.api._

  protected val bankTableQuery = TableQuery[BankTable]

  protected def bankTableAutoInc = bankTableQuery returning bankTableQuery.map(_.id)

  class BankTable(tag: Tag) extends Table[Bank](tag, "bank") {
    val id = column[Int]("id", O.PrimaryKey, O.AutoInc)
    val name = column[String]("name")

    def * = (name, id.?) <>(Bank.tupled, Bank.unapply)

  }

}

//for demo(connected to H2 in memory database )
trait BankRepositoryImpl extends BankRepository with H2DBImpl

//use this for production
//trait BankRepositoryImpl extends BankRepository with MySQLDBImpl

case class Bank(name: String, id: Option[Int] = None)
