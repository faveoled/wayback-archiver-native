package app

import scala.concurrent.Future

import sttp.client3._

object ApiClient {


  def fetchGet(url: String): String = {
    // Create a basic request with the given URL
    val request = basicRequest.get(uri"$url")
    // Send the request using the default backend and get the response
    implicit val backend: SttpBackend[Identity, Any] = CurlBackend()
    val response = request.send()

    // Extract the body of the response as a string
    val body = response.body

    val result = body.fold(error => throw new Exception(error), identity)
    result
  }

  def fetchPost(url: String, headersArg: Map[String, String], bodyArg: String): String = {
    // create a basic request with the given url, headers and body
    val request = basicRequest.post(uri"$url").headers(headersArg).body(bodyArg)
    // create an asynchronous backend based on AsyncHttpClient
    implicit val backend: SttpBackend[Identity, Any] = CurlBackend()
    // send the request and map the response body to a string
    val resp = request.send().body.fold(error => throw new Exception(error), identity)
    resp
  }

  def hasConnection(): Boolean = {
    try {
      fetchGet("https://google.com").nonEmpty
      true
    } catch {
      case e: Exception =>
        e.printStackTrace()
        false
    }
  }
}
