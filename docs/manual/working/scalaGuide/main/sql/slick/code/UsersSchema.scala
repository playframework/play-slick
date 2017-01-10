/*
 * Copyright (C) 2009-2014 Typesafe Inc. <http://www.typesafe.com>
 */
package scalaguide.slick

import slick.jdbc.H2Profile.api._

object UsersSchema {

  case class User(name: String, surname: String)

  class UsersTable(tag: Tag) extends Table[User](tag, "USER") {
    def name = column[String]("name", O.PrimaryKey)
    def surname = column[String]("surname")
    def * = (name, surname) <> (User.tupled, User.unapply)
  }

  val Users = TableQuery[UsersTable]
}
