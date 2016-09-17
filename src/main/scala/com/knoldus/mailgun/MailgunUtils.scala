package com.knoldus.mailgun

/**
  * Created by adav on 16/09/2016.
  */
object MailgunUtils {

  def reminderEmailBody(firstname: String, date: String) =
    s"""
       |Dear ${firstname.capitalize},
       |We're looking forward to seeing you at Conway House for FEAST! on ${date}.
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