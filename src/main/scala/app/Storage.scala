package app

import java.nio.charset.StandardCharsets
import java.nio.file.{Files, Paths}
import java.util.Base64
import scala.collection.mutable

object Storage {

  def setDelay(toSet: Int): Unit = {
    setStorageVal("DELAY", toSet.toString)
  }

  def getDelay(): Int = {
    val item = getStorageVal("DELAY")
    item.map(_.toInt).getOrElse(15000)
  }

  def setTextToContain(text: String): Unit = {
    setStorageVal ("TEXT_CONTAIN" , text)
  }

  def getTextToContain(): String = {
    val item = getStorageVal("TEXT_CONTAIN")
    item.getOrElse("google.com")
  }

  def setTargetWebsite(url: String): Unit = {
    setStorageVal ("TARGET_WEBSITE" , url)
  }

  def getTargetWebsite(): String = {
    val item = getStorageVal("TARGET_WEBSITE")
    item.getOrElse("https://google.com")
  }

  private def getKey(url: String): String = {
    val escaped = url.replaceAll("[/:]", "_")
    val key = s"URL_$escaped"
    key
  }

  def isUrlDone(url: String): Boolean = {
    val item = getStorageVal(getKey(url))
    if (item.isEmpty) {
      false
    } else {
      true
    }
  }

  def setUrlDone(url: String): Unit = {
    setStorageVal(getKey(url) , "+")
  }

  var dirsCreated = false

  private def getStorageVal(key: String): Option[String] = {
    if (!dirsCreated) {
      Files.createDirectories(Paths.get("storage"))
      dirsCreated = true
    }
    if (!Files.exists(Paths.get(s"storage/$key.txt"))) {
      return Option.empty
    }
    val value = Files.readString(Paths.get(s"storage/$key.txt"), StandardCharsets.UTF_8)
    Some(value)
  }

  private def setStorageVal(key: String, vall: String): Unit = {
    if (!dirsCreated) {
      Files.createDirectories(Paths.get("storage"))
      dirsCreated = true
    }
    Files.writeString(Paths.get(s"storage/${key}.txt"), vall, StandardCharsets.UTF_8)
  }
}
