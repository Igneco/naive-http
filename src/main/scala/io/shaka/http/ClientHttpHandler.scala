package io.shaka.http

import io.shaka.http.Http._
import java.net.{HttpURLConnection, URL}
import io.shaka.http.Status._
import scala.Some
import Headers.toHeaders
import IO.inputStreamToByteArray

class ClientHttpHandler extends HttpHandler {

  override def apply(request: Request): Response = {
    val connection = createConnection(request.url)
    connection.setRequestMethod(request.method.name)
    request.headers.foreach {
      header =>
        connection.setRequestProperty(header._1.name, header._2)
    }
    request.entity.map {
      entity =>
        connection.setDoOutput(true)
        connection.getOutputStream.write(entity.content)
    }
    buildResponse(connection)
  }

  def buildResponse(connection: HttpURLConnection) = {
    val s = status(connection.getResponseCode, connection.getResponseMessage)
    val is = Option(if(s.code >=400) connection.getErrorStream else connection.getInputStream)
    val entity = Entity(is.fold("".getBytes)(inputStreamToByteArray))
    val headers: Headers = toHeaders(connection.getHeaderFields)

    Response(
      status = s,
      headers = headers,
      entity = if(entity.content.isEmpty) None else Some(entity)
    )
  }

  def createConnection(url: Url) = {
    val connection = new URL(url).openConnection().asInstanceOf[HttpURLConnection]
    val timeoutInMillis = 0
    connection.setUseCaches(false)
    connection.setConnectTimeout(timeoutInMillis)
    connection.setReadTimeout(timeoutInMillis)
    connection.setInstanceFollowRedirects(false)
    connection
  }


}

