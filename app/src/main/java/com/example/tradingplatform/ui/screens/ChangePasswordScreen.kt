package com.example.tradingplatform.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.tradingplatform.ui.viewmodel.AuthViewModel
import com.example.tradingplatform.ui.viewmodel.AuthUiState
import com.example.tradingplatform.ui.i18n.AppLanguage
import com.example.tradingplatform.ui.i18n.LocalAppLanguage

@Composable
fun ChangePasswordScreen(
    onBack: () -> Unit,
    viewModel: AuthViewModel = viewModel()
) {
    val currentPassword = remember { mutableStateOf("") }
    val newPassword = remember { mutableStateOf("") }
    val confirmPassword = remember { mutableStateOf("") }
    val uiState by viewModel.state.collectAsState()
    val lang = LocalAppLanguage.current
    val isEnglish = lang == AppLanguage.EN
    
    // 当密码更改成功时，返回上一页 / Return to previous page when password change succeeds
    LaunchedEffect(uiState) {
        if (uiState is AuthUiState.Success) {
            onBack()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = if (isEnglish) "Change password" else "更改密码",
            style = MaterialTheme.typography.headlineSmall
        )

        OutlinedTextField(
            value = currentPassword.value,
            onValueChange = { currentPassword.value = it },
            label = { Text(if (isEnglish) "Current password" else "当前密码") },
            visualTransformation = PasswordVisualTransformation(),
            enabled = uiState !is AuthUiState.Loading,
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = newPassword.value,
            onValueChange = { newPassword.value = it },
            label = { Text(if (isEnglish) "New password (at least 6 characters)" else "新密码（至少6个字符）") },
            visualTransformation = PasswordVisualTransformation(),
            enabled = uiState !is AuthUiState.Loading,
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = confirmPassword.value,
            onValueChange = { confirmPassword.value = it },
            label = { Text(if (isEnglish) "Confirm new password" else "确认新密码") },
            visualTransformation = PasswordVisualTransformation(),
            enabled = uiState !is AuthUiState.Loading,
            modifier = Modifier.fillMaxWidth()
        )

        // 错误消息 / Error message
        if (uiState is AuthUiState.Error) {
            Text(
                text = (uiState as AuthUiState.Error).message,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyMedium
            )
        }

        // 成功消息 / Success message
        if (uiState is AuthUiState.Success) {
            Text(
                text = if (isEnglish) "Password changed successfully" else "密码更改成功",
                color = MaterialTheme.colorScheme.primary,
                style = MaterialTheme.typography.bodyMedium
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedButton(
                onClick = onBack,
                modifier = Modifier.weight(1f),
                enabled = uiState !is AuthUiState.Loading
            ) {
                Text(if (isEnglish) "Cancel" else "取消")
            }
            Button(
                onClick = {
                    // 验证输入 / Validate input
                    if (newPassword.value != confirmPassword.value) {
                        // 这里需要显示错误，但AuthViewModel没有提供直接设置错误的方法 / Need to show error here, but AuthViewModel doesn't provide direct error setting method
                        // 我们可以通过检查来避免调用 / We can avoid calling by checking
                        return@Button
                    }
                    viewModel.changePassword(
                        currentPassword = currentPassword.value,
                        newPassword = newPassword.value
                    )
                },
                modifier = Modifier.weight(1f),
                enabled = currentPassword.value.isNotBlank()
                        && newPassword.value.isNotBlank()
                        && confirmPassword.value.isNotBlank()
                        && newPassword.value == confirmPassword.value
                        && uiState !is AuthUiState.Loading
            ) {
                if (uiState is AuthUiState.Loading) {
                    CircularProgressIndicator(modifier = Modifier.size(16.dp))
                } else {
                    Text(if (isEnglish) "Confirm change" else "确认更改")
                }
            }
        }
        
        // 如果新密码和确认密码不匹配，显示提示 / If new password and confirm password don't match, show hint
        if (newPassword.value.isNotBlank() 
            && confirmPassword.value.isNotBlank() 
            && newPassword.value != confirmPassword.value) {
            Text(
                text = if (isEnglish) "New password and confirmation do not match" else "新密码和确认密码不匹配",
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}


