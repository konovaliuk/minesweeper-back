package edu.mmsa.danikvitek.minesweeper
package web.filter

import javax.servlet.FilterChain
import javax.servlet.annotation.WebFilter
import javax.servlet.http.{ HttpFilter, HttpServletRequest, HttpServletResponse }

@WebFilter(urlPatterns = Array("/api/*"))
class JWTFilter extends HttpFilter {
    override def doFilter(req: HttpServletRequest, res: HttpServletResponse, chain: FilterChain): Unit = {

        chain.doFilter(req, res)
    }
}
