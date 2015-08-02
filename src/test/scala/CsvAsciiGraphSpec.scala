import org.junit.runner.RunWith
import org.specs2.mutable.Specification
import org.specs2.runner.JUnitRunner


@RunWith(classOf[JUnitRunner])
class CsvAsciiGraphSpec extends Specification {

  "printTable" should {

    "simple input" in {

      CsvAsciiGraph.printTable(Seq((1, 5.0), (2, 45.0), (3, -30.0), (4, 50.0), (5, 10.0), (6, 0.0), (7, -100.0)).toStream).mkString("\n") must beEqualTo(
        """> 50 |                                   |
          |  50 |                 *                 |
          |  45 |       *                           |
          |  40 |                                   |
          |  35 |                                   |
          |  30 |                                   |
          |  25 |                                   |
          |  20 |                                   |
          |  15 |                                   |
          |  10 |                      *            |
          |   5 |  *                                |
          |   0 +---------------------------*-------+
          |  -5 |                                   |
          | -10 |                                   |
          | -15 |                                   |
          | -20 |                                   |
          | -25 |                                   |
          | -30 |            *                      |
          | -35 |                                   |
          | -40 |                                   |
          | -45 |                                   |
          | -50 |                                   |
          |<-50 |                                *  |""".stripMargin)

    }

  }

  "readCsv" should {
    "return simple output" in {
      val input = """date;value
                    |01-08-2015;20
                    |02-08-2015;10
                    |04-08-2015;10
                    |03-08-2015;0
                    |04-08-2015;-20
                    |05-08-2015;30
                    |06-08-2015;-20
                    |07-08-2015;45""".stripMargin.split("\n").toStream
      CsvAsciiGraph.readCsv(input, "date", "value").sorted.map(r => CsvAsciiGraph.format.format(r._1) -> r._2) must
        beEqualTo(Seq(
          "01/08/2015" -> 20.0,
          "02/08/2015" -> 10.0,
          "03/08/2015" -> 0.0,
          "04/08/2015" -> -10.0,
          "05/08/2015" -> 30.0,
          "06/08/2015" -> -20.0,
          "07/08/2015" -> 45.0
        ).sorted)
    }
    "filter" in {
      val input = """date;value
                    |01-01-2015;20
                    |02-01-2015;10
                    |04-01-2015;10
                    |03-01-2015;0
                    |04-01-2015;-20
                    |05-01-2015;30
                    |06-01-2015;-20
                    |07-01-2015;45""".stripMargin.split("\n").toStream
      CsvAsciiGraph.getWeekData(2015, 1, CsvAsciiGraph.readCsv(input, "date", "value")).toSeq.sorted must
        beEqualTo(Seq(4 -> 20.0, 5 -> 10.0, 2 -> 0.0, 3 -> 0.0, 6 -> 0.0, 7 -> -10.0, 1 -> 30.0).sorted)
    }
  }

}
