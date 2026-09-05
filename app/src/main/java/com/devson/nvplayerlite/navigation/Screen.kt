package com.devson.nvplayerlite.navigation

import kotlinx.serialization.Serializable

// --- Existing string-route sealed class ---
// All current destinations are preserved as-is. Do NOT remove or rename them.
sealed class Screen(val route: String) {
    object Onboarding : Screen("onboarding")
    object Home       : Screen("home")
    object Videos     : Screen("videos")
    object Settings   : Screen("settings")
    object Appearance : Screen("appearance")
    object About      : Screen("about")
    object Logs       : Screen("logs")
    object PrivacyPolicy : Screen("privacy_policy")
    object History    : Screen("history")
    object RecycleBin : Screen("recycle_bin")
    object SearchResults : Screen("search_results/{query}") {
        fun createRoute(query: String) = "search_results/${java.net.URLEncoder.encode(query, "UTF-8")}"
    }
    object ListOption : Screen("list_option")
    object ScanFolders : Screen("scan_folders")
    object Tool : Screen("tool_screen")
    object MilliSecond : Screen("milli_second")
    object VideoEditor : Screen("video_editor")
    object PlayerInterface : Screen("player_interface")
    object CustomHome : Screen("custom_home")
    object Gestures : Screen("gestures")
    object MediaStoreFinder : Screen("media_store_finder")
}

// --- Type-safe route definitions (Kotlin Serialization) ---
// Phase 3 demonstration: SearchResultsRoute uses @Serializable for compile-time
// safety and automatic argument encoding - no manual URL encoding required.
//
// Usage in NavGraph:
//   composable<SearchResultsRoute> { backStackEntry ->
//       val args = backStackEntry.toRoute<SearchResultsRoute>()
//       SearchResultsScreen(query = args.query, ...)
//   }
// Navigate via:
//   navController.navigate(SearchResultsRoute(query = searchQuery))
//
// Future migration: define remaining destinations here and replace their
// composable(Screen.X.route) registrations one screen at a time.
@Serializable
data class SearchResultsRoute(val query: String)
