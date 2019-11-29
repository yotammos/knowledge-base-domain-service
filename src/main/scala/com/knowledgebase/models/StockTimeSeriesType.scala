package com.knowledgebase.models

abstract class StockTimeSeriesType

case object TIME_SERIES_INTRADAY extends StockTimeSeriesType
case object DAILY extends StockTimeSeriesType

object StockTimeSeriesType {
  private val TIME_SERIES_INTRADAY_NAME = "TIME_SERIES_INTRADAY"
  private val DAILY_NAME = "DAILY"

  def toString(stockTimeSeries: StockTimeSeriesType): String =
    stockTimeSeries match {
      case TIME_SERIES_INTRADAY => TIME_SERIES_INTRADAY_NAME
      case DAILY | _ => DAILY_NAME
    }

  def fromName(name: String): StockTimeSeriesType =
    name match {
      case TIME_SERIES_INTRADAY_NAME => TIME_SERIES_INTRADAY
      case DAILY_NAME => DAILY
    }
}
