package edu.mmsa.danikvitek.minesweeper
package dto

import little.json.*
import little.json.Implicits.{ *, given }

import scala.language.implicitConversions

case class ErrorDto(statusCode: Int, message: String)

given errorDtoToJson: JsonOutput[ErrorDto] with
    override def apply(value: ErrorDto): JsonValue = Json.obj(
        "status_code" -> value.statusCode,
        "message" -> value.message
    )