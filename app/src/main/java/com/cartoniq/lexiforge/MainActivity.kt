package com.cartoniq.lexiforge

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.cartoniq.lexiforge.navigation.LexiNavGraph
import com.cartoniq.lexiforge.ui.theme.LexiForgeTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val adManager = application.lexiApp().adManager
        setContent {
            LexiForgeTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    LexiNavGraph(adManager = adManager)
                }
            }
        }
    }
}
