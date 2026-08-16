package com.example.bypassyou

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.example.bypassyou.ui.MainScreen
import com.example.bypassyou.ui.MainViewModel
import com.example.bypassyou.ui.theme.BypassYouTheme
import com.example.bypassyou.ui.theme.screenBackgroundColor

class MainActivity : ComponentActivity() {

    private val mainViewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val themeMode by mainViewModel.themeMode.collectAsState()

            BypassYouTheme(themeMode = themeMode) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.screenBackgroundColor
                ) {
                    MainScreen(viewModel = mainViewModel)
                }
            }
        }
    }
}