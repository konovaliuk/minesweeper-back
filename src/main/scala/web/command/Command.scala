package edu.mmsa.danikvitek.minesweeper
package web.command

import edu.mmsa.danikvitek.minesweeper.util.exception.IllegalContentTypeException
import little.json.{ Json, JsonStructure }

import java.nio.charset.Charset
import javax.servlet.http.{ HttpServletRequest, HttpServletResponse }
import scala.util.{ Failure, Try }

trait Command:
    def execute(req: HttpServletRequest, resp: HttpServletResponse): Unit

object Command:
    @throws[IllegalContentTypeException]("if the request content type is not application/json")
    def parseJsonReq(req: HttpServletRequest): JsonStructure = {
        if req.getContentType == null || !req.getContentType.equals("application/json") then
            throw new IllegalContentTypeException("Request must be application/json")
        val bytes = req.getInputStream.readAllBytes()
        val encoding = req.getCharacterEncoding
        val content = if encoding == null then String(bytes) else String(bytes, Charset forName encoding)
        Json.parse(content)
    }