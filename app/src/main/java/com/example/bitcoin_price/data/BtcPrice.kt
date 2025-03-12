package com.example.bitcoin_price.data




data class MarketPriceResponse (
    val description:String,
    val values:List<MarketPriceValue>
)

data class MarketPriceValue(
    val x: Long,
    val y: Double
)