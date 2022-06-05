package edu.mmsa.danikvitek.minesweeper
package persistence.dao.sql

import persistence.connection.ConnectionPool
import persistence.dao.DAO
import persistence.entity.{ Cell, CellPK }

import com.typesafe.scalalogging.Logger
import org.intellij.lang.annotations.Language

import scala.annotation.tailrec
import scala.concurrent.Await
import scala.concurrent.duration.*

object SQLGameCellsDAO extends DAO[Cell, CellPK] {
    private final lazy val UOE_MSG = "This operation is not supported"
    private final lazy val LOGGER = Logger(SQLGameCellsDAO.getClass)

    private final lazy val TABLE = "cells"
    private final lazy val COLUMN_GAME_ID = "game_id"
    private final lazy val COLUMN_X = "x"
    private final lazy val COLUMN_Y = "y"
    private final lazy val COLUMN_IS_MINED = "is_mined"
    private final lazy val COLUMN_IS_FLAGGED = "is_flagged"
    private final lazy val COLUMN_IS_DISCOVERED = "is_discovered"

    override def findById(id: CellPK): Option[Cell] = throw new UnsupportedOperationException(UOE_MSG)

    override def findAll: List[Cell] = throw new UnsupportedOperationException(UOE_MSG)

    override def save(entity: Cell): Cell = throw new UnsupportedOperationException(UOE_MSG)

    override def update(entity: Cell): Cell = throw new UnsupportedOperationException(UOE_MSG)

    override def delete(id: CellPK): Unit = throw new UnsupportedOperationException(UOE_MSG)

    def findAllByGameId(gameId: Long): List[Cell] = {
        val conn = Await.result(ConnectionPool.startConnection, 0.5.seconds)
        val ps = conn.prepareStatement(s"select * from $TABLE where $COLUMN_GAME_ID = ?;")
        ps.setLong(1, gameId)
        val rs = ps.executeQuery

        @tailrec
        def collectResults(result: List[Cell] = Nil): List[Cell] = {
            if !rs.next() then result
            else collectResults(
                result :+ Cell.builder
                  .withX(rs.getByte(COLUMN_X))
                  .withY(rs.getByte(COLUMN_Y))
                  .withMined(rs.getBoolean(COLUMN_IS_MINED))
                  .withFlagged(rs.getBoolean(COLUMN_IS_FLAGGED))
                  .withDiscovered(rs.getBoolean(COLUMN_IS_DISCOVERED))
                  .build
            )
        }

        val res = collectResults()
        LOGGER info s"Fetched cells: $res"
        
        ps.close()
        ConnectionPool endConnection conn
        
        res
    }

    def saveAll(cells: List[Cell]): Unit = {
        if cells.sizeIs == 0 then return
        val conn = Await.result(ConnectionPool.startConnection, 0.5.seconds)
        @Language("MariaDB") val st = s"insert ignore into $TABLE (" +
          s"$COLUMN_GAME_ID, $COLUMN_X, $COLUMN_Y, $COLUMN_IS_MINED, $COLUMN_IS_FLAGGED, $COLUMN_IS_DISCOVERED" +
          s") values ${"(?, ?, ?, ?, ?, ?), " * (cells.size - 1)} (?, ?, ?, ?, ?, ?);"
        val ps = conn.prepareStatement(st)

        @tailrec
        def insertValuesInStatement(i: Int = 1, rest: List[Cell] = cells): Unit = {
            if rest.sizeIs > 0 then {
                ps.setLong(i, rest.head.gameId)
                ps.setByte(i + 1, rest.head.x)
                ps.setByte(i + 2, rest.head.y)
                ps.setBoolean(i + 3, rest.head.isMined)
                ps.setBoolean(i + 4, rest.head.isFlagged)
                ps.setBoolean(i + 5, rest.head.isDiscovered)
                insertValuesInStatement(i + 6, rest.tail)
            }
        }

        insertValuesInStatement()

        ps.executeLargeUpdate()

        LOGGER.info(s"Saved cells: $cells")

        ps.close()
        ConnectionPool endConnection conn
    }

    def updateAll(cells: List[Cell]): Unit = {
        if cells.sizeIs == 0 then return
        val conn = Await.result(ConnectionPool.startConnection, 0.5.seconds)

        @Language("MariaDB") val st = s"update ignore $TABLE set " +
          s"$COLUMN_IS_FLAGGED = ?, " +
          s"$COLUMN_IS_DISCOVERED = ? " +
          s"where $COLUMN_GAME_ID = ? and $COLUMN_X = ? and $COLUMN_Y = ?;"
        @tailrec
        def loop(rest: List[Cell] = cells): Unit = {
            val head = rest.head
            val ps = conn.prepareStatement(st)
            ps.setBoolean(1, head.isFlagged)
            ps.setBoolean(2, head.isDiscovered)
            ps.setLong(3, head.gameId)
            ps.setByte(4, head.x)
            ps.setByte(5, head.y)
            ps.executeUpdate()
            ps.close()
            loop(rest.tail)
        }
        
        conn.beginRequest()
        loop()
        conn.endRequest()
        if !conn.getAutoCommit then conn.commit()
        
        ConnectionPool endConnection conn
    }
}
