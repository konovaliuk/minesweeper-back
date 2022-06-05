package edu.mmsa.danikvitek.minesweeper
package web.command

import org.intellij.lang.annotations.Language

import javax.servlet.http.{ HttpServletRequest, HttpServletResponse }

object HelloCommand extends Command {    
    override def execute(req: HttpServletRequest, resp: HttpServletResponse): Unit = {
        resp.setStatus(200)
        resp.setContentType("text/html")
        @Language("HTML") val content = "<h1>Hello world!</h1>"
        resp.getWriter.write(content)
    }
}
