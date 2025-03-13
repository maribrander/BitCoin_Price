package com.example.bitcoin_price


import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras
import com.example.bitcoin_price.data.BtcDataBase
import com.example.bitcoin_price.data.BtcRepository
import com.example.bitcoin_price.data.MarketPriceValueEntity


class BtcViewModel( private val repository: BtcRepository): ViewModel() {

    private val _marketPrice = MutableLiveData<List<MarketPriceValueEntity>?>()
    val marketPrice: MutableLiveData<List<MarketPriceValueEntity>?> get() = _marketPrice

    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: MutableLiveData<String?> get() = _errorMessage

    val localMarketPrice: LiveData<List<MarketPriceValueEntity>> = repository.getLocaldMarketPrice()

    fun fetchMarketPrice() {
        repository.fetchMarketPrice { response, error ->
            if (response != null) {
                _marketPrice.postValue(response)
            } else {

                _errorMessage.postValue(error)
            }
        }
    }


    class factory(private val context: Context): ViewModelProvider.Factory {

        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(
            modelClass: Class<T>,
            extras: CreationExtras
        ): T {
            val dataBase = BtcDataBase.getDatabase(context)
            val dao = dataBase.marketPriceDao()
            return BtcViewModel(repository = BtcRepository(marketPriceDao = dao ) )as T
        }
    }


}



