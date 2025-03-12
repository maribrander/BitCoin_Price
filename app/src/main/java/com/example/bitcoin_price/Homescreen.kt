package com.example.bitcoin_price

import android.annotation.SuppressLint
import android.health.connect.datatypes.units.Percentage
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.bitcoin_price.data.MarketPriceValue
import com.example.bitcoin_price.data.MarketPriceValueEntity
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
    private val viewModel: BtcViewModel by viewModels{ BtcViewModel.factory(applicationContext)}

    private lateinit var tvOpen: TextView
    private lateinit var tvHigh: TextView
    private lateinit var tvAverage: TextView
    private lateinit var tvClose: TextView
    private lateinit var tvLow: TextView
    private lateinit var tvChange: TextView
    private lateinit var tvPercentage: TextView

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
        tvOpen = findViewById<TextView>(R.id.tv_open)
        tvHigh = findViewById<TextView>(R.id.tv_high)
        tvAverage = findViewById<TextView>(R.id.tv_average)
        tvClose = findViewById<TextView>(R.id.tv_close)
        tvLow = findViewById<TextView>(R.id.tv_low)
        tvChange = findViewById<TextView>(R.id.tv_change)
        tvPercentage = findViewById<TextView>(R.id.tv_percentage)

        //val aboutChartDescription = findViewById<TextView>(R.id.tv_aboutchart_description)



        viewModel.errorMessage.observe(this) { error ->
            error?.let {
                aboutChart.text = it
            }
        }




        // Faz a requisição à API
        viewModel.fetchMarketPrice()


       viewModel.localMarketPrice.observe(this) { localData ->
            if (!localData.isNullOrEmpty()) {
                bitcoinPrice.text = localData.last().price.toString()
                val stats = calculateStats(localData)
                updateScreen(stats)
                updateChart(localData)
            } else {
                Log.d("RoomDatabase", "Nenhum dado local encontrado")
            }
       }

        viewModel.marketPrice.observe(this) { apiData ->
            if (!apiData.isNullOrEmpty()) {
                val stats = calculateStats(apiData)
                updateScreen(stats)
                updateChart(apiData)
            }
        }

    }

    // Configurações inicias do Gráfico
    private fun setupChart() {
        chart.apply {
            description.isEnabled = false
            xAxis.position = XAxis.XAxisPosition.BOTTOM
            axisRight.isEnabled = false
            axisLeft.setDrawGridLines(false)
            xAxis.setDrawGridLines(false)

            //Configuração do marcador personalizado

            val markerView = CustomMarkerView(context)

            markerView.chartView = this
            marker = markerView
        }
    }

    // Atualiza o gráfico com novos dados recebidos da API
    private fun updateChart(values: List<MarketPriceValueEntity>) {
        chartEntries.clear()
        val sortedValues = values.sortedBy { it.timestamp }
        sortedValues.forEach { data ->
            chartEntries.add(Entry(data.timestamp.toFloat(), data.price.toFloat()))
        }

        // Conjunto de dados para o gráfico, como cor da linha, tamanho.
        val dataSet = LineDataSet(chartEntries, "Preço do Bitcoin").apply {
            color = ColorTemplate.COLORFUL_COLORS[0]
            valueTextSize = 16f
            setDrawCircles(true)
            setDrawValues(false)
            setDrawFilled(true)

        }

        //atualiza com novos dados e redesenha.
        chart.data = LineData(dataSet)
        chart.invalidate()


    }

    private fun calculateStats(values: List<MarketPriceValueEntity>): Map<String, Any> {
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
        val average = sortedValues.map { it.price}.average()
        val change = ((close - open) / open) * 100

        return mapOf(
            "Open" to formatToUsd(open),
            "High" to formatToUsd(high),
            "Average" to formatToUsd(average),
            "Close" to formatToUsd(close),
            "Low" to formatToUsd(low),
            "Change" to "$$change%",
            "Percentage" to "%.2f%%".format(change)
        )

    }

    private fun updateScreen(stats: Map<String, Any>){
        tvOpen.text = stats["Open"].toString()
        tvHigh.text = stats["High"].toString()
        tvAverage.text = stats["Average"].toString()
        tvClose.text = stats["Close"].toString()
        tvLow.text = stats["Low"].toString()
        tvChange.text = stats["Change"].toString()
        tvPercentage.text = stats["Percentage"].toString()

    }




    fun formatToUsd(value: Comparable<*>): String {
        val format = NumberFormat.getCurrencyInstance(Locale.US)
        return format.format(value)
    }
}