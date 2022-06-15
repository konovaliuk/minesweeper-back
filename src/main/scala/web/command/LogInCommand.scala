package edu.mmsa.danikvitek.minesweeper
package web.command

import dto.LoginDto
import dto.LoginDto.Variant
import persistence.dao.DAOFactory
import persistence.entity.{ Email, User }
import util.exception.{ InvalidCredentialsException, InvalidDtoException }
import web.{ HOUR, MAYBE_JWT_SECRET, WEEK }

import com.typesafe.scalalogging.Logger
import little.json.Implicits.stringToJsonString
import little.json.{ Json, JsonString }
import pdi.jwt.{ Jwt, JwtAlgorithm, JwtClaim, JwtHeader }

import java.nio.charset.Charset
import java.security.MessageDigest
import javax.servlet.http.{ HttpServletRequest, HttpServletResponse }
import scala.util.{ Failure, Success, Try }

object LogInCommand extends Command {
    private val LOGGER = Logger(LogInCommand.getClass)
    private lazy val md = MessageDigest.getInstance("SHA-256")

    override def execute(req: HttpServletRequest, resp: HttpServletResponse): Unit = {
        req.getAttribute("dto").asInstanceOf[LoginDto] match
            case LoginDto(variant, login, password) => variant match
                case Variant.EMAIL =>
                    DAOFactory.getUserDAO.findByEmail(Email(login)).foreach(processLogin(resp, password, _))
                case Variant.USERNAME =>
                    DAOFactory.getUserDAO.findByUsername(login).foreach(processLogin(resp, password, _))
    }

    private def processLogin(resp: HttpServletResponse, password: String, user: User): Unit = user match
        case User(id, _, _, passwordHash, salt, _) =>
            md.update((password + salt).getBytes(Charset.forName("UTF-8")))
            val loginPasswordHash = String(md.digest())
            if loginPasswordHash equals passwordHash then
                val (at, rt) = createTokens(id, user)
                val dto = Json.obj("at" -> at, "rt" -> rt)
                resp.setStatus(200)
                resp.getWriter.write(Json.toPrettyPrint(dto))
                LOGGER info "Login performed"
            else throw new InvalidCredentialsException("Invalid password")

    private def createTokens(userId: Long, user: User): (String, String) = {
        val issuedAt = System.currentTimeMillis()
        (
          Command.createToken(userId, HOUR, issuedAt),
          if user.refreshToken.exists(rt => Jwt.isValid(rt, MAYBE_JWT_SECRET.get, Seq(JwtAlgorithm.HS256))) then
              user.refreshToken.get
          else {
              val newRT = Command.createToken(userId, WEEK, issuedAt)
              DAOFactory.getUserDAO.update(user.copy(refreshToken = Some(newRT)))
              newRT
          }
        )
    }
}
