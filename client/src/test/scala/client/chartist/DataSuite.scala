package client.chartist

import munit.FunSuite

class DataSuite extends FunSuite {

  test("initialize") {
    val data = new Data(1.0)
    assertEquals(data.labels.toList, List(0, 1))
    assertEquals(data.series.map(_.toList).toList, List(List(0.0, 1.0)))
  }

  test("add") {
    val data = new Data(1.0)
    data.add(2.0)
    data.add(3.0)
    assertEquals(data.labels.toList, List(0, 1, 2, 3))
    assertEquals(data.series.map(_.toList).toList, List(List(0.0, 1.0, 2.0, 3.0)))
  }
}
