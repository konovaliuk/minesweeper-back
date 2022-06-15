package edu.mmsa.danikvitek.minesweeper
package dto

import little.json.*
import little.json.Implicits.{ *, given }

import scala.language.implicitConversions

case class RatingEntry(username: String, wins: Long, losses: Long, totalPoints: Long)

given ratingPageEntryToJson: JsonOutput[RatingEntry] with
    override def apply(entry: RatingEntry): JsonValue = entry match
        case RatingEntry(username, wins, losses, totalPoints) => Json.obj(
            "username" -> username,
            "wins" -> wins,
            "losses" -> losses,
            "totalPoints" -> totalPoints
        )