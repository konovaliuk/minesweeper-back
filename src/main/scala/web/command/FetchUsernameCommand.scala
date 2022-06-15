package edu.mmsa.danikvitek.minesweeper
package web.command

import persistence.dao.DAOFactory
import util.exception.InvalidTokenException
import web.MAYBE_JWT_SECRET

import com.typesafe.scalalogging.Logger
import little.json.Json
import pdi.jwt.{ Jwt, JwtAlgorithm }

import javax.servlet.http.{ HttpServletRequest, HttpServletResponse }
import scala.util.{ Failure, Success }

object FetchUsernameCommand extends Command {
    private lazy val LOGGER = Logger(FetchUsernameCommand.getClass)

    override def execute(req: HttpServletRequest, resp: HttpServletResponse): Unit = {
        val accessToken = req.getParameter("at")
        if (!Command.validateToken(accessToken)) {
            resp.setStatus(400)
            return
        }
        Jwt.decode(accessToken, MAYBE_JWT_SECRET.get, Seq(JwtAlgorithm.HS256)) match
            case Failure(exception) => LOGGER.error(exception.getMessage, exception)
            case Success(claim) =>
                LOGGER info s"claim: $claim"
                claim.subject.map(_.toLong) match
                    case Some(userId) =>
                        DAOFactory.getUserDAO.findById(userId).map(_.username).foreach { username =>
                            resp.setStatus(200)
                            resp.getWriter.write(username)
                            LOGGER.info(s"Fetched username for $userId")
                            return
                        }
                        resp.setStatus(404)
                        LOGGER.info(s"User not found: $userId")
                    case None =>
                        resp.setStatus(400)
                        throw new InvalidTokenException("Subject with user id is not present")
    }
}
