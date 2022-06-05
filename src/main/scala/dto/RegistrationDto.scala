package edu.mmsa.danikvitek.minesweeper
package dto

import little.json.*
import little.json.Implicits.{ *, given }

import scala.language.implicitConversions

case class RegistrationDto(username: String,
                           email: String,
                           password: String)

given jsonToRegistrationDto: JsonInput[RegistrationDto] with
    override def apply(json: JsonValue): RegistrationDto =
        RegistrationDto(
            json("username"),
            json("email"),
            json("password")
        )