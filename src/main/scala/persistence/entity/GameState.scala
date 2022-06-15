package edu.mmsa.danikvitek.minesweeper
package persistence.entity

enum GameState(rating: Byte) {
    case WON extends GameState(1)
    case LOST extends GameState(-1)
}
