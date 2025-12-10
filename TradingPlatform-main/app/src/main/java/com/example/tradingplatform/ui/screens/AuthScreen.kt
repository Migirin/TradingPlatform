package com.example.tradingplatform.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.tradingplatform.data.auth.AuthRepository
import com.example.tradingplatform.ui.components.LanguageToggleButton
import com.example.tradingplatform.ui.i18n.LocalAppStrings
import com.example.tradingplatform.ui.viewmodel.AuthUiState
import com.example.tradingplatform.ui.viewmodel.AuthViewModel

enum class AuthMode {
    LOGIN, REGISTER, VERIFY_EMAIL
}

@Composable
fun AuthScreen(onAuthenticated: () -> Unit) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val authRepo = remember { AuthRepository(context) }
    val strings = LocalAppStrings.current
    
    val emailState = remember { mutableStateOf("") }
    val passwordState = remember { mutableStateOf("") }
    val verificationCodeState = remember { mutableStateOf("") }
    val authMode = remember { mutableStateOf(AuthMode.LOGIN) }
    val vm: AuthViewModel = viewModel()
    val uiState by vm.state.collectAsState()
    
    // 在启动时加载保存的邮箱和密码 / Load saved email and password on startup
    LaunchedEffect(Unit) {
        val savedEmail = authRepo.getSavedEmail()
        val savedPassword = authRepo.getSavedPassword()
        if (savedEmail != null) {
            emailState.value = savedEmail
        }
        if (savedPassword != null) {
            passwordState.value = savedPassword
        }
    }

    // 当登录成功时触发导航 / Trigger navigation when login succeeds
    LaunchedEffect(uiState) {
        when (uiState) {
            is AuthUiState.Success -> {
                onAuthenticated()
            }
            is AuthUiState.EmailVerified -> {
                // 邮箱验证成功后，自动切换到登录模式 / After email verification succeeds, automatically switch to login mode
                authMode.value = AuthMode.LOGIN
            }
            is AuthUiState.VerificationEmailSent -> {
                // 验证邮件已发送，切换到验证界面 / Verification email sent, switch to verification screen
                authMode.value = AuthMode.VERIFY_EMAIL
            }
            else -> {}
        }
    }

    BoxWithConstraints(
        modifier = Modifier.fillMaxSize()
    ) {
        // 淡红色到白色的渐变背景（从左上角到右下角）/ Light red to white gradient background (from top-left to bottom-right)
        val gradientBrush = remember(maxWidth, maxHeight) {
            Brush.linearGradient(
                colors = listOf(
                    Color(0xFFE57373), // 淡红色（降低饱和度）/ Light red (reduced saturation)
                    Color(0xFFFFFFFF)  // 白色 / White
                ),
                start = androidx.compose.ui.geometry.Offset(0f, 0f), // 左上角 / Top-left corner
                end = androidx.compose.ui.geometry.Offset(maxWidth.value, maxHeight.value) // 右下角 / Bottom-right corner
            )
        }
        
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(gradientBrush)
        ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(PaddingValues(top = 80.dp, start = 24.dp, end = 24.dp, bottom = 24.dp)),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = when (authMode.value) {
                        AuthMode.LOGIN -> strings.authLoginTitle
                        AuthMode.REGISTER -> strings.authRegisterTitle
                        AuthMode.VERIFY_EMAIL -> strings.authVerifyEmailTitle
                    },
                    style = MaterialTheme.typography.headlineSmall,
                    color = Color.Black
                )
                LanguageToggleButton()
            }

            when (authMode.value) {
                AuthMode.LOGIN -> {
                    LoginForm(
                        emailState = emailState,
                        passwordState = passwordState,
                        uiState = uiState,
                        onLogin = { vm.login(emailState.value.trim(), passwordState.value) },
                        onSwitchToRegister = { authMode.value = AuthMode.REGISTER }
                    )
                }
                AuthMode.REGISTER -> {
                    RegisterForm(
                        emailState = emailState,
                        passwordState = passwordState,
                        uiState = uiState,
                        onRegister = { vm.register(emailState.value.trim(), passwordState.value) },
                        onSwitchToLogin = { authMode.value = AuthMode.LOGIN },
                        onDeleteUser = { email ->
                            vm.deleteUser(email)
                            // 删除成功后，清空密码字段，用户可以重新注册 / After successful deletion, clear password field so user can re-register
                            passwordState.value = ""
                        }
                    )
                }
                AuthMode.VERIFY_EMAIL -> {
                    VerifyEmailForm(
                        emailState = emailState,
                        verificationCodeState = verificationCodeState,
                        uiState = uiState,
                        onVerify = { vm.verifyEmail(emailState.value.trim(), verificationCodeState.value.trim()) },
                        onResend = { vm.resendVerificationEmail(emailState.value.trim()) }
                    )
                }
            }
        }
        }
    }
}

@Composable
fun LoginForm(
    emailState: androidx.compose.runtime.MutableState<String>,
    passwordState: androidx.compose.runtime.MutableState<String>,
    uiState: AuthUiState,
    onLogin: () -> Unit,
    onSwitchToRegister: () -> Unit
) {
    val strings = LocalAppStrings.current
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        OutlinedTextField(
            value = emailState.value,
            onValueChange = { emailState.value = it },
            label = { Text(strings.authEmailLabel) },
            enabled = uiState !is AuthUiState.Loading,
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color.Black,
                unfocusedBorderColor = Color.Black,
                focusedLabelColor = Color.Black,
                unfocusedLabelColor = Color.Black,
                focusedTextColor = Color.Black,
                unfocusedTextColor = Color.Black
            )
        )
        OutlinedTextField(
            value = passwordState.value,
            onValueChange = { passwordState.value = it },
            label = { Text(strings.authPasswordLabel) },
            visualTransformation = PasswordVisualTransformation(),
            enabled = uiState !is AuthUiState.Loading,
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color.Black,
                unfocusedBorderColor = Color.Black,
                focusedLabelColor = Color.Black,
                unfocusedLabelColor = Color.Black,
                focusedTextColor = Color.Black,
                unfocusedTextColor = Color.Black
            )
        )
        // 注册链接 - 小字，放在密码框下方 / Register link - small text, placed below password field
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            Text(
                text = strings.authRegisterTab,
                color = Color(0xFF1976D2), // 深蓝色
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier
                    .clickable(enabled = uiState !is AuthUiState.Loading) {
                        onSwitchToRegister()
                    }
                    .padding(horizontal = 4.dp, vertical = 4.dp)
            )
        }
        Button(
            onClick = onLogin,
            enabled = emailState.value.trim().endsWith("@ucdconnect.ie", ignoreCase = true)
                    && passwordState.value.isNotBlank()
                    && uiState !is AuthUiState.Loading,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFFE57373),
                contentColor = Color.White,
                disabledContainerColor = Color.Gray,
                disabledContentColor = Color.White
            )
        ) {
            if (uiState is AuthUiState.Loading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(16.dp),
                    color = Color.White
                )
            } else {
                Text(strings.authLoginButton)
            }
        }
        // 测试账号信息备注 / Test account information note
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = "测试账号：",
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFF666666),
                modifier = Modifier.padding(vertical = 4.dp)
            )
            Text(
                text = "账号1: testuser1@ucdconnect.ie / test123",
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFF666666)
            )
            Text(
                text = "账号2: testuser2@ucdconnect.ie / test456",
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFF666666)
            )
        }
        ErrorMessage(uiState = uiState)
    }
}

@Composable
fun RegisterForm(
    emailState: androidx.compose.runtime.MutableState<String>,
    passwordState: androidx.compose.runtime.MutableState<String>,
    uiState: AuthUiState,
    onRegister: () -> Unit,
    onSwitchToLogin: () -> Unit,
    onDeleteUser: ((String) -> Unit)? = null
) {
    val strings = LocalAppStrings.current
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        OutlinedTextField(
            value = emailState.value,
            onValueChange = { emailState.value = it },
            label = { Text(strings.authEmailLabel) },
            enabled = uiState !is AuthUiState.Loading,
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color.Black,
                unfocusedBorderColor = Color.Black,
                focusedLabelColor = Color.Black,
                unfocusedLabelColor = Color.Black,
                focusedTextColor = Color.Black,
                unfocusedTextColor = Color.Black
            )
        )
        OutlinedTextField(
            value = passwordState.value,
            onValueChange = { passwordState.value = it },
            label = { Text(strings.authRegisterPasswordLabel) },
            visualTransformation = PasswordVisualTransformation(),
            enabled = uiState !is AuthUiState.Loading,
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color.Black,
                unfocusedBorderColor = Color.Black,
                focusedLabelColor = Color.Black,
                unfocusedLabelColor = Color.Black,
                focusedTextColor = Color.Black,
                unfocusedTextColor = Color.Black
            )
        )
        // 返回登录链接 - 小字，放在密码框下方 / Back to login link - small text, placed below password field
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            Text(
                text = strings.authLoginTab,
                color = Color(0xFF1976D2), // 深蓝色
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier
                    .clickable(enabled = uiState !is AuthUiState.Loading) {
                        onSwitchToLogin()
                    }
                    .padding(horizontal = 4.dp, vertical = 4.dp)
            )
        }
        Button(
            onClick = onRegister,
            enabled = emailState.value.trim().endsWith("@ucdconnect.ie", ignoreCase = true)
                    && passwordState.value.length >= 6
                    && uiState !is AuthUiState.Loading,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFFE57373),
                contentColor = Color.White,
                disabledContainerColor = Color.Gray,
                disabledContentColor = Color.White
            )
        ) {
            if (uiState is AuthUiState.Loading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(16.dp),
                    color = Color.White
                )
            } else {
                Text(strings.authRegisterButton)
            }
        }
        ErrorMessage(
            uiState = uiState,
            email = emailState.value.trim(),
            onDeleteUser = onDeleteUser
        )
    }
}

