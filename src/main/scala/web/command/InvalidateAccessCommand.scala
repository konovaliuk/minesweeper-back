package edu.mmsa.danikvitek.minesweeper
package web.command

import persistence.dao.DAOFactory
import util.exception.InvalidTokenException
import web.{ MAYBE_JWT_SECRET, WEEK }

import com.typesafe.scalalogging.Logger
import pdi.jwt.{ Jwt, JwtAlgorithm }

import javax.servlet.http.{ HttpServletRequest, HttpServletResponse }
import scala.util.{ Failure, Success }

object InvalidateAccessCommand extends Command {
    private val LOGGER = Logger(InvalidateAccessCommand.getClass)

    override def execute(req: HttpServletRequest, resp: HttpServletResponse): Unit = {
        val accessToken = req.getReader.readLine()
        MAYBE_JWT_SECRET.foreach { JWT_SECRET =>
            Jwt.decode(accessToken, JWT_SECRET, Seq(JwtAlgorithm.HS256)) match
                case Failure(exception) =>
                    LOGGER.error(exception.getMessage, exception)
                    throw new InvalidTokenException("invalid access token")
                case Success(claim) =>
                    claim.subject.map(_.toLong).foreach { userId =>
                        val newRefreshToken = Command.createToken(userId, WEEK)
                        DAOFactory.getUserDAO
                          .findById(userId)
                          .map(_.copy(refreshToken = Some(newRefreshToken)))
                          .foreach(DAOFactory.getUserDAO.save)
                    }
                    resp.setStatus(200)
                    return
        }
        resp.setStatus(500)
    }
}
