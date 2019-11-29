package com.knowledgebase.dao

import com.knowledgebase.models.{Interest, InterestType}
import com.knowledgebase.sql.KnowledgeBaseSql.{INTEREST, INTERESTS_TABLE, INTEREST_COLS, INTEREST_TYPE, USER_ID}
import com.knowledgebase.thrift.UserId
import com.twitter.finagle.Mysql
import com.twitter.finagle.mysql.{Client, Row}
import com.twitter.util.Future

trait KnowledgeBaseDaoComponent {
  def knowledgeBaseDao: KnowledgeBaseDao

  class KnowledgeBaseDao(database: String, host: String, username: String = "root", password: String = "password", port: Int = 3306) {
    private val client: Client = Mysql.client
      .withCredentials(username, password)
      .withDatabase(database)
      .newRichClient(s"$host:$port")

    private def getInterestsByUserIdSql(userId: Long): String =
      s"select ${INTEREST_COLS.mkString(",")} from $INTERESTS_TABLE where user_id = $userId"

    private val interestRowParser: Row => Interest = r => {
      for {
        interestName <- r.getString(INTEREST)
        interestType <- r.getString(INTEREST_TYPE)
      } yield Interest(
        name = interestName,
        interestType = InterestType.fromName(interestType)
      )
    }.getOrElse(throw new Exception("Can't parse interest"))

    def getInterestsByUserId(userId: UserId): Future[Seq[Interest]] =
      client.select(getInterestsByUserIdSql(userId.value))(interestRowParser)

    private def addInterestsByUserIdSql(userId: Long, interests: Seq[Interest]): String =
      s"insert into $INTERESTS_TABLE (${(INTEREST_COLS ++ Seq(USER_ID)).mkString(",")}) values ${interests.map(i => s"('${i.name}','${i.interestType}',$userId)").mkString(",")}"

    def addInterestsByUserId(userId: UserId, interests: Seq[Interest]): Future[Unit] =
      client.modify(addInterestsByUserIdSql(userId.value, interests)) map {
        status => println("number of affected rows = " + status.affectedRows)
      }
  }
}
