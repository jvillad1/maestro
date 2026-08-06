package com.maestro.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.layout.Box
import com.maestro.app.theme.MaestroColors

/**
 * Paid/pending chip. With [onClick] it grows its touch target and acts as
 * the toggle button (phone pattern — replaces a separate text button).
 */
@Composable
fun StatusChip(paid: Boolean, onClick: (() -> Unit)? = null) {
    Box(
        modifier = Modifier
            .let { if (onClick != null) it.clip(RoundedCornerShape(4.dp)).clickable { onClick() } else it }
            .background(
                if (paid) MaestroColors.SoftGreen else Color(0xFFFDE8D8),
                RoundedCornerShape(4.dp)
            )
            .padding(horizontal = 8.dp, vertical = if (onClick != null) 6.dp else 3.dp)
    ) {
        Text(
            if (paid) "Pagado" else "Pendiente", fontSize = 10.sp,
            color = if (paid) MaestroColors.Forest else MaestroColors.Terra,
            fontWeight = FontWeight.SemiBold
        )
    }
}
