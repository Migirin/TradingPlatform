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
import com.example.tradingplatform.ui.i18n.LocalAppStrings
import com.example.tradingplatform.ui.theme.MessageBubbleOther
import com.example.tradingplatform.ui.theme.MessageBubbleSelf
import com.example.tradingplatform.ui.viewmodel.ChatViewModel
import java.text.SimpleDateFormat
import java.util.Locale

@Composable
fun ChatScreen(
    onBack: () -> Unit,
    receiverUid: String? = null,
    receiverEmail: String? = null,
    itemId: String? = null,
    itemTitle: String? = null,
    onConversationClick: (String, String, String, String) -> Unit = { _, _, _, _ -> }
) {
    val vm: ChatViewModel = viewModel()
    val messages by vm.messages.collectAsState()
    val conversations by vm.conversations.collectAsState()
    val uiState by vm.state.collectAsState()
    val messageText = remember { mutableStateOf("") }
    val targetReceiverUid = remember { mutableStateOf("") }
    val targetReceiverEmail = remember { mutableStateOf("") }
    val currentUserUid = remember { mutableStateOf<String?>(null) }

    val context = LocalContext.current
    val authRepo = remember { AuthRepository(context) }
    val listState = rememberLazyListState()

    // 获取当前用户 UID / Get current user UID
    LaunchedEffect(Unit) {
        currentUserUid.value = authRepo.getCurrentUserUid()
    }
    
    // 如果提供了接收者信息，显示对话详情；否则显示对话列表 / If receiver info provided, show conversation detail; otherwise show conversation list
    LaunchedEffect(receiverUid, receiverEmail) {
        if (receiverUid != null && receiverEmail != null) {
            targetReceiverUid.value = receiverUid
            targetReceiverEmail.value = receiverEmail
            // 加载与该用户的聊天记录 / Load chat history with this user
            vm.loadMessagesWithUser(receiverUid)
        } else {
            // 如果没有指定接收者，加载对话列表 / If no receiver specified, load conversation list
            vm.loadConversations()
        }
    }
    
    // 如果通过参数提供了接收者，立即设置（不等待LaunchedEffect）/ If receiver provided via parameter, set immediately (don't wait for LaunchedEffect)
    if (receiverUid != null && receiverEmail != null) {
        if (targetReceiverUid.value.isBlank()) {
            targetReceiverUid.value = receiverUid
            targetReceiverEmail.value = receiverEmail
        }
    }
    
    // 如果指定了接收者，显示对话详情；否则显示对话列表 / If receiver specified, show conversation detail; otherwise show conversation list
    if (receiverUid != null && receiverEmail != null) {
        ChatDetailScreen(
            onBack = onBack,
            receiverUid = receiverUid,
            receiverEmail = receiverEmail,
            itemId = itemId,
            itemTitle = itemTitle,
            vm = vm,
            messages = messages,
            uiState = uiState,
            messageText = messageText,
            currentUserUid = currentUserUid.value,
            listState = listState
        )
    } else {
        ChatListScreen(
            onBack = onBack,
            conversations = conversations,
            uiState = uiState,
            onConversationClick = onConversationClick
        )
    }
}

