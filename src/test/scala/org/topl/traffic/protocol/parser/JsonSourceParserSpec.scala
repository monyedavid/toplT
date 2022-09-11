package org.topl.traffic.protocol.parser

import org.scalatest.TryValues
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should
import org.topl.traffic.protocol.parsers.JsonSourceParser

import scala.util.{Success, Try}

class JsonSourceParserSpec extends AnyFlatSpec with should.Matchers with TryValues {
  import org.topl.traffic.protocol.models._

  "Source parser" should "identify httplink correctly" in {
    val sp = JsonSourceParser[Try]

    sp.fromString("https://somerandomlink.com") should be(Success(ExtJsonSource("https://somerandomlink.com")))
    sp.fromString("http://somerandomlink.com") should be(Success(ExtJsonSource("http://somerandomlink.com")))
  }

  it should "identify file source correctly" in {
    val sp = JsonSourceParser[Try]
    sp.fromString("c:/pippo/pluto/paperino/zio.paperone.json") should
    be(Success(FileJsonSource("c:/pippo/pluto/paperino/zio.paperone.json")))

    sp.fromString("c:\\my-folder\\another_folder\\abc.v2.json") should
    be(Success(FileJsonSource("c:\\my-folder\\another_folder\\abc.v2.json")))

    sp.fromString("\\Users\\bot\\Desktop\\void\\com.github\\toplT\\src\\main\\resources\\sample-data.json") should
    be(
      Success(FileJsonSource("\\Users\\bot\\Desktop\\void\\com.github\\toplT\\src\\main\\resources\\sample-data.json"))
    )

    sp.fromString("/Users/bot/Desktop/void/com.github/toplT/src/main/resources/sample-data.json") should
    be(
      Success(FileJsonSource("/Users/bot/Desktop/void/com.github/toplT/src/main/resources/sample-data.json"))
    )
  }

  it should "identify all incorrect sources" in {
    val sp = JsonSourceParser[Try]
    // sp.fromString("c:/pippo/pluto/paperino/zio.paperone.notJson").isFailure should be(true)
    //sp.fromString("").isFailure should be(true)
  }
}
