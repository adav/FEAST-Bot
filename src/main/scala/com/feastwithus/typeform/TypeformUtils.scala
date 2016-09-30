package com.feastwithus.typeform

import java.sql.Date

import com.feastwithus.ui.DateUtils
import org.json4s.JsonAST.{JArray, JObject, JString}
import org.json4s._
import org.json4s.native.JsonMethods._

/**
  * Created by adav on 14/09/2016.
  */
object TypeformUtils {
  implicit val formats = DefaultFormats


  def createTypeformJsonRequest: String = {
    val datesJson = DateUtils.findNextDays(weeks = 8).map(DateUtils.formatHumanDate(_, includeDayOfTheWeek = true)).map(d => s"""{"label": "$d"}""")
    s"""
      |{
      |  "title": "Feast Volunteers",
      |  "webhook_submit_url": "${sys.env.getOrElse("TYPEFORM_WEBHOOK_ADDRESS", "https://hooks.zapier.com/hooks/catch/740757/6tr5i5/")}",
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
      |      "required": true,
      |      "choices": [${datesJson.mkString(",")}]
      |    },
      |    {
      |        "type": "yes_no",
      |        "question": "For each of these events, would you like to be a facilitator?",
      |        "tags": ["facilitator"]
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
  }

  def landingHtml(url: String): String =
    s"""
       |<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
       |<html>
       |<head>
       |  <title>FEAST!</title>
       |
       |  <!--CSS styles that ensure your typeform takes up all the available screen space (DO NOT EDIT!)-->
       |<style type="text/css">
       |    html{
       |      margin: 0;
       |      height: 100%;
       |      overflow: hidden;
       |    }
       |    iframe{
       |      position: absolute;
       |      left:0;
       |      right:0;
       |      bottom:0;
       |      top:0;
       |      border:0;
       |    }
       |  </style>
       |</head>
       |<body>
       |  <iframe id="typeform-full" width="100%" height="100%" frameborder="0" src="$url"></iframe>
       |  <script type="text/javascript" src="https://s3-eu-west-1.amazonaws.com/share.typeform.com/embed.js"></script>
       |</body>
       |</html>
            """.stripMargin

  def processWebhook(message: String) = {

    val json = parse(message)
    val answers = (json \ "answers").children

    def getShortTextValue(tagValue: String): String = {
      val filteredAnswers = for {
        JObject(field) <- answers
        JField("tags", tag) <- field
        JField("value", JString(value)) <- field
        if tag equals JArray(List(JString(tagValue)))
      } yield value
      filteredAnswers.head
    }

    val isFacilitator: Boolean = {
      val filteredBooleans = Option(for {
        JObject(field) <- answers
        JField("tags", tag) <- field
        JField("value", JBool(value)) <- field
        if tag equals JArray(List(JString("facilitator")))
      } yield value)
      filteredBooleans match {
        case Some(List(value)) => value
        case _ => false
      }
    }

    val dates = {
      val filteredDates: List[List[String]] = for {
        answer@JObject(x) <- json \ "answers"
        if x contains JField("tags", JArray(List(JString("dates"))))
        JArray(values) <- answer \\ "labels"
      } yield values.map(_.extract[String])
      filteredDates.flatten
    }

    TypeformResult(
      firstname = getShortTextValue("firstname"),
      lastname = getShortTextValue("lastname"),
      email = getShortTextValue("email"),
      mobile = getShortTextValue("mobile"),
      dates = DateUtils.convertToSqlDates(dates),
      facilitator = isFacilitator
    )
  }


}

case class TypeformResult(firstname: String, lastname: String, email: String, mobile: String, dates: List[Date], facilitator: Boolean)
