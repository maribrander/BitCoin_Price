package com.example.bitcoin_price.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase


@Database(entities = [MarketPriceValueEntity::class], version = 1)
abstract class BtcDataBase : RoomDatabase() {
    abstract fun marketPriceDao(): MarketPriceDao

    companion object {
        @Volatile
        private var INSTANCE: BtcDataBase? = null

        fun getDatabase(context: Context): BtcDataBase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    BtcDataBase::class.java,
                    "market_price_db"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}