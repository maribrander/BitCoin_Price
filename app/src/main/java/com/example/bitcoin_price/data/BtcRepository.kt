package com.example.bitcoin_price.data

import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class BtcRepository {

    fun fetchMarketPrice(callback: (MarketPriceResponse?, String?)-> Unit) {
        val call = RetrofitClient.retrofitInstance.getMarketPrice()

        call.enqueue(object : Callback<MarketPriceResponse> {
            override fun onResponse(
                call: Call<MarketPriceResponse>,
                response: Response<MarketPriceResponse>
            ) {
                if (response.isSuccessful) {
                    callback(response.body(), null)
                } else {
                    callback (null, "Erro: ${response.code()}")
                }
            }

            override fun onFailure(p0: Call<MarketPriceResponse>, p1: Throwable) {
                callback(null, "Erro na requisição: ${p1.message}")
            }

        })

    }
}