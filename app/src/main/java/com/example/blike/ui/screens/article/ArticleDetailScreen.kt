package com.example.blike.ui.screens.article

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.blike.data.model.ArticleComment
import com.example.blike.data.remote.ApiConfig


@Composable
fun ArticleDetailScreen(
    articleId: Long,
    vm: ArticleDetailViewModel = viewModel(
        factory = ArticleDetailViewModelFactory(articleId)
    )
) {
    val state by vm.state.collectAsState()
    var input by remember { mutableStateOf("") }

    when {
        state.loading && state.article == null -> Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }

        state.article == null -> Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(state.error ?: "加载失败")
        }

        else -> {
            val article = state.article!!

            Column(modifier = Modifier.fillMaxSize()) {

                LazyColumn(
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // 标题
                    item {
                        Text(
                            text = article.title,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF222222)
                        )
                    }

                    // 作者信息
                    item {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            AsyncImage(
                                model = ApiConfig.fullUrl(article.avatar),
                                contentDescription = article.author,
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape),
                                contentScale = ContentScale.Crop,
                                placeholder = ColorPainter(Color(0xFFEEEEEE)),
                                error = ColorPainter(Color(0xFFFFCDD2))
                            )
                            Spacer(Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = article.author ?: "未知作者",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium
                                )
                                Text(
                                    text = buildString {
                                        append(article.publishTime ?: "")
                                        append("  ·  ")
                                        append("${article.viewCount ?: 0} 阅读")
                                    },
                                    fontSize = 12.sp,
                                    color = Color(0xFF888888)
                                )
                            }
                        }
                    }

                    // 正文
                    item {
                        Text(
                            text = article.content ?: "",
                            fontSize = 15.sp,
                            color = Color(0xFF333333),
                            lineHeight = 22.sp
                        )
                    }

                    // 统计信息
                    item {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 4.dp),
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Text("👍 ${article.likeCount ?: 0}", fontSize = 13.sp,
                                color = Color(0xFF666666))
                            Text("💬 ${article.commentCount ?: state.comments.size}",
                                fontSize = 13.sp, color = Color(0xFF666666))
                        }
                    }

                    // 评论标题
                    item {
                        HorizontalDivider()
                        Text(
                            text = "评论 (${state.comments.size})",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }

                    // 评论列表
                    if (state.comments.isEmpty()) {
                        item {
                            Text(
                                text = "还没有评论，快来抢沙发～",
                                fontSize = 13.sp,
                                color = Color(0xFF999999),
                                modifier = Modifier.padding(vertical = 8.dp)
                            )
                        }
                    } else {
                        items(state.comments, key = { it.id }) { c ->
                            CommentItem(c)
                        }
                    }
                }

                // 底部输入框
                HorizontalDivider()
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = input,
                        onValueChange = { input = it },
                        modifier = Modifier.weight(1f),
                        placeholder = { Text("说点什么…") },
                        singleLine = true,
                        shape = RoundedCornerShape(20.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Button(
                        onClick = {
                            vm.postComment(input)
                            input = ""
                        },
                        enabled = !state.sending && input.isNotBlank()
                    ) {
                        Text(if (state.sending) "发送中" else "发送")
                    }
                }
            }
        }
    }
}


@Composable
private fun CommentItem(c: ArticleComment) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
    ) {
        AsyncImage(
            model = ApiConfig.fullUrl(c.avatar),
            contentDescription = c.username,
            modifier = Modifier
                .size(32.dp)
                .clip(CircleShape),
            contentScale = ContentScale.Crop,
            placeholder = ColorPainter(Color(0xFFEEEEEE)),
            error = ColorPainter(Color(0xFFFFCDD2))
        )
        Spacer(Modifier.width(8.dp))
        Column {
            Text(
                text = c.username ?: "匿名用户",
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF333333)
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text = c.content,
                fontSize = 14.sp,
                color = Color(0xFF555555)
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text = c.createTime ?: "",
                fontSize = 11.sp,
                color = Color(0xFF999999)
            )
        }
    }
}