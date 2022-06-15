package edu.mmsa.danikvitek.minesweeper
package web.command

import util.exception.IllegalContentTypeException
import web.MAYBE_JWT_SECRET

import edu.mmsa.danikvitek.minesweeper
import little.json.{ Json, JsonStructure }
import pdi.jwt.{ Jwt, JwtAlgorithm, JwtClaim, JwtHeader }

import java.nio.charset.Charset
import java.util.ResourceBundle
import javax.servlet.http.{ HttpServletRequest, HttpServletResponse }
import scala.util.{ Failure, Try }

trait Command:
    def execute(req: HttpServletRequest, resp: HttpServletResponse): Unit

object Command {
    @throws[IllegalContentTypeException]("if the request content type is not application/json")
    def parseJsonReq(req: HttpServletRequest): JsonStructure = {
        if req.getContentType == null || !req.getContentType.equals("application/json") then
            throw new IllegalContentTypeException("Request must be application/json")
        val bytes = req.getInputStream.readAllBytes()
        val encoding = req.getCharacterEncoding
        val content = if encoding == null then String(bytes) else String(bytes, Charset forName encoding)
        Json.parse(content)
    }

    def validateToken(token: String): Boolean = MAYBE_JWT_SECRET exists { JWT_SECRET =>
        Jwt.isValid(token, JWT_SECRET, Seq(JwtAlgorithm.HS256))
    }

    def createToken(userId: Long, duration: Long, issuedAt: Long = System.currentTimeMillis()): String = {
        Jwt.encode(
            JwtHeader(JwtAlgorithm.HS256, "JWT"),
            JwtClaim(
                subject = Some(userId.toString),
                issuedAt = Some(issuedAt),
                expiration = Some(issuedAt + duration)
            ),
            MAYBE_JWT_SECRET.get
        )
    }
}