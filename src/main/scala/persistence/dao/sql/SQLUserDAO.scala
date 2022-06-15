package edu.mmsa.danikvitek.minesweeper
package persistence.dao.sql

import dto.RatingEntry
import persistence.connection.ConnectionPool
import persistence.dao.UserDAO
import persistence.entity.{ Email, User }
import util.exception.NullRefreshedTokenException
import web.MAYBE_JWT_SECRET

import com.typesafe.scalalogging.Logger
import org.intellij.lang.annotations.Language
import pdi.jwt.{ Jwt, JwtAlgorithm, JwtClaim, JwtHeader }

import java.sql.ResultSet
import scala.annotation.tailrec
import scala.concurrent.Await
import scala.concurrent.duration.*

object SQLUserDAO extends UserDAO {
    private final lazy val LOGGER = Logger(SQLUserDAO.getClass)

    private final lazy val TABLE = "users"
    private final lazy val COLUMN_ID = "id"
    private final lazy val COLUMN_USERNAME = "username"
    private final lazy val COLUMN_EMAIL = "email"
    private final lazy val COLUMN_PASSWORD_HASH = "password_hash"
    private final lazy val COLUMN_SALT = "salt"
    private final lazy val COLUMN_REFRESH_TOKEN = "refresh_token"

    private final lazy val TABLE_GAMES = "games"
    private final lazy val COLUMN_GAME_ID = "id"
    private final lazy val COLUMN_GAME_USER_ID = "user_id"
    private final lazy val COLUMN_GAME_STATE = "state"

    private final lazy val TABLE_CELLS = "cells"
    private final lazy val COLUMN_CELLS_GAME_ID = "game_id"
    private final lazy val COLUMN_CELLS_IS_DISCOVERED = "is_discovered"
    private final lazy val COLUMN_CELLS_IS_MINED = "is_mined"

    private def extractUserFromRS(rs: ResultSet): User = User.builder
      .withId(rs.getLong(COLUMN_ID))
      .withUsername(rs.getString(COLUMN_USERNAME))
      .withEmail(Email(rs.getString(COLUMN_EMAIL)))
      .withPasswordHash(rs.getString(COLUMN_PASSWORD_HASH))
      .withSalt(rs.getString(COLUMN_SALT))
      .withRefreshToken(rs.getString(COLUMN_REFRESH_TOKEN))
      .build

    override def findByEmail(email: Email): Option[User] = {
        val conn = Await.result(ConnectionPool.startConnection, 0.5.seconds)
        val ps = conn.prepareStatement(s"select * from $TABLE where $COLUMN_EMAIL = ?;")
        ps.setString(1, email.value)
        val rs = ps.executeQuery

        val result = if rs.next() then Some {
            val user = extractUserFromRS(rs)
            LOGGER info s"Found user by email \"$email\": $user"
            user
        }
        else None

        ps.close()
        ConnectionPool endConnection conn

        result
    }

    override def findRatingById(id: Long): Option[RatingEntry] = {
        val conn = Await.result(ConnectionPool.startConnection, 0.5.seconds)

        this.findById(id).map(_ => {
            @Language("MariaDB") val stTotalPoints = s"select count(1) " +
              s"from $TABLE u " +
              s"join $TABLE_GAMES g on u.$COLUMN_ID = g.$COLUMN_GAME_USER_ID " +
              s"join $TABLE_CELLS c on g.$COLUMN_GAME_ID = c.$COLUMN_CELLS_GAME_ID " +
              s"where c.$COLUMN_CELLS_IS_DISCOVERED = true and c.$COLUMN_CELLS_IS_MINED = false and u.$COLUMN_ID = ?;"
            @Language("MariaDB") val stWins = s"select u.$COLUMN_USERNAME, count(1) " +
              s"from $TABLE u " +
              s"join $TABLE_GAMES g on u.$COLUMN_ID = g.$COLUMN_GAME_USER_ID " +
              s"where g.$COLUMN_GAME_STATE = 'WON' and u.$COLUMN_ID = ?;"
            @Language("MariaDB") val stLosses = s"select count(1) " +
              s"from $TABLE u " +
              s"join $TABLE_GAMES g on u.$COLUMN_ID = g.$COLUMN_GAME_USER_ID " +
              s"where g.$COLUMN_GAME_STATE = 'LOST' and u.$COLUMN_ID = ?;"

            val psTotalPoints = conn.prepareStatement(stTotalPoints)
            psTotalPoints.setLong(1, id)
            val rsTotalPoints = psTotalPoints.executeQuery()
            rsTotalPoints.next()
            val totalPoints = rsTotalPoints.getLong(1)
            psTotalPoints.close()

            val psWins = conn.prepareStatement(stWins)
            psWins.setLong(1, id)
            val rsWins = psWins.executeQuery()
            rsWins.next()
            val (username, wins) = (rsWins.getString(1), rsWins.getLong(2))
            psWins.close()

            val psLosses = conn.prepareStatement(stLosses)
            psLosses.setLong(1, id)
            val rsLosses = psLosses.executeQuery()
            rsLosses.next()
            val losses = rsLosses.getLong(1)
            psLosses.close()

            ConnectionPool endConnection conn

            RatingEntry(username, wins, losses, totalPoints)
        })
    }

