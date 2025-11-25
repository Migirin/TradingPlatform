package com.example.tradingplatform.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.Alignment
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.tradingplatform.data.auth.AuthRepository
import com.example.tradingplatform.data.chat.ChatMessage
import com.example.tradingplatform.ui.viewmodel.ChatViewModel
import java.text.SimpleDateFormat
import java.util.Locale

@Composable
fun ChatScreen(onBack: () -> Unit) {
    val vm: ChatViewModel = viewModel()
    val messages by vm.messages.collectAsState()
    val uiState by vm.state.collectAsState()
    val messageText = remember { mutableStateOf("") }
    val receiverUid = remember { mutableStateOf("") }
    val receiverEmail = remember { mutableStateOf("") }
    val currentUserUid = remember { mutableStateOf<String?>(null) }

    val context = LocalContext.current
    val authRepo = remember { AuthRepository(context) }
    val listState = rememberLazyListState()

    // 获取当前用户 UID
    LaunchedEffect(Unit) {
        currentUserUid.value = authRepo.getCurrentUserUid()
    }

    // 自动滚动到底部
    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // 顶部栏
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "我的消息",
                style = MaterialTheme.typography.titleLarge
            )
            Button(onClick = onBack) {
                Text("返回")
            }
        }

        // 消息列表
        if (messages.isEmpty()) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "暂无消息",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "在商品详情页可以联系卖家",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        } else {
            LazyColumn(
                state = listState,
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(messages, key = { it.id }) { message ->
                    MessageCard(message = message, currentUserUid = currentUserUid.value)
                }
            }
        }

        // 发送消息区域（简化版：直接发送给第一个消息的对方）
        if (messages.isNotEmpty()) {
            val firstMessage = messages.first()
            val currentUid = currentUserUid.value ?: ""
            val otherUser = if (firstMessage.senderUid == currentUid) {
                firstMessage.receiverUid to firstMessage.receiverEmail
            } else {
                firstMessage.senderUid to firstMessage.senderEmail
            }

            LaunchedEffect(otherUser.first) {
                receiverUid.value = otherUser.first
                receiverEmail.value = otherUser.second
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = messageText.value,
                    onValueChange = { messageText.value = it },
                    label = { Text("输入消息") },
                    modifier = Modifier.weight(1f),
                    enabled = uiState !is com.example.tradingplatform.ui.viewmodel.ChatUiState.Sending
                )
                Button(
                    onClick = {
                        if (messageText.value.isNotBlank() && receiverUid.value.isNotBlank()) {
                            vm.sendMessage(
                                receiverUid = receiverUid.value,
                                receiverEmail = receiverEmail.value,
                                content = messageText.value
                            )
                            messageText.value = ""
                        }
                    },
                    enabled = messageText.value.isNotBlank() 
                            && uiState !is com.example.tradingplatform.ui.viewmodel.ChatUiState.Sending
                ) {
                    Text("发送")
                }
            }
        }
    }
}

@Composable
fun MessageCard(
    message: ChatMessage,
    currentUserUid: String? = null
) {
    val isMyMessage = message.senderUid == (currentUserUid ?: "")
    val dateFormat = SimpleDateFormat("MM-dd HH:mm", Locale.getDefault())

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isMyMessage) Arrangement.End else Arrangement.Start
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.7f)
                .padding(horizontal = 4.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (isMyMessage) {
                    MaterialTheme.colorScheme.primaryContainer
                } else {
                    MaterialTheme.colorScheme.surfaceVariant
                }
            )
        ) {
            Column(
                modifier = Modifier.padding(12.dp)
            ) {
                if (!isMyMessage) {
                    Text(
                        text = message.senderEmail,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Text(
                    text = message.content,
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = dateFormat.format(java.util.Date(message.timestamp)),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }
    }
}
