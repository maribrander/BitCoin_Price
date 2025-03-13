package com.example.bitcoin_price

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.Group
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
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
    private lateinit var imEmptyState: ImageView
    private lateinit var dataGroup: Group
    private lateinit var btRetry: Button
    private lateinit var tvEmptyMessage: TextView
    private lateinit var imArrowDown: ImageView
    private lateinit var imArrowUp: ImageView



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
        val btn1D = findViewById<TextView>(R.id.tv_one_day)
        val btn7D = findViewById<TextView>(R.id.tv_seven_days)
        val btn30D = findViewById<TextView>(R.id.tv_thirty_days)

        tvOpen = findViewById<TextView>(R.id.tv_open)
        tvHigh = findViewById<TextView>(R.id.tv_high)
        tvAverage = findViewById<TextView>(R.id.tv_average)
        tvClose = findViewById<TextView>(R.id.tv_close)
        tvLow = findViewById<TextView>(R.id.tv_low)
        tvChange = findViewById<TextView>(R.id.tv_change)
        tvPercentage = findViewById<TextView>(R.id.tv_percentage)
        imEmptyState = findViewById<ImageView>(R.id.im_empty_State)
        dataGroup = findViewById<Group>(R.id.data_group)
        btRetry = findViewById<Button>(R.id.bt_retry)
        tvEmptyMessage = findViewById<TextView>(R.id.tv_empty_message)
        imArrowDown = findViewById<ImageView>(R.id.im_arrow_down)
        imArrowUp = findViewById<ImageView>(R.id.im_arrow_up)









        // Faz a requisição à API
        viewModel.fetchMarketPrice()

       // Busca os dados do banco de dados pra exibir na ui se houver dados.
       viewModel.localMarketPrice.observe(this) { localData ->
            if (!localData.isNullOrEmpty()) {
                val bitcoinPriceConverter = localData.last().price
                bitcoinPrice.text = formatToUsd(bitcoinPriceConverter)
                val stats = calculateStats(localData)
                updateScreen(stats)
                updateChart(localData)
                imEmptyState.visibility = View.GONE
                dataGroup.visibility = View.VISIBLE
                tvEmptyMessage.visibility = View.GONE
                btRetry.visibility = View.GONE

            } else {
                imEmptyState.visibility = View.VISIBLE
                dataGroup.visibility = View.GONE
                btRetry.visibility = View.VISIBLE
                btRetry.setOnClickListener {
                    viewModel.fetchMarketPrice()
                }
                tvEmptyMessage.visibility = View.VISIBLE
            }
       }

        // Atualiza a UI com os dados da API
        viewModel.marketPrice.observe(this) { apiData ->
            if (!apiData.isNullOrEmpty()) {
                val stats = calculateStats(apiData)
                updateScreen(stats)
                updateChart(apiData)

            }
        }

        viewModel.errorMessage.observe(this) { error ->
            error?.let {
                val builder : AlertDialog.Builder = AlertDialog.Builder(this)
                builder.setTitle(" Ops! Something went wrong")
                builder.setMessage("We could not establish a connection with our server." +
                        " If you want to see outdated data, you can cancel or try the connection again! ")
                builder.setIcon(R.drawable.ic_wifi)
                builder.setPositiveButton("Try Again") { dialog, _ ->
                    viewModel.fetchMarketPrice()
                }
                builder.setNegativeButton("Cancel") { dialog, _ ->
                    dialog.dismiss()
                }
                val dialog : AlertDialog = builder.create()
                dialog.show()
            }
        }

        viewModel.filteredData.observe(this){ data ->
            if(!data.isNullOrEmpty()){
                updateChart(data)
                updatePriceDetails(data)
            } else {
                Toast.makeText(this, "Sem dados disponíveis", Toast.LENGTH_SHORT).show()
            }

        }

        btn1D.setOnClickListener { onFilterSelected(1) }
        btn7D.setOnClickListener { onFilterSelected(7) }
        btn30D.setOnClickListener { onFilterSelected(30) }
    }

    fun onFilterSelected(days: Int) {
        viewModel.filterData(days)
    }

    // Configurações inicias do Gráfico
    private fun setupChart() {
        chart.apply {
            description.isEnabled = false
            xAxis.position = XAxis.XAxisPosition.BOTTOM
            axisRight.isEnabled = false
            axisLeft.setDrawGridLines(false)
            xAxis.setDrawGridLines(false)
            xAxis.setDrawLabels(false)

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
        val dataSet = LineDataSet(chartEntries, "Bitcoin Price").apply {
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
        val arrow = if (change > 0) R.drawable.ic_arrow_up else R.drawable.ic_arrow_down

        return mapOf(
            "Open" to formatToUsd(open),
            "High" to formatToUsd(high),
            "Average" to formatToUsd(average),
            "Close" to formatToUsd(close),
            "Low" to formatToUsd(low),
            "Change" to "$$change%",
            "Percentage" to "%.2f%%".format(change),
            "Arrow" to arrow
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
        imArrowDown.setImageResource(stats["Arrow"] as Int)
        imArrowUp.setImageResource(stats["Arrow"] as Int)

    }

    private fun updatePriceDetails(data: List<MarketPriceValueEntity>){
        if(data.isEmpty()) return

        val openingPrice = data.first().price
        val closingPrice = data.last().price
        val highestPrice = data.maxOf { it.price }
        val lowestPrice = data.minOf { it.price }
        val averagePrice = data.map {it.price}.average()

        val changePercent = if (openingPrice != 0.0){
            ((closingPrice - openingPrice) / openingPrice) * 100
        } else {
            0.0
        }

        val openingPriceTextView = findViewById<TextView>(R.id.tv_open)
        val closingPriceTextView = findViewById<TextView>(R.id.tv_close)
        val highestPriceTextView = findViewById<TextView>(R.id.tv_high)
        val lowestPriceTextView = findViewById<TextView>(R.id.tv_low)
        val averagePriceTextView = findViewById<TextView>(R.id.tv_average)
        val changePercentTextView  = findViewById<TextView>(R.id.tv_change)

        openingPriceTextView.text = " ${formatToUsd(openingPrice)}"
        closingPriceTextView.text = " ${formatToUsd(closingPrice)}"
        highestPriceTextView.text = " ${formatToUsd(highestPrice)}"
        lowestPriceTextView.text = "  ${formatToUsd(lowestPrice)}"
        averagePriceTextView.text = "${formatToUsd(averagePrice)}"
        changePercentTextView.text = "${formatToUsd(changePercent)}" 

    }




    fun formatToUsd(value: Comparable<*>): String {
        val format = NumberFormat.getCurrencyInstance(Locale.US)
        return format.format(value)
    }
}