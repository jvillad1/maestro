package com.maestro.app.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import maestro.composeapp.generated.resources.Res
import maestro.composeapp.generated.resources.fraunces_italic
import maestro.composeapp.generated.resources.fraunces_regular
import maestro.composeapp.generated.resources.fraunces_semibold
import org.jetbrains.compose.resources.Font

object MaestroColors {
    val Cream = Color(0xFFF5F0E8)
    val Espresso = Color(0xFF2C1810)
    val Gold = Color(0xFFC9A84C)
    val Forest = Color(0xFF2D5016)
    val Terra = Color(0xFFB85C38)
    val White = Color(0xFFFDFAF5)
    val Muted = Color(0xFF8B7355)
    val LightGold = Color(0xFFF0E6C8)
    val SoftGreen = Color(0xFFE8F0E0)
    val Paper = Color(0xFFFAF6EE)
    val Ink = Color(0xFF1F0F02)
    val EspressoHi = Color(0xFF52280A)
}

@Composable
fun frauncesFamily() = FontFamily(
    Font(Res.font.fraunces_regular, FontWeight.Normal),
    Font(Res.font.fraunces_italic, FontWeight.Normal, FontStyle.Italic),
    Font(Res.font.fraunces_semibold, FontWeight.SemiBold),
)

@Composable
fun MaestroTheme(content: @Composable () -> Unit) {
    val fraunces = frauncesFamily()
    val typography = Typography(
        displayLarge  = TextStyle(fontFamily = fraunces, fontWeight = FontWeight.SemiBold, fontSize = 57.sp),
        displayMedium = TextStyle(fontFamily = fraunces, fontWeight = FontWeight.Normal,   fontSize = 45.sp),
        displaySmall  = TextStyle(fontFamily = fraunces, fontWeight = FontWeight.Normal,   fontSize = 36.sp),
        headlineLarge = TextStyle(fontFamily = fraunces, fontWeight = FontWeight.SemiBold, fontSize = 32.sp),
        headlineMedium= TextStyle(fontFamily = fraunces, fontWeight = FontWeight.SemiBold, fontSize = 28.sp),
        headlineSmall = TextStyle(fontFamily = fraunces, fontWeight = FontWeight.Normal,   fontSize = 24.sp),
        titleLarge    = TextStyle(fontFamily = fraunces, fontWeight = FontWeight.SemiBold, fontSize = 22.sp),
        titleMedium   = TextStyle(fontFamily = fraunces, fontWeight = FontWeight.SemiBold, fontSize = 16.sp),
        titleSmall    = TextStyle(fontFamily = fraunces, fontWeight = FontWeight.Normal,   fontSize = 14.sp),
    )
    MaterialTheme(typography = typography, content = content)
}
