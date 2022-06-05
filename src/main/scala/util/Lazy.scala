package edu.mmsa.danikvitek.minesweeper
package util

class Lazy[+A](value: => A) {
    private lazy val internal: A = value

    def flatMap[B](f: (=> A) => Lazy[B]): Lazy[B] = f(internal)

    def map[B](f: A => B): Lazy[B] = flatMap(x => Lazy(f(x)))

    def get: A = internal
}

object Lazy:
    def apply[A](value: => A): Lazy[A] = new Lazy(value)

    def flatten[A](m: Lazy[Lazy[A]]): Lazy[A] = m.flatMap(x => x)
