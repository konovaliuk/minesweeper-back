package edu.mmsa.danikvitek.minesweeper
package web.filter

import dto.{ LoginDto, jsonToLoginDto }
import util.exception.{ InvalidDtoException, InvalidRequestException }
import web.MAYBE_JWT_SECRET
import web.command.ErrorCommand

import com.typesafe.scalalogging.Logger
import little.json.Json
import pdi.jwt.{ Jwt, JwtAlgorithm }

import java.util.ResourceBundle
import javax.servlet.FilterChain
import javax.servlet.annotation.WebFilter
import javax.servlet.http.{ HttpFilter, HttpServletRequest, HttpServletResponse }
import scala.util.{ Failure, Success, Try }

@WebFilter(urlPatterns = Array("/api/login"))
class JWTLoginFilter extends HttpFilter {
    private val LOGGER = Logger(this.getClass)

    LOGGER info "JWTLoginFilter created"

    override def doFilter(req: HttpServletRequest, res: HttpServletResponse, chain: FilterChain): Unit = {
        LOGGER info s"req: ${req.getRequestURI}"
        val validMethod = req.getMethod equals "POST"
        val validContentType = req.getContentType equals "application/json"
        val token = req.getReader.readLine()
        MAYBE_JWT_SECRET match {
            case Some(secret) => Jwt.decodeRaw(token, secret, Seq(JwtAlgorithm.HS256)) match {
                case Failure(exception) => 
                    LOGGER.error(exception.getMessage, exception)
                    throw new InvalidDtoException("Invalid login dto")
                case Success(claim) =>
                    LOGGER info s"claim: $claim"
                    if validMethod && validContentType then
                        req.setAttribute("dto", Json.parse(claim).as[LoginDto])
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