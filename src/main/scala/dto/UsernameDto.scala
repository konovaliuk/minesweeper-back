package edu.mmsa.danikvitek.minesweeper
package dto

import little.json.*
import little.json.Implicits.{ *, given }

import scala.language.implicitConversions

case class UsernameDto(username: String)

given jsonToUsernameDto: JsonInput[UsernameDto] with
    override def apply(json: JsonValue): UsernameDto = UsernameDto(json("username").as[String])