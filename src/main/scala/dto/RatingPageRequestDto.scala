package edu.mmsa.danikvitek.minesweeper
package dto

import little.json.*
import little.json.Implicits.{ *, given }

import scala.language.implicitConversions

case class RatingPageRequestDto(page: Int, pageSize: Int)

given jsonToRatingPageRequestDto: JsonInput[RatingPageRequestDto] with
    override def apply(json: JsonValue): RatingPageRequestDto = RatingPageRequestDto(
        json("page"), json("pageSize")
    )