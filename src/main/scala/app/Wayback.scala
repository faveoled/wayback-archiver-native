package app

import scala.concurrent.Future
import java.nio.charset.StandardCharsets
import encoder.URLEncoder

object Wayback {

  def hasSnapshots(url: String): Boolean = {
    val encodedUrl = URLEncoder.encode(url)
    ApiClient.fetchGet(s"https://archive.org/wayback/available?url=${encodedUrl}")
      .contains("\"timestamp\"")
  }

  def saveNow(url: String): Unit = {
    println(s"posting link ${url}")
    val headers = Map[String, String](
      "Content-Type" -> "application/x-www-form-urlencoded"
    )
    ApiClient.fetchPost(
      s"https://web.archive.org/save/${url}",
      headers,
      s"url=${URLEncoder.encode(url)}"
    )
  }
}
