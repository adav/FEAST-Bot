package com.knoldus.repo

import com.knoldus.connection.DBComponent
import org.slf4j.LoggerFactory


trait TestH2DBImpl extends DBComponent {



  val logger = LoggerFactory.getLogger(this.getClass)

  val driver = slick.driver.H2Driver

  import driver.api._
  val h2Url = "jdbc:h2:mem:demo;MODE=MySql;DATABASE_TO_UPPER=false;INIT=runscript from 'src/test/resources/schema.sql'\\;runscript from 'src/test/resources/schemadata.sql'"

  val db: Database = {
    logger.info("Creating test connection ..................................")
    Database.forURL(url = h2Url, driver = "org.h2.Driver")
  }
}
