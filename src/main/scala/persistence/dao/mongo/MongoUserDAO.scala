package edu.mmsa.danikvitek.minesweeper
package persistence.dao.mongo

import persistence.dao.UserDAO
import persistence.entity.{ Email, User }

object MongoUserDAO extends UserDAO {
    override def findByUsername(username: String): Option[User] = ???

    override def findByEmail(email: Email): Option[User] = ???

    override def findRatingById(id: Long): Option[Long] = ???

    override def findById(id: Long): Option[User] = ???

    override def findAll: List[User] = ???

    override def save(entity: User): User = ???

    override def update(entity: User): User = ???

    override def delete(id: Long): Unit = ???
}
