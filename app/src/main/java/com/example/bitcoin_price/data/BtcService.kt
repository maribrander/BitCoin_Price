package com.example.bitcoin_price.data

import retrofit2.Call
import retrofit2.http.GET

// Responsável pelo endpoint da API, ou seja os dados que serão requisitados.

interface BtcService {
    @GET("/charts/market-price?timespan=4weeks&format=json")
     fun getMarketPrice(): Call<MarketPriceResponse>

}