package com.example.tradingplatform.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.*
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.tradingplatform.ui.theme.AvatarBackgroundColor
import com.example.tradingplatform.ui.theme.ItemCardColor
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.rememberAsyncImagePainter
import com.example.tradingplatform.data.auth.AuthRepository
import com.example.tradingplatform.data.items.Item
import com.example.tradingplatform.data.wishlist.WishlistItem
import com.example.tradingplatform.data.chat.ChatConversation
import com.example.tradingplatform.data.timetable.TimetableRepository
import com.example.tradingplatform.data.local.TimetableCourseEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import com.example.tradingplatform.ui.components.LanguageToggleButton
import com.example.tradingplatform.ui.i18n.LocalAppStrings
import com.example.tradingplatform.ui.viewmodel.ItemViewModel
import com.example.tradingplatform.ui.viewmodel.WishlistViewModel
import com.example.tradingplatform.ui.viewmodel.ChatViewModel
import java.text.SimpleDateFormat
import java.util.Locale
import kotlinx.coroutines.delay

enum class MyScreenTab {
    MY_SOLD,    // 我出售的 / My sold items
    MY_BOUGHT,  // 我买到的 / My bought items
    WISHLIST,   // 我的愿望清单 / My wishlist
    TIMETABLE,  // 我的课表 / My timetable
    MY_MESSAGES, // 我的消息 / My messages
    SETTINGS    // 设置 / Settings
}

