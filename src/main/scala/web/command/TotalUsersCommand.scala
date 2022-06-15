package edu.mmsa.danikvitek.minesweeper
package web.command

import persistence.dao.DAOFactory

import javax.servlet.http.{ HttpServletRequest, HttpServletResponse }

object TotalUsersCommand extends Command {
    override def execute(req: HttpServletRequest, resp: HttpServletResponse): Unit = {
        val totalUsers = DAOFactory.getUserDAO.count
        resp.setStatus(200)
        resp.getWriter.print(totalUsers)
    }
}
