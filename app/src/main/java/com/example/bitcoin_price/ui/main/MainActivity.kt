package com.example.bitcoin_price.ui.main


import android.annotation.SuppressLint
import android.content.res.Configuration
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.bitcoin_price.viewmodel.BtcViewModel
import com.example.bitcoin_price.utils.CustomMarkerView
import com.example.bitcoin_price.R
import com.example.bitcoin_price.data.local.MarketPriceValueEntity
import com.example.bitcoin_price.databinding.ActivityMainBinding
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.ValueFormatter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import androidx.core.graphics.toColorInt


class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private val viewModel: BtcViewModel by viewModels { BtcViewModel.Factory(applicationContext) }

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        setupChart()

        // Faz a requisição à API
        viewModel.fetchMarketPrice()

        // Busca os dados do banco de dados pra exibir na ui se houver dados.
        viewModel.localMarketPrice.observe(this) { localData ->
            if (!localData.isNullOrEmpty()) {
                val bitcoinPriceConverter = localData.last().price
                binding.tvTitlePrice.text = viewModel.formatToUsd(bitcoinPriceConverter)
                val stats = viewModel.calculateStats(localData)
                updateScreen(stats)
                updateChart(localData)
                updateVisibility(true)

            } else {
                updateVisibility(false)

            }
        }

        // Atualiza a UI com os dados da API
        viewModel.marketPrice.observe(this) { apiData ->
            if (!apiData.isNullOrEmpty()) {
                val stats = viewModel.calculateStats(apiData)
                updateScreen(stats)
                updateChart(apiData)

            }
        }

        viewModel.errorMessage.observe(this) { error ->
            error?.let {
                val builder: AlertDialog.Builder = AlertDialog.Builder(this)
                builder.setTitle(R.string.alert_dialog_title)
                builder.setMessage(R.string.alert_dialog_message)
                builder.setIcon(R.drawable.ic_wifi)
                builder.setPositiveButton(R.string.alert_dialog_positive_button) { dialog, _ ->
                    viewModel.fetchMarketPrice()
                }
                builder.setNegativeButton(R.string.alert_dialog_negative_button) { dialog, _ ->
                    dialog.dismiss()
                }
                val dialog: AlertDialog = builder.create()
                dialog.show()
            }
        }

        viewModel.filteredData.observe(this) { data ->
            if (!data.isNullOrEmpty()) {
                updateChart(data)
                val stats = viewModel.calculateStats(data)
                updateScreen(stats)
            } else {
                Toast.makeText(this, "Sem dados disponíveis", Toast.LENGTH_SHORT).show()
            }

        }

        binding.radioGroup.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                R.id.btn1d -> {
                    onFilterSelected(1)
                }

                R.id.btn7d -> {
                    onFilterSelected(7)
                }

                R.id.btn30d -> {
                    onFilterSelected(30)
                }
            }
        }
    }

    fun onFilterSelected(days: Int) {
        viewModel.filterData(days)
    }

    // Configurações iniciais do Gráfico
    private fun setupChart() {
        binding.chart.apply {
            description.isEnabled = false
            xAxis.position = XAxis.XAxisPosition.BOTTOM
            axisRight.isEnabled = false
            axisLeft.setDrawGridLines(true)
            xAxis.setDrawGridLines(false)
            xAxis.setDrawLabels(true)

            xAxis.valueFormatter = object : ValueFormatter() {
                private val sdf = SimpleDateFormat("dd/MM", Locale.getDefault())

                override fun getFormattedValue(value: Float): String {
                    return sdf.format(Date(value.toLong() * 1000))
                }
            }

            // Define a quantidade de rótulos visíveis
            xAxis.setLabelCount(5, true)
            axisLeft.enableGridDashedLine(10f, 5f, 0f)
            binding.chart.axisLeft.apply {
                setDrawGridLines(true)
                enableGridDashedLine(10f, 5f, 0f)
                gridColor = Color.LTGRAY
                gridLineWidth = 1f
            }

            val isDarkMode = resources.configuration.uiMode and
                    Configuration.UI_MODE_NIGHT_MASK == Configuration.UI_MODE_NIGHT_YES
            val textColor = if (isDarkMode) Color.WHITE else Color.BLACK

            xAxis.textColor = textColor
            axisLeft.textColor = textColor
            axisRight.textColor = textColor
            legend.textColor = textColor


            //Configuração do marcador personalizado

            val markerView = CustomMarkerView(context)

            markerView.chartView = this
            marker = markerView
        }
    }

    // Atualiza o gráfico com novos dados recebidos da API
    private fun updateChart(values: List<MarketPriceValueEntity>) {
        val newEntries = values.sortedBy { it.timestamp }.map { data ->
            Entry(data.timestamp.toFloat(), data.price.toFloat())
        }

        // Conjunto de dados para o gráfico, como cor da linha, tamanho.
        val dataSet = LineDataSet(newEntries, "Bitcoin Price").apply {
            color = "#4682B4".toColorInt()
            valueTextSize = 16f
            setDrawCircles(false)
            setDrawValues(false)
            setDrawFilled(true)

            lineWidth = 3f
            mode = LineDataSet.Mode.HORIZONTAL_BEZIER
            cubicIntensity = 0.3f
            fillDrawable =
                ContextCompat.getDrawable(binding.chart.context, R.drawable.gradient_fill)
        }

        //atualiza com novos dados e redesenha.
        val data = LineData(dataSet)
        binding.chart.data = data
        binding.chart.invalidate()
    }


    private fun updateScreen(stats: Map<String, Any>) {
        binding.tvOpen.text = stats["Open"].toString()
        binding.tvHigh.text = stats["High"].toString()
        binding.tvAverage.text = stats["Average"].toString()
        binding.tvClose.text = stats["Close"].toString()
        binding.tvLow.text = stats["Low"].toString()
        binding.tvChange.text = stats["Change"].toString()
        binding.tvPercentage.text = stats["ChangePercentage"].toString()
        binding.imArrowDown.setImageResource(stats["Arrow"] as Int)
        binding.imArrowUp.setImageResource(stats["Arrow"] as Int)

    }

    private fun updateVisibility(isDataAvailable: Boolean) {
        if (isDataAvailable) {
            binding.imEmptyState.visibility = View.GONE
            binding.dataGroup.visibility = View.VISIBLE
            binding.tvEmptyMessage.visibility = View.GONE
            binding.btRetry.visibility = View.GONE
        } else {
            binding.imEmptyState.visibility = View.VISIBLE
            binding.dataGroup.visibility = View.GONE
            binding.btRetry.visibility = View.VISIBLE
            binding.tvEmptyMessage.visibility = View.VISIBLE
            binding.btRetry.setOnClickListener {
                viewModel.fetchMarketPrice()
            }
        }
    }


}