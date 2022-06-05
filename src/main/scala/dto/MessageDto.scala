package edu.mmsa.danikvitek.minesweeper
package dto

import little.json.*
import little.json.Implicits.{ *, given }

import scala.language.implicitConversions

case class MessageDto(message: String)

given messageDtoToJson: JsonOutput[MessageDto] with
    override def apply(dto: MessageDto): JsonValue = Json.obj("message" -> dto.message)