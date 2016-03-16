package com.knoldus.json

import net.liftweb.json.DefaultFormats
import net.liftweb.json.JValue
import net.liftweb.json.JNothing
import net.liftweb.json.Serialization
import net.liftweb.json.{ parse => liftParser }

trait JsonHelper {

  implicit protected val formats = DefaultFormats

  protected def write[T <: AnyRef](value: T): String = Serialization.write(value)

  protected def parse(value: String): JValue = liftParser(value)

  implicit protected def extractOrEmptyString(json: JValue): String = json match {
    case JNothing => ""
    case data     => data.extract[String]
  }

}
