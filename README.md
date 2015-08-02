Display ascii graph in console
==============================

```
$ cat sample/sample.csv | java -jar target/scala-2.11/csv-ascii-graph.jar CsvAsciiGraph --date-column Buchungsdatum --value-column Betrag -y 2013 -w 3
Data for 2013/3
> 50 |                                   |
  50 |                                   |
  45 |                                   |
  40 |                                   |
  35 |                                   |
  30 |            *                      |
  25 |                                   |
  20 |                                   |
  15 |                                   |
  10 |                                   |
   5 |                                   |
   0 +--*----*--------------*----*----*--+
  -5 |                 *                 |
 -10 |                                   |
 -15 |                                   |
 -20 |                                   |
 -25 |                                   |
 -30 |                                   |
 -35 |                                   |
 -40 |                                   |
 -45 |                                   |
 -50 |                                   |
<-50 |                                   |
```
