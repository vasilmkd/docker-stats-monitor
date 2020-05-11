package models

import scala.scalajs.js

import cats.implicits._
import munit.FunSuite

class StatsSuite extends FunSuite {

  test("parse") {
    val json = js.Dynamic.literal("data" -> js.Array[js.Dynamic]())
    assertEquals(Stats.parse[Either[Throwable, *]](json), Right(Stats(Set.empty)))
  }

  test("fail") {
    assert(Stats.parse[Either[Throwable, *]](js.Dynamic.literal()).isLeft)
  }
}
