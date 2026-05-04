package com.maestro.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.maestro.app.theme.MaestroColors
import com.maestro.app.theme.frauncesFamily

@Composable
fun EmptyState(
    icon: ImageVector,
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier,
    bgColor: Color = MaestroColors.Gold,
    actionLabel: String? = null,
    onAction: () -> Unit = {}
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(0.dp)
        ) {
            // Icon circle
            Box(
                modifier = Modifier
                    .size(60.dp)
                    .background(bgColor.copy(alpha = 0.15f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = bgColor,
                    modifier = Modifier.size(26.dp)
                )
            }

            Spacer(Modifier.height(16.dp))

            // Title
            Text(
                text = title,
                fontFamily = frauncesFamily(),
                fontSize = 20.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaestroColors.Espresso,
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(6.dp))

            // Subtitle
            Text(
                text = subtitle,
                fontSize = 13.sp,
                color = MaestroColors.Muted,
                textAlign = TextAlign.Center
            )

            if (actionLabel != null) {
                Spacer(Modifier.height(20.dp))

                Button(
                    onClick = onAction,
                    colors = ButtonDefaults.buttonColors(containerColor = MaestroColors.Terra),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 20.dp, vertical = 10.dp)
                ) {
                    Text(
                        text = actionLabel,
                        color = Color.White,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
