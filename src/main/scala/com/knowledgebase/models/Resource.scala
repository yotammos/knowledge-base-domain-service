package com.knowledgebase.models

import java.time.LocalDateTime

abstract class Resource

case class StockResource(currentValue: Double, time: LocalDateTime) extends Resource
