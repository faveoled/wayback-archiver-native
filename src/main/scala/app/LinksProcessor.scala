package app

import app.RemoteCheckedStatus.{FAILED, POSTED, REMOTELY_DONE}

import java.util.Timer
import java.util.concurrent.TimeUnit
import scala.collection.mutable.Queue
import scala.concurrent.Future
import scala.concurrent.Promise
import scala.util.Failure
import scala.util.Success

class LinksProcessor(
  val links: Queue[String],
  val statusConsumer: StatusConsumer,
  val requestDelay: Int
) {

  def this(
    newLinks: List[String],
    statusConsumer: StatusConsumer,
    requestDelay: Int
  ) = {
    this(Queue(), statusConsumer, requestDelay)
    addLinks(newLinks)
  }

  def addLinks(newLinks: List[String]): Unit = {
    links.addAll(newLinks)
  }

  def hasNext(): Boolean = {
    return !links.isEmpty
  }

  case class CheckedLinkStatus(
    val link: String,
    val status: Either[LocalCheckedStatus, RemoteCheckedStatus]
  )


  def consumeStatus(link: String, status: LocalCheckedStatus | RemoteCheckedStatus): Unit = {
    statusConsumer.consumeStatus(link, status)
  }

  def processNext(): CheckedLinkStatus = {
    println("processing next")
    if (links.isEmpty) {
      throw new Exception("No more links to process")
    }
    val link = links.dequeue()
    if (Storage.isUrlDone(link)) {
      return CheckedLinkStatus(link, Left(LocalCheckedStatus.PREV_DONE))
    }

    var checkedStatus: RemoteCheckedStatus = null
    try {
      val hasSnapshots = Wayback.hasSnapshots(link)
      if (hasSnapshots) {
        checkedStatus = RemoteCheckedStatus.REMOTELY_DONE
      } else {
        Wayback.saveNow(link)
        checkedStatus = RemoteCheckedStatus.POSTED
      }
    } catch {
      case e: Exception =>
        e.printStackTrace()
        checkedStatus = RemoteCheckedStatus.FAILED
    }
    CheckedLinkStatus(link, Right(checkedStatus))
  }

  def singleIntervalRun(): Unit = {
    println("interval execution")
    while (true) {
      if (!hasNext()) {
        return
      }
      val next = processNext()
      next.status match
        case Left(status) =>
          consumeStatus(next.link, status)
        case Right(value) =>
          value match {
            case REMOTELY_DONE | POSTED =>
              Storage.setUrlDone(next.link)
              consumeStatus(next.link, value)
            case FAILED =>
              consumeStatus(next.link, RemoteCheckedStatus.FAILED)
          }
      if (next.status.isRight) {
        return
      }
    }
  }

  def processAll(): Unit = {
    if (!hasNext()) {
      println("nothing to do")
      return
    }

    while (hasNext()) {
      val startMs = System.nanoTime() / 1_000_000
      singleIntervalRun()
      val currMs = System.nanoTime() / 1_000_000
      val waitTimeMs = startMs + requestDelay - currMs
      if (waitTimeMs > 0 && links.nonEmpty) {
        println(s"Waiting for next request opportunity... Links remaining: ${links.length}")
        Thread.sleep(waitTimeMs)
      }
    }
    println("Processing DONE")
  }
}
