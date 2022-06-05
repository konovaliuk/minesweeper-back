package edu.mmsa.danikvitek.minesweeper
package web.controller

import web.command.*

import javax.servlet.http.HttpServletRequest

object ControllerHelper:
    def getCommand(req: HttpServletRequest): Command = {
        lazy val path = req.getRequestURI.tail split '/'
        req.getMethod match
            case "GET" => path match
                case Array("api", "hello") => HelloCommand
                case _ => NoCommand
            case "POST" => path match
                case Array("api", "reg") => RegisterCommand
                case Array("api", "check-email") => CheckEmailCommand
                case Array("api", "check-username") => CheckUsernameCommand
                case _ => NoCommand
            case "PUT" => path match
                case _ => NoCommand
            case "DELETE" => path match
                case _ => NoCommand
            case _ => NoCommand
    }
end ControllerHelper