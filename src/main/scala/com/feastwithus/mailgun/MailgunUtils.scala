package com.feastwithus.mailgun

/**
  * Created by adav on 16/09/2016.
  */
object MailgunUtils {

  def reminderEmailBody(firstname: String, date: String) =
    s"""
       |Dear ${firstname.capitalize},
       |
       |We're looking forward to seeing you for FEAST! on $date.
       |
       |Please join us anytime from 5.30pm onwards at Conway House, 20-22 Quex Rd, North Maida Vale, London NW6 4PG
       |
       |$tupperwareNotice
       |
       |See you soon and lots of love,
       |FEAST! Team
       |
       |
       |
     """.stripMargin

  def thankYouEmailBody(firstname: String, dates: Vector[String], facilitator: Boolean) = {

    val facitatorText: (String, String) = facilitator match {
      case true => ("Thank you for signing up to facilitate FEAST!", "your help running the evening")
      case false => ("Thank you for signing up to FEAST!", "seeing you")
    }

    s"""
       |Dear ${firstname.capitalize},
       |
       |${facitatorText._1}, the food sustainability community project.
       |
       |We're looking forward to ${facitatorText._2} at Conway House for FEAST! on:
       |${dates.mkString("\n")}
       |
       |Please join us anytime from 5.30pm onwards at Conway House, 20-22 Quex Rd, North Maida Vale, London NW6 4PG
       |
       |$tupperwareNotice
       |
       |See you soon and lots of love,
       |FEAST! Team
       |
       |
       |
     """.stripMargin
  }

  def deleteEmailBody(firstname: String, date: String) =
    s"""
       |Dear ${firstname.capitalize},
       |
       |Looks like you can't make FEAST! this time, no problem. We look forward to seeing you at another FEAST! soon.
       |We've removed you from the week you selected: $date
       |
       |Sign up to future weeks at https://feastwithus.org.uk
       |
       |See you soon and lots of love,
       |FEAST! Team
     """.stripMargin

  val tupperwareNotice =
    "If you can, bring along any empty jam jars, reusable food packaging or tupperware " +
    "that could be used to provide an extra meal for someone who comes late."

}