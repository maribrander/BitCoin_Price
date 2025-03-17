package com.example.bitcoin_price.data.repository

import androidx.lifecycle.LiveData
import com.example.bitcoin_price.data.local.MarketPriceDao
import com.example.bitcoin_price.data.local.MarketPriceValueEntity
import com.example.bitcoin_price.data.remote.RetrofitClient

class BtcRepository(private val marketPriceDao: MarketPriceDao) {

    val localMarketPrice: LiveData<List<MarketPriceValueEntity>> = marketPriceDao.getAll()

    suspend fun fetchMarketPrice(): Result<List<MarketPriceValueEntity>> {
        return try {
            val response = RetrofitClient.retrofitInstance.getMarketPrice().execute()

            if (response.isSuccessful) {
                val apiResponse = response.body()
                if (apiResponse != null) {
                    // Converter MarketPriceValue para MarketPriceValueEntity
                    val entityList = apiResponse.values.map {
                        MarketPriceValueEntity(timestamp = it.x, price = it.y)
                    }

                    // Salvar no Room Database
                    marketPriceDao.insertAll(entityList)

                    Result.success(entityList)
                } else {
                    Result.failure(Exception("Resposta da API vazia"))
                }
            } else {
                Result.failure(Exception("Erro na requisição: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}