package edu.mmsa.danikvitek.minesweeper
package persistence.dao

import dto.RatingEntry
import persistence.entity.{ Email, User }

import org.jetbrains.annotations.NotNull

trait UserDAO extends DAO[User, Long] {
    /**
     * Finds the User by his/her username
     *
     * @param username The username to find the user by
     * @return Option of User
     */
    def findByUsername(username: String): Option[User]

    /**
     * Finds the User by his/her email
     *
     * @param email The email to find the user by
     * @return Option of User
     */
    def findByEmail(email: Email): Option[User]

    def findByRefreshToken(@NotNull token: String): Option[User]

    def findRatingById(id: Long): Option[RatingEntry]

    def count: Long

    def fetchRatings(page: Int, pageSize: Int): List[RatingEntry]
}
