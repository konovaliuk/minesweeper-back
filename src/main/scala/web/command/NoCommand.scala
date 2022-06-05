package edu.mmsa.danikvitek.minesweeper
package web.command

import javax.servlet.http.{ HttpServletRequest, HttpServletResponse }

object NoCommand extends Command {
    override def execute(req: HttpServletRequest, resp: HttpServletResponse): Unit = ()
}
