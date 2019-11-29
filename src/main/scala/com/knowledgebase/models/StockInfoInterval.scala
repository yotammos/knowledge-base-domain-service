package com.knowledgebase.models

abstract class StockInfoInterval

case object FIVE_MINUTES extends StockInfoInterval
case object SIXTY_MINUTES extends StockInfoInterval

object StockInfoInterval {
  private val FIVE_MINUTES_NAME = "5min"
  private val SIXTY_MINUTES_NAME = "60min"

  def toString(stockInfoInterval: StockInfoInterval): String =
    stockInfoInterval match {
      case FIVE_MINUTES => FIVE_MINUTES_NAME
      case SIXTY_MINUTES => SIXTY_MINUTES_NAME
    }

  def fromName(name: String): StockInfoInterval =
    name match {
      case FIVE_MINUTES_NAME => FIVE_MINUTES
      case SIXTY_MINUTES_NAME => SIXTY_MINUTES
    }
}
