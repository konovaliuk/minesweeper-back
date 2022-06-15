package edu.mmsa.danikvitek.minesweeper
package web.command

import dto.{ ErrorDto, errorDtoToJson }
import util.exception.{
    IllegalContentTypeException as ICTE,
    InvalidDtoException as IDE,
    InvalidEmailException as IEE,
    UserAlreadyRegisteredException as UARE,
    IncorrectlyBuiltUserException as IBUE,
    InvalidRequestException as IRE,
    InvalidCredentialsException as ICE,
    InvalidTokenException as ITE,
    UserNotFoundException as UNFE
}

import com.typesafe.scalalogging.Logger
import little.json.Implicits.jsonValueToJsonObject
import little.json.Json

import java.lang.UnsupportedOperationException as UOE
import javax.servlet.http.{ HttpServletRequest, HttpServletResponse }

case class ErrorCommand(private val th: Throwable) extends Command {
    private final lazy val LOGGER = Logger(this.getClass)

    override def execute(req: HttpServletRequest, resp: HttpServletResponse): Unit = {
        val json = th match
            case e: ICTE => ex400(e, resp)
            case e: IDE => ex400(e, resp)
            case e: UARE => ex400(e, resp)
            case e: IEE => ex400(e, resp)
            case e: UOE => ex400(e, resp)
            case e: IBUE => ex400(e, resp)
            case e: IRE => ex400(e, resp)
            case e: ICE => ex400(e, resp)
            case e: ITE => ex400(e, resp)
            case e: UNFE => ex404(e, resp)
            case _ => ex500(th, resp)
        LOGGER info s"Sent the error dto: $json"
    }

    private def ex400(e: RuntimeException, resp: HttpServletResponse): String = ex(e, resp, 400)

    private def ex404(e: RuntimeException, resp: HttpServletResponse): String = ex(e, resp, 404)
    
    private def ex500(th: Throwable, resp: HttpServletResponse): String = ex(th, resp, 500)

    private def ex(th: Throwable, resp: HttpServletResponse, erCode: Int): String = {
        resp.setStatus(erCode)
        resp.setContentType("application/json")
        val json = Json.toPrettyPrint(Json.toJson(ErrorDto(erCode, th.getMessage)))
        resp.getWriter.write(json)
        json
    }
}