@Composable
fun ChatListScreen(
    onBack: () -> Unit,
    conversations: List<com.example.tradingplatform.data.chat.ChatConversation>,
    uiState: com.example.tradingplatform.ui.viewmodel.ChatUiState,
    onConversationClick: (String, String, String, String) -> Unit
) {
    val strings = LocalAppStrings.current
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // 顶部栏 / Top bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = strings.myTabMessages,
                style = MaterialTheme.typography.titleLarge
            )
            Button(onClick = onBack) {
                Text(strings.myBack)
            }
        }
        
        // 对话列表 / Conversation list / Conversation list
        if (conversations.isEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = strings.myMessagesEmptyTitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = strings.myMessagesEmptySubtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(conversations, key = { it.otherUserUid }) { conversation ->
                    ConversationCard(
                        conversation = conversation,
                        onClick = {
                            onConversationClick(
                                conversation.otherUserUid,
                                conversation.otherUserEmail,
                                conversation.itemId,
                                conversation.itemTitle
                            )
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun ConversationCard(
    conversation: com.example.tradingplatform.data.chat.ChatConversation,
    onClick: () -> Unit
) {
    val strings = LocalAppStrings.current
    val dateFormat = java.text.SimpleDateFormat("MM-dd HH:mm", java.util.Locale.getDefault())
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = conversation.otherUserEmail,
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = conversation.lastMessage,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1
                )
                if (conversation.itemTitle.isNotEmpty()) {
                    Text(
                        text = strings.myConversationAboutPrefix + conversation.itemTitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
            Text(
                text = dateFormat.format(java.util.Date(conversation.lastMessageTime)),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun ChatDetailScreen(
    onBack: () -> Unit,
    receiverUid: String,
    receiverEmail: String,
    itemId: String?,
    itemTitle: String?,
    vm: ChatViewModel,
    messages: List<ChatMessage>,
    uiState: com.example.tradingplatform.ui.viewmodel.ChatUiState,
    messageText: androidx.compose.runtime.MutableState<String>,
    currentUserUid: String?,
    listState: androidx.compose.foundation.lazy.LazyListState
) {
    val strings = LocalAppStrings.current
    // 自动滚动到底部（当消息列表更新时）/ Auto scroll to bottom (when message list updates)
    LaunchedEffect(messages.size, messages.lastOrNull()?.id) {
        if (messages.isNotEmpty()) {
            // 使用 kotlinx.coroutines.delay 确保布局完成后再滚动 / Use kotlinx.coroutines.delay to ensure layout completes before scrolling
            kotlinx.coroutines.delay(100)
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // 顶部栏 / Top bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = strings.chatDetailTitlePattern.format(receiverEmail),
                    style = MaterialTheme.typography.titleLarge
                )
                if (itemTitle != null) {
                    Text(
                        text = strings.myConversationAboutPrefix + itemTitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Button(onClick = onBack) {
                Text(strings.myBack)
            }
        }

        // 消息列表 / Message list
        if (messages.isEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = strings.chatDetailEmptyTitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = strings.chatDetailEmptySubtitlePattern.format(receiverEmail),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        } else {
            // 确保消息按时间升序排列（最早的在上，最新的在下）/ Ensure messages sorted by time ascending (oldest on top, newest at bottom)
            val sortedMessages = remember(messages) {
                messages.sortedBy { it.timestamp }
            }
            
            LazyColumn(
                state = listState,
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(sortedMessages, key = { it.id }) { message ->
                    MessageCard(message = message, currentUserUid = currentUserUid)
                }
            }
        }

        // 发送消息区域 / Send message area
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = messageText.value,
                onValueChange = { messageText.value = it },
                label = { Text(strings.chatInputLabel) },
                modifier = Modifier.weight(1f),
                enabled = uiState !is com.example.tradingplatform.ui.viewmodel.ChatUiState.Sending
            )
            Button(
                onClick = {
                    if (messageText.value.isNotBlank() && receiverUid.isNotBlank()) {
                        vm.sendMessage(
                            receiverUid = receiverUid,
                            receiverEmail = receiverEmail,
                            content = messageText.value,
                            itemId = itemId ?: "",
                            itemTitle = itemTitle ?: ""
                        )
                        messageText.value = ""
                    }
                },
                enabled = messageText.value.isNotBlank() 
                        && receiverUid.isNotBlank()
                        && uiState !is com.example.tradingplatform.ui.viewmodel.ChatUiState.Sending
            ) {
                Text(strings.chatSendButtonLabel)
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
                    MessageBubbleSelf // 自己发出的消息：红色（饱和度略高于导航栏）/ Self message: red (slightly higher saturation than nav bar)
                } else {
                    MessageBubbleOther // 对方发送的消息：灰白色（比背景白色稍暗）/ Other's message: off-white (slightly darker than white background)
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
