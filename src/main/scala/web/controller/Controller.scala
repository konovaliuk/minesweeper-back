package edu.mmsa.danikvitek.minesweeper
package web.controller

import web.command.ErrorCommand

import com.typesafe.scalalogging.Logger
import org.intellij.lang.annotations.Language

import java.io.IOException
import javax.servlet.ServletException
import javax.servlet.annotation.WebServlet
import javax.servlet.http.{ HttpServlet, HttpServletRequest, HttpServletResponse }
import scala.util.Try

@WebServlet(name = "web", urlPatterns = Array("/api/*"))
class Controller extends HttpServlet {
    private lazy val LOGGER = Logger(this.getClass)

    @throws[ServletException]
    @throws[IOException]
    override def doGet(req: HttpServletRequest, resp: HttpServletResponse): Unit = processRequest(req, resp)

    @throws[ServletException]
    @throws[IOException]
    override def doPost(req: HttpServletRequest, resp: HttpServletResponse): Unit = processRequest(req, resp)

    @throws[ServletException]
    @throws[IOException]
    override def doPut(req: HttpServletRequest, resp: HttpServletResponse): Unit = processRequest(req, resp)

    @throws[ServletException]
    @throws[IOException]
    override def doDelete(req: HttpServletRequest, resp: HttpServletResponse): Unit = processRequest(req, resp)

    private def processRequest(req: HttpServletRequest, resp: HttpServletResponse): Unit =
        Try(ControllerHelper.getCommand(req).execute(req, resp)) recover {
            case th: Throwable =>
                ErrorCommand(th).execute(req, resp)
                LOGGER.error(th.getMessage, th)
        }
}