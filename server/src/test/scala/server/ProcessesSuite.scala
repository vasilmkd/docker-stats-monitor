package server

import cats.implicits._
import munit.FunSuite

class ProcessesSuite extends FunSuite {

  test("parse") {
    val line =
      "8c2f7598fc14\tcassandra:3.11.6\t3 hours ago\t7000-7001/tcp, 7199/tcp, 9042/tcp, 9160/tcp\tUp 3 hours\t884kB (virtual 379MB)"
    val result = Processes(
      "8c2f7598fc14",
      "cassandra:3.11.6",
      "3 hours ago",
      "7000-7001/tcp, 7199/tcp, 9042/tcp, 9160/tcp",
      "Up 3 hours",
      "884kB (virtual 379MB)"
    )
    assertEquals(Processes.parseCSV[Either[Throwable, *]](line), Right(result))
  }

  test("fail") {
    assert(Processes.parseCSV[Either[Throwable, *]]("").isLeft)
  }
}
