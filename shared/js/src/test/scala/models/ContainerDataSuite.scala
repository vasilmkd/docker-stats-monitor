package models

import scala.scalajs.js

import cats.implicits._
import munit.FunSuite

class ContainerDataSuite extends FunSuite {

  test("parse") {
    val json = js.Dynamic.literal(
      "id"            -> "ed4e5c72308a",
      "name"          -> "affectionate_shockley",
      "cpuPercentage" -> 0.00,
      "memUsage"      -> "1.148MiB / 1.944GiB",
      "memPercentage" -> 0.06,
      "netIO"         -> "1.45kB / 0B",
      "blockIO"       -> "0B / 0B",
      "pids"          -> 1
    )
    val result = ContainerData(
      "ed4e5c72308a",
      "affectionate_shockley",
      0.00,
      "1.148MiB / 1.944GiB",
      0.06,
      "1.45kB / 0B",
      "0B / 0B",
      1
    )
    assertEquals(ContainerData.parse[Either[Throwable, *]](json), Right(result))
  }

  test("fail") {
    assert(ContainerData.parse[Either[Throwable, *]](js.Dynamic.literal()).isLeft)
  }
}
