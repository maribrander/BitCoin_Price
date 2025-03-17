package com.example.bitcoin_price.viewmodel

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import com.example.bitcoin_price.R
import com.example.bitcoin_price.data.local.BtcDataBase
import com.example.bitcoin_price.data.local.MarketPriceValueEntity
import com.example.bitcoin_price.data.repository.BtcRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.NumberFormat
import java.util.Locale
import kotlin.collections.sortedBy

class BtcViewModel(private val repository: BtcRepository) : ViewModel() {

    private val _marketPrice = MutableLiveData<List<MarketPriceValueEntity>?>()
    val marketPrice: LiveData<List<MarketPriceValueEntity>?> get() = _marketPrice

    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> get() = _errorMessage

    val localMarketPrice: LiveData<List<MarketPriceValueEntity>> = repository.localMarketPrice

    private val _filteredData = MutableLiveData<List<MarketPriceValueEntity>>()
    val filteredData: LiveData<List<MarketPriceValueEntity>> get() = _filteredData


    fun fetchMarketPrice() {
        viewModelScope.launch(Dispatchers.IO) {
            val result = repository.fetchMarketPrice()
            withContext(Dispatchers.Main) {
                result.onSuccess {
                    _marketPrice.value = it
                }.onFailure {
                    _errorMessage.value = it.localizedMessage
                }
            }
        }
    }

    fun filterData(days: Int) {
        val now = System.currentTimeMillis() / 1000 //
        val startTime = now - ((days + 1) * 86400)

        val filtered = localMarketPrice.value?.filter {
            it.timestamp in startTime..now
        }

        _marketPrice.postValue(filtered)
    }


    fun calculateStats(values: List<MarketPriceValueEntity>): Map<String, Any> {
        if (values.isEmpty()) {
            return mapOf(
                "Open" to "-",
                "High" to "-",
                "Average" to "-",
                "Close" to "-",
                "Low" to "-",
                "Change" to "-"
            )
        }
        val sortedValues = values.sortedBy { it.timestamp }
        val open = sortedValues.first().price
        val close = sortedValues.last().price
        val high = sortedValues.maxOf { it.price }
        val low = sortedValues.minOf { it.price }
        val average = sortedValues.map { it.price }.average()
        val change = close - open
        val changePercentage = ((close - open) / open) * 100
        val arrow = if (change > 0) R.drawable.ic_arrow_up else R.drawable.ic_arrow_down

        return mapOf(
            "Open" to formatToUsd(open),
            "High" to formatToUsd(high),
            "Average" to formatToUsd(average),
            "Close" to formatToUsd(close),
            "Low" to formatToUsd(low),
            "Change" to formatToUsd(change),
            "ChangePercentage" to "%.2f%%".format(changePercentage),
            "Arrow" to arrow
        )

    }

    fun formatToUsd(value: Comparable<*>): String {
        val format = NumberFormat.getCurrencyInstance(Locale.US)
        return format.format(value)
    }


    class Factory(private val context: Context) : ViewModelProvider.Factory {

        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(
            modelClass: Class<T>,
            extras: CreationExtras
        ): T {
            val dataBase = BtcDataBase.Companion.getDatabase(context)
            val dao = dataBase.marketPriceDao()
            return BtcViewModel(repository = BtcRepository(marketPriceDao = dao)) as T
        }
    }


}