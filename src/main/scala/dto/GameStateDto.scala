package edu.mmsa.danikvitek.minesweeper
package dto

import persistence.entity.GameState

import little.json.*
import little.json.Implicits.{ *, given }

import scala.language.implicitConversions

case class GameStateDto(gameState: GameState, width: Byte, height: Byte, cells: Array[Array[CellDto]])

given jsonToGameStateDto: JsonInput[GameStateDto] with
    override def apply(json: JsonValue): GameStateDto = GameStateDto(
        GameState.valueOf(json("gameState")),
        json("width"),
        json("height"),
        json("cells").as[Array[Array[CellDto]]]
    )