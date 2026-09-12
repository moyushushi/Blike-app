package com.example.blike.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.blike.ui.common.PlaceholderScreen
import com.example.blike.ui.screens.login.LoginScreen
import com.example.blike.ui.screens.video.VideoListScreen

object Routes{
    const val LOGIN = "login"
    const val VIDEO_LIST = "video_list"
    const val REGISTER = "register"
    const val FORGET = "forget"
}

@Composable
fun AppNav() {
    val nav= rememberNavController()

    NavHost(navController=nav,startDestination = Routes.LOGIN){
        composable(Routes.LOGIN) {
            LoginScreen(nav)
        }
        composable(Routes.VIDEO_LIST) {
            VideoListScreen(nav)
        }
        composable(Routes.REGISTER) {
            PlaceholderScreen("注册")
        }
        composable(Routes.FORGET) {
            PlaceholderScreen("忘记密码")
        }
    }
    
}

