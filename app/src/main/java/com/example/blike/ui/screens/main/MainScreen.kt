package com.example.blike.ui.screens.main

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Whatshot
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.navigation.NavHostController
import com.example.blike.ui.navigation.Routes
import com.example.blike.ui.screens.article.ArticleListScreen
import com.example.blike.ui.screens.profile.ProfileScreen
import com.example.blike.ui.screens.video.VideoListScreen

private data class BottomTab(
    val label: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector
)

private val tabs = listOf(
    BottomTab("热点", Icons.Default.Whatshot),
    BottomTab("上传", Icons.Default.Add),
    BottomTab("待开发", Icons.Default.Build),
    BottomTab("我的", Icons.Default.AccountCircle)
)

private val Pink = Color(0xFFFB7299)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(navController: NavHostController) {
    var selectedTab by rememberSaveable { mutableIntStateOf(0) }
    var hotSubTab by rememberSaveable { mutableIntStateOf(0) }  // 0=视频 1=图文
    var videoRefreshKey by remember { mutableIntStateOf(0) }
    var articleRefreshKey by remember { mutableIntStateOf(0) }

    // 监听发布页回传
    val entry = navController.currentBackStackEntry
    val publishResult by (entry
        ?.savedStateHandle
        ?.getStateFlow("publish_success", "")
        ?.collectAsState()
        ?: remember { mutableStateOf("") })

    LaunchedEffect(publishResult) {
        when (publishResult) {
            "VIDEO" -> {
                selectedTab = 0
                hotSubTab = 0
                videoRefreshKey++
            }
            "ARTICLE" -> {
                selectedTab = 0
                hotSubTab = 1
                articleRefreshKey++
            }
        }
        if (publishResult.isNotEmpty()) {
            entry?.savedStateHandle?.remove<String>("publish_success")
        }
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text("blike") }) },
        bottomBar = {
            NavigationBar {
                tabs.forEachIndexed { index, tab ->
                    val isUpload = index == 1
                    NavigationBarItem(
                        selected = !isUpload && selectedTab == index,
                        onClick = {
                            if (isUpload) navController.navigate(Routes.UPLOAD)
                            else selectedTab = index
                        },
                        icon = {
                            Icon(
                                imageVector = tab.icon,
                                contentDescription = tab.label,
                                tint = if (isUpload) Pink else LocalContentColor.current
                            )
                        },
                        label = {
                            Text(
                                text = tab.label,
                                color = if (isUpload) Pink else LocalContentColor.current
                            )
                        }
                    )
                }
            }
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when (selectedTab) {
                0 -> HotTabContent(
                    navController = navController,
                    subTab = hotSubTab,
                    onSubTabChange = { hotSubTab = it },
                    videoRefreshKey = videoRefreshKey,
                    articleRefreshKey = articleRefreshKey
                )
                2 -> PlaceholderTab("待开发")
                3 -> ProfileScreen(navController)
            }
        }
    }
}

@Composable
private fun HotTabContent(
    navController: NavHostController,
    subTab: Int,
    onSubTabChange: (Int) -> Unit,
    videoRefreshKey: Int,
    articleRefreshKey: Int
) {
    Column(modifier = Modifier.fillMaxSize()) {
        TabRow(
            selectedTabIndex = subTab,
            containerColor = Color.White,
            contentColor = Pink
        ) {
            Tab(
                selected = subTab == 0,
                onClick = { onSubTabChange(0) },
                text = { Text("视频") }
            )
            Tab(
                selected = subTab == 1,
                onClick = { onSubTabChange(1) },
                text = { Text("图文") }
            )
        }

        Box(modifier = Modifier.fillMaxSize()) {
            when (subTab) {
                0 -> VideoListScreen(
                    navController = navController,
                    refreshKey = videoRefreshKey
                )
                1 -> ArticleListScreen(
                    navController = navController,
                    refreshKey = articleRefreshKey
                )
            }
        }
    }
}

@Composable
private fun PlaceholderTab(name: String) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = androidx.compose.ui.Alignment.Center
    ) {
        Text("$name 页面开发中...")
    }
}