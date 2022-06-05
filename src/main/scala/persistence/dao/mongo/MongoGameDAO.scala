package edu.mmsa.danikvitek.minesweeper
package persistence.dao.mongo

import persistence.dao.GameDAO
import persistence.entity.Game

import scala.collection.mutable

object MongoGameDAO extends GameDAO {
    override def findLastByUser(userId: Long): Option[Game] = ???

    override def findById(id: Long): Option[Game] = ???

    override def findAll: List[Game] = ???

    override def save(entity: Game): Game = ???

    override def update(entity: Game): Game = ???

    override def delete(id: Long): Unit = ???
}
