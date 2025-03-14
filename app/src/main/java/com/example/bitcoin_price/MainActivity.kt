package com.example.bitcoin_price

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.ImageView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        Handler (Looper.getMainLooper()).postDelayed({
            val intent = Intent(this, Homescreen::class.java)
            startActivity(intent)
            finish()
        }, 3000)

        val splashLogo: ImageView = findViewById(R.id.logo_bitcoin)

        val currentNightMode = resources.configuration.uiMode and
                android.content.res.Configuration.UI_MODE_NIGHT_MASK

        when (currentNightMode) {
            android.content.res.Configuration.UI_MODE_NIGHT_YES -> {
                splashLogo.setImageResource(R.drawable.logo_darkmode)
            }
            android.content.res.Configuration.UI_MODE_NIGHT_NO -> {
                splashLogo.setImageResource(R.drawable.logo_lightmode)
            }

        }
    }
}
