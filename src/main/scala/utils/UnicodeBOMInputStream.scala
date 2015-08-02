package utils

import java.io.{InputStream, PushbackInputStream}

class UnicodeBOMInputStream(inputStream: InputStream) extends InputStream {
  private var skipped = false
  private val in = new PushbackInputStream(inputStream, 4)
  private val bytes: Array[Byte] = new Array[Byte](4)
  private val cnt = in.read(bytes, 0, 4)

  val EF = 0xEF.toByte
  val BB = 0xBB.toByte
  val BF = 0xBF.toByte
  val FE = 0xFE.toByte
  val FF = 0xFF.toByte

  val BOM_UTF_8 = Array(EF, BB, BF)
  val BOM_UTF_16_BE = Array(FE, FF)
  val BOM_UTF_16_LE = Array(FF, FE)
  val BOM_UTF_32_BE = Array(0, 0, FE, FF)
  val BOM_UTF_32_LE = Array(FF, FE, 0, 0)
  val BOM_NONE = Array.empty[Byte]

  val bom = bytes match {
    case Array(EF, BB, BF, _*) => BOM_UTF_8
    case Array(FE, FF, _*) => BOM_UTF_16_BE
    case Array(FF, FE, _*) => BOM_UTF_16_LE
    case Array(0, 0, FE, FF) => BOM_UTF_32_BE
    case Array(FF, FE, 0, 0) => BOM_UTF_32_LE
    case _ => BOM_NONE
  }
  if (cnt > 0) in.unread(bytes, 0, cnt)

  def skipBom() = {
    if (!skipped) {
      in.skip(bom.length)
      skipped = true
    }
    this
  }

  override def read(): Int = in.read()
}
