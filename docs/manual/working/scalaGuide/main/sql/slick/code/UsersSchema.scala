/*
 * Copyright (C) from 2022 The Play Framework Contributors <https://github.com/playframework>, 2011-2021 Lightbend Inc. <https://www.lightbend.com>
 */
package scalaguide.slick

import slick.jdbc.H2Profile.api._

object UsersSchema {

  case class User(name: String, surname: String)

  class UsersTable(tag: Tag) extends Table[User](tag, "USER") {
    def name    = column[String]("name", O.PrimaryKey)
    def surname = column[String]("surname")
    def *       = (name, surname) <> (User.tupled, User.unapply)
  }

  val Users = TableQuery[UsersTable]
}
