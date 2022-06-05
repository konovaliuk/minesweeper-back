package edu.mmsa.danikvitek.minesweeper
package persistence.dao.sql

import persistence.connection.ConnectionPool
import persistence.dao.UserDAO
import persistence.entity.{ Email, User }

import com.typesafe.scalalogging.Logger
import org.intellij.lang.annotations.Language

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

    private def extractUserFromRS(rs: ResultSet): User = User.builder
      .withId(rs.getLong(COLUMN_ID))
      .withUsername(rs.getString(COLUMN_USERNAME))
      .withEmail(Email(rs.getString(COLUMN_EMAIL)))
      .withPasswordHash(rs.getString(COLUMN_PASSWORD_HASH))
      .withSalt(rs.getString(COLUMN_SALT))
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

    override def findRatingById(id: Long): Option[Long] = ??? // todo: Implement rating fetch

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

        val conn = Await.result(ConnectionPool.startConnection, 1.second)
        @Language("MariaDB") val st = s"insert into $TABLE (" +
          s"$COLUMN_USERNAME, " +
          s"$COLUMN_EMAIL, " +
          s"$COLUMN_PASSWORD_HASH, " +
          s"$COLUMN_SALT) " +
          s"value (?, ?, ?, ?);"
        val ps = conn.prepareStatement(st)
        
        ps.setString(1, username)
        ps.setString(2, email.value)
        ps.setString(3, hash)
        ps.setString(4, salt)

        ps.executeUpdate
        LOGGER info s"Saved user: $user"

        ps.close()
        ConnectionPool endConnection conn

        User.builder
          .withId(findByUsername(username).get.id)
          .withUsername(username)
          .withEmail(email)
          .withPasswordHash(hash)
          .withSalt(salt)
          .build
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
          s"$COLUMN_SALT = ? " +
          s"where $COLUMN_ID = ?;"
        val ps = conn.prepareStatement(st)

        ps.setString(1, user.username)
        ps.setString(2, user.email.value)
        ps.setString(3, user.passwordHash)
        ps.setString(4, user.salt)
        ps.setLong(5, user.id)

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
