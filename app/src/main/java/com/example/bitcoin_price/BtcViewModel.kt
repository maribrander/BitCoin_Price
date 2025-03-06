package com.example.bitcoin_price


import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.bitcoin_price.data.BtcRepository
import com.example.bitcoin_price.data.MarketPriceResponse


class BtcViewModel: ViewModel() {

    private val repository = BtcRepository()

    private val _marketPrice = MutableLiveData<MarketPriceResponse?>()
    val marketPrice: MutableLiveData<MarketPriceResponse?> get() = _marketPrice

    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: MutableLiveData<String?> get() = _errorMessage

    fun fetchMarketPrice() {
        repository.fetchMarketPrice { response, error ->
            if (response != null) {
                _marketPrice.postValue(response)
            } else {
                _errorMessage.postValue(error)
            }
        }

    }

}