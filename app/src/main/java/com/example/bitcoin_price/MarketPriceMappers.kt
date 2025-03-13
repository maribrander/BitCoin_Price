package com.example.bitcoin_price

import com.example.bitcoin_price.data.MarketPriceValue
import com.example.bitcoin_price.data.MarketPriceValueEntity

fun MarketPriceValue.toEntity(): MarketPriceValueEntity {
    return MarketPriceValueEntity(
        timestamp = this.x,
        price = this.y
    )
}

