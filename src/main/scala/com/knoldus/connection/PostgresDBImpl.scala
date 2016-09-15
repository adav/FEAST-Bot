package com.knoldus.connection

import org.slf4j.LoggerFactory
import slick.driver.PostgresDriver

trait PostgresDBImpl extends DBComponent {

  val driver = slick.driver.PostgresDriver
  val db: PostgresDriver.backend.Database = PostgresDB.connectionPool

}

private[connection] object PostgresDB {
  val logger = LoggerFactory.getLogger(this.getClass)

  val dbURL = sys.env.getOrElse("DATABASE_URL", "~~FAILED TO READ ENV: DATABASE_URL~~")

  import slick.driver.PostgresDriver.api._

  logger.info(s"Setting up db at DATABASE_URL=$dbURL .......")

  val connectionPool = Database.forURL(url = dbURL)

}