    override def count: Long = {
        val conn = Await.result(ConnectionPool.startConnection, 0.5.seconds)
        val ps = conn.prepareStatement(s"select count(1) from $TABLE;")
        val rs = ps.executeQuery()

        val result = if rs.next() then rs.getLong(1) else 0L
        LOGGER info s"Total users count: $result"

        ps.close()
        ConnectionPool endConnection conn

        result
    }

    override def fetchRatings(page: Int, pageSize: Int): List[RatingEntry] = {
        val conn = Await.result(ConnectionPool.startConnection, 0.5.seconds)

        @Language("MariaDB") val stTopSorted =
            s"select u.$COLUMN_USERNAME, u.$COLUMN_ID, count(1) " +
              s"from $TABLE u " +
              s"join $TABLE_GAMES g on u.$COLUMN_ID = g.$COLUMN_GAME_USER_ID " +
              s"join $TABLE_CELLS c on g.$COLUMN_GAME_ID = c.$COLUMN_CELLS_GAME_ID " +
              s"where c.$COLUMN_CELLS_IS_DISCOVERED = true and c.$COLUMN_CELLS_IS_MINED = false " +
              s"group by u.$COLUMN_USERNAME " +
              s"order by 3 desc limit ?, ?;"
        val offset: Int = page * pageSize

        val psTopSorted = conn.prepareStatement(stTopSorted)
        psTopSorted.setInt(1, offset)
        psTopSorted.setInt(2, pageSize)
        val rsTopSorted = psTopSorted.executeQuery()

        @tailrec
        def collectTopSorted(result: List[(String, Long, Long)] = Nil): List[(String, Long, Long)] = {
            if rsTopSorted.next() then
                collectTopSorted(result :+ (
                  rsTopSorted.getString(1), rsTopSorted.getLong(2), rsTopSorted.getLong(3)
                ))
            else {
                psTopSorted.close()
                result
            }
        }

        val result: List[RatingEntry] = for {
            (username, id, totalPoints) <- collectTopSorted()
        } yield {
            @Language("MariaDB") val stWins = s"select count(1) " +
              s"from $TABLE u " +
              s"join $TABLE_GAMES g on u.$COLUMN_ID = g.$COLUMN_GAME_USER_ID " +
              s"where g.$COLUMN_GAME_STATE = 'WON' and u.$COLUMN_ID = ?;"
            @Language("MariaDB") val stLosses = s"select count(1) " +
              s"from $TABLE u " +
              s"join $TABLE_GAMES g on u.$COLUMN_ID = g.$COLUMN_GAME_USER_ID " +
              s"where g.$COLUMN_GAME_STATE = 'LOST' and u.$COLUMN_ID = ?;"

            val psWins = conn.prepareStatement(stWins)
            psWins.setLong(1, id)
            val rsWins = psWins.executeQuery()
            val wins = if rsWins.next() then rsWins.getLong(1) else 0L
            psWins.close()

            val psLosses = conn.prepareStatement(stLosses)
            psLosses.setLong(1, id)
            val rsLosses = psLosses.executeQuery()
            val losses = if rsLosses.next() then rsLosses.getLong(1) else 0L
            psLosses.close()

            RatingEntry(username, wins, losses, totalPoints)
        }

        ConnectionPool endConnection conn

        result
    }

    override def findByRefreshToken(token: String): Option[User] = {
        val conn = Await.result(ConnectionPool.startConnection, 0.5.seconds)
        val ps = conn.prepareStatement(s"select * from $TABLE where $COLUMN_REFRESH_TOKEN = ?;")
        ps.setString(1, token)
        val rs = ps.executeQuery()

        val result = if rs.next() then Some {
            val user = extractUserFromRS(rs)
            LOGGER info s"Found user by token \"$token\": $user"
            user
        }
        else None

        ps.close()
        ConnectionPool endConnection conn

        result
    }

