package com.example.bitcoin_price

import android.annotation.SuppressLint
import android.os.Bundle
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.bitcoin_price.data.MarketPriceValue
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.utils.ColorTemplate
import java.text.NumberFormat
import java.util.Locale

class Homescreen : AppCompatActivity() {

    private lateinit var chart: LineChart
    private val chartEntries = mutableListOf<Entry>()
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

        chart = findViewById(R.id.chart)
        setupChart()

        val bitcoinPrice = findViewById<TextView>(R.id.tv_title_price)
        val aboutChart = findViewById<TextView>(R.id.tv_title_aboutchart)
        val aboutChartDescription = findViewById<TextView>(R.id.tv_aboutchart_description)

        viewModel.marketPrice.observe(this) { response ->
            response?.let {
                val latestPrice = it.values.lastOrNull()?.y ?: "Sem dados"
                bitcoinPrice.text = "BitCoin: ${formatToUsd(latestPrice)}"
                aboutChartDescription.text = it.description
                updateChart(it.values)
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

    private fun setupChart() {
        chart.apply {
            description.isEnabled = false
            xAxis.position = XAxis.XAxisPosition.BOTTOM
            axisRight.isEnabled = false
            axisLeft.setDrawGridLines(false)
            xAxis.setDrawGridLines(false)
        }
    }

    private fun updateChart(values: List<MarketPriceValue>) {
        chartEntries.clear()
        values.forEachIndexed { index, data ->
            chartEntries.add(Entry(index.toFloat(), data.y.toFloat()))
        }

        val dataSet = LineDataSet(chartEntries, "Preço do Bitcoin").apply {
            color = ColorTemplate.COLORFUL_COLORS[0]
            valueTextSize = 12f
            setDrawCircles(false)
            setDrawValues(false)
        }

        chart.data = LineData(dataSet)
        chart.invalidate()


    }

    fun formatToUsd(value: Comparable<*>): String {
        val format = NumberFormat.getCurrencyInstance(Locale.US)
        return format.format(value)
    }
}