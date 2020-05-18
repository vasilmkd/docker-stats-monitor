package client.chartist

import munit.FunSuite

class DataSuite extends FunSuite {

  test("initialize") {
    val data = new Data(1.0)
    assertEquals(data.labels.toList.length, 30)
    assertEquals(data.labels.toList, List.range(0, 30))
    assertEquals(data.series(0).toList.length, 30)
    assertEquals(data.series(0).toList, List.fill(29)(0.0) :+ 1.0)
  }

  test("add") {
    val data = new Data(1.0)
    assertEquals(data.labels.toList.length, 30)
    assertEquals(data.labels.toList, List.range(0, 30))
    assertEquals(data.series(0).toList.length, 30)
    assertEquals(data.series(0).toList, List.fill(29)(0.0) :+ 1.0)
    data.add(2.0)
    assertEquals(data.labels.toList.length, 30)
    assertEquals(data.labels.toList, List.range(0, 30))
    assertEquals(data.series(0).toList.length, 30)
    assertEquals(data.series(0).toList, List.fill(28)(0.0) ++ List(1.0, 2.0))
    data.add(3.0)
    assertEquals(data.labels.toList.length, 30)
    assertEquals(data.labels.toList, List.range(0, 30))
    assertEquals(data.series(0).toList.length, 30)
    assertEquals(data.series(0).toList, List.fill(27)(0.0) ++ List(1.0, 2.0, 3.0))
  }
}
