package com.knoldus.typeform

import java.sql.Date
import java.time.DayOfWeek._
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit.WEEKS
import java.time.temporal.TemporalAdjusters
import java.time.{DayOfWeek, LocalDate}

import org.json4s._
import org.json4s.native.JsonMethods._

/**
  * Created by adav on 14/09/2016.
  */
object TypeformUtils {

  val datePattern = "dd MMM uuuu"

  val request =
    """
      |{
      |  "title": "Feast Volunteers",
      |  "webhook_submit_url": "https://hooks.zapier.com/hooks/catch/740757/6tr5i5/",
      |  "branding": false,
      |  "fields": [
      |    {
      |      "type": "statement",
      |      "question": "Awesome, you're interested in volunteering with FEAST!"
      |    },
      |    {
      |        "type": "short_text",
      |        "question": "What's your first name?",
      |        "required": true,
      |        "tags": ["firstname"],
      |        "ref": "name"
      |    },
      |    {
      |        "type": "short_text",
      |        "question": "Thanks for that, {{name}}. What's your last name?",
      |        "required": true,
      |        "tags": ["lastname"]
      |    },
      |    {
      |      "type": "statement",
      |      "question": "Lovely to meet you, {{name}}. Here's some background.",
      |      "description": "blah blah"
      |    },
      |    {
      |      "type": "multiple_choice",
      |      "question": "Which upcoming Thursdays are you free for this month?",
      |      "allow_multiple_selections": true,
      |      "tags": ["dates"],
      |      "choices": [
      |        {
      |          "label": "Thursday 15th September"
      |        },
      |        {
      |          "label": "Thursday 22nd September"
      |        },
      |        {
      |          "label": "Thursday 29th September"
      |        },
      |        {
      |          "label": "Thursday 6th October"
      |        }
      |      ]
      |    },
      |    {
      |        "type": "email",
      |        "question": "Wonderful. What's your email?",
      |        "required": true,
      |        "tags": ["email"]
      |    },
      |    {
      |        "type": "short_text",
      |        "question": "What's your mobile number?",
      |        "required": true,
      |        "tags": ["mobile"]
      |    }
      |  ]
      |}
    """.stripMargin

  def processWebhook(message: String) = {

    val json = parse(message)

    def getShortTextValue(tag: String): String = for {
      answer@JObject(x) <- json \ "answers"
      if x contains JField("tags", JArray(List(JString(tag))))
      JString(value) <- answer \ "value"
    } yield value

    val dates = for {
      answer@JObject(x) <- json \ "answers"
      if x contains JField("tags", JArray(List(JString("dates"))))
      JArray(values) <- answer \\ "labels"
    } yield values.map(_.extract[String])

    TypeformResult(
      firstname = getShortTextValue("firstname"),
      lastname = getShortTextValue("lastname"),
      email = getShortTextValue("email"),
      mobile = getShortTextValue("mobile"),
      dates = convertToSqlDates(dates)
    )
  }

  def convertToSqlDates(dates: List[String]): List[Date] = dates.map { date =>
    Date.valueOf(LocalDate.parse(date, DateTimeFormatter.ofPattern(datePattern)))
  }

  def getNextDays(weeks: Int = 4, dayOfWeek: DayOfWeek = THURSDAY): Seq[String] = {
    (0 to 3).map { weeksAhead =>
      val now = LocalDate.now().plus(weeksAhead, WEEKS)
      now.`with`(TemporalAdjusters.next(dayOfWeek)).formatted(datePattern)
    }
  }
}

case class TypeformResult(firstname: String, lastname: String, email: String, mobile: String, dates: List[Date])

