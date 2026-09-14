package com.example.blike.ui.navigation

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.blike.di.ServiceLocator
import com.example.blike.ui.common.PlaceholderScreen
import com.example.blike.ui.screens.article.ArticleDetailScreen
import com.example.blike.ui.screens.article.ArticleListScreen
import com.example.blike.ui.screens.login.LoginScreen
import com.example.blike.ui.screens.main.MainScreen
import com.example.blike.ui.screens.video.VideoDetailScreen

object Routes {
    const val LOGIN = "login"
    const val MAIN = "main"
    const val REGISTER = "register"
    const val FORGET = "forget"

    const val VIDEO_DETAIL = "video_detail/{videoId}"
    fun videoDetail(id: Long) = "video_detail/$id"

    const val ARTICLE_LIST = "article_list"
    const val ARTICLE_DETAIL = "article_detail/{articleId}"
    fun articleDetail(id: Long) = "article_detail/$id"
}

@Composable
fun AppNav() {
    val nav = rememberNavController()

    // 关键：根据 token 是否存在，决定起始页
    val startDestination = if (ServiceLocator.tokenManager.isLoggedIn) {
        Routes.MAIN
    } else {
        Routes.LOGIN
    }

    NavHost(
        navController = nav,
        startDestination = startDestination,

        enterTransition = {
            slideInHorizontally(
                initialOffsetX = { it },
                animationSpec = tween(300)
            ) + fadeIn(animationSpec = tween(300))
        },
        exitTransition = {
            slideOutHorizontally(
                targetOffsetX = { -it / 4 },
                animationSpec = tween(300)
            ) + fadeOut(animationSpec = tween(300))
        },
        popEnterTransition = {
            slideInHorizontally(
                initialOffsetX = { -it / 4 },
                animationSpec = tween(300)
            ) + fadeIn(animationSpec = tween(300))
        },
        popExitTransition = {
            slideOutHorizontally(
                targetOffsetX = { it },
                animationSpec = tween(300)
            ) + fadeOut(animationSpec = tween(300))
        }
    ) {
        composable(Routes.LOGIN) { LoginScreen(nav) }
        composable(Routes.MAIN) { MainScreen(nav) }
        composable(Routes.REGISTER) { PlaceholderScreen("注册") }
        composable(Routes.FORGET) { PlaceholderScreen("忘记密码") }

        composable(
            route = Routes.VIDEO_DETAIL,
            arguments = listOf(navArgument("videoId") { type = NavType.LongType })
        ) { backStackEntry ->
            val videoId = backStackEntry.arguments?.getLong("videoId") ?: 0L
            VideoDetailScreen(navController = nav, videoId = videoId)
        }

        composable(Routes.ARTICLE_LIST) {
            ArticleListScreen(navController = nav)
        }

        composable(
            route = Routes.ARTICLE_DETAIL,
            arguments = listOf(navArgument("articleId") { type = NavType.LongType })
        ) { backStackEntry ->
            val articleId = backStackEntry.arguments?.getLong("articleId") ?: 0L
            ArticleDetailScreen(articleId = articleId)
        }
    }
}