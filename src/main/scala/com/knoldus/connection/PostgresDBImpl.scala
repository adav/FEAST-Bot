package com.knoldus.connection

import java.net.URI

import org.slf4j.LoggerFactory

trait PostgresDBImpl extends DBComponent {

  val driver = slick.driver.PostgresDriver
  val db = PostgresDB.connectionPool

}

private[connection] object PostgresDB {

  val logger = LoggerFactory.getLogger(this.getClass)

  val dbURL = getHerokuDbUrl

  import slick.driver.PostgresDriver.api._

  logger.info(s"Setting up db at DATABASE_URL=$dbURL .......")

  val connectionPool = Database.forURL(url = dbURL, driver = "org.postgresql.Driver" )

  connectionPool.source.createConnection().close()

  private def getHerokuDbUrl: String = sys.env.get("DATABASE_URL") match {
    case Some(herokuUrl) =>
      val uri = new URI(herokuUrl)

      val username = uri.getUserInfo.split(":").head
      val password = uri.getUserInfo.split(":").tail

      s"jdbc:postgresql://${uri.getHost}:${uri.getPort}${uri.getPath}?user=$username&password=$password"

    case None => "jdbc:postgresql://localhost:5432/?user=adav&password=password"
  }

}

// local dev run:
// docker run --name feast-postgres -p 5432:5432 -e POSTGRES_PASSWORD=password -e POSTGRES_USER=adav -d postgres