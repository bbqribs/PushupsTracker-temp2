// File: app/src/main/java/com/github/bbqribs/pushupstracker/ui/navigation/AppNavigation.kt
package com.github.bbqribs.pushupstracker.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.github.bbqribs.pushupstracker.ui.HomeScreen
import com.github.bbqribs.pushupstracker.ui.editlog.EditLogScreen
import com.github.bbqribs.pushupstracker.ui.test.TestScreen
import com.github.bbqribs.pushupstracker.ui.SessionScreen
import com.github.bbqribs.pushupstracker.ui.importlog.ImportScreen

@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = Screen.Home.route) {

        composable(route = Screen.Home.route) {
            HomeScreen(
                onStartSession = { week, day, column ->
                    navController.navigate(Screen.Session.createRoute(week, day, column))
                },
                onNavigateToEdit = { navController.navigate(Screen.EditLog.route) },
                onNavigateToTest = { navController.navigate(Screen.Test.route) }
            )
        }

        composable(
            route = Screen.Session.route,
            arguments = listOf(
                navArgument("week") { type = NavType.IntType },
                navArgument("day") { type = NavType.IntType },
                navArgument("column") { type = NavType.StringType }
            )
        ) {
            SessionScreen(onNavigateBack = { navController.popBackStack() })
        }

        composable(route = Screen.EditLog.route) {
            EditLogScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToImport = { navController.navigate(Screen.ImportLog.route) }
            )
        }

        composable(route = Screen.Test.route) {
            TestScreen(onNavigateBack = { navController.popBackStack() })
        }
        composable(route = Screen.ImportLog.route) {
            ImportScreen(onNavigateBack = { navController.popBackStack() })
        }
    }
}
