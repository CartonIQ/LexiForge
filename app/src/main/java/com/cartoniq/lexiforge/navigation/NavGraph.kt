package com.cartoniq.lexiforge.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.cartoniq.lexiforge.ads.AdManager
import com.cartoniq.lexiforge.ui.screens.AddCardScreen
import com.cartoniq.lexiforge.ui.screens.DashboardScreen
import com.cartoniq.lexiforge.ui.screens.SettingsScreen
import com.cartoniq.lexiforge.ui.screens.StatsScreen
import com.cartoniq.lexiforge.ui.screens.StudyScreen

object Routes {
    const val DASHBOARD = "dashboard"
    const val STUDY = "study/{deckId}"
    const val DECK = "deck/{deckId}"
    const val SETTINGS = "settings"
    const val STATS = "stats"

    /** deckId = -1 studies due cards from every deck. */
    fun study(deckId: Long) = "study/$deckId"
    fun deck(deckId: Long) = "deck/$deckId"
}

@Composable
fun LexiNavGraph(
    adManager: AdManager,
    navController: NavHostController = rememberNavController()
) {
    val deckArgs = listOf(navArgument("deckId") { type = NavType.LongType })

    NavHost(navController = navController, startDestination = Routes.DASHBOARD) {
        composable(Routes.DASHBOARD) {
            DashboardScreen(
                onStartReview = { navController.navigate(Routes.study(-1L)) },
                onStudyDeck = { id -> navController.navigate(Routes.study(id)) },
                onOpenDeck = { id -> navController.navigate(Routes.deck(id)) },
                onOpenStats = { navController.navigate(Routes.STATS) },
                onOpenSettings = { navController.navigate(Routes.SETTINGS) }
            )
        }
        composable(Routes.STUDY, arguments = deckArgs) {
            StudyScreen(
                adManager = adManager,
                onExit = { navController.popBackStack() }
            )
        }
        composable(Routes.DECK, arguments = deckArgs) {
            AddCardScreen(
                onBack = { navController.popBackStack() },
                onStudyDeck = { id -> navController.navigate(Routes.study(id)) }
            )
        }
        composable(Routes.STATS) {
            StatsScreen(onBack = { navController.popBackStack() })
        }
        composable(Routes.SETTINGS) {
            SettingsScreen(onBack = { navController.popBackStack() })
        }
    }
}
