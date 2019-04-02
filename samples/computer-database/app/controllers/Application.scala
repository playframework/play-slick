package controllers

import javax.inject.Inject

import dao.{ CompaniesDAO, ComputersDAO }
import models.Computer
import play.api.data.Form
import play.api.data.Forms.{ date, longNumber, mapping, nonEmptyText, optional }
import play.api.i18n.I18nSupport
import play.api.mvc.{ AbstractController, ControllerComponents, Flash, RequestHeader }
import views.html

import scala.concurrent.ExecutionContext

/** Manage a database of computers. */
class Application @Inject() (
  companiesDao: CompaniesDAO,
  computersDao: ComputersDAO,
  controllerComponents: ControllerComponents)(implicit executionContext: ExecutionContext) extends AbstractController(controllerComponents) with I18nSupport {

  /** This result directly redirect to the application home.*/
  val Home = Redirect(routes.Application.list(0, 2, ""))

  /** Describe the computer form (used in both edit and create screens).*/
  val computerForm = Form(
    mapping(
      "id" -> optional(longNumber),
      "name" -> nonEmptyText,
      "introduced" -> optional(date("yyyy-MM-dd")),
      "discontinued" -> optional(date("yyyy-MM-dd")),
      "company" -> optional(longNumber))(Computer.apply)(Computer.unapply))

  // -- Actions

  /** Handle default path requests, redirect to computers list */
  def index = Action { Home }

  /**
   * Display the paginated list of computers.
   *
   * @param page Current page number (starts from 0)
   * @param orderBy Column to be sorted
   * @param filter Filter applied on computer names
   */
  def list(page: Int, orderBy: Int, filter: String) = Action.async { implicit request =>
    val computers = computersDao.list(page = page, orderBy = orderBy, filter = ("%" + filter + "%"))
    computers.map(cs => Ok(html.list(cs, orderBy, filter)))
  }

  /**
   * Display the 'edit form' of a existing Computer.
   *
   * @param id Id of the computer to edit
   */
  def edit(id: Long) = Action.async { implicit rs =>
    val computerAndOptions = for {
      computer <- computersDao.findById(id)
      options <- companiesDao.options()
    } yield (computer, options)

    computerAndOptions.map {
      case (computer, options) =>
        computer match {
          case Some(c) => Ok(html.editForm(id, computerForm.fill(c), options))
          case None => NotFound
        }
    }
  }

  /**
   * Handle the 'edit form' submission
   *
   * @param id Id of the computer to edit
   */
  def update(id: Long) = Action.async { implicit rs =>
    computerForm.bindFromRequest.fold(
      formWithErrors => companiesDao.options().map(options => BadRequest(html.editForm(id, formWithErrors, options))),
      computer => {
        for {
          _ <- computersDao.update(id, computer)
        } yield Home.flashing("success" -> "Computer %s has been updated".format(computer.name))
      })
  }

  /** Display the 'new computer form'. */
  def create = Action.async { implicit rs =>
    companiesDao.options().map(options => Ok(html.createForm(computerForm, options)))
  }

  /** Handle the 'new computer form' submission. */
  def save = Action.async { implicit rs =>
    computerForm.bindFromRequest.fold(
      formWithErrors => companiesDao.options().map(options => BadRequest(html.createForm(formWithErrors, options))),
      computer => {
        for {
          _ <- computersDao.insert(computer)
        } yield Home.flashing("success" -> "Computer %s has been created".format(computer.name))
      })
  }

  /** Handle computer deletion. */
  def delete(id: Long) = Action.async { implicit rs =>
    for {
      _ <- computersDao.delete(id)
    } yield Home.flashing("success" -> "Computer has been deleted")
  }
}
