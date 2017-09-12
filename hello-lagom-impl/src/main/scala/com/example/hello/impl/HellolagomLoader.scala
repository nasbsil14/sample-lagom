package com.example.hello.impl

import java.net.URI

import com.lightbend.lagom.scaladsl.api.ServiceLocator
import com.lightbend.lagom.scaladsl.api.ServiceLocator.NoServiceLocator
import com.lightbend.lagom.scaladsl.persistence.cassandra.CassandraPersistenceComponents
import com.lightbend.lagom.scaladsl.server._
import com.lightbend.lagom.scaladsl.devmode.LagomDevModeComponents
import play.api.libs.ws.ahc.AhcWSComponents
import com.example.hello.api.HellolagomService
import com.lightbend.lagom.internal.scaladsl.registry.ServiceRegistry
import com.lightbend.lagom.scaladsl.api.Descriptor.Call
import com.lightbend.lagom.scaladsl.broker.kafka.LagomKafkaComponents
import com.softwaremill.macwire._
import play.api.LoggerConfigurator
import play.api.mvc.EssentialFilter
import play.filters.cors.CORSComponents

import scala.concurrent.{ExecutionContext, Future}

class HellolagomLoader extends LagomApplicationLoader {

  override def load(context: LagomApplicationContext): LagomApplication =
    new HellolagomApplication(context) {
      override def serviceLocator: ServiceLocator = new ServiceLocator {
        lazy val devModeServiceLocatorUrl: URI = URI.create(configuration.underlying.getString("lagom.service-locator.url"))

        override def doWithService[T](name: String, serviceCall: Call[_, _])(block: (URI) => Future[T])(implicit executionContext: ExecutionContext): Future[Option[T]] = {
          if (name == ServiceRegistry.ServiceName) {
            block(devModeServiceLocatorUrl).map(Some.apply)
          } else {
            Future.successful(None)
          }
        }

        override def locate(name: String, serviceCall: Call[_, _]): Future[Option[URI]] = {
          if (name == ServiceRegistry.ServiceName) {
            Future.successful(Some(devModeServiceLocatorUrl))
          } else if (name == "cas_native") {
            Future.successful(Some(URI.create(configuration.underlying.getString("lagom.services.cas_native"))))
          } else {
            Future.successful(None)
          }
        }
      }
      //override def serviceLocator: ServiceLocator = NoServiceLocator
    }

  override def loadDevMode(context: LagomApplicationContext): LagomApplication = {
//    // debug log setting
//    val environment = context.playContext.environment
//    LoggerConfigurator(environment.classLoader).foreach {
//      _.configure(environment)
//    }
//
//    new HellolagomApplication(context) {
//      override def serviceLocator: ServiceLocator = new ServiceLocator {
//        lazy val devModeServiceLocatorUrl: URI = URI.create(configuration.underlying.getString("lagom.service-locator.url"))
//
//        override def doWithService[T](name: String, serviceCall: Call[_, _])(block: (URI) => Future[T])(implicit executionContext: ExecutionContext): Future[Option[T]] = {
//          if (name == ServiceRegistry.ServiceName) {
//            block(devModeServiceLocatorUrl).map(Some.apply)
//          } else {
//            Future.successful(None)
//          }
//        }
//
//        override def locate(name: String, serviceCall: Call[_, _]): Future[Option[URI]] = {
//          if (name == ServiceRegistry.ServiceName) {
//            Future.successful(Some(devModeServiceLocatorUrl))
//          } else if (name == "cas_native") {
//            Future.successful(Some(URI.create(configuration.underlying.getString("lagom.services.cas_native"))))
//          } else {
//            Future.successful(None)
//          }
//        }
//      }
//    }
    new HellolagomApplication(context) with LagomDevModeComponents
  }

  override def describeService = Some(readDescriptor[HellolagomService])
}

abstract class HellolagomApplication(context: LagomApplicationContext)
  extends LagomApplication(context)
    with CassandraPersistenceComponents
    with LagomKafkaComponents
    with AhcWSComponents
    with CORSComponents {

  // Bind the service that this server provides
  override lazy val lagomServer = serverFor[HellolagomService](wire[HellolagomServiceImpl])

  // Register the JSON serializer registry
  override lazy val jsonSerializerRegistry = HellolagomSerializerRegistry

  // Register the hello-lagom persistent entity
  persistentEntityRegistry.register(wire[HellolagomEntity])

  override lazy val httpFilters: Seq[EssentialFilter] = Seq(corsFilter)
}
