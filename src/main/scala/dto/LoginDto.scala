package edu.mmsa.danikvitek.minesweeper
package dto

import dto.LoginDto.Variant

import little.json.*
import little.json.Implicits.{ *, given }

import scala.language.implicitConversions

case class LoginDto(variant: Variant, login: String, password: String)

object LoginDto {
    enum Variant {
        case EMAIL extends Variant
        case USERNAME extends Variant
    }

    object Variant {
        def apply(str: String): Variant = str match
            case "email" => EMAIL
            case "username" => USERNAME
    }
}

given jsonToLoginDto: JsonInput[LoginDto] with
    override def apply(json: JsonValue): LoginDto = LoginDto(
        Variant(json("variant")), json("login"), json("password")
    )