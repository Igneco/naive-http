package io.shaka.http

import io.shaka.http.Status.OK
import io.shaka.http.HttpHeader.CONTENT_TYPE

object Response {
  def respond(content: String): Response = Response().entity(content)
}

case class Response(status: Status = OK, headers: Headers = Headers.Empty, entity: Option[Entity] = None){

  def status(newStatus: Status) = copy(status = newStatus)

  def header(header: HttpHeader, value: String) = copy(headers = (header, value) :: headers)

  def contentType(value: String) = header(CONTENT_TYPE, value)

  def contentType(value: ContentType) = header(CONTENT_TYPE, value.value)

  def entity(content: String): Response = copy(entity = Some(Entity(content)))

  def entityAsString: String = entity match {
    case Some(value) => value.toString
    case _ => throw new RuntimeException("There is no entity in this response! Consider using response.entity:Option[Entity] instead.")
  }
}
