package com.knowledgebase.models

abstract class InterestType

case object StockInterestType extends InterestType
case object InfoInterestType extends InterestType

object InterestType {
  private val STOCK_INTEREST_TYPE = "StockInterestType"
  private val INFO_INTEREST_TYPE = "InfoInterestType"

  def fromName(name: String): InterestType = name match {
    case STOCK_INTEREST_TYPE => StockInterestType
    case INFO_INTEREST_TYPE | _ => InfoInterestType
  }

  def toString(interestType: InterestType): String = interestType match {
    case StockInterestType => STOCK_INTEREST_TYPE
    case InfoInterestType | _ => INFO_INTEREST_TYPE
  }
}