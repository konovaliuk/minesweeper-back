package edu.mmsa.danikvitek.minesweeper
package web.command

import com.typesafe.scalalogging.Logger

import javax.servlet.http.{ HttpServletRequest, HttpServletResponse }

object NoCommand extends Command {
    private val LOGGER = Logger(this.getClass)
    
    override def execute(req: HttpServletRequest, resp: HttpServletResponse): Unit =
        LOGGER info s"req: ${req.getRequestURI}, ${req.getMethod}"
        LOGGER info "No Command"
}
