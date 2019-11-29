package com.knowledgebase

import com.knowledgebase.services.KnowledgeBaseDomainService
import com.twitter.finagle.Thrift
import com.twitter.util.Await

object Main extends App {
  private val port = "8080"

  val service = new KnowledgeBaseDomainService

  val server = Thrift.server.serveIface(s":$port", service)

  Await ready server
}