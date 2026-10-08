package com.example.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.automirrored.outlined.TrendingUp
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.Chat
import androidx.compose.material.icons.outlined.Dashboard
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(
    val route: String,
    val title: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val navLabel: String = title
) {
    data object Dashboard : Screen(
        route = "dashboard",
        title = "Dashboard",
        selectedIcon = Icons.Filled.Dashboard,
        unselectedIcon = Icons.Outlined.Dashboard
    )

    data object Opportunities : Screen(
        route = "opportunities",
        title = "Opportunities",
        selectedIcon = Icons.AutoMirrored.Filled.TrendingUp,
        unselectedIcon = Icons.AutoMirrored.Outlined.TrendingUp,
        navLabel = "Trends"
    )

    data object Generator : Screen(
        route = "generator",
        title = "Generator",
        selectedIcon = Icons.Filled.AutoAwesome,
        unselectedIcon = Icons.Outlined.AutoAwesome
    )

    data object Chat : Screen(
        route = "chat",
        title = "AI Chat",
        selectedIcon = Icons.Filled.Chat,
        unselectedIcon = Icons.Outlined.Chat,
        navLabel = "AI Chat"
    )

    data object ContentQueue : Screen(
        route = "content_queue",
        title = "Queue",
        selectedIcon = Icons.Filled.Schedule,
        unselectedIcon = Icons.Outlined.Schedule
    )

    data object Settings : Screen(
        route = "settings",
        title = "Settings",
        selectedIcon = Icons.Filled.Settings,
        unselectedIcon = Icons.Outlined.Settings
    )

    data object MetaConnection : Screen(
        route = "meta_connection",
        title = "Meta Connections",
        selectedIcon = Icons.Filled.Settings,
        unselectedIcon = Icons.Outlined.Settings
    )

    data object OpportunityDetail : Screen(
        route = "opportunity_detail/{opportunityId}",
        title = "Opportunity Details",
        selectedIcon = Icons.AutoMirrored.Filled.TrendingUp,
        unselectedIcon = Icons.AutoMirrored.Outlined.TrendingUp
    ) {
        fun createRoute(opportunityId: String): String = "opportunity_detail/$opportunityId"
    }

    data object ContentPreview : Screen(
        route = "content_preview/{contentId}",
        title = "Content Preview",
        selectedIcon = Icons.Filled.Schedule,
        unselectedIcon = Icons.Outlined.Schedule
    ) {
        fun createRoute(contentId: String): String = "content_preview/$contentId"
    }

    data object FinalVerification : Screen(
        route = "final_verification/{contentId}/{contentType}",
        title = "Quality Control Gate",
        selectedIcon = Icons.Filled.Schedule,
        unselectedIcon = Icons.Outlined.Schedule
    ) {
        fun createRoute(contentId: String, contentType: String): String = "final_verification/$contentId/$contentType"
    }

    data object Logs : Screen(
        route = "logs",
        title = "Diagnostic Logs",
        selectedIcon = Icons.Filled.Settings,
        unselectedIcon = Icons.Outlined.Settings
    )

    companion object {
        val bottomNavItems = listOf(
            Dashboard,
            Opportunities,
            Generator,
            Chat,
            ContentQueue,
            Settings
        )
    }
}
