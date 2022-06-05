package edu.mmsa.danikvitek.minesweeper
package dto

import little.json.*
import little.json.Implicits.{ *, given }

import scala.language.implicitConversions

case class EmailDto(email: String)

given jsonToEmailDto: JsonInput[EmailDto] with
    override def apply(json: JsonValue): EmailDto = EmailDto(json("email"))