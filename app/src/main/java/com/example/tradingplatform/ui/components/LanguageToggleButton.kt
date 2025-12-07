package com.example.tradingplatform.ui.components

import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.tradingplatform.ui.i18n.AppLanguage
import com.example.tradingplatform.ui.i18n.LocalAppLanguageState
import com.example.tradingplatform.ui.i18n.LocalAppStrings

/**
 * Simple button for switching app language between Chinese and English.
 * It toggles the global language state provided by CompositionLocals.
 */
@Composable
fun LanguageToggleButton(
    modifier: Modifier = Modifier
) {
    val languageState = LocalAppLanguageState.current
    val strings = LocalAppStrings.current

    TextButton(
        onClick = {
            val current = languageState.value
            languageState.value = if (current == AppLanguage.ZH) AppLanguage.EN else AppLanguage.ZH
        },
        modifier = modifier
    ) {
        Text(text = strings.languageToggleLabel)
    }
}
