package com.example.bitcoin_price.data

import androidx.lifecycle.LiveData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class BtcRepository(private val marketPriceDao: MarketPriceDao) {

    fun fetchMarketPrice(callback: (List<MarketPriceValueEntity>?, String?) -> Unit) {
        val call = RetrofitClient.retrofitInstance.getMarketPrice()

        call.enqueue(object : Callback<MarketPriceResponse> {
            override fun onResponse(
                call: Call<MarketPriceResponse>,
                response: Response<MarketPriceResponse>
            ) {
                if (response.isSuccessful) {
                    val apiResponse = response.body()

                    if (apiResponse != null) {
                        // Converter MarketPriceValue para MarketPriceValueEntity
                        val entityList = apiResponse.values.map {
                            MarketPriceValueEntity(timestamp = it.x, price = it.y)
                        }

                        // Salvar no Room Database
                        CoroutineScope(Dispatchers.IO).launch {
                            marketPriceDao.insertAll(entityList)
                        }

                        callback(entityList, null)
                    } else {
                        callback(null, "Resposta da API vazia")
                    }
                } else {
                    callback(null, "Erro: ${response.code()}")
                }
            }

            override fun onFailure(call: Call<MarketPriceResponse>, t: Throwable) {
                callback(null, "Erro na requisição: ${t.message}")
            }
        })
    }

     fun getLocaldMarketPrice(): LiveData<List<MarketPriceValueEntity>> {
         return marketPriceDao.getAll()
     }

}