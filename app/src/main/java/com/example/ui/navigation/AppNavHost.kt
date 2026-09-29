package com.example.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.ui.screens.dashboard.DashboardScreen
import com.example.ui.screens.generator.GeneratorScreen
import com.example.ui.screens.opportunities.OpportunitiesScreen
import com.example.ui.screens.opportunities.OpportunityDetailScreen
import com.example.ui.screens.queue.ContentPreviewScreen
import com.example.ui.screens.queue.ContentQueueScreen
import com.example.ui.screens.settings.MetaConnectionScreen
import com.example.ui.screens.settings.SettingsScreen
import com.example.ui.screens.verification.FinalVerificationScreen
import com.example.ui.viewmodel.AppViewModel

@Composable
fun AppNavHost(
    navController: NavHostController,
    viewModel: AppViewModel,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Dashboard.route,
        modifier = modifier
    ) {
        composable(route = Screen.Dashboard.route) {
            DashboardScreen(
                viewModel = viewModel,
                onNavigateToMetaConnection = {
                    navController.navigate(Screen.MetaConnection.route)
                },
                onNavigateToOpportunities = {
                    navController.navigate(Screen.Opportunities.route)
                },
                onNavigateToQueue = {
                    navController.navigate(Screen.ContentQueue.route)
                }
            )
        }
        composable(route = Screen.Opportunities.route) {
            OpportunitiesScreen(
                viewModel = viewModel,
                onNavigateToDetail = { id ->
                    navController.navigate(Screen.OpportunityDetail.createRoute(id))
                }
            )
        }
        composable(
            route = Screen.OpportunityDetail.route,
            arguments = listOf(navArgument("opportunityId") { type = NavType.StringType })
        ) { backStackEntry ->
            val opportunityId = backStackEntry.arguments?.getString("opportunityId") ?: ""
            OpportunityDetailScreen(
                opportunityId = opportunityId,
                viewModel = viewModel,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
        composable(route = Screen.ContentQueue.route) {
            ContentQueueScreen(
                viewModel = viewModel,
                onNavigateToPreview = { contentId ->
                    navController.navigate(Screen.ContentPreview.createRoute(contentId))
                },
                onNavigateToVerification = { contentId, contentType ->
                    navController.navigate(Screen.FinalVerification.createRoute(contentId, contentType))
                }
            )
        }
        composable(
            route = Screen.ContentPreview.route,
            arguments = listOf(navArgument("contentId") { type = NavType.StringType })
        ) { backStackEntry ->
            val contentId = backStackEntry.arguments?.getString("contentId") ?: ""
            ContentPreviewScreen(
                contentId = contentId,
                viewModel = viewModel,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
        composable(route = Screen.Generator.route) {
            GeneratorScreen(
                viewModel = viewModel,
                onNavigateToQueue = {
                    navController.navigate(Screen.ContentQueue.route)
                }
            )
        }
        composable(route = Screen.Settings.route) {
            SettingsScreen(
                viewModel = viewModel,
                onNavigateToMetaConnection = {
                    navController.navigate(Screen.MetaConnection.route)
                }
            )
        }
        composable(route = Screen.MetaConnection.route) {
            MetaConnectionScreen(
                viewModel = viewModel,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
        composable(
            route = Screen.FinalVerification.route,
            arguments = listOf(
                navArgument("contentId") { type = NavType.StringType },
                navArgument("contentType") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val contentId = backStackEntry.arguments?.getString("contentId") ?: ""
            val contentType = backStackEntry.arguments?.getString("contentType") ?: "POST"
            FinalVerificationScreen(
                contentId = contentId,
                contentType = contentType,
                viewModel = viewModel,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
    }
}
