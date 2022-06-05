package edu.mmsa.danikvitek.minesweeper
package web.command

import dto.{ UsernameDto, jsonToUsernameDto, messageDtoToJson, MessageDto as MsgDto }
import persistence.dao.{ DAOFactory, UserDAO }
import persistence.entity.Email

import com.typesafe.scalalogging.Logger
import edu.mmsa.danikvitek.minesweeper.util.exception.{ IllegalContentTypeException, InvalidDtoException }
import little.json.Implicits.jsonValueToJsonObject
import little.json.Json

import javax.servlet.http.{ HttpServletRequest, HttpServletResponse }
import scala.util.{ Failure, Success, Try }

object CheckUsernameCommand extends Command {
    private final lazy val LOGGER = Logger(CheckEmailCommand.getClass)
    private final lazy val userDao: UserDAO = DAOFactory.getUserDAO

    @throws[IllegalContentTypeException]("if the request content type is not application/json")
    @throws[InvalidDtoException]("if there is no key needed for UsernameDto")
    override def execute(req: HttpServletRequest, resp: HttpServletResponse): Unit =
        val json = Command.parseJsonReq(req)
        Try(json.as[UsernameDto]) match
            case Failure(e) => throw new InvalidDtoException(e.getMessage)
            case Success(UsernameDto(username)) =>
                val msgDto: MsgDto = userDao.findByUsername(username) match
                    case Some(_) => MsgDto("Username is taken")
                    case None => MsgDto("Username is available")
                val msgStr = Json.toPrettyPrint(Json.toJson(msgDto))
                resp.setStatus(200)
                resp.setContentType("application/json")
                resp.getWriter.write(msgStr)
                LOGGER info s"Username \"$username\" was checked: $msgDto"
    end execute
}