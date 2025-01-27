package app

import java.util.regex.Pattern
import scala.collection.mutable

import StringExtensions.substringBeforeFirst
import StringExtensions.substringBeforeLast


object LinksExtractor {
  def extractLinks(htmlContent: String): List[String] = {
    val result: mutable.ArrayBuffer[String] = mutable.ArrayBuffer[String]()
    val regex = "(https?|ftp|file)://[-a-zA-Z0-9+&@#/%?=~_|!:,.;]*[-a-zA-Z0-9+&@#/%=~_|]";
    val p = Pattern.compile(regex);
    val m = p.matcher(htmlContent);
    while (m.find()) {
      result += m.group();
    }
    val a = result
      .filter(link => link.contains(Storage.getTextToContain()))
      .map(link => link.substringBeforeFirst("?"))
      .filter(link => !link.isEmpty())
      .distinct
      .toList
    a
  }
}
