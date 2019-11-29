package com.knowledgebase.config

import com.knowledgebase.clients.StockInfoHttpClientComponent
import com.knowledgebase.dao.KnowledgeBaseDaoComponent
import com.knowledgebase.utils.Defaults.{ DB_PASSWORD, DB_PORT, DB_USERNAME, STOCK_API_KEY }

class ComponentProvider
  extends KnowledgeBaseDaoComponent
  with StockInfoHttpClientComponent {

  lazy val knowledgeBaseDao: KnowledgeBaseDao = new KnowledgeBaseDao(
    database = "knowledge_base",
    host = "localhost",
    username = DB_USERNAME,
    password = DB_PASSWORD,
    port = DB_PORT
  )

  lazy val stockInfoHttpClient: StockInfoHttpClient = new StockInfoHttpClient(STOCK_API_KEY)
}
