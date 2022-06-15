package edu.mmsa.danikvitek.minesweeper
package web.command

import dto.{ RatingPageRequestDto, jsonToRatingPageRequestDto, ratingPageEntryToJson }
import persistence.dao.{ DAOFactory, UserDAO }
import persistence.entity.Email
import util.exception.InvalidDtoException

import com.typesafe.scalalogging.Logger
import little.json.Implicits.*
import little.json.{ Json, JsonArray }

import javax.servlet.http.{ HttpServletRequest, HttpServletResponse }
import scala.util.{ Failure, Success, Try }

object RatingPageCommand extends Command {
    private final lazy val LOGGER = Logger(RatingPageCommand.getClass)
    private final lazy val userDao: UserDAO = DAOFactory.getUserDAO

    override def execute(req: HttpServletRequest, resp: HttpServletResponse): Unit = {
        val page = req.getParameter("page").toInt
        val pageSize = req.getParameter("pageSize").toInt
        val ratingsPage: String = Json.toPrettyPrint(JsonArray(userDao.fetchRatings(page, pageSize).map(Json.toJson)))
        resp.setStatus(200)
        resp.setContentType("application/json")
        resp.getWriter.write(ratingsPage)
        LOGGER info s"fetched ratings page: (size: $pageSize, number: $page)"
    }
}
