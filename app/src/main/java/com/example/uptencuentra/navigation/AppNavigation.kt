package com.example.uptencuentra.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.uptencuentra.ui.screens.*

@Composable
fun AppNavigation() {
    val nav = rememberNavController()

    NavHost(navController = nav, startDestination = Route.Login.route) {

        composable(Route.Login.route) {
            LoginScreen(onLogin = {
                nav.navigate(Route.Home.route) {
                    popUpTo(Route.Login.route) { inclusive = true }
                }
            })
        }

        composable(Route.Home.route) {
            HomeScreen(
                onReportLost = { nav.navigate(Route.ReportLost.route) },
                onReportFound = { nav.navigate(Route.ReportFound.route) },
                onMyReports = { nav.navigate(Route.Reports.route) }
            )
        }

        composable(Route.ReportLost.route) {
            ReportLostScreen(onBack = { nav.popBackStack() })
        }

        composable(Route.ReportFound.route) {
            ReportFoundScreen(onBack = { nav.popBackStack() })
        }

        composable(Route.Reports.route) {
            ReportsScreen(
                onOpen = { id -> nav.navigate(Route.Detail.create(id)) },
                onBack = { nav.popBackStack() }
            )
        }

        composable(
            route = Route.Detail.route,
            arguments = listOf(navArgument(Route.Detail.ARG) { type = NavType.IntType })
        ) { entry ->
            val id = entry.arguments?.getInt(Route.Detail.ARG) ?: 0
            ObjectDetailScreen(
                objectId = id,
                onMatches = { nav.navigate(Route.Matches.create(id)) },
                onBack = { nav.popBackStack() }
            )
        }

        composable(
            route = Route.Matches.route,
            arguments = listOf(navArgument(Route.Matches.ARG) { type = NavType.IntType })
        ) { entry ->
            val id = entry.arguments?.getInt(Route.Matches.ARG) ?: 0
            MatchesScreen(
                objectId = id,
                onVerify = { matchId -> nav.navigate(Route.Verification.create(matchId)) },
                onBack = { nav.popBackStack() }
            )
        }

        composable(
            route = Route.Verification.route,
            arguments = listOf(navArgument(Route.Verification.ARG) { type = NavType.IntType })
        ) { entry ->
            val id = entry.arguments?.getInt(Route.Verification.ARG) ?: 0
            VerificationScreen(
                matchId = id,
                onConfirm = { nav.navigate(Route.Recovery.create(id)) },
                onBack = { nav.popBackStack() }
            )
        }

        composable(
            route = Route.Recovery.route,
            arguments = listOf(navArgument(Route.Recovery.ARG) { type = NavType.IntType })
        ) { entry ->
            val id = entry.arguments?.getInt(Route.Recovery.ARG) ?: 0
            RecoveryScreen(
                matchId = id,
                onFinish = { nav.popBackStack(Route.Home.route, inclusive = false) },
                onBack = { nav.popBackStack() }
            )
        }
    }
}