package com.example.blike.ui.screens.video

import android.view.ViewGroup
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.ColorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import com.example.blike.data.model.VideoComment
import com.example.blike.data.remote.ApiConfig

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VideoDetailScreen(
    navController: NavHostController,
    videoId: Long,
    vm: VideoDetailViewModel = viewModel()
) {
    val state by vm.state.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(videoId) {
        vm.load(videoId)
    }

    val exoPlayer = remember {
        ExoPlayer.Builder(context).build()
    }

    DisposableEffect(Unit) {
        onDispose { exoPlayer.release() }
    }

    LaunchedEffect(state.video?.id) {
        val url = state.video?.url ?: return@LaunchedEffect
        val fullUrl = ApiConfig.fullUrl(url) ?: return@LaunchedEffect
        exoPlayer.setMediaItem(MediaItem.fromUri(fullUrl))
        exoPlayer.prepare()
    }

    Scaffold(
        topBar = {
            TopAppBar(navigationIcon = {
                IconButton(onClick = { navController.popBackStack() }) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "返回"
                    )
                }
            },
                title = {
                    Text(
                        text = state.video?.title ?: "视频详情",
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            )
        }
    ) { padding ->
        when {
            state.loading -> Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }

            state.video == null -> Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Text(state.error ?: "视频不存在")
            }

            else -> {
                val video = state.video!!

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                ) {
                    // 视频播放器
                    AndroidView(
                        factory = { ctx ->
                            PlayerView(ctx).apply {
                                player = exoPlayer
                                useController = true
                                layoutParams = ViewGroup.LayoutParams(
                                    ViewGroup.LayoutParams.MATCH_PARENT,
                                    ViewGroup.LayoutParams.MATCH_PARENT
                                )
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(16f / 9f)
                            .background(Color.Black)
                    )

                    // 下方可滚动区域
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // 标题
                        item {
                            Text(
                                text = video.title,
                                fontSize = 17.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF222222)
                            )
                        }

                        // 播放量 / 发布时间
                        item {
                            Text(
                                text = "${video.playCount ?: 0L} 播放 · ${video.publishTime ?: ""}",
                                fontSize = 12.sp,
                                color = Color(0xFF999999)
                            )
                        }

                        // 作者 + 关注按钮
                        item {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                AsyncImage(
                                    model = ApiConfig.fullUrl(video.avatar),
                                    contentDescription = video.author,
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(CircleShape),
                                    contentScale = ContentScale.Crop,
                                    placeholder = ColorPainter(Color(0xFFEEEEEE)),
                                    error = ColorPainter(Color(0xFFFFCDD2))
                                )
                                Spacer(Modifier.width(10.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = video.author ?: "未知作者",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                    Text(
                                        text = "UP主",
                                        fontSize = 11.sp,
                                        color = Color(0xFF999999)
                                    )
                                }

                                val following = state.isFollowing
                                Button(
                                    onClick = { vm.toggleFollow() },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (following) Color.Transparent
                                        else Color(0xFFFB7299),
                                        contentColor = if (following) Color(0xFFFB7299)
                                        else Color.White
                                    ),
                                    border = if (following)
                                        ButtonDefaults.outlinedButtonBorder(enabled = true)
                                    else null,
                                    shape = RoundedCornerShape(20.dp),
                                    contentPadding = PaddingValues(horizontal = 18.dp)
                                ) {
                                    Text(if (following) "已关注" else "关注", fontSize = 13.sp)
                                }
                            }
                        }

                        // 点赞 + 分享
                        item {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                val liked = state.hasLiked
                                Button(
                                    onClick = { vm.toggleLike() },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (liked) Color(0xFFFB7299)
                                        else Color.Transparent,
                                        contentColor = if (liked) Color.White
                                        else Color(0xFFFB7299)
                                    ),
                                    border = if (liked) null
                                    else ButtonDefaults.outlinedButtonBorder(enabled = true),
                                    shape = RoundedCornerShape(20.dp)
                                ) {
                                    Icon(
                                        Icons.Default.ThumbUp,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(Modifier.width(4.dp))
                                    Text((video.likeCount ?: 0L).toString(), fontSize = 13.sp)
                                }

                                OutlinedButton(
                                    onClick = { },
                                    shape = RoundedCornerShape(20.dp)
                                ) {
                                    Text("分享", fontSize = 13.sp)
                                }
                            }
                        }

                        // 视频简介
                        if (!video.desc.isNullOrBlank()) {
                            item {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(
                                            Color(0xFFF7F7F7),
                                            RoundedCornerShape(8.dp)
                                        )
                                        .padding(12.dp)
                                ) {
                                    Text(
                                        text = video.desc,
                                        fontSize = 13.sp,
                                        color = Color(0xFF444444)
                                    )
                                }
                            }
                        }

                        item { HorizontalDivider() }

                        // 评论标题
                        item {
                            Text(
                                text = "评论 ${video.commentCount ?: 0L}",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        // 评论输入框
                        item {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                OutlinedTextField(
                                    value = state.commentText,
                                    onValueChange = vm::onCommentChange,
                                    placeholder = { Text("发一条友善的评论...") },
                                    modifier = Modifier.weight(1f),
                                    maxLines = 3,
                                    shape = RoundedCornerShape(20.dp)
                                )
                                Spacer(Modifier.width(8.dp))
                                Button(
                                    onClick = { vm.submitComment() },
                                    enabled = state.commentText.isNotBlank()
                                            && !state.submittingComment,
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Color(0xFFFB7299)
                                    ),
                                    shape = RoundedCornerShape(20.dp)
                                ) {
                                    if (state.submittingComment) {
                                        CircularProgressIndicator(
                                            modifier = Modifier.size(16.dp),
                                            strokeWidth = 2.dp,
                                            color = Color.White
                                        )
                                    } else {
                                        Text("发送")
                                    }
                                }
                            }
                        }

                        // 评论列表
                        if (state.comments.isEmpty()) {
                            item {
                                Text(
                                    text = "暂无评论，来做第一个评论的人吧~",
                                    fontSize = 12.sp,
                                    color = Color(0xFF999999),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 24.dp)
                                )
                            }
                        } else {
                            items(state.comments) { comment ->
                                CommentItem(comment)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CommentItem(comment: VideoComment) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.Top
    ) {
        AsyncImage(
            model = ApiConfig.fullUrl(comment.avatar),
            contentDescription = comment.username,
            modifier = Modifier
                .size(32.dp)
                .clip(CircleShape),
            contentScale = ContentScale.Crop,
            placeholder = ColorPainter(Color(0xFFEEEEEE)),
            error = ColorPainter(Color(0xFFFFCDD2))
        )
        Spacer(Modifier.width(10.dp))
        Column(modifier = Modifier.weight(1f)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = comment.username ?: "用户${comment.userId}",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = comment.createTime ?: "",
                    fontSize = 11.sp,
                    color = Color(0xFF999999)
                )
            }
            Spacer(Modifier.height(2.dp))
            Text(
                text = comment.content,
                fontSize = 13.sp,
                color = Color(0xFF333333)
            )
        }
    }
}