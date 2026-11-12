package com.example.uptencuentra

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import com.example.uptencuentra.navigation.AppNavigation
import com.example.uptencuentra.ui.theme.UPTEncuentraTheme
import com.example.uptencuentra.ui.viewmodel.SessionViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private val sessionViewModel: SessionViewModel by viewModels()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent { UPTEncuentraTheme { AppNavigation(sessionViewModel) } }
    }
}
