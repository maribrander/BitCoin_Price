package com.example.bitcoin_price.ui.splash


import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity

import com.example.bitcoin_price.R
import com.example.bitcoin_price.databinding.ActivitySplashScreenBinding
import com.example.bitcoin_price.ui.main.MainActivity

@SuppressLint("CustomSplashScreen")
class SplashScreen : AppCompatActivity() {

    private lateinit var binding: ActivitySplashScreenBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivitySplashScreenBinding.inflate(layoutInflater)
        setContentView(binding.root)
        Handler(Looper.getMainLooper()).postDelayed({
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }, 3000)


        val currentNightMode = resources.configuration.uiMode and
                android.content.res.Configuration.UI_MODE_NIGHT_MASK

        when (currentNightMode) {
            android.content.res.Configuration.UI_MODE_NIGHT_YES -> {
                binding.logoBitcoin.setImageResource(R.drawable.logo_darkmode)
            }

            android.content.res.Configuration.UI_MODE_NIGHT_NO -> {
                binding.logoBitcoin.setImageResource(R.drawable.logo_lightmode)
            }

        }
    }
}
