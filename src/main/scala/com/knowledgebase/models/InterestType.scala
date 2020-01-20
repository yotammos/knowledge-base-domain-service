package com.knowledgebase.models

abstract class InterestType

case object StockInterestType extends InterestType
case object PollInterestType extends InterestType
case object InfoInterestType extends InterestType

object InterestType {
  private val STOCK_INTEREST_TYPE = "StockInterestType"
  private val POLL_INTEREST_TYPE = "PollInterestType"
  private val INFO_INTEREST_TYPE = "InfoInterestType"

  def fromName(name: String): InterestType = name match {
    case STOCK_INTEREST_TYPE | "Stock" => StockInterestType
    case POLL_INTEREST_TYPE | "Poll" => PollInterestType
    case INFO_INTEREST_TYPE | "Info" | _ => InfoInterestType
  }

  def toString(interestType: InterestType): String = interestType match {
    case StockInterestType => STOCK_INTEREST_TYPE
    case PollInterestType => POLL_INTEREST_TYPE
    case InfoInterestType | _ => INFO_INTEREST_TYPE
  }
}