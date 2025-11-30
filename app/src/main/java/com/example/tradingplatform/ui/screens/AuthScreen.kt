package com.example.tradingplatform.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
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
    
    // 在启动时加载保存的邮箱和密码
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

    // 当登录成功时触发导航
    LaunchedEffect(uiState) {
        when (uiState) {
            is AuthUiState.Success -> {
                onAuthenticated()
            }
            is AuthUiState.EmailVerified -> {
                // 邮箱验证成功后，自动切换到登录模式
                authMode.value = AuthMode.LOGIN
            }
            is AuthUiState.VerificationEmailSent -> {
                // 验证邮件已发送，切换到验证界面
                authMode.value = AuthMode.VERIFY_EMAIL
            }
            else -> {}
        }
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(PaddingValues(24.dp)),
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
                style = MaterialTheme.typography.headlineSmall
            )
            LanguageToggleButton()
        }

        // 模式切换按钮
        if (uiState !is AuthUiState.Loading) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = { authMode.value = AuthMode.LOGIN },
                    modifier = Modifier.weight(1f),
                    enabled = authMode.value != AuthMode.LOGIN
                ) {
                    Text(strings.authLoginTab)
                }
                OutlinedButton(
                    onClick = { authMode.value = AuthMode.REGISTER },
                    modifier = Modifier.weight(1f),
                    enabled = authMode.value != AuthMode.REGISTER
                ) {
                    Text(strings.authRegisterTab)
                }
            }
        }

        when (authMode.value) {
            AuthMode.LOGIN -> {
                LoginForm(
                    emailState = emailState,
                    passwordState = passwordState,
                    uiState = uiState,
                    onLogin = { vm.login(emailState.value.trim(), passwordState.value) }
                )
            }
            AuthMode.REGISTER -> {
                RegisterForm(
                    emailState = emailState,
                    passwordState = passwordState,
                    uiState = uiState,
                    onRegister = { vm.register(emailState.value.trim(), passwordState.value) },
                    onDeleteUser = { email ->
                        vm.deleteUser(email)
                        // 删除成功后，清空密码字段，用户可以重新注册
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

        // 开发模式：临时跳过认证
        Divider(modifier = Modifier.padding(vertical = 16.dp))
        Text(
            text = strings.authDevModeTitle,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        OutlinedButton(
            onClick = { onAuthenticated() },
            modifier = Modifier.fillMaxWidth(),
            enabled = uiState !is AuthUiState.Loading
        ) {
            Text(strings.authDevSkipButton)
        }
    }
}

@Composable
fun LoginForm(
    emailState: androidx.compose.runtime.MutableState<String>,
    passwordState: androidx.compose.runtime.MutableState<String>,
    uiState: AuthUiState,
    onLogin: () -> Unit
) {
    val strings = LocalAppStrings.current
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        OutlinedTextField(
            value = emailState.value,
            onValueChange = { emailState.value = it },
            label = { Text(strings.authEmailLabel) },
            enabled = uiState !is AuthUiState.Loading,
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = passwordState.value,
            onValueChange = { passwordState.value = it },
            label = { Text(strings.authPasswordLabel) },
            visualTransformation = PasswordVisualTransformation(),
            enabled = uiState !is AuthUiState.Loading,
            modifier = Modifier.fillMaxWidth()
        )
        Button(
            onClick = onLogin,
            enabled = emailState.value.trim().endsWith("@ucdconnect.ie", ignoreCase = true)
                    && passwordState.value.isNotBlank()
                    && uiState !is AuthUiState.Loading,
            modifier = Modifier.fillMaxWidth()
        ) {
            if (uiState is AuthUiState.Loading) {
                CircularProgressIndicator(modifier = Modifier.size(16.dp))
            } else {
                Text(strings.authLoginButton)
            }
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
    onDeleteUser: ((String) -> Unit)? = null
) {
    val strings = LocalAppStrings.current
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        OutlinedTextField(
            value = emailState.value,
            onValueChange = { emailState.value = it },
            label = { Text("校内邮箱 @ucdconnect.ie") },
            enabled = uiState !is AuthUiState.Loading,
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = passwordState.value,
            onValueChange = { passwordState.value = it },
            label = { Text(strings.authRegisterPasswordLabel) },
            visualTransformation = PasswordVisualTransformation(),
            enabled = uiState !is AuthUiState.Loading,
            modifier = Modifier.fillMaxWidth()
        )
        Button(
            onClick = onRegister,
            enabled = emailState.value.trim().endsWith("@ucdconnect.ie", ignoreCase = true)
                    && passwordState.value.length >= 6
                    && uiState !is AuthUiState.Loading,
            modifier = Modifier.fillMaxWidth()
        ) {
            if (uiState is AuthUiState.Loading) {
                CircularProgressIndicator(modifier = Modifier.size(16.dp))
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
        // 显示验证邮件已发送的信息
        if (uiState is AuthUiState.VerificationEmailSent) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = strings.authVerificationSentPrefix + uiState.email,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = strings.authVerificationHintCheckEmail,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = strings.authVerificationHintExpire,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        OutlinedTextField(
            value = emailState.value,
            onValueChange = { emailState.value = it },
            label = { Text(strings.authEmailReadonlyLabel) },
            enabled = false, // 验证时邮箱不可修改
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = verificationCodeState.value,
            onValueChange = { verificationCodeState.value = it },
            label = { Text(strings.authVerificationCodeLabel) },
            enabled = uiState !is AuthUiState.Loading,
            modifier = Modifier.fillMaxWidth()
        )
        Button(
            onClick = onVerify,
            enabled = verificationCodeState.value.isNotBlank()
                    && uiState !is AuthUiState.Loading,
            modifier = Modifier.fillMaxWidth()
        ) {
            if (uiState is AuthUiState.Loading) {
                CircularProgressIndicator(modifier = Modifier.size(16.dp))
            } else {
                Text(strings.authVerifyButton)
            }
        }
        OutlinedButton(
            onClick = onResend,
            enabled = uiState !is AuthUiState.Loading,
            modifier = Modifier.fillMaxWidth()
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
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyMedium
            )
            // 如果错误是"已注册"，提供删除选项
            if (uiState.message.contains("已注册") && email.isNotBlank() && onDeleteUser != null) {
                OutlinedButton(
                    onClick = { onDeleteUser(email) },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = uiState !is AuthUiState.Loading
                ) {
                    Text(strings.authDeleteAndReregisterButton)
                }
            }
        }
    }
}
