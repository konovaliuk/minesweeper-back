package edu.mmsa.danikvitek.minesweeper
package persistence.dao.sql

import persistence.connection.ConnectionPool
import persistence.dao.*
import persistence.entity.{ Cell, Game, GameState }
import util.Lazy

import com.typesafe.scalalogging.Logger
import org.intellij.lang.annotations.Language

import java.sql.{ Connection, PreparedStatement, ResultSet }
import scala.annotation.tailrec
import scala.concurrent.Await
import scala.concurrent.duration.*

object SQLGameDAO extends GameDAO {
    private final lazy val LOGGER = Logger(SQLGameDAO.getClass)

    private final lazy val TABLE = "games"
    private final lazy val COLUMN_ID = "id"
    private final lazy val COLUMN_USER_ID = "user_id"
    private final lazy val COLUMN_WIDTH = "width"
    private final lazy val COLUMN_HEIGHT = "height"
    private final lazy val COLUMN_GAME_STATE = "state"

    private final lazy val cellsDAO = SQLGameCellsDAO

    private def extractGameFromRS(rs: ResultSet): Game = {
        val gameId = rs.getLong(COLUMN_ID)
        Game.builder
          .withId(gameId)
          .withUserId(rs.getLong(COLUMN_USER_ID))
          .withWidth(rs.getByte(COLUMN_WIDTH))
          .withHeight(rs.getByte(COLUMN_HEIGHT))
          .withGameSate(GameState.valueOf(rs.getString(COLUMN_GAME_STATE)))
          .withLazyCells(Lazy(cellsDAO.findAllByGameId(gameId)))
          .build
    }

    override def findById(id: Long): Option[Game] = {
        val conn = Await.result(ConnectionPool.startConnection, 0.5.seconds)
        val ps = conn.prepareStatement(s"select * from $TABLE where $COLUMN_ID = ?;")
        ps.setLong(1, id)
        val rs = ps.executeQuery()

        val result = if rs.next then Some {
            val game = extractGameFromRS(rs)
            LOGGER info s"Found game by id $id: $game"
            game
        }
        else None

        ps.close()
        ConnectionPool endConnection conn

        result
    }

    override def findAll: List[Game] = {
        val conn = Await.result(ConnectionPool.startConnection, 0.5.seconds)
        val ps = conn.prepareStatement(s"select * from $TABLE;")
        val rs = ps.executeQuery()

        collectResults(rs, ps, conn)
    }

    private def collectResults(rs: ResultSet, ps: PreparedStatement, conn: Connection): List[Game] = {
        @tailrec
        def collectResults(acc: List[Game] = Nil): List[Game] = {
            if rs.next then collectResults(
                acc :+ {
                    val game = extractGameFromRS(rs)
                    LOGGER info s"One of findAll games: $game"
                    game
                }
            )
            else {
                ps.close()
                ConnectionPool endConnection conn
                acc
            }
        }

        collectResults()
    }

    override def save(game: Game): Game = {
        val userId = game.userId
        val state = game.state
        val width = game.width
        val height = game.height

        val conn = Await.result(ConnectionPool.startConnection, 0.5.seconds)
        @Language("MariaDB") val st =
            s"insert into $TABLE ($COLUMN_USER_ID, $COLUMN_GAME_STATE, $COLUMN_WIDTH, $COLUMN_HEIGHT) value(?, ?, ?, ?);"
        val ps = conn.prepareStatement(st)
        ps.setLong(1, userId)
        ps.setString(2, state.toString)
        ps.setByte(3, width)
        ps.setByte(4, height)

        ps.executeUpdate()

        ps.close()
        ConnectionPool endConnection conn

        val gameId = this.findLastByUser(userId).get.id
        val cells = game.cells.get.map(_.copy(gameId = gameId))
        LOGGER info s"cells: $cells"

        if cells.sizeIs > 0 then cellsDAO.saveAll(cells)

        Game.builder
          .withId(gameId)
          .withWidth(width)
          .withHeight(height)
          .withCells(cells)
          .withUserId(userId)
          .withGameSate(state)
          .build
    }

    override def findLastByUser(userId: Long): Option[Game] = {
        val conn = Await.result(ConnectionPool.startConnection, 0.5.seconds)
        @Language("MariaDB") val st = s"select * from $TABLE " +
          s"where $COLUMN_USER_ID = ? and $COLUMN_ID = ( " +
          s"    select max($COLUMN_ID) from $TABLE where $COLUMN_USER_ID = ? " +
          s");"
        val ps = conn.prepareStatement(st)
        ps.setLong(1, userId)
        ps.setLong(2, userId)
        val rs = ps.executeQuery()

        val result = if rs.next() then Some {
            val game = extractGameFromRS(rs)
            LOGGER info s"Found last game by user_id $userId: $game"
            game
        }
        else None

        ps.close()
        ConnectionPool endConnection conn

        result
    }

    @throws[IllegalArgumentException]("If the game id is null")
    override def update(game: Game): Game = {
        if game.id == null then throw new IllegalArgumentException("game id must be non null")
        val conn = Await.result(ConnectionPool.startConnection, 0.5.seconds)
        @Language("MariaDB") val st = s"update ignore $TABLE set $COLUMN_GAME_STATE = ? where $COLUMN_ID = ?;"
        val ps = conn.prepareStatement(st)
        ps.setString(1, game.state.toString)
        ps.setLong(2, game.id)

        ps.executeUpdate()
        ps.close()
        ConnectionPool endConnection conn

        cellsDAO.updateAll(game.cells.get)

        LOGGER info s"Updated game: $game"

        game
    }

    override def delete(id: Long): Unit = {
        val conn = Await.result(ConnectionPool.startConnection, 0.5.seconds)
        @Language("MariaDB") val st = s"delete ignore from $TABLE where $COLUMN_ID = ?;"
        val ps = conn.prepareStatement(st)
        ps.setLong(1, id)

        ps.executeUpdate()
        LOGGER info s"Deleted game by id: $id"

        ps.close()
        ConnectionPool endConnection conn
    }
}
