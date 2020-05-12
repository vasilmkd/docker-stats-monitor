package models

import cats.implicits._
import munit.FunSuite

class ContainerDataSuite extends FunSuite {

  test("parse") {
    val line = "ed4e5c72308a,affectionate_shockley,0.00%,1.148MiB / 1.944GiB,0.06%,1.45kB / 0B,0B / 0B,1"
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
    assertEquals(ContainerData.parseCSV[Either[Throwable, *]](line), Right(result))
  }

  test("fail") {
    assert(ContainerData.parseCSV[Either[Throwable, *]]("").isLeft)
  }
}
