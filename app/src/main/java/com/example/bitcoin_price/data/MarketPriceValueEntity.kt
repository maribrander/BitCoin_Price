package com.example.bitcoin_price.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "market_price_values")
data class MarketPriceValueEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val timestamp: Long,
    val price: Double
)


