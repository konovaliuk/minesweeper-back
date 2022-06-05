package edu.mmsa.danikvitek.minesweeper
package persistence.entity

enum GameState(rating: Byte) {
    case IN_PROGRESS extends GameState(0) 
    case WON extends GameState(1)
    case LOST extends GameState(-1)
}
