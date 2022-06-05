package edu.mmsa.danikvitek.minesweeper
package persistence.dao

/**
 * Main Data Access Object trait
 *
 * @tparam A user type
 * @tparam I user ID type
 */
trait DAO[A, I] {
    /**
     * Find an genre by its ID
     *
     * @param id user's ID
     * @return Option of genre
     */
    def findById(id: I): Option[A]

    /**
     * @return a list of all existing entities of the given type
     */
    def findAll: List[A]

    /**
     * Saves or updates the given genre in the database
     *
     * @param entity Entity to save
     * @return saved genre
     */
    def save(entity: A): A

    def update(entity: A): A

    /**
     * Deletes the genre by the given ID if any
     *
     * @param id The ID of the genre to delete from database
     */
    def delete(id: I): Unit
}
