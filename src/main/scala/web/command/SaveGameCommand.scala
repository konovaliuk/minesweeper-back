package edu.mmsa.danikvitek.minesweeper
package web.command

import dto.{ GameStateDto, jsonToGameStateDto }
import persistence.dao.DAOFactory
import persistence.entity.{ Cell, Game }
import util.exception.{ BackendSecretNotSetError, InvalidTokenException }
import web.MAYBE_JWT_SECRET

import com.typesafe.scalalogging.Logger
import little.json.*
import little.json.Implicits.jsonValueToString
import little.json.JsonObject.unapply
import pdi.jwt.{ Jwt, JwtAlgorithm }

import javax.servlet.http.{ HttpServletRequest, HttpServletResponse }
import scala.language.implicitConversions
import scala.util.{ Failure, Success }

object SaveGameCommand extends Command {
    private val LOGGER = Logger(SaveGameCommand.getClass)

    override def execute(req: HttpServletRequest, resp: HttpServletResponse): Unit = {
        Json.parse(req.getReader) match
            case jsonObject: JsonObject => unapply(jsonObject) match
                case Some(jsonMap) =>
                    val accessToken = jsonMap.getOrElse(
                        "accessToken",
                        throw new InvalidTokenException("No access token specified")
                    ).as[String]
                    MAYBE_JWT_SECRET.foreach { JWT_SECRET =>
                        if !Jwt.isValid(accessToken, JWT_SECRET, Seq(JwtAlgorithm.HS256)) then
                            throw new InvalidTokenException("Invalid access token")
                        Jwt.decode(accessToken, JWT_SECRET, Seq(JwtAlgorithm.HS256)) match
                            case Success(claim) =>
                                val userId = claim.subject
                                  .map(_.toLong)
                                  .getOrElse(throw new InvalidTokenException("No subject present in the access token"))
                                jsonMap.get("game").map(_.as[GameStateDto]).foreach { game =>
                                    DAOFactory.getGameDAO.save(Game.builder
                                      .withUserId(userId)
                                      .withGameSate(game.gameState)
                                      .withWidth(game.width)
                                      .withHeight(game.height)
                                      .withCells(game.cells
                                        .flatten
                                        .toList
                                        .map(cellDto => Cell.builder
                                          .withX(cellDto.x)
                                          .withY(cellDto.y)
                                          .withMined(cellDto.isMined)
                                          .withFlagged(cellDto.isFlagged)
                                          .withDiscovered(cellDto.isDiscovered)
                                          .build
                                        )
                                      )
                                      .build
                                    )
                                }
                                resp.setStatus(201)
                            case Failure(ex) =>
                                LOGGER.error(ex.getMessage, ex)
                                throw new InvalidTokenException(ex.getMessage)
                        return
                    }
                    throw new BackendSecretNotSetError()
                case None =>
            case _ =>
    }
}