@Composable
fun MyScreen(
    onBack: () -> Unit = {},
    onItemClick: (Item) -> Unit,
    onExchangeMatch: () -> Unit = {},
    onWishlistItemMatch: (String) -> Unit = {},
    onChangePassword: () -> Unit = {},
    onChatWithUser: (String, String, String, String) -> Unit = { _, _, _, _ -> },
    initialTab: MyScreenTab = MyScreenTab.MY_SOLD,
    itemViewModel: ItemViewModel = viewModel(),
    wishlistViewModel: WishlistViewModel = viewModel()
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val authRepo = remember { AuthRepository(context) }
    val strings = LocalAppStrings.current
    
    var currentUid by remember { mutableStateOf<String?>(null) }
    var currentEmail by remember { mutableStateOf<String?>(null) }
    var avatarUri by remember { mutableStateOf<String?>(null) }
    
    LaunchedEffect(Unit) {
        currentUid = authRepo.getCurrentUserUid()
        currentEmail = authRepo.getCurrentUserEmail()
        avatarUri = authRepo.getAvatarUri()
        // 刷新商品列表，确保从 Supabase 获取最新数据 / Refresh item list, ensure latest data from Supabase
        itemViewModel.loadItems()
    }
    
    // 图片选择器 / Image picker
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            val uriString = it.toString()
            authRepo.saveAvatarUri(uriString)
            avatarUri = uriString
        }
    }
    
    val items by itemViewModel.items.collectAsState()
    val wishlist by wishlistViewModel.wishlist.collectAsState()
    
    // 我出售的商品 - 同时匹配 ownerUid 和 ownerEmail / My sold items - match both ownerUid and ownerEmail
    val mySoldItems = remember(items, currentUid, currentEmail) {
        items.filter { item ->
            // 优先匹配 ownerUid，如果没有则匹配 ownerEmail / Prioritize matching ownerUid, if not then match ownerEmail
            val uidMatch = currentUid?.let { item.ownerUid == it } ?: false
            val emailMatch = currentEmail?.let { 
                item.ownerEmail.equals(it, ignoreCase = true) 
            } ?: false
            uidMatch || emailMatch
        }
    }
    
    // 我买到的商品（暂时为空，需要购买记录系统）/ My bought items (empty for now, needs purchase record system)
    val myBoughtItems = remember {
        emptyList<Item>()
    }
    
    var selectedTab by remember { mutableStateOf(initialTab) }
    
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // 顶部用户信息（无卡片，直接显示）/ Top user information (no card, direct display)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 头像和文字 / Avatar and text
            Row(
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 头像框（可点击上传）/ Avatar frame (clickable to upload)
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                        .border(2.dp, MaterialTheme.colorScheme.primary, CircleShape)
                        .background(AvatarBackgroundColor)
                        .clickable {
                            imagePickerLauncher.launch("image/*")
                        },
                    contentAlignment = Alignment.Center
                ) {
                    if (avatarUri != null) {
                        // 显示上传的头像 / Display uploaded avatar
                        Image(
                            painter = rememberAsyncImagePainter(Uri.parse(avatarUri)),
                            contentDescription = "头像",
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(CircleShape),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        // 显示默认图标 / Display default icon
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = "头像",
                            modifier = Modifier.size(40.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
                // 文字信息 / Text information
                Column(
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = strings.myTitle,
                        style = MaterialTheme.typography.headlineMedium
                    )
                    Text(
                        text = currentEmail ?: strings.myNotLoggedIn,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            // 语言切换按钮 / Language toggle button
            LanguageToggleButton()
        }
        
        // 标签页选择 / Tab selection
        TabRow(selectedTabIndex = when (selectedTab) {
            MyScreenTab.MY_SOLD -> 0
            MyScreenTab.MY_BOUGHT -> 1
            MyScreenTab.WISHLIST -> 2
            MyScreenTab.TIMETABLE -> 3
            MyScreenTab.SETTINGS -> 4
            MyScreenTab.MY_MESSAGES -> 0 // 不应该出现，但为了安全起见 / Should not appear, but for safety
        }) {
            Tab(
                selected = selectedTab == MyScreenTab.MY_SOLD,
                onClick = { selectedTab = MyScreenTab.MY_SOLD },
                text = { Text("${strings.myTabSold} (${mySoldItems.size})") }
            )
            Tab(
                selected = selectedTab == MyScreenTab.MY_BOUGHT,
                onClick = { selectedTab = MyScreenTab.MY_BOUGHT },
                text = { Text("${strings.myTabBought} (${myBoughtItems.size})") }
            )
            Tab(
                selected = selectedTab == MyScreenTab.WISHLIST,
                onClick = { selectedTab = MyScreenTab.WISHLIST },
                text = { Text("${strings.myTabWishlist} (${wishlist.size})") }
            )
            Tab(
                selected = selectedTab == MyScreenTab.TIMETABLE,
                onClick = { selectedTab = MyScreenTab.TIMETABLE },
                text = { Text(strings.myTabTimetable) }
            )
            Tab(
                selected = selectedTab == MyScreenTab.SETTINGS,
                onClick = { selectedTab = MyScreenTab.SETTINGS },
                text = { Text(strings.myTabSettings) }
            )
        }
        
        // 内容区域 / Content area
        when (selectedTab) {
            MyScreenTab.MY_SOLD -> {
                MySoldItemsTab(
                    items = mySoldItems,
                    onItemClick = onItemClick,
                    onExchangeMatch = onExchangeMatch
                )
            }
            MyScreenTab.MY_BOUGHT -> {
                MyBoughtItemsTab(
                    items = myBoughtItems
                )
            }
            MyScreenTab.WISHLIST -> {
                MyWishlistTab(
                    wishlist = wishlist,
                    onItemMatch = onWishlistItemMatch,
                    onDelete = { wishlistViewModel.deleteWishlistItem(it) }
                )
            }
            MyScreenTab.TIMETABLE -> {
                MyTimetableTab()
            }
            MyScreenTab.MY_MESSAGES -> {
                // 我的消息功能已移动到底部导航栏，这里不再显示 / My messages feature moved to bottom navigation bar, no longer displayed here
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = strings.myTabMessages,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
            MyScreenTab.SETTINGS -> {
                MySettingsTab(
                    onChangePassword = onChangePassword
                )
            }
        }
    }
}

@Composable
fun MySettingsTab(
    onChangePassword: () -> Unit
) {
    val strings = LocalAppStrings.current
    val context = androidx.compose.ui.platform.LocalContext.current
    val authRepo = remember { AuthRepository(context) }
    var studentId by remember {
        mutableStateOf(authRepo.getCurrentStudentId() ?: "")
    }
    var simulatedMonthText by remember {
        mutableStateOf(authRepo.getSimulatedMonth()?.toString() ?: "")
    }
    var studentIdSaved by remember { mutableStateOf(false) }
    var simulatedMonthApplied by remember { mutableStateOf(false) }

    LaunchedEffect(studentIdSaved) {
        if (studentIdSaved) {
            delay(3000)
            studentIdSaved = false
        }
    }

    LaunchedEffect(simulatedMonthApplied) {
        if (simulatedMonthApplied) {
            delay(3000)
            simulatedMonthApplied = false
        }
    }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        OutlinedTextField(
            value = studentId,
            onValueChange = { input ->
                val digitsOnly = input.filter { it.isDigit() }
                studentId = digitsOnly.take(8)
                studentIdSaved = false
            },
            label = { Text(strings.settingsStudentIdLabel) },
            modifier = Modifier.fillMaxWidth()
        )
        Button(
            onClick = {
                authRepo.setCurrentStudentId(studentId)
                studentIdSaved = true
            },
            enabled = studentId.length == 8,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(strings.settingsStudentIdSaveButton)
        }
        if (studentIdSaved) {
            Text(
                text = strings.settingsStudentIdSavedMessage,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.primary
            )
        }
        Button(
            onClick = onChangePassword,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(strings.myChangePasswordButton)
        }
        Divider(modifier = Modifier.padding(top = 16.dp))
        Text(
            text = strings.settingsDevSectionTitle,
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(top = 8.dp)
        )
        OutlinedTextField(
            value = simulatedMonthText,
            onValueChange = { input ->
                val digitsOnly = input.filter { it.isDigit() }
                simulatedMonthText = digitsOnly.take(2)
                simulatedMonthApplied = false
            },
            label = { Text(strings.settingsDevSimulatedMonthLabel) },
            modifier = Modifier.fillMaxWidth()
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedButton(
                onClick = {
                    simulatedMonthText = ""
                    authRepo.setSimulatedMonth(null)
                    simulatedMonthApplied = true
                },
                modifier = Modifier.weight(1f)
            ) {
                Text(strings.settingsDevSimulatedMonthFollowSystem)
            }
            Button(
                onClick = {
                    val month = simulatedMonthText.toIntOrNull()
                    authRepo.setSimulatedMonth(month)
                    simulatedMonthApplied = true
                },
                enabled = simulatedMonthText.isBlank() || simulatedMonthText.toIntOrNull() in 1..12,
                modifier = Modifier.weight(1f)
            ) {
                Text(strings.settingsDevSimulatedMonthApplyButton)
            }
        }
        if (simulatedMonthApplied) {
            Text(
                text = strings.settingsDevSimulatedMonthAppliedMessage,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
fun MySoldItemsTab(
    items: List<Item>,
    onItemClick: (Item) -> Unit,
    onExchangeMatch: () -> Unit
) {
    val strings = LocalAppStrings.current
    if (items.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = strings.mySoldEmptyTitle,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = strings.mySoldEmptySubtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    } else {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // 交换匹配按钮 / Exchange match button
            Button(
                onClick = onExchangeMatch,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                enabled = items.isNotEmpty()
            ) {
                Text(strings.myExchangeMatchButton)
            }
            
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(items, key = { it.id }) { item ->
                    ItemCard(
                        item = item,
                        onClick = { onItemClick(item) }
                    )
                }
            }
        }
    }
}

@Composable
fun MyBoughtItemsTab(
    items: List<Item>
) {
    val strings = LocalAppStrings.current
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = strings.myBoughtEmptyTitle,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = strings.myBoughtEmptySubtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun MyWishlistTab(
    wishlist: List<WishlistItem>,
    onItemMatch: (String) -> Unit,
    onDelete: (String) -> Unit
) {
    val strings = LocalAppStrings.current
    if (wishlist.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = strings.myWishlistEmptyTitle,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = strings.myWishlistEmptySubtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(wishlist, key = { it.id }) { item ->
                WishlistItemCard(
                    item = item,
                    onDelete = { onDelete(item.id) },
                    onFindMatches = { onItemMatch(item.id) }
                )
            }
        }
    }
}

@Composable
fun MyMessagesTab(
    onChatWithUser: (String, String, String, String) -> Unit,
    chatViewModel: ChatViewModel = viewModel()
) {
    val strings = LocalAppStrings.current
    val conversations by chatViewModel.conversations.collectAsState()
    
    LaunchedEffect(Unit) {
        chatViewModel.loadConversations()
    }
    
    // 对话列表（不显示顶部栏，因为已经在标签页中）/ Conversation list (no top bar, already in tab)
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
                MyConversationCard(
                    conversation = conversation,
                    onClick = {
                        onChatWithUser(
                            conversation.otherUserUid,
                            conversation.otherUserEmail,
                            conversation.itemId ?: "",
                            conversation.itemTitle ?: ""
                        )
                    }
                )
            }
        }
    }
}

@Composable
fun MyTimetableTab() {
    val strings = LocalAppStrings.current
    val context = LocalContext.current
    val authRepo = remember { AuthRepository(context) }
    val timetableRepo = remember { TimetableRepository(context) }
    
    var courses by remember { mutableStateOf<List<TimetableCourseEntity>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var studentId by remember { mutableStateOf<String?>(null) }
    
    LaunchedEffect(Unit) {
        studentId = authRepo.getCurrentStudentId()
        if (studentId != null && studentId!!.length == 8) {
            isLoading = true
            errorMessage = null
            try {
                courses = withContext(Dispatchers.IO) {
                    timetableRepo.getCoursesForStudent(studentId!!)
                }
            } catch (e: Exception) {
                errorMessage = e.message ?: "加载课表失败"
            } finally {
                isLoading = false
            }
        } else {
            errorMessage = "请先在设置中填写学号"
            isLoading = false
        }
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        if (isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else if (errorMessage != null) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = errorMessage!!,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        } else if (courses.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "暂无课程",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "请确认学号正确且已初始化课表数据",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            Text(
                text = "本学期课程 (${courses.size}门)",
                style = MaterialTheme.typography.titleMedium
            )
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(courses, key = { it.courseCode }) { course ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = ItemCardColor // 使用与商品卡片相同的颜色 / Use same color as item card
                        )
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = course.courseNameCn,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            if (course.courseNameEn.isNotEmpty()) {
                                Text(
                                    text = course.courseNameEn,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = "课程代码: ${course.courseCode}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = "•",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = "专业: ${course.major}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = "•",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = "年级: ${course.gradeLevel}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MyConversationCard(
    conversation: ChatConversation,
    onClick: () -> Unit
) {
    val strings = LocalAppStrings.current
    val dateFormat = SimpleDateFormat("MM-dd HH:mm", Locale.getDefault())
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = conversation.otherUserEmail,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                if (conversation.itemTitle != null && conversation.itemTitle!!.isNotEmpty()) {
                    Text(
                        text = strings.myConversationAboutPrefix + conversation.itemTitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
                if (conversation.lastMessage != null && conversation.lastMessage!!.isNotEmpty()) {
                    Text(
                        text = conversation.lastMessage!!.take(50) + if (conversation.lastMessage!!.length > 50) "..." else "",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
            if (conversation.lastMessageTime != null) {
                Text(
                    text = dateFormat.format(java.util.Date(conversation.lastMessageTime)),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
