package app

import StringExtensions.substringBeforeFirst
import StringExtensions.substringBeforeLast

import java.nio.charset.StandardCharsets
import java.util.regex.Pattern
import scala.collection.mutable
import scala.util.Failure
import scala.util.Success
import ApiClient.hasConnection

import java.util.Timer

implicit val ec: scala.concurrent.ExecutionContext = scala.concurrent.ExecutionContext.global

object Main {

  def startRoutine(targetWebsite: String, textToContain: String, delay: Int): Unit = {

    Storage.setTargetWebsite(targetWebsite)
    Storage.setTextToContain(textToContain)
    Storage.setDelay(delay)

    if (ApiClient.hasConnection()) {
      println("has connection")
    } else {
      println("no connection")
    }

    val html = ApiClient.fetchGet(Storage.getTargetWebsite())
    val links = LinksExtractor.extractLinks(html)
    val consumer: StatusConsumer = (link, status) => {
      println(s"status update: ${link} - ${status} - https://web.archive.org/web/${link}")
    }
    val processor = LinksProcessor(links, consumer, Storage.getDelay())
    processor.processAll()
  }

  def main(args: Array[String]): Unit = {
    startRoutine(args(0), args(1), args(2).toInt)
  }
}
