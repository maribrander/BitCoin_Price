package com.example.bitcoin_price.data

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface MarketPriceDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(prices : List<MarketPriceValueEntity>)

    @Query("SELECT * FROM market_price_values")
     fun getAll(): LiveData<List<MarketPriceValueEntity>>

}