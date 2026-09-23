package com.elderlylauncher.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.drawable.toBitmap
import com.elderlylauncher.data.AppInfo
import kotlin.math.min

/**
 * One app tile for the home screen, the apps page and the games page.
 * The icon fills most of the card. The card color comes from that icon.
 */
@Composable
fun AppIconCard(
    appInfo: AppInfo,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val colors = remember(appInfo.packageName) { iconColors(appInfo.icon) }
    val bitmap = remember(appInfo.packageName) {
        appInfo.icon.toBitmap(256, 256).asImageBitmap()
    }
    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .clip(RoundedCornerShape(24.dp))
            .background(colors.background)
            .border(2.dp, colors.border, RoundedCornerShape(24.dp))
            .clickable(
                indication = ripple(color = colors.accent),
                interactionSource = remember { MutableInteractionSource() },
                onClick = onClick
            )
            .semantics { contentDescription = appInfo.label },
        contentAlignment = Alignment.Center
    ) {
        val short = min(maxWidth.value, maxHeight.value).dp
        val icon = (short * 0.62f).coerceIn(72.dp, 220.dp)
        val label = when {
            short < 150.dp -> 18.sp
            short < 240.dp -> 24.sp
            else -> 28.sp
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Image(
                bitmap = bitmap,
                contentDescription = null,
                modifier = Modifier
                    .size(icon)
                    .clip(RoundedCornerShape(icon * 0.22f))
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = appInfo.label,
                color = colors.accent,
                fontSize = label,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}
