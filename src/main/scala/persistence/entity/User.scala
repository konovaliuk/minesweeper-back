package edu.mmsa.danikvitek.minesweeper
package persistence.entity

import edu.mmsa.danikvitek.minesweeper.util.exception.IncorrectlyBuiltUserException
import org.jetbrains.annotations.{ NotNull, Nullable }

import java.nio.charset.Charset
import java.security.MessageDigest
import scala.util.Random

case class User(@Nullable id: java.lang.Long,
                username: String,
                email: Email,
                passwordHash: String,
                salt: String,
                @NotNull refreshToken: Option[String])
  extends Comparable[User] {
    override def compareTo(o: User): Int = id compareTo o.id
}

object User:
    def builder: UserBuilder = new UserBuilder();

private class UserBuilder {
    private var id: java.lang.Long = _
    private var username: String = _
    private var email: Email = _
    private var passwordHash: String = _
    private var salt: String = _
    private var refreshToken: String = _

    def withId(@Nullable id: Long): UserBuilder = {
        this.id = id
        this
    }

    def withUsername(@NotNull username: String): UserBuilder = {
        this.username = username
        this
    }

    def withEmail(@NotNull email: Email): UserBuilder = {
        this.email = email
        this
    }

    def withPasswordHash(@NotNull passwordHash: String): UserBuilder = {
        this.passwordHash = passwordHash
        this
    }

    def withSalt(@NotNull salt: String): UserBuilder = {
        this.salt = salt
        this
    }

    def withPassword(@NotNull password: String): UserBuilder = {
        this.salt = Random.alphanumeric.take(8).foldLeft("")(_ + _)
        val md = MessageDigest.getInstance("SHA-256")
        md.update((password + this.salt).getBytes(Charset.forName("UTF-8")))
        this.passwordHash = String(md.digest());
        this
    }

    def withRefreshToken(@Nullable refreshToken: String): UserBuilder = {
        this.refreshToken = refreshToken
        this
    }

    def build: User = {
        if Seq(username, email, passwordHash, salt) contains null then throw IncorrectlyBuiltUserException(
            "Some of the required fields are null"
        )
        User(id, username, email, passwordHash, salt, Option(refreshToken))
    }
}
