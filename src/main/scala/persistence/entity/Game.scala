package edu.mmsa.danikvitek.minesweeper
package persistence.entity

import persistence.entity.Game.GameBuilder
import util.Lazy

import org.jetbrains.annotations.{ NotNull, Nullable }

case class Game private(id: java.lang.Long,
                        userId: Long,
                        width: Byte,
                        height: Byte,
                        state: GameState,
                        cells: Lazy[List[Cell]])

object Game {
    def builder: GameBuilder = new GameBuilder()

    class GameBuilder {
        private var id: java.lang.Long = _
        private var userId: Long = _
        private var gameState: GameState = _

        def withId(@Nullable id: java.lang.Long): GameBuilder = {
            this.id = id
            this
        }

        def withUserId(userId: Long): GameBuilder = {
            this.userId = userId
            this
        }

        def withGameSate(gameState: GameState): GameBuilder = {
            this.gameState = gameState
            this
        }

        def withWidth(width: Byte): GameBuilderWithWidth = new GameBuilderWithWidth(id, userId, gameState, width)

        def withHeight(height: Byte): GameBuilderWithHeight = new GameBuilderWithHeight(id, userId, gameState, height)
    }

    class GameBuilderWithWidth(private var id: java.lang.Long,
                               private var userId: Long,
                               private var gameState: GameState,
                               private var width: Byte) {
        def withId(@Nullable id: java.lang.Long): GameBuilderWithWidth = {
            this.id = id
            this
        }

        def withUserId(userId: Long): GameBuilderWithWidth = {
            this.userId = userId
            this
        }

        def withWidth(width: Byte): GameBuilderWithWidth = {
            this.width = width
            this
        }

        def withGameSate(@NotNull gameState: GameState): GameBuilderWithWidth = {
            this.gameState = gameState
            this
        }

        def withHeight(height: Byte): GameBuilderWithWidthAndHeight =
            new GameBuilderWithWidthAndHeight(id, userId, gameState, width, height)
    }

    class GameBuilderWithHeight(private var id: java.lang.Long,
                                private var userId: Long,
                                private var gameState: GameState,
                                private var height: Byte) {
        def withId(@Nullable id: java.lang.Long): GameBuilderWithHeight = {
            this.id = id
            this
        }

        def withUserId(userId: Long): GameBuilderWithHeight = {
            this.userId = userId
            this
        }

        def withHeight(height: Byte): GameBuilderWithHeight = {
            this.height = height
            this
        }

        def withGameSate(@NotNull gameState: GameState): GameBuilderWithHeight = {
            this.gameState = gameState
            this
        }

        def withWidth(width: Byte): GameBuilderWithWidthAndHeight = {
            new GameBuilderWithWidthAndHeight(id, userId, gameState, width, height)
        }
    }

    class GameBuilderWithWidthAndHeight(private var id: java.lang.Long,
                                        private var userId: Long,
                                        private var gameState: GameState,
                                        private var width: Byte,
                                        private var height: Byte) {
        private var cells: Lazy[List[Cell]] = _

        def withId(@Nullable id: java.lang.Long): GameBuilderWithWidthAndHeight = {
            this.id = id
            this
        }

        def withUserId(userId: Long): GameBuilderWithWidthAndHeight = {
            this.userId = userId
            this
        }

        def withGameSate(@NotNull gameState: GameState): GameBuilderWithWidthAndHeight = {
            this.gameState = gameState
            this
        }

        def withWidth(width: Byte): GameBuilderWithWidthAndHeight = {
            this.width = width
            this
        }

        def withHeight(height: Byte): GameBuilderWithWidthAndHeight = {
            this.height = height
            this
        }

        def withCells(@NotNull cells: List[Cell]): GameBuilderWithWidthAndHeight = {
            if cells.sizeIs < width * height then throw new IllegalArgumentException("Cells list is too short")
            else if cells.sizeIs > width * height then throw new IllegalArgumentException("Cells list is too long")
            val coords = cells.map(c => (c.x, c.y))
            for {
                x <- 0 to width
                y <- 0 to height
            } {
                if !coords.contains((x, y)) then throw new IllegalArgumentException("Invalid list contents")
            }
            this.cells = Lazy(cells)
            this
        }

        def withLazyCells(@NotNull cells: Lazy[List[Cell]]): GameBuilderWithWidthAndHeight = {
            this.cells = cells
            this
        }

        def build: Game = Game(id, userId, width, height, gameState, cells)
    }
}

case class Cell(gameId: Long,
                x: Byte, y: Byte,
                isMined: Boolean,
                isFlagged: Boolean,
                isDiscovered: Boolean)

case class CellPK(gameId: Long, x: Byte, y: Byte)

object Cell {
    def builder: CellBuilder = new CellBuilder()

    class CellBuilder {
        private var gameId: Long = _
        private var x: Byte = _
        private var y: Byte = _
        private var isMined: Boolean = _
        private var isFlagged: Boolean = false
        private var isDiscovered: Boolean = false

        def withGameId(gameId: Long): CellBuilder = {
            this.gameId = gameId
            this
        }

        def withX(x: Byte): CellBuilder = {
            this.x = x
            this
        }

        def withY(y: Byte): CellBuilder = {
            this.y = y
            this
        }

        def withMined(mined: Boolean): CellBuilder = {
            this.isMined = mined
            this
        }

        def withFlagged(flagged: Boolean): CellBuilder = {
            this.isFlagged = flagged
            this
        }

        def withDiscovered(discovered: Boolean): CellBuilder = {
            this.isDiscovered = discovered
            this
        }

        def build: Cell = Cell(gameId, x, y, isMined, isFlagged, isDiscovered)
    }
}