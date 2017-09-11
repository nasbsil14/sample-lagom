package com.example.hellostream.impl

import akka.NotUsed
import com.lightbend.lagom.scaladsl.api.ServiceCall
import com.example.hellostream.api.{Hello, HellolagomStreamService}
import com.example.hello.api.HellolagomService
import com.lightbend.lagom.scaladsl.pubsub.{PubSubRegistry, TopicId}

import scala.concurrent.Future

/**
  * Implementation of the HellolagomStreamService.
  */
class HellolagomStreamServiceImpl(hellolagomService: HellolagomService, pubSubRegistry: PubSubRegistry) extends HellolagomStreamService {
  def stream = ServiceCall { hellos =>
    Future.successful(hellos.mapAsync(8)(hellolagomService.hello(_).invoke()))
  }

  def pub() = ServiceCall { hello =>
    val topic = pubSubRegistry.refFor(TopicId[Hello]("pubsub"))
    topic.publish(hello)
    Future.successful(NotUsed.getInstance())
  }
  def sub() = ServiceCall { _ =>
    val topic = pubSubRegistry.refFor(TopicId[Hello]("pubsub"))
    Future.successful(topic.subscriber)
  }
}
