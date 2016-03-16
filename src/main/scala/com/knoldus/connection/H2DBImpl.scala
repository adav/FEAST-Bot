package com.knoldus.connection

import org.slf4j.LoggerFactory

/**
  * Only for demo
  */
trait H2DBImpl extends DBComponent {



  val logger = LoggerFactory.getLogger(this.getClass)

  val driver = slick.driver.H2Driver

  import driver.api._

  val h2Url = "jdbc:h2:mem:test;DB_CLOSE_DELAY=-1"

  val db: Database = {
    logger.info("Creating test connection ..................................")
    Database.forURL(url = h2Url, driver = "org.h2.Driver")

   }
}
