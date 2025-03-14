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

    private val _filteredData = MutableLiveData<List<MarketPriceValueEntity>>()
    val filteredData: LiveData<List<MarketPriceValueEntity>> get() = _filteredData

    fun fetchMarketPrice() {
        repository.fetchMarketPrice { response, error ->
            if (response != null) {
                _marketPrice.postValue(response)
            } else {

                _errorMessage.postValue(error)
            }
        }
    }

    fun filterData (days: Int){
        val allData = _marketPrice.value ?: return
        val currentTime = System.currentTimeMillis() / 1000
        val timeLimit = currentTime - (days * 24 * 60 * 60)

        val filteredData = allData.filter {it.timestamp >= timeLimit}
        _filteredData.postValue(filteredData)
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



