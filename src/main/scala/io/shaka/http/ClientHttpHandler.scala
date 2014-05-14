package io.shaka.http

import io.shaka.http.Http._
import java.net.{HttpURLConnection, URL}
import io.shaka.http.Status._
import scala.Some
import scala.io.Source
import HttpHeader.httpHeader
import scala.collection.JavaConversions.mapAsScalaMap
import scala.collection.JavaConversions.collectionAsScalaIterable

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
    val is = if(s.code >=400) connection.getErrorStream else connection.getInputStream
    val entity = Entity(Source.fromInputStream(is).map(_.toByte).toArray)
    val headers: Map[HttpHeader, String] = connection.getHeaderFields.map(pair => (httpHeader(pair._1), pair._2.head)).toMap

    Response(
      status = s,
      headers = headers,
      entity = Some(entity)
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
