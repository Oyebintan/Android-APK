package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
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
            MyApplicationTheme {
                SpamShieldApp(viewModel = viewModel)
            }
        }
    }
}
