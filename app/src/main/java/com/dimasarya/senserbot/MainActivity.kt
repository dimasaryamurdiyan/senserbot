package com.dimasarya.senserbot

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.dimasarya.senserbot.presentation.navigation.AppNavGraph
import com.dimasarya.senserbot.ui.theme.BoilerplateCodeTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            BoilerplateCodeTheme {
                AppNavGraph()
            }
        }
    }
}
