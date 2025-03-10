package com.example.bitcoin_price

import android.content.Context
import android.widget.TextView
import com.github.mikephil.charting.components.MarkerView
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.utils.MPPointF

class CustomMarkerView(context: Context): MarkerView(context,R.layout.marker_view) {

    private val tvContent: TextView = findViewById(R.id.tv_marker_value)

    override fun refreshContent(e: Entry?, highlight: Highlight?) {
        e?.let{
            tvContent.text = "$${it.y}"
        }
        super.refreshContent(e, highlight)
    }

    override fun getOffset(): MPPointF {
        return MPPointF(-(width/2).toFloat(), -height.toFloat())
    }

}