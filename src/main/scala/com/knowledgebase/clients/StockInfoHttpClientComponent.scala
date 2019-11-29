package com.knowledgebase.clients

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

import com.knowledgebase.models.{FIVE_MINUTES, StockInfoInterval, StockResource, StockTimeSeriesType, TIME_SERIES_INTRADAY}
import com.knowledgebase.utils.Defaults.{STOCK_HOST, STOCK_PORT}
import com.twitter.finagle.{Http, Service}
import com.twitter.finagle.http.{Method, Request, Response, Status}
import com.twitter.util.Future
import io.circe.{Decoder, parser}

trait StockInfoHttpClientComponent {
  def stockInfoHttpClient: StockInfoHttpClient

  class StockInfoHttpClient(apiKey: String) {
    private val host = STOCK_HOST
    private val client: Service[Request, Response] = Http.client.withTls(host).newService(s"$host:$STOCK_PORT")

    def stockResourceDecoder(interval: StockInfoInterval): Decoder[StockResource] = c => {
      val dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
      for {
        lastRefreshed <- c.downField("Meta Data")
          .get[String]("3. Last Refreshed")
        closingValue <- c.downField(s"Time Series (${StockInfoInterval toString interval})")
          .downField(lastRefreshed)
          .get[String]("4. close")
      } yield StockResource(closingValue.toDouble, LocalDateTime.parse(lastRefreshed, dateTimeFormatter))
    }

    def getStockData(symbol: String, seriesType: StockTimeSeriesType = TIME_SERIES_INTRADAY, interval: StockInfoInterval = FIVE_MINUTES): Future[StockResource] = {
      client(
        Request(Method.Get, s"/query?function=${StockTimeSeriesType toString seriesType}&symbol=$symbol&interval=${StockInfoInterval toString interval}&apikey=$apiKey")
      ) map { response =>
        if (response.status == Status.Ok) {
          parser.decode[StockResource](response.getContentString())(stockResourceDecoder(interval)) match {
            case Right(stockResource) =>
              stockResource
            case Left(error) => throw new Exception("failed parsing stock response, error = " + error.getMessage)
          }
        } else {
          throw new Exception(s"failed getting stock data, status = ${response.status.code}, error = ${response.getContentString()}")
        }
      }
    }
  }
}
