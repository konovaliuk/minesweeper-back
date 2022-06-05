package edu.mmsa.danikvitek.minesweeper
package web.command

import dto.{ jsonToRegistrationDto, RegistrationDto as RegDto }
import persistence.dao.DAOFactory
import persistence.entity.{ Email, User }

import com.typesafe.scalalogging.Logger
import edu.mmsa.danikvitek.minesweeper.util.exception.{ IllegalContentTypeException, InvalidDtoException, UserAlreadyRegisteredException }
import little.json.Json

import java.nio.charset.Charset
import javax.servlet.http.{ HttpServletRequest, HttpServletResponse }
import scala.util.{ Failure, Success, Try }

object RegisterCommand extends Command :
    private final lazy val LOGGER = Logger(RegisterCommand.getClass)
    private final lazy val userDao = DAOFactory.getUserDAO

    @throws[IllegalContentTypeException]("if the request content type is not application/json")
    @throws[InvalidDtoException]("if there is no key needed for RegistrationDto")
    @throws[UserAlreadyRegisteredException]("if such account is already registered")
    override def execute(req: HttpServletRequest, resp: HttpServletResponse): Unit =
        val json = Command.parseJsonReq(req)
        Try(json.as[RegDto]) match
            case Failure(e) => throw new InvalidDtoException(e.getMessage)
            case Success(regDto) =>
                userDao findByUsername regDto.username foreach { _ =>
                    throw new UserAlreadyRegisteredException("Account with such username is already registered")
                }
                val email = Email(regDto.email)
                userDao findByEmail email foreach { _ =>
                    throw new UserAlreadyRegisteredException("Account with such email is already registered")
                }
                userDao save User.builder
                  .withUsername(regDto.username)
                  .withEmail(email)
                  .withPassword(regDto.password)
                  .build
                resp.setStatus(201)
                resp.setHeader("Location", "/api/login")
                LOGGER info s"Registered user ${userDao.findByEmail(email).get}"
        end match
    end execute
end RegisterCommand