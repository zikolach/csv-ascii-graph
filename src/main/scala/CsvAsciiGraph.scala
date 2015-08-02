import java.util.Calendar

import utils.UnicodeBOMInputStream

import scala.io.BufferedSource

object CsvAsciiGraph {
  type Date = java.util.Date
  val format = new java.text.SimpleDateFormat("dd/MM/yyyy")

  def main(args: Array[String]): Unit = {
    var dateColumnOpt: Option[String] = None
    var valueColumnOpt: Option[String] = None
    var yearOpt: Option[Int] = None
    var weekOpt: Option[Int] = None
    try {
      args.sliding(2, 1).toList.collect {
        case Array("-d", argDateColumn) => dateColumnOpt = Some(argDateColumn)
        case Array("--date-column", argDateColumn) => dateColumnOpt = Some(argDateColumn)
        case Array("-v", argValueColumn) => valueColumnOpt = Some(argValueColumn)
        case Array("--value-column", argValueColumn) => valueColumnOpt = Some(argValueColumn)
        case Array("-y", argYear) => yearOpt = Some(argYear.toInt)
        case Array("--year", argYear) => yearOpt = Some(argYear.toInt)
        case Array("-w", argWeek) => weekOpt = Some(argWeek.toInt)
        case Array("--week", argWeek) => yearOpt = Some(argWeek.toInt)
      }
    } catch {
      case _: NumberFormatException =>
        println("Invalid number argument week or year")
    }
//    println((dateColumnOpt, valueColumnOpt, yearOpt, weekOpt))
    (dateColumnOpt, valueColumnOpt, yearOpt, weekOpt) match {
      case (Some(dateColumn), Some(valueColumn), Some(year), Some(week)) =>
        val input = new BufferedSource(new UnicodeBOMInputStream(System.in).skipBom()).getLines().toStream
        println(s"Data for $year/$week")
        println(printTable(getWeekData(year, week, readCsv(input, dateColumn, valueColumn))).mkString("\n"))
      case _ =>
        println(
          """
            |Usage:
            | -d, --date-colum    - date column name
            | -v, --value-column  - value column name
            | -y, --year          - year
            | -w, --week          - week number
            |
          """.stripMargin)
    }
  }

  def readCsv(input: Stream[String],
              dateColumn: String,
              valueColumn: String): List[(Date, Double)] = {
    val columns: Seq[String] = input.head.split(';').map(_.trim)
    val (di, vi) = (columns.indexOf(dateColumn), columns.indexOf(valueColumn))
    if (di < 0) {
      println(s"$dateColumn column not found in ${columns.mkString(", ")}")
      List.empty
    } else if (vi < 0) {
      println(s"$valueColumn column not found in ${columns.mkString(", ")}")
      List.empty
    } else {
      val agg = input.tail.foldLeft(Map.empty[Date, Double])((acc: Map[Date, Double], line: String) => {
        val parts = line.split(';')
        if (parts.length == columns.length) {
          val date: Date = format.parse(parts(di).replace("-", "/"))
          val value: Double = try {
            parts(vi).replace(",", ".").toDouble
          } catch {
            case e: NumberFormatException =>
              println(s"unparseble number ${parts(vi)}")
              0.0
          }
          val existingValue: Double = acc.getOrElse(date, 0.0)
          acc + (date -> (existingValue + value))
        } else acc
      })
      agg.toList
    }
  }

  def getWeekData(year: Int, week: Int, data: List[(Date, Double)]): Stream[(Int, Double)] = {

    def getStartEndDay(year: Int, week: Int, day: Int): (Date, Date) = {
      val cal = Calendar.getInstance()
      cal.set(Calendar.YEAR, year)
      cal.set(Calendar.WEEK_OF_YEAR, week)
      cal.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
      cal.add(Calendar.DAY_OF_MONTH, day)
      val beginDate = cal.getTime
      cal.add(Calendar.DAY_OF_MONTH, 1)
      (beginDate, cal.getTime)
    }

    def getDayOfWeek(date: Date): Int = {
      val c = Calendar.getInstance()
      c.setTime(date)
      c.get(Calendar.DAY_OF_WEEK)
    }

    for {
      day <- (0 to 6).toStream
      dates = getStartEndDay(year, week, day)
      dayOfWeek = getDayOfWeek(dates._1)
      dayData = data.filter({
        case (date, _) => (date.after(dates._1) || date.equals(dates._1)) && date.before(dates._2)
      }).map(_._2).sum
    } yield (dayOfWeek, dayData)
  }

  def printTable(input: Stream[(Int, Double)]): Stream[String] = for {
    v <- (55 to -55 by -5).toStream
  } yield (v match {
      case 55 => f"> 50 |"
      case -55 => f"<-50 |"
      case 0 => f"$v%4d +"
      case _ => f"$v%4d |"
    }) + input.sortBy(_._1).map(_._2.toInt / 5 * 5).map {
      case `v` if v == 0 => "--*--"
      case `v` => "  *  "
      case i if i > 50 && v == 55 => "  *  "
      case i if i < -50 && v == -55 => "  *  "
      case _ if v == 0 => "-----"
      case _ => "     "
    }.mkString + (if (v == 0) "+" else "|")


}
