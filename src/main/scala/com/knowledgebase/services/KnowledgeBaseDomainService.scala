package com.knowledgebase.services

import java.sql.Timestamp

import com.knowledgebase.config.ComponentProvider
import com.knowledgebase.models.{InfoInterestType, Interest, InterestType, StockInterestType}
import com.knowledgebase.thrift.{
  AddInterestsRequest,
  GetInterestsRequest,
  GetInterestsResponse,
  InfoResource,
  StockResource,
  KnowledgeBaseService,
  Resource,
  SimpleResponse,
  Interest => ThriftInterest,
  InterestType => ThriftInterestType
}
import com.twitter.util.Future

class KnowledgeBaseDomainService extends KnowledgeBaseService[Future] {

  val context = new ComponentProvider

  override def addInterests(request: AddInterestsRequest): Future[SimpleResponse] = {
    context.knowledgeBaseDao.addInterestsByUserId(
      request.userId,
      request.interests
        .map(interest => Interest(
          name = interest.name,
          interestType = InterestType fromName interest.interestType.name
        ))
    )
      .map(_ => SimpleResponse(isSuccess = true))
  } handle {
    case t: Throwable =>
      println("Error in addInterests(), error = " + t.getMessage)
      SimpleResponse(
        isSuccess = false,
        errorMessage = Option(t.getMessage)
      )
  }

  override def getInterests(request: GetInterestsRequest): Future[GetInterestsResponse] =
    context.knowledgeBaseDao.getInterestsByUserId(request.userId) flatMap { interests =>
        val interestsWithResources = Future collect interests.map { interest =>
          interest.interestType match {
            case StockInterestType =>
              context.stockInfoHttpClient.getStockData(interest.name)
                .map(stockResource => ThriftInterest(
                  name = interest.name,
                  interestType = ThriftInterestType.Stock,
                  resources = Seq(
                    Resource.StockResource(
                      StockResource(
                        currentValue = stockResource.currentValue,
                        timestamp = Timestamp.valueOf(stockResource.time).getTime.toString
                      )
                    )
                  )
                ))
            case InfoInterestType =>
              Future value ThriftInterest(
                name = interest.name,
                interestType = ThriftInterestType.Info,
                Seq(
                  Resource.InfoResource(
                    InfoResource("Info about " + interest.name)
                  )
                )
              )
          }
        }

      interestsWithResources.map(interests =>
        GetInterestsResponse(
        isSuccess = true,
        errorMessage = None,
        interests = Option(interests)
        )
      )
    }
}
