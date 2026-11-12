package com.example.uptencuentra.navigation

import androidx.compose.runtime.*
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import com.example.uptencuentra.ui.screens.*
import com.example.uptencuentra.ui.viewmodel.SessionViewModel

@Composable
fun AppNavigation(sessionViewModel: SessionViewModel) {
    val nav = rememberNavController()
    val usuario by sessionViewModel.usuario.collectAsStateWithLifecycle()
    val demo = usuario?.esDemostracion == true
    LaunchedEffect(usuario?.id) {
        nav.navigate(if (usuario == null) Route.Login.route else Route.Home.route) {
            popUpTo(nav.graph.id) { inclusive = false }; launchSingleTop = true
        }
    }
    NavHost(navController = nav, startDestination = Route.Login.route) {
        composable(Route.Login.route) { LoginScreen(sessionViewModel) }
        composable(Route.Home.route) {
            HomeScreen(onReportLost = { nav.navigate(Route.ReportLost.route) },
                onReportFound = { nav.navigate(Route.ReportFound.route) },
                onMyReports = { nav.navigate(Route.Reports.route) },
                onRequests = { nav.navigate(Route.Recovery.route) },
                onEncargado = { nav.navigate(Route.Encargado.route) },
                esEncargado = usuario?.esEncargado == true,
                onLogout = sessionViewModel::cerrarSesion, correo = usuario?.correo.orEmpty(), demostracion = demo)
        }
        composable(Route.ReportLost.route) { ReportLostScreen({ nav.popBackStack() }, demo) }
        composable(Route.ReportFound.route) { ReportFoundScreen({ nav.popBackStack() }, demo) }
        composable(Route.Reports.route) { ReportsScreen({ nav.navigate(Route.Detail.create(it)) }, { nav.popBackStack() }, demo) }
        composable(Route.Detail.route, arguments = listOf(navArgument(Route.Detail.ARG) { type = NavType.StringType })) {
            ObjectDetailScreen(onBack = { nav.popBackStack() }, onMatches = { nav.navigate(Route.Matches.create(it)) }, demostracion = demo)
        }
        composable(Route.Matches.route, arguments = listOf(navArgument(Route.Matches.ARG) { type = NavType.StringType })) {
            MatchesScreen(onVerify = { nav.navigate(Route.Verification.create(it)) }, onBack = { nav.popBackStack() })
        }
        composable(Route.Verification.route, arguments = listOf(navArgument(Route.Verification.ARG) { type = NavType.StringType })) {
            VerificationScreen(onBack = { nav.popBackStack() }, onSaved = {
                nav.navigate(Route.Recovery.route) { popUpTo(Route.Home.route) { inclusive = false } }
            })
        }
        composable(Route.Recovery.route) { RecoveryScreen(onBack = { nav.popBackStack() }) }
        composable(Route.Encargado.route) {
            if (usuario?.esEncargado == true) EncargadoScreen(onBack = { nav.popBackStack() })
        }
    }
}
