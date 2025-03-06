package com.example.bitcoin_price

import android.annotation.SuppressLint
import android.os.Bundle
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import java.text.NumberFormat
import java.util.Locale

class Homescreen : AppCompatActivity() {

    private val viewModel: BtcViewModel by viewModels()

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_homescreen)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val bitcoinPrice = findViewById<TextView>(R.id.tv_title_price)
        val aboutChart = findViewById<TextView>(R.id.tv_title_aboutchart)
        val aboutChartDescription = findViewById<TextView>(R.id.tv_aboutchart_description)

        viewModel.marketPrice.observe(this) { response ->
            response?.let {
                val latestPrice = it.values.lastOrNull()?.y ?: "Sem dados"
                bitcoinPrice.text = "BitCoin: ${formatToUsd(latestPrice)}"
                aboutChartDescription.text = it.description
            }
        }

        viewModel.errorMessage.observe(this) { error ->
            error?.let {
                aboutChart.text = it
            }
        }

        // Faz a requisição à API
        viewModel.fetchMarketPrice()


    }
}
fun formatToUsd(value: Comparable<*>): String{
    val format = NumberFormat.getCurrencyInstance(Locale.US)
    return format.format(value)
}