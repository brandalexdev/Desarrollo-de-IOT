package com.iot.controlluces

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.viewmodel.compose.viewModel
import com.iot.controlluces.ui.screens.MainScreen
import com.iot.controlluces.ui.theme.ControlLucesEdificioTheme
import com.iot.controlluces.viewmodel.LucesViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ControlLucesEdificioTheme {
                val lucesViewModel: LucesViewModel = viewModel()
                MainScreen(viewModel = lucesViewModel)
            }
        }
    }
}
