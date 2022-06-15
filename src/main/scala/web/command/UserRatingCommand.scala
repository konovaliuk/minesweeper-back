package edu.mmsa.danikvitek.minesweeper
package web.command

import dto.ratingPageEntryToJson
import persistence.dao.DAOFactory
import util.exception.UserNotFoundException

import com.typesafe.scalalogging.Logger
import little.json.Json
import little.json.Implicits.jsonValueToJsonObject

import javax.servlet.http.{ HttpServletRequest, HttpServletResponse }
import scala.language.implicitConversions

object UserRatingCommand extends Command {
    private val LOGGER = Logger(UserRatingCommand.getClass)

    override def execute(req: HttpServletRequest, resp: HttpServletResponse): Unit = {
        val username = req.getParameter("username")
        val rating = DAOFactory.getUserDAO
          .findByUsername(username)
          .flatMap(user => DAOFactory.getUserDAO.findRatingById(user.id))
          .getOrElse(throw new UserNotFoundException(s"Not found for user with username $username"))
        resp.setStatus(200)
        resp.setContentType("application/json")
        resp.getWriter.write(Json.toPrettyPrint(Json.toJson(rating)))
        LOGGER info s"fetched rating for $username: $rating"
    }
}
