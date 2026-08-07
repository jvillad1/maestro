package com.maestro.app.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.maestro.app.theme.MaestroColors

/**
 * Screen title + primary action. Side by side on desktop; on phones the
 * title gets its own line and the action becomes a full-width button.
 */
@Composable
fun ScreenHeader(title: String, actionLabel: String, onAction: () -> Unit) {
    val compact = LocalWindowWidthClass.current == WindowWidthClass.Compact
    if (compact) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(
                title,
                style = MaterialTheme.typography.titleLarge,
                color = MaestroColors.Espresso,
                fontWeight = FontWeight.Bold
            )
            Button(
                onClick = onAction,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = MaestroColors.Terra),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(actionLabel, color = MaestroColors.White)
            }
        }
    } else {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                title,
                style = MaterialTheme.typography.titleLarge,
                color = MaestroColors.Espresso,
                fontWeight = FontWeight.Bold
            )
            Button(
                onClick = onAction,
                colors = ButtonDefaults.buttonColors(containerColor = MaestroColors.Terra),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(actionLabel, color = MaestroColors.White)
            }
        }
    }
}
