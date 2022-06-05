package edu.mmsa.danikvitek.minesweeper
package persistence.dao

import persistence.entity.Game

import org.jetbrains.annotations.NotNull

trait GameDAO extends DAO[Game, Long] {
    def findLastByUser(@NotNull userId: Long): Option[Game]
}
