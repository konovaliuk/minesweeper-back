package edu.mmsa.danikvitek.minesweeper
package dto

import little.json.*
import little.json.Implicits.{ *, given }

import scala.language.implicitConversions

case class CellDto(x: Byte, y: Byte, isMined: Boolean, isFlagged: Boolean, isDiscovered: Boolean)

given jsonToCellDto: JsonInput[CellDto] with
    override def apply(json: JsonValue): CellDto = CellDto(
        json("x"), json("y"), 
        json("isMined"), json("isFlagged"), json("isDiscovered")
    )
