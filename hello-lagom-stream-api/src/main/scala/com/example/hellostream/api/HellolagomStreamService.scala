package com.example.hellostream.api

import akka.NotUsed
import akka.stream.scaladsl.Source
import com.lightbend.lagom.scaladsl.api.transport.Method
import com.lightbend.lagom.scaladsl.api.{Service, ServiceCall}
import play.api.libs.json.{Format, Json}

/**
  * The hello-lagom stream interface.
  *
  * This describes everything that Lagom needs to know about how to serve and
  * consume the HellolagomStream service.
  */
trait HellolagomStreamService extends Service {

  def stream: ServiceCall[Source[String, NotUsed], Source[String, NotUsed]]

  // publisherとsubscriberは同一service内でないといけないらしい
  def pub: ServiceCall[Hello, NotUsed]
  def sub: ServiceCall[Source[Hello, NotUsed], Source[Hello, NotUsed]]

  override final def descriptor = {
    import Service._

    named("hello-lagom-stream")
      .withCalls(
        restCall(Method.POST, "/stream/pub", pub _),
        pathCall("/stream/sub", sub)
//        , namedCall("stream", stream)
      ).withAutoAcl(true)
  }
}

case class Hello(name: String, msg: String)

object Hello {
  implicit val format: Format[Hello] = Json.format[Hello]
}
