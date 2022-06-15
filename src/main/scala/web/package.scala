package edu.mmsa.danikvitek.minesweeper

import java.util.ResourceBundle
import scala.util.Try

package object web {
    private lazy val secretsBundle = ResourceBundle.getBundle("secrets")
    lazy val MAYBE_JWT_SECRET: Option[String] = Try(secretsBundle getString "JWT_SECRET").toOption
    
    val HOUR: Long = 3600000L
    val WEEK: Long = 604800000L
}
