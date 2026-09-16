package com.example.blike.ui.screens.upload

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import coil.compose.AsyncImage

private val Pink = Color(0xFFFB7299)

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun UploadScreen(
    navController: NavHostController,
    vm: PublishViewModel = viewModel()
) {
    val state by vm.state.collectAsState()
    val snackbar = remember { SnackbarHostState() }

    LaunchedEffect(state.error) {
        state.error?.let {
            snackbar.showSnackbar(it)
            vm.consumeError()
        }
    }

    LaunchedEffect(state.success) {
        if (state.success) {
            // 把发布类型回传给 MainScreen
            val result = when (state.mode) {
                PublishMode.VIDEO -> "VIDEO"
                PublishMode.ARTICLE -> "ARTICLE"
            }
            navController.previousBackStackEntry
                ?.savedStateHandle
                ?.set("publish_success", result)

            vm.consumeSuccess()
            snackbar.showSnackbar("发布成功")
            navController.popBackStack()
        }
    }

    val context = LocalContext.current

    val videoPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri != null) {
            // 持久化权限，防止 Uri 在后续操作中失效
            runCatching {
                context.contentResolver.takePersistableUriPermission(
                    uri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
            }
            vm.onVideoSelected(uri)
        }
    }

    val imagePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri != null) {
            runCatching {
                context.contentResolver.takePersistableUriPermission(
                    uri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
            }
            vm.onCoverSelected(uri)
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbar) },
        topBar = {
            TopAppBar(
                title = { Text("发布") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                    }
                },
                actions = {
                    TextButton(
                        onClick = vm::publish,
                        enabled = !state.publishing
                    ) {
                        if (state.publishing) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                strokeWidth = 2.dp,
                                color = Pink
                            )
                        } else {
                            Text("发布", color = Pink, fontWeight = FontWeight.Medium)
                        }
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            // Tab
            TabRow(
                selectedTabIndex = if (state.mode == PublishMode.VIDEO) 0 else 1,
                containerColor = Color.White,
                contentColor = Pink
            ) {
                Tab(
                    selected = state.mode == PublishMode.VIDEO,
                    onClick = { vm.switchMode(PublishMode.VIDEO) },
                    text = { Text("视频") }
                )
                Tab(
                    selected = state.mode == PublishMode.ARTICLE,
                    onClick = { vm.switchMode(PublishMode.ARTICLE) },
                    text = { Text("图文") }
                )
            }

            Spacer(Modifier.height(16.dp))

            // ============ 视频选择区 ============
            if (state.mode == PublishMode.VIDEO) {
                PickerBox(
                    label = if (state.videoUri == null) "点击选择视频" else "已选择视频",
                    uri = state.videoUri,
                    isVideo = true,
                    onClick = { videoPicker.launch(arrayOf("video/*")) }
                )
                Spacer(Modifier.height(16.dp))
            }

            // ============ 封面 ============
            PickerBox(
                label = if (state.coverUri == null) "点击选择封面（可选）" else "已选择封面",
                uri = state.coverUri,
                isVideo = false,
                onClick = { imagePicker.launch(arrayOf("image/*")) }
            )
            Spacer(Modifier.height(16.dp))

            // ============ 标题 ============
            OutlinedTextField(
                value = state.title,
                onValueChange = vm::onTitleChange,
                label = { Text("标题") },
                singleLine = true,
                enabled = !state.publishing,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(12.dp))

            // ============ 简介 / 正文 ============
            if (state.mode == PublishMode.VIDEO) {
                OutlinedTextField(
                    value = state.desc,
                    onValueChange = vm::onDescChange,
                    label = { Text("简介（可选）") },
                    enabled = !state.publishing,
                    minLines = 3,
                    maxLines = 6,
                    modifier = Modifier.fillMaxWidth()
                )
            } else {
                OutlinedTextField(
                    value = state.content,
                    onValueChange = vm::onContentChange,
                    label = { Text("正文") },
                    enabled = !state.publishing,
                    minLines = 8,
                    maxLines = 20,
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                        keyboardType = KeyboardType.Text
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(Modifier.height(16.dp))

            // ============ 分类 ============
            Text("分类", fontSize = 14.sp, color = Color(0xFF666666))
            Spacer(Modifier.height(8.dp))
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                CATEGORIES.forEach { c ->
                    FilterChip(
                        selected = state.category == c,
                        onClick = { vm.onCategoryChange(c) },
                        label = { Text(c) },
                        enabled = !state.publishing
                    )
                }
            }

            Spacer(Modifier.height(32.dp))
        }
    }
}

@Composable
private fun PickerBox(
    label: String,
    uri: Uri?,
    isVideo: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(if (isVideo) 200.dp else 160.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(Color(0xFFF5F5F5))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        if (uri != null && !isVideo) {
            AsyncImage(
                model = uri,
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        } else {
            Text(
                text = label,
                color = Color(0xFF888888),
                fontSize = 14.sp
            )
        }
    }
}