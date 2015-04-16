/*
 * Copyright (C) 2009-2014 Typesafe Inc. <http://www.typesafe.com>
 */
package scalaguide.slick

import slick.driver.H2Driver.api._

object UsersSchema {

  case class User(name: String, surname: String)

  class UsersTable(tag: Tag) extends Table[User](tag, "USER") {
    def name = column[String]("name", O.PrimaryKey)
    def surname = column[String]("surname")
    def * = (name, surname) <> (User.tupled, User.unapply _)
  }

  val Users = TableQuery[UsersTable]
}
