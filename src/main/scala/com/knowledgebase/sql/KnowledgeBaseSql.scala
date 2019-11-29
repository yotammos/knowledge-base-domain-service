package com.knowledgebase.sql

object KnowledgeBaseSql {
  val INTERESTS_TABLE = "interests"

  val INTEREST = "interest"
  val INTEREST_TYPE = "interest_type"
  val USER_ID = "user_id"

  val INTEREST_COLS: Seq[String] = Seq[String](
    INTEREST,
    INTEREST_TYPE
  )
}
