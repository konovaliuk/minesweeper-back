package edu.mmsa.danikvitek.minesweeper
package web.filter

import dto.{ RegistrationDto, jsonToRegistrationDto }
import util.exception.InvalidRequestException
import web.command.ErrorCommand

import com.typesafe.scalalogging.Logger
import little.json.Json
import pdi.jwt.{ Jwt, JwtAlgorithm }

import java.util.ResourceBundle
import javax.servlet.FilterChain
import javax.servlet.annotation.WebFilter
import javax.servlet.http.{ HttpFilter, HttpServletRequest, HttpServletResponse }
import scala.util.{ Failure, Success, Try }

@WebFilter(urlPatterns = Array("/api/reg"))
class JWTRegFilter extends HttpFilter {
    private val secretsBundle = ResourceBundle getBundle "secrets"

    private val LOGGER = Logger(this.getClass)
    private val MAYBE_JWT_SECRET: Option[String] = Try(secretsBundle getString "JWT_SECRET").toOption

    LOGGER info "JWTRegFilter created"
//    LOGGER info s"JWT_SECRET: $MAYBE_JWT_SECRET"

    override def doFilter(req: HttpServletRequest, res: HttpServletResponse, chain: FilterChain): Unit = {
        LOGGER info s"req: ${req.getRequestURI}"
        val validMethod = req.getMethod equals "POST"
        val validContentType = req.getContentType equals "application/json"
        val token = req.getReader.readLine()
        MAYBE_JWT_SECRET match {
            case Some(secret) => Jwt.decodeRaw(token, secret, Seq(JwtAlgorithm.HS256)) match {
                case Failure(exception) => LOGGER.error(exception.getMessage, exception)
                case Success(claim) =>
                    LOGGER info s"claim: $claim"
                    if validMethod && validContentType then
                        req.setAttribute("dto", Json.parse(claim).as[RegistrationDto])
                        LOGGER info s"dto: ${req.getAttribute("dto")}"
                        chain.doFilter(req, res)
                    else ErrorCommand(new InvalidRequestException(
                        s"validMethod: $validMethod; validContentType: $validContentType"
                    )).execute(req, res)
            }
            case None => LOGGER.error("No secret present")
        }
    }
}