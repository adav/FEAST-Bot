package com.knoldus.typeform

import java.sql.Date

import org.scalatest.{FunSuite, Matchers}

/**
  * Created by adav on 14/09/2016.
  */
class TypeformUtilsTest extends FunSuite with Matchers {

  test("extracting values from webhook json") {

    val json =
      """
        |{
        |  "uid": "AbCdEfGhIj",
        |  "token": "440d47cd38ed9234a45cd160649f2203",
        |  "answers": [
        |	  {
        |    	"field_id": 35647923,
        |		"tags": ["dates"],
        |		"type": "choices",
        |		"value":
        |		  {"labels": ["Thursday 15th September 2016", "Thursday 29th September 2016"],
        |				  "other": "None"
        |				 }
        |	  },
        |	  {
        |"field_id": 35647925,
        |"tags": ["mobile"],
        |"type": "text",
        |"value": "0303023023"
        |	  },
        |	  {
        |"field_id": 35647924,
        |"tags": ["email"],
        |"type": "text",
        |"value": "asdasd@asdasdad.com"
        |	  },
        |	  {
        |"field_id": 35647920,
        |"tags": ["firstname"],
        |"type": "text",
        |"value": "Andrew"
        |	  },
        |	  {
        |"field_id": 35647921,
        |"tags": ["lastname"],
        |"type": "text",
        |"value": "David"
        |	  },
        |   {
        |"field_id": 356479331,
        |"tags": ["facilitator"],
        |"type": "boolean",
        |"value": true
        |	  }
        |  ]
        |}
      """.stripMargin



    val expected = TypeformResult("Andrew","David","asdasd@asdasdad.com","0303023023",List(Date.valueOf("2016-09-15"), Date.valueOf("2016-09-29")), true)

    TypeformUtils.processWebhook(json) should be (expected)

  }

}
