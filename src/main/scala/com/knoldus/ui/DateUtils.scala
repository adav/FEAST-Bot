package com.knoldus.ui

import java.sql.Date
import java.time.DayOfWeek._
import java.time.{DayOfWeek, LocalDate}
import java.time.format.{DateTimeFormatter, TextStyle}
import java.time.temporal.ChronoUnit._
import java.time.temporal.TemporalAdjusters
import java.util.Locale

/**
  * Created by adav on 16/09/2016.
  */
object DateUtils {

  val datePattern = "dd MMM uuuu"

  def convertToSqlDates(dates: List[String]): List[Date] = dates.map { date =>
    Date.valueOf(parseHumanDate(date))
  }

  def findNextDays(weeks: Int = 4, dayOfWeek: DayOfWeek = THURSDAY): Seq[LocalDate] = {
    val formatter = DateTimeFormatter.ofPattern(datePattern)

    (0 until weeks).map { weeksAhead =>
      val now = LocalDate.now().plus(weeksAhead, WEEKS)
      now.`with`(TemporalAdjusters.next(dayOfWeek))
    }
  }

  def formatHumanDate(date: LocalDate, includeDayOfTheWeek: Boolean = false): String = {

    def ordinalise(n: Int): String = n match {
      case 1 => "1st"
      case 2 => "2nd"
      case 3 => "3rd"
      case 21 => "21st"
      case 22 => "22nd"
      case 23 => "23rd"
      case 31 => "31st"
      case num => num + "th"
    }

    val dayOfTheWeek = date.getDayOfWeek.getDisplayName(TextStyle.FULL, Locale.ENGLISH)
    val month = date.getMonth.getDisplayName(TextStyle.FULL, Locale.ENGLISH)

    val formattedDate = s"${ordinalise(date.getDayOfMonth)} $month ${date.getYear}"

    if (includeDayOfTheWeek) dayOfTheWeek + " " + formattedDate
    else formattedDate
  }

  def parseHumanDate(date: String): LocalDate = {
    val dateMinusOrdinal = date
      .replace("st ", "")
      .replace("nd ", "")
      .replace("rd ", "")
      .replace("th ", "")

    LocalDate.parse(date, DateTimeFormatter.ofPattern("EEEE d MMMM YYYY"))
  }

}