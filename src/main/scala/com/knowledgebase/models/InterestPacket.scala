package com.knowledgebase.models

abstract class InterestPacket(interest: Interest, resources: Seq[Resource])

case class InterestStockPacket(interest: Interest, resources: Seq[StockResource]) extends InterestPacket(interest, resources)