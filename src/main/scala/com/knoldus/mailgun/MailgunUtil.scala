package com.knoldus.mailgun

import java.time.LocalDate

/**
  * Created by adav on 16/09/2016.
  */
object MailgunUtil {

  def reminderEmailBody(firstname: String, date: LocalDate) =
    s"""
       |Dear ${firstname.capitalize},
       |We're looking forward to seeing you at Conway House for FEAST! on ${date.toString}.
       |
       |We'll be kicking off at 6pm and the address is 20-22 Quex Rd, North Maida Vale, London NW6 4PG
       |
       |See you soon and lots of love,
       |FEAST! Team
     """.stripMargin

  def thankYouEmailBody(firstname: String, dates: Vector[String]) =
    s"""
       |Dear ${firstname.capitalize},
       |
       |Thank you for signing up to FEAST!, the food sustainability social action project.
       |
       |We're looking forward to seeing you at Conway House for FEAST! on:
       |${dates.mkString("\n")}
       |
       |We be kick off each evening at 6pm and the address is 20-22 Quex Rd, North Maida Vale, London NW6 4PG
       |
       |See you soon and lots of love,
       |FEAST! Team
     """.stripMargin

}