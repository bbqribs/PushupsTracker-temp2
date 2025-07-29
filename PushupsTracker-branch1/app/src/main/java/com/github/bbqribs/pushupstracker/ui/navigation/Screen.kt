// File: app/src/main/java/com/github/bbqribs/pushupstracker/ui/navigation/Screen.kt
package com.github.bbqribs.pushupstracker.ui.navigation

sealed class Screen(val route: String) {
    object Home : Screen("home")
    object EditLog : Screen("edit_log")
    object Test : Screen("test")
    object ImportLog : Screen("import_log")

    // Route for a session, with arguments for week, day, and column
    object Session : Screen("session/{week}/{day}/{column}") {
        fun createRoute(week: Int, day: Int, column: String) = "session/$week/$day/$column"
    }
}