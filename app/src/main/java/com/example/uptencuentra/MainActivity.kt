package com.example.uptencuentra

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.uptencuentra.navigation.AppNavigation
import com.example.uptencuentra.ui.theme.UPTEncuentraTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            UPTEncuentraTheme {
                AppNavigation()
            }
        }
    }
}