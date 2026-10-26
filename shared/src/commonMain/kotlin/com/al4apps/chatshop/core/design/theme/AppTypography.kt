package com.al4apps.chatshop.core.design.theme

import androidx.compose.runtime.Immutable
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

/** Типографика приложения v2. Manrope; системный шрифт — fallback для previews и тестов. */
@Immutable
data class AppTypography(
    val fontFamily: FontFamily = FontFamily.Default,
    val display: TextStyle = TextStyle(
        fontFamily = fontFamily,
        fontSize = 52.sp,
        lineHeight = 56.sp,
        fontWeight = FontWeight.Bold,
    ),
    val h1: TextStyle = TextStyle(
        fontFamily = fontFamily,
        fontSize = 32.sp,
        lineHeight = 38.sp,
        fontWeight = FontWeight.Bold,
    ),
    val h2: TextStyle = TextStyle(
        fontFamily = fontFamily,
        fontSize = 24.sp,
        lineHeight = 30.sp,
        fontWeight = FontWeight.Bold,
    ),
    val title: TextStyle = TextStyle(
        fontFamily = fontFamily,
        fontSize = 17.sp,
        lineHeight = 22.sp,
        fontWeight = FontWeight.SemiBold,
    ),
    val body: TextStyle = TextStyle(
        fontFamily = fontFamily,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        fontWeight = FontWeight.Normal,
    ),
    val caption: TextStyle = TextStyle(
        fontFamily = fontFamily,
        fontSize = 13.sp,
        lineHeight = 18.sp,
        fontWeight = FontWeight.Normal,
    ),
    val button: TextStyle = TextStyle(
        fontFamily = fontFamily,
        fontSize = 16.sp,
        lineHeight = 20.sp,
        fontWeight = FontWeight.SemiBold,
    ),
)
