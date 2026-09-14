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

    // 视频
    const val VIDEO_DETAIL = "video_detail/{videoId}"
    fun videoDetail(id: Long) = "video_detail/$id"

    // 文章
    const val ARTICLE_LIST = "article_list"
    const val ARTICLE_DETAIL = "article_detail/{articleId}"
    fun articleDetail(id: Long) = "article_detail/$id"
}

@Composable
fun AppNav() {
    val nav = rememberNavController()

    NavHost(
        navController = nav,
        startDestination = Routes.LOGIN,

        // 进入新页面
        enterTransition = {
            slideInHorizontally(
                initialOffsetX = { fullWidth -> fullWidth },
                animationSpec = tween(durationMillis = 300)
            ) + fadeIn(animationSpec = tween(300))
        },

        // 离开当前页面
        exitTransition = {
            slideOutHorizontally(
                targetOffsetX = { fullWidth -> -fullWidth / 4 },
                animationSpec = tween(durationMillis = 300)
            ) + fadeOut(animationSpec = tween(300))
        },

        // 返回时，被覆盖页面重新进入
        popEnterTransition = {
            slideInHorizontally(
                initialOffsetX = { fullWidth -> -fullWidth / 4 },
                animationSpec = tween(durationMillis = 300)
            ) + fadeIn(animationSpec = tween(300))
        },

        // 返回时，当前页面滑出
        popExitTransition = {
            slideOutHorizontally(
                targetOffsetX = { fullWidth -> fullWidth },
                animationSpec = tween(durationMillis = 300)
            ) + fadeOut(animationSpec = tween(300))
        }
    ) {
        composable(Routes.LOGIN) { LoginScreen(nav) }
        composable(Routes.MAIN) { MainScreen(nav) }
        composable(Routes.REGISTER) { PlaceholderScreen("注册") }
        composable(Routes.FORGET) { PlaceholderScreen("忘记密码") }

        // ==================== 视频 ====================
        composable(
            route = Routes.VIDEO_DETAIL,
            arguments = listOf(navArgument("videoId") { type = NavType.LongType })
        ) { backStackEntry ->
            val videoId = backStackEntry.arguments?.getLong("videoId") ?: 0L
            VideoDetailScreen(navController = nav, videoId = videoId)
        }

        // ==================== 文章 ====================
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