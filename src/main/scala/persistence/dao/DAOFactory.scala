package edu.mmsa.danikvitek.minesweeper
package persistence.dao

import persistence.connection.DBConnection
import persistence.dao.mongo.*
import persistence.dao.sql.*

case object DAOFactory {
    def getGameDAO: GameDAO = DBConnection.MODE match
        case "SQL" => SQLGameDAO
        case "Mongo" => MongoGameDAO

    def getUserDAO: UserDAO = DBConnection.MODE match
        case "SQL" => SQLUserDAO
        case "Mongo" => MongoUserDAO
}
