package models

import play.api.libs.json.Json

/** Row of data from the table "records" */
case class Record(id: Int, name: String)

object Record {
  implicit val recordWrites = Json.writes[Record]
}