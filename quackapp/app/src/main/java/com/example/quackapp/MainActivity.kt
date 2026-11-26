package com.example.quackapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.lifecycleScope
import com.example.quackapp.Controller.LotteryDraw
import com.example.quackapp.View.QuackApp
import com.example.quackapp.ui.theme.QuackappTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        // Draw any lotteries whose registration period ended (US 02.05.01)
        lifecycleScope.launch { LotteryDraw.drawDueLotteries() }
        setContent {
            QuackappTheme {
                QuackApp()
            }
        }
    }
}

