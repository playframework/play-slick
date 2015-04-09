package models

import play.api.libs.json.Json

case class Cat(name: String, color: String)

object Cat {
  //JSON read/write
  implicit val catFormat = Json.format[Cat]
}