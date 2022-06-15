package edu.mmsa.danikvitek.minesweeper
package web.command

import persistence.dao.DAOFactory
import util.exception.{ BackendSecretNotSetError, InvalidTokenException, UserNotFoundException }
import web.{ HOUR, MAYBE_JWT_SECRET, WEEK }

import com.typesafe.scalalogging.Logger
import little.json.Implicits.stringToJsonString
import little.json.Json
import pdi.jwt.{ Jwt, JwtAlgorithm }

import javax.servlet.http.{ HttpServletRequest, HttpServletResponse }
import scala.language.implicitConversions
import scala.util.{ Failure, Success }

object RefreshLoginCommand extends Command {
    private val LOGGER = Logger(RefreshLoginCommand.getClass)

    override def execute(req: HttpServletRequest, resp: HttpServletResponse): Unit = {
        val refreshToken = req.getReader.readLine()
        if Jwt.isValid(refreshToken) then
            val user = DAOFactory.getUserDAO
              .findByRefreshToken(refreshToken)
              .getOrElse(throw new UserNotFoundException("User with such rt was not found. Probably invalidated token"))
            MAYBE_JWT_SECRET.foreach { JWT_SECRET =>
                Jwt.decode(refreshToken, JWT_SECRET, Seq(JwtAlgorithm.HS256)) match
                    case Failure(exception) =>
                        LOGGER.error(exception.getMessage, exception)
                        throw new InvalidTokenException("invalid access token")
                    case Success(claim) =>
                        claim.subject.map(_.toLong).foreach { userId =>
                            val issuedAt = System.currentTimeMillis()
                            val newRefreshToken = Command.createToken(userId, WEEK, issuedAt)
                            val newAccessToken = Command.createToken(userId, HOUR, issuedAt)
                            DAOFactory.getUserDAO.save(user.copy(refreshToken = Some(newRefreshToken)))
                            resp.setStatus(200)
                            resp.getWriter.write(
                                Json.toPrettyPrint(Json.obj("at" -> newAccessToken, "rt" -> newRefreshToken))
                            )
                            return
                        }
                        throw new InvalidTokenException("Subject user id is not present")
            }
            throw new BackendSecretNotSetError()
        else resp.setStatus(400)
    }
}
