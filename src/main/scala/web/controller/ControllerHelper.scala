package edu.mmsa.danikvitek.minesweeper
package web.controller

import web.command.*

import com.typesafe.scalalogging.Logger

import javax.servlet.http.HttpServletRequest

object ControllerHelper:
    private val LOGGER = Logger(ControllerHelper.getClass)

    def getCommand(req: HttpServletRequest): Command = {
        lazy val path = (req.getRequestURI.tail split '/').toList
        LOGGER info s"path: $path"
        path match
            case "api" :: _ => req.getMethod match
                case "GET" => path.tail match
                    case List("hello") => HelloCommand
                    case List("total-users") => TotalUsersCommand
                    case List("username") => FetchUsernameCommand
                    case List("validate-login") => ValidateLoginCommand
                    case List("rating-page") => RatingPageCommand
                    case List("user-rating") => UserRatingCommand
                    case _ => NoCommand
                case "POST" => path.tail match
                    case List("reg") => RegisterCommand
                    case List("login") => LogInCommand
                    case List("check-email") => CheckEmailCommand
                    case List("check-username") => CheckUsernameCommand
                    case List("refresh-login") => RefreshLoginCommand
                    case List("save-game-result") => SaveGameCommand
                    case _ => NoCommand
                case "PUT" => path.tail match
                    case _ => NoCommand
                case "DELETE" => path.tail match
                    case List("invalidate-access") => InvalidateAccessCommand
                    case _ => NoCommand
                case _ => NoCommand
            case _ => NoCommand
    }
end ControllerHelper