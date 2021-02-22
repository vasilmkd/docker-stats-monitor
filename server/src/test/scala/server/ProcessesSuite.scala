package server

import munit.FunSuite

class ProcessesSuite extends FunSuite {

  test("parse") {
    val line   =
      "8c2f7598fc14-everythingelseistruncated,,,cassandra:3.11.6,,,3 hours ago,,,7000-7001/tcp, 7199/tcp, 9042/tcp, 9160/tcp,,,Up 3 hours,,,884kB (virtual 379MB)"
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

  test("invalid id") {
    val line   =
      "short,,,cassandra:3.11.6,,,3 hours ago,,,7000-7001/tcp, 7199/tcp, 9042/tcp, 9160/tcp,,,Up 3 hours,,,884kB (virtual 379MB)"
    val result = Processes(
      "",
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
