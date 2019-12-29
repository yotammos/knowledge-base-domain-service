package com.knowledgebase.services

import java.sql.Timestamp

import com.knowledgebase.config.ComponentProvider
import com.knowledgebase.models.{InfoInterestType, Interest, InterestType, PollInterestType, StockInterestType}
import com.knowledgebase.thrift.{AddInterestsRequest, GetInterestsRequest, GetInterestsResponse, InfoResource, KnowledgeBaseService, Resource, SimpleResponse, StockResource, Interest => ThriftInterest, InterestType => ThriftInterestType}
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

  override def getInterests(request: GetInterestsRequest): Future[GetInterestsResponse] = {
    println(s"getting interests for user, userId = ${request.userId.value}")
    context.knowledgeBaseDao.getInterestsByUserId(request.userId) flatMap { interests =>
        val interestsWithResources = Future collect interests.map { interest =>
          print(s"fetching interest = $interest")
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
            case PollInterestType =>
              context.fiveThirtyEightClient.fetchPresidentPrimaryPollingData()
                .map(resources => ThriftInterest(
                  name = interest.name,
                  interestType = ThriftInterestType.Poll,
                  resources = resources.map(resource =>
                    Resource PollResource resource
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

      println(s"returning interests = $interests")
      interestsWithResources.map(interests =>
        GetInterestsResponse(
        isSuccess = true,
        errorMessage = None,
        interests = Option(interests)
        )
      )
    }
  }
}
