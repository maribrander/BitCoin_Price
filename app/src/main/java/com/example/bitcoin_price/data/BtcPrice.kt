package com.example.bitcoin_price.data


data class MarketPriceResponse (
    val status:String,
    val name:String,
    val unit:String,
    val period:String,
    val description:String,
    val values:List<MarketPriceValue>

)

data class MarketPriceValue(
    val x: Long,
    val y: Double
)