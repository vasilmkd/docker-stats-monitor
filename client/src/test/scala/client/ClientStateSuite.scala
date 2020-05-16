package client

import cats.implicits._
import munit.FunSuite

import model._

class ClientStateSuite extends FunSuite {

  private val chartState = ChartState(
    new TestLineChart[Either[Throwable, *]],
    new TestLineChart[Either[Throwable, *]]
  )

  test("add") {
    val state   = ClientState(Map("974a307336dc" -> chartState))
    val updated = state + ("a50c5dbb6f4a" -> chartState)
    assert(updated.map.contains("a50c5dbb6f4a"))
  }

  test("remove") {
    val state   = ClientState(Map("974a307336dc" -> chartState))
    val updated = state - "974a307336dc"
    assert(!updated.map.contains("974a307336dc"))
  }

  test("get present") {
    val state = ClientState(Map("974a307336dc" -> chartState))
    assert(state.get("974a307336dc").isRight)
  }

  test("get not present") {
    val state = ClientState(Map("974a307336dc" -> chartState))
    assert(state.get("blah").isLeft)
  }

  test("partition") {
    val stats = Stats(
      Set(
        ContainerData(
          "974a307336dc",
          "xenodochial_colden",
          4.59,
          "1.147GiB / 1.944GiB",
          59.00,
          "1.05kB / 0B",
          "0B / 0B",
          42
        ),
        ContainerData(
          "a50c5dbb6f4a",
          "happy_stonebraker",
          5.51,
          "1.111GiB / 1.944GiB",
          57.14,
          "766B / 0B",
          "0B / 0B",
          61
        )
      )
    )

    val state = ClientState[Either[Throwable, *]](
      Map(
        "a50c5dbb6f4a" -> chartState,
        "8275d414836b" -> chartState
      )
    )

    val ClientState.Partition(removed, added, updated) = state.partition(stats)

    assertEquals(removed, Set("8275d414836b"))
    assertEquals(added, Set("974a307336dc"))
    assertEquals(updated, Set("a50c5dbb6f4a"))
  }
}
