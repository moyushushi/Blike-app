package com.example.blike.ui.screens.forget

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.blike.ui.navigation.Routes

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ForgetScreen(
    navController: NavHostController,
    vm: ForgetViewModel = viewModel()
) {
    val state by vm.state.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(state.error) {
        state.error?.let {
            snackbarHostState.showSnackbar(it)
            vm.consumeError()
        }
    }

    LaunchedEffect(state.success) {
        if (state.success) {
            vm.consumeSuccess()
            // 重置成功回登录页
            navController.navigate(Routes.LOGIN) {
                popUpTo(Routes.LOGIN) { inclusive = true }
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text(if (state.step == 1) "找回密码" else "设置新密码") },
                navigationIcon = {
                    IconButton(
                        onClick = {
                            if (state.step == 2) vm.backToStep1()
                            else navController.popBackStack()
                        }
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 24.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "blike",
                color = Color(0xFFFB7299),
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = if (state.step == 1) "验证邮箱" else "设置新密码",
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(Modifier.height(32.dp))

            if (state.step == 1) {
                // ===== 步骤 1 =====
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = state.email,
                        onValueChange = vm::onEmailChange,
                        label = { Text("邮箱") },
                        singleLine = true,
                        enabled = !state.loading,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(Modifier.width(8.dp))
                    OutlinedButton(
                        onClick = vm::sendResetCode,
                        enabled = !state.loading &&
                                !state.sendingCode &&
                                state.countdown == 0
                    ) {
                        Text(
                            when {
                                state.sendingCode -> "发送中"
                                state.countdown > 0 -> "${state.countdown}s"
                                else -> "发送验证码"
                            }
                        )
                    }
                }
                Spacer(Modifier.height(16.dp))

                OutlinedTextField(
                    value = state.code,
                    onValueChange = vm::onCodeChange,
                    label = { Text("邮箱验证码") },
                    singleLine = true,
                    enabled = !state.loading,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(24.dp))

                Button(
                    onClick = vm::verifyCode,
                    enabled = !state.loading,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFB7299)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                ) {
                    if (state.loading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = Color.White,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text("下一步")
                    }
                }
            } else {
                // ===== 步骤 2 =====
                OutlinedTextField(
                    value = state.password,
                    onValueChange = vm::onPasswordChange,
                    label = { Text("新密码（至少 6 位）") },
                    singleLine = true,
                    enabled = !state.loading,
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(16.dp))

                OutlinedTextField(
                    value = state.confirmPassword,
                    onValueChange = vm::onConfirmChange,
                    label = { Text("确认新密码") },
                    singleLine = true,
                    enabled = !state.loading,
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(24.dp))

                Button(
                    onClick = vm::submitNewPassword,
                    enabled = !state.loading,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFB7299)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                ) {
                    if (state.loading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = Color.White,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text("完成重置")
                    }
                }
            }

            Spacer(Modifier.height(12.dp))

            TextButton(onClick = { navController.popBackStack() }) {
                Text("返回登录", color = Color(0xFF666666))
            }
        }
    }
}