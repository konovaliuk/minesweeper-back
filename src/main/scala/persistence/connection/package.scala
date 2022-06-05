package edu.mmsa.danikvitek.minesweeper
package persistence

import org.jetbrains.annotations.NotNull

import java.util.ResourceBundle
import scala.util.Try

package object connection {
    private final lazy val CONNECTION_RESOURCE = ResourceBundle.getBundle("connection")

    def getProperty(@NotNull name: String): Option[String] = Try(CONNECTION_RESOURCE.getString(name)).toOption
}