    override def findById(id: Long): Option[User] = {
        val conn = Await.result(ConnectionPool.startConnection, 0.5.seconds)
        val ps = conn.prepareStatement(s"select * from $TABLE where $COLUMN_ID = ?;")
        ps.setLong(1, id)
        val rs = ps.executeQuery

        val result = if rs.next() then Some {
            val user = extractUserFromRS(rs)
            LOGGER info s"Found user by id $id: $user"
            user
        }
        else None

        ps.close()
        ConnectionPool endConnection conn

        result
    }

    override def findAll: List[User] = {
        val conn = Await.result(ConnectionPool.startConnection, 0.5.seconds)
        val ps = conn.prepareStatement(s"select * from $TABLE;")
        val rs = ps.executeQuery

        @tailrec
        def collectToList(acc: List[User] = Nil): List[User] =
            if rs.next() then collectToList(
                acc :+ {
                    val user = extractUserFromRS(rs)
                    LOGGER info s"One of findAll users: $user"
                    user
                }
            )
            else {
                ps.close()
                ConnectionPool endConnection conn
                acc
            }

        val result = collectToList()

        ps.close()
        ConnectionPool endConnection conn

        result
    }

    override def save(user: User): User = {
        val username = user.username
        val email = user.email
        val hash = user.passwordHash
        val salt = user.salt
        val refreshToken = user.refreshToken

        val conn = Await.result(ConnectionPool.startConnection, 1.second)
        @Language("MariaDB") val st = s"insert into $TABLE (" +
          s"$COLUMN_USERNAME, " +
          s"$COLUMN_EMAIL, " +
          s"$COLUMN_PASSWORD_HASH, " +
          s"$COLUMN_SALT, " +
          s"$COLUMN_REFRESH_TOKEN) " +
          s"value (?, ?, ?, ?, ?);"
        val ps = conn.prepareStatement(st)

        ps.setString(1, username)
        ps.setString(2, email.value)
        ps.setString(3, hash)
        ps.setString(4, salt)
        ps.setString(5, refreshToken.orNull)

        ps.executeUpdate
        LOGGER info s"Saved user: $user"

        ps.close()
        ConnectionPool endConnection conn

        val id = findByUsername(username).get.id
        val issuedAt = System.currentTimeMillis()
        this.update(user.copy(
            id = id,
            refreshToken = Some(Jwt.encode(
                JwtHeader(JwtAlgorithm.HS256, "JWT"),
                JwtClaim(
                    subject = Some(id.toString),
                    issuedAt = Some(issuedAt),
                    expiration = Some(issuedAt + 604800000L)
                ),
                MAYBE_JWT_SECRET.get
            ))
        ))
    }

    override def findByUsername(username: String): Option[User] = {
        val conn = Await.result(ConnectionPool.startConnection, 0.5.seconds)
        val ps = conn.prepareStatement(s"select * from $TABLE where $COLUMN_USERNAME = ?;")
        ps.setString(1, username)
        val rs = ps.executeQuery

        val result = if rs.next() then Some {
            val user = extractUserFromRS(rs)
            LOGGER info s"Found user by username \"$username\": $user"
            user
        }
        else None

        ps.close()
        ConnectionPool endConnection conn

        result
    }

    override def update(user: User): User = {
        if user.id == null then throw new IllegalArgumentException("provided user must have a non null id")

        val conn = Await.result(ConnectionPool.startConnection, 0.5.seconds)
        @Language("MariaDB") val st = s"update ignore $TABLE set " +
          s"$COLUMN_USERNAME = ?, " +
          s"$COLUMN_EMAIL = ?, " +
          s"$COLUMN_PASSWORD_HASH = ?, " +
          s"$COLUMN_SALT = ?, " +
          s"$COLUMN_REFRESH_TOKEN = ? " +
          s"where $COLUMN_ID = ?;"
        val ps = conn.prepareStatement(st)

        ps.setString(1, user.username)
        ps.setString(2, user.email.value)
        ps.setString(3, user.passwordHash)
        ps.setString(4, user.salt)
        ps.setString(5, user.refreshToken.getOrElse(throw new NullRefreshedTokenException(
            s"New refresh token for user ${user.id} is null"
        )))
        ps.setLong(6, user.id)

        ps.executeUpdate
        LOGGER info s"Updated user: $user"

        ps.close()
        ConnectionPool endConnection conn

        user
    }

    override def delete(id: Long): Unit = {
        val conn = Await.result(ConnectionPool.startConnection, 0.5.seconds)
        @Language("MariaDB") val st = s"delete ignore from $TABLE where $COLUMN_ID = ?;"
        val ps = conn.prepareStatement(st)
        ps.setLong(1, id)
        ps.executeUpdate

        ps.close()
        ConnectionPool endConnection conn
        LOGGER info s"Deleted user by id: $id"
    }
}