@Composable
fun VerifyEmailForm(
    emailState: androidx.compose.runtime.MutableState<String>,
    verificationCodeState: androidx.compose.runtime.MutableState<String>,
    uiState: AuthUiState,
    onVerify: () -> Unit,
    onResend: () -> Unit
) {
    val strings = LocalAppStrings.current
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        // 显示验证邮件已发送的信息 / Display verification email sent information
        if (uiState is AuthUiState.VerificationEmailSent) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = strings.authVerificationSentPrefix + uiState.email,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Black
                )
                Text(
                    text = strings.authVerificationHintCheckEmail,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Black
                )
                Text(
                    text = strings.authVerificationHintExpire,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Black
                )
            }
        }

        OutlinedTextField(
            value = emailState.value,
            onValueChange = { emailState.value = it },
            label = { Text(strings.authEmailReadonlyLabel) },
            enabled = false, // 验证时邮箱不可修改 / Email cannot be modified during verification
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color(0xFFE57373),
                unfocusedBorderColor = Color.White,
                focusedLabelColor = Color(0xFFE57373),
                unfocusedLabelColor = Color.White,
                focusedTextColor = Color.Black,
                unfocusedTextColor = Color.Black
            )
        )
        OutlinedTextField(
            value = verificationCodeState.value,
            onValueChange = { verificationCodeState.value = it },
            label = { Text(strings.authVerificationCodeLabel) },
            enabled = uiState !is AuthUiState.Loading,
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color(0xFFE57373),
                unfocusedBorderColor = Color.White,
                focusedLabelColor = Color(0xFFE57373),
                unfocusedLabelColor = Color.White,
                focusedTextColor = Color.Black,
                unfocusedTextColor = Color.Black
            )
        )
        Button(
            onClick = onVerify,
            enabled = verificationCodeState.value.isNotBlank()
                    && uiState !is AuthUiState.Loading,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFFE57373),
                contentColor = Color.White,
                disabledContainerColor = Color.Gray,
                disabledContentColor = Color.White
            )
        ) {
            if (uiState is AuthUiState.Loading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(16.dp),
                    color = Color.White
                )
            } else {
                Text(strings.authVerifyButton)
            }
        }
        OutlinedButton(
            onClick = onResend,
            enabled = uiState !is AuthUiState.Loading,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = Color.Black
            )
        ) {
            Text(strings.authResendCodeButton)
        }
        ErrorMessage(uiState = uiState)
    }
}

@Composable
fun ErrorMessage(
    uiState: AuthUiState,
    email: String = "",
    onDeleteUser: ((String) -> Unit)? = null
) {
    val strings = LocalAppStrings.current
    if (uiState is AuthUiState.Error) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                text = uiState.message,
                color = Color(0xFFFF6B6B), // 浅红色用于错误信息 / Light red for error message
                style = MaterialTheme.typography.bodyMedium
            )
            // 如果错误是"已注册"，提供删除选项 / If error is "already registered", provide delete option
            if (uiState.message.contains("已注册") && email.isNotBlank() && onDeleteUser != null) {
                OutlinedButton(
                    onClick = { onDeleteUser(email) },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = uiState !is AuthUiState.Loading,
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = Color.Black
                    )
                ) {
                    Text(strings.authDeleteAndReregisterButton)
                }
            }
        }
    }
}
