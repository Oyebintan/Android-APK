package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.example.ui.SpamShieldApp
import com.example.ui.SpamViewModel
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
    
    private val viewModel: SpamViewModel by viewModels {
        SpamViewModel.Factory(application)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Edge-to-edge transparent system overlay bars layout
        enableEdgeToEdge()
        
        setContent {
            var isDarkTheme by remember { mutableStateOf(true) }
            
            MyApplicationTheme(darkTheme = isDarkTheme) {
                SpamShieldApp(
                    viewModel = viewModel,
                    isDarkTheme = isDarkTheme,
                    onToggleTheme = { isDarkTheme = !isDarkTheme }
                )
            }
        }
    }
}
