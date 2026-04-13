package com.dimasarya.senserbot.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.dimasarya.senserbot.presentation.user.UserScreen
import kotlinx.serialization.Serializable

@Serializable
object UserRoute

@Composable
fun AppNavGraph(
    navController: NavHostController = rememberNavController()
) {
    NavHost(
        navController = navController,
        startDestination = UserRoute
    ) {
        composable<UserRoute> {
            UserScreen()
        }
    }
}
