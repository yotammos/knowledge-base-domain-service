package com.knowledgebase.services

import java.sql.Timestamp

import com.knowledgebase.config.ComponentProvider
import com.knowledgebase.models.{InfoInterestType, Interest, InterestType, PollInterestType, StockInterestType}
import com.knowledgebase.thrift.{AddInterestsRequest, GetInterestInfoResponse, GetInterestsResponse, InfoResource, InterestInfo, KnowledgeBaseService, RemoveInterestsRequest, Resource, SimpleRequest, SimpleResponse, StockResource, Interest => ThriftInterest, InterestType => ThriftInterestType}
import com.twitter.util.Future

class KnowledgeBaseDomainService extends KnowledgeBaseService[Future] {

  val context = new ComponentProvider

  override def addInterests(request: AddInterestsRequest): Future[SimpleResponse] = {
    println("adding interests: request = " + request)
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

  override def getInterestInfo(request: SimpleRequest): Future[GetInterestInfoResponse] = {
    println(s"getting interest info for user, userId = ${request.userId.value}")
    context.knowledgeBaseDao.getInterestsByUserId(request.userId)
      .map(info => GetInterestInfoResponse(
        isSuccess = true,
        None,
        interestInfos = Option(info.map(interest => InterestInfo(
            interest.name,
            interest.interestType match {
              case StockInterestType => ThriftInterestType.Stock
              case PollInterestType => ThriftInterestType.Poll
              case InfoInterestType | _ => ThriftInterestType.Info
            }
          )
        ))
      ))
  }

  override def getInterests(request: SimpleRequest): Future[GetInterestsResponse] = {
    println(s"getting interests for user, userId = ${request.userId.value}")
    context.knowledgeBaseDao.getInterestsByUserId(request.userId) flatMap { interests =>
        val interestsF = Future collect interests.map { interest =>
          println(s"fetching interest = $interest")
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
      interestsF.map(interests =>
        GetInterestsResponse(
        isSuccess = true,
        errorMessage = None,
        interests = Option(interests)
        )
      )
    }
  }

  override def removeInterests(request: RemoveInterestsRequest): Future[SimpleResponse] = {
    context.knowledgeBaseDao.removeInterestsByIdAndName(
      request.userId,
      request.interestNames
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
}
