package com.telecommande

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.navigation.compose.rememberNavController
import com.telecommande.navigation.AppNavigation
import com.telecommande.ui.theme.TelecommandeTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            TelecommandeTheme {
                val navController = rememberNavController()
                AppNavigation(
                    navController = navController
                )
            }
        }
    }
}