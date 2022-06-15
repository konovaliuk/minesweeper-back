package edu.mmsa.danikvitek.minesweeper
package web.command

import persistence.dao.DAOFactory
import web.MAYBE_JWT_SECRET

import com.typesafe.scalalogging.Logger
import pdi.jwt.{ Jwt, JwtAlgorithm, JwtClaim }

import javax.servlet.http.{ HttpServletRequest, HttpServletResponse }
import scala.util.{ Failure, Success }

object ValidateLoginCommand extends Command {
    private val LOGGER = Logger(ValidateLoginCommand.getClass)

    override def execute(req: HttpServletRequest, resp: HttpServletResponse): Unit = {
        val accessToken = req.getParameter("at")
        resp.setStatus(200)
        resp.getWriter.print(
            MAYBE_JWT_SECRET.exists(JWT_SECRET =>
                Jwt.isValid(accessToken, JWT_SECRET, Seq(JwtAlgorithm.HS256)) &&
                  (Jwt.decode(accessToken, JWT_SECRET, Seq(JwtAlgorithm.HS256)) match
                      case Failure(exception) =>
                          LOGGER.error(exception.getMessage, exception)
                          false
                      case Success(claim) =>
                          claim.subject.map(_.toLong).flatMap(DAOFactory.getUserDAO.findById).nonEmpty
                    )
            )
        )
    }
}
