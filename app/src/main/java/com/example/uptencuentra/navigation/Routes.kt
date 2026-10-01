package com.example.uptencuentra.navigation

sealed class Route(val route: String) {
    data object Login : Route("login")
    data object Home : Route("home")
    data object Reports : Route("reports")
    data object ReportLost : Route("report_lost")
    data object ReportFound : Route("report_found")

    data object Detail : Route("object/{objectId}") {
        const val ARG = "objectId"
        fun create(id: Int) = "object/$id"
    }
    data object Matches : Route("matches/{objectId}") {
        const val ARG = "objectId"
        fun create(id: Int) = "matches/$id"
    }
    data object Verification : Route("verification/{matchId}") {
        const val ARG = "matchId"
        fun create(id: Int) = "verification/$id"
    }
    data object Recovery : Route("recovery/{matchId}") {
        const val ARG = "matchId"
        fun create(id: Int) = "recovery/$id"
    }
}