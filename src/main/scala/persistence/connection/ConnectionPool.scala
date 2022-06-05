package edu.mmsa.danikvitek.minesweeper
package persistence.connection

import com.typesafe.scalalogging.Logger

import java.sql.Connection
import scala.collection.mutable
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.Try

/**
 * The singleton object for accessing the database connections.
 *
 * @note End your connections after you've stopped using them. Otherwise you won't be able to start a new one.
 */
object ConnectionPool {
    private final lazy val LOGGER = Logger("ConnectionPool")
    private final lazy val N_CONNECTIONS: Int = {
        val value = getProperty("n_connections") map (_.toInt) getOrElse 1
        LOGGER info s"Got config value n_connections = $value"
        value
    }

    private final lazy val pool: mutable.Queue[Connection] = mutable.Queue.empty[Connection]

    /**
     * Opens new connection or dequeues an existing one.
     *
     * @return Future of connection
     */
    def startConnection: Future[Connection] = Future {
        LOGGER info "Trying to start connection"
        if pool.isEmpty || pool.sizeIs < N_CONNECTIONS then pool enqueue DBConnection.get
        val conn = pool.dequeue
        LOGGER info "Connection started"
        conn
    }

    /**
     * Ends the connection by adding it back to the pool
     *
     * @param connection Connection to end
     */
    def endConnection(connection: Connection): Unit = {
        if !pool.contains(connection) && !connection.isClosed then pool enqueue connection
        LOGGER info "Connection ended"
    }

    def closeConnections(): Unit = {
        val n = pool.size
        pool.foreach(_.close())
        pool.clear()
        LOGGER info s"All connections closed ($n)"
    }
}
