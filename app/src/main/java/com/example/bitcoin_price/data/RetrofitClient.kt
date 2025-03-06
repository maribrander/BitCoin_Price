package com.example.bitcoin_price.data

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

// Responsável pela comunicaçao com a API do Blockchain. Converte o JSON para um objeto Kotlin.

object RetrofitClient {
    private const val BASE_URL = "https://api.blockchain.info"

    val retrofitInstance: BtcService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(BtcService::class.java)
    }


}