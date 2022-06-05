package edu.mmsa.danikvitek.minesweeper
package persistence.entity

import persistence.entity.Email.isValidEmail
import edu.mmsa.danikvitek.minesweeper.util.exception.InvalidEmailException

import java.util.regex.Pattern

case class Email(value: String) {
    if !isValidEmail(value) then throw new InvalidEmailException(s"Email \"$value\" is invalid")
}

object Email {
    private lazy val emailPattern = Pattern.compile(
        "^[\\w!#$%&’*+/=?`{|}~^-]+(?:\\.[\\w!#$%&’*+/=?`{|}~^-]+)*@(?:[a-zA-Z\\d-]+\\.)+[a-zA-Z]{2,6}$"
    )

    def isValidEmail(value: String): Boolean = emailPattern.matcher(value).matches()
}