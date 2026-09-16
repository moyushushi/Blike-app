package com.example.blike.ui.screens.profile

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.ColorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import com.example.blike.data.remote.ApiConfig
import com.example.blike.di.ServiceLocator
import com.example.blike.ui.navigation.Routes

private val Pink = Color(0xFFFB7299)


@Composable
fun ProfileScreen(
    navController: NavHostController,
    vm: ProfileViewModel = viewModel()
) {
    val state by vm.state.collectAsState()
    val snackbar = remember { SnackbarHostState() }
    // 上传头像的对话框（这里只做示意：让用户输入一个本地路径或跳过）
    var showChangePwdDialog by remember { mutableStateOf(false) }

    LaunchedEffect(state.error) {
        state.error?.let {
            snackbar.showSnackbar(it)
            vm.consumeError()
        }
    }

    LaunchedEffect(state.loggedOut) {
        if (state.loggedOut) {
            navController.navigate(Routes.LOGIN) {
                popUpTo(Routes.MAIN) { inclusive = true }
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbar) }
    ) { padding ->
        when {
            state.loading && state.user == null -> Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = Pink)
            }

            state.user == null -> Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Text("加载失败")
            }

            else -> {
                val user = state.user!!
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .verticalScroll(rememberScrollState())
                ) {
                    // ==================== 头部 ====================
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Pink)
                            .padding(vertical = 32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            AsyncImage(
                                model = ApiConfig.fullUrl(user.avatar)
                                    ?: ApiConfig.fullUrl("/upload/avatar/default.png"),
                                contentDescription = user.username,
                                modifier = Modifier
                                    .size(88.dp)
                                    .clip(CircleShape),
                                contentScale = ContentScale.Crop,
                                placeholder = ColorPainter(Color(0xFFEEEEEE)),
                                error = ColorPainter(Color(0xFFFFCDD2))
                            )
                            Spacer(Modifier.height(12.dp))
                            Text(
                                text = user.username,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Spacer(Modifier.height(4.dp))
                            Text(
                                text = user.email ?: "",
                                fontSize = 13.sp,
                                color = Color(0xFFFFEEF4)
                            )
                        }
                    }

                    // ==================== 简介 ====================
                    SectionCard {
                        Text(
                            text = "简介",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF333333)
                        )
                        Spacer(Modifier.height(8.dp))

                        if (state.editingBio) {
                            OutlinedTextField(
                                value = state.bioInput,
                                onValueChange = vm::onBioChange,
                                modifier = Modifier.fillMaxWidth(),
                                enabled = !state.savingBio,
                                placeholder = { Text("写点什么介绍自己…") }
                            )
                            Spacer(Modifier.height(8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.End
                            ) {
                                TextButton(
                                    onClick = vm::cancelEditBio,
                                    enabled = !state.savingBio
                                ) { Text("取消", color = Color(0xFF888888)) }
                                Spacer(Modifier.width(8.dp))
                                Button(
                                    onClick = vm::saveBio,
                                    enabled = !state.savingBio,
                                    colors = ButtonDefaults.buttonColors(containerColor = Pink)
                                ) {
                                    Text(if (state.savingBio) "保存中…" else "保存")
                                }
                            }
                        } else {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { vm.startEditBio() },
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = user.bio?.takeIf { it.isNotBlank() }
                                        ?: "还没填写简介，点击编辑",
                                    fontSize = 14.sp,
                                    color = if (user.bio.isNullOrBlank())
                                        Color(0xFFAAAAAA) else Color(0xFF555555),
                                    modifier = Modifier.weight(1f)
                                )
                                Text("编辑", fontSize = 13.sp, color = Pink)
                            }
                        }
                    }

                    Spacer(Modifier.height(12.dp))

                    // ==================== 功能入口 ====================
                    SectionCard {
                        MenuItem("我关注的") {
                            // navController.navigate(Routes.FOLLOWING)
                        }
                        HorizontalDivider(color = Color(0xFFF0F0F0))
                        MenuItem("我点赞的视频") {
                            // navController.navigate(Routes.LIKED_VIDEOS)
                        }
                        HorizontalDivider(color = Color(0xFFF0F0F0))
                        MenuItem("我点赞的文章") {
                            // navController.navigate(Routes.LIKED_ARTICLES)
                        }
                        HorizontalDivider(color = Color(0xFFF0F0F0))
                        MenuItem("修改密码") {
                            showChangePwdDialog = true
                        }
                    }

                    Spacer(Modifier.height(12.dp))

                    // ==================== 退出登录 ====================
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                    ) {
                        OutlinedButton(
                            onClick = vm::logout,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = Color(0xFFE53935)
                            )
                        ) {
                            Text("退出登录")
                        }
                    }

                    Spacer(Modifier.height(24.dp))
                }
            }
        }
    }

    if (showChangePwdDialog) {
        ChangePasswordDialog(
            loading = state.changingPassword,
            onDismiss = { if (!state.changingPassword) showChangePwdDialog = false },
            onConfirm = { old, new ->
                vm.changePassword(old, new) { ok ->
                    if (ok) {
                        showChangePwdDialog = false
                        // 通过 snackbar 提示成功（可选）
                    }
                }
            }
        )
    }
}


@Composable
private fun SectionCard(content: @Composable () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            content()
        }
    }
}

@Composable
private fun MenuItem(title: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            fontSize = 15.sp,
            color = Color(0xFF333333),
            modifier = Modifier.weight(1f)
        )
        Text("›", fontSize = 18.sp, color = Color(0xFFBBBBBB))
    }
}


@Composable
private fun ChangePasswordDialog(
    loading: Boolean,
    onDismiss: () -> Unit,
    onConfirm: (old: String, new: String) -> Unit
) {
    var oldPwd by remember { mutableStateOf("") }
    var newPwd by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = { if (!loading) onDismiss() },
        title = { Text("修改密码") },
        text = {
            Column {
                OutlinedTextField(
                    value = oldPwd,
                    onValueChange = { oldPwd = it },
                    label = { Text("旧密码") },
                    singleLine = true,
                    enabled = !loading,
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(12.dp))
                OutlinedTextField(
                    value = newPwd,
                    onValueChange = { newPwd = it },
                    label = { Text("新密码（至少 6 位）") },
                    singleLine = true,
                    enabled = !loading,
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onConfirm(oldPwd, newPwd) },
                enabled = !loading && oldPwd.isNotBlank() && newPwd.isNotBlank()
            ) {
                if (loading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        strokeWidth = 2.dp,
                        color = Pink
                    )
                } else {
                    Text("确定", color = Pink)
                }
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                enabled = !loading
            ) {
                Text("取消", color = Color(0xFF888888))
            }
        }
    )
}