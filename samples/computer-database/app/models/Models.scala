package models

import java.util.Date

case class Page[A](items: Seq[A], page: Int, offset: Long, total: Long) {
  lazy val prev = Option(page - 1).filter(_ >= 0)
  lazy val next = Option(page + 1).filter(_ => (offset + items.size) < total)
}

case class Company(id: Option[Long], name: String)

case class Computer(id: Option[Long] = None, name: String, introduced: Option[Date] = None, discontinued: Option[Date] = None, companyId: Option[Long] = None)