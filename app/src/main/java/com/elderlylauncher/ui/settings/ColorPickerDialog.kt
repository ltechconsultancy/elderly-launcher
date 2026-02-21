package com.elderlylauncher.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.material3.ripple
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.elderlylauncher.R
import com.elderlylauncher.ui.theme.LauncherColors

data class ThemeColor(
    val name: String,
    val displayNameResId: Int,
    val primary: Color,
    val light: Color
) {
    // Keep displayName for backward compatibility with non-composable contexts
    val displayName: String
        get() = name
}

val availableThemeColors = listOf(
    ThemeColor("blue", R.string.color_blue, LauncherColors.Blue500, LauncherColors.Blue50),
    ThemeColor("green", R.string.color_green, LauncherColors.Green500, LauncherColors.Green50),
    ThemeColor("purple", R.string.color_purple, LauncherColors.Purple500, LauncherColors.Purple50),
    ThemeColor("orange", R.string.color_orange, LauncherColors.Orange500, LauncherColors.Orange50),
    ThemeColor("pink", R.string.color_pink, LauncherColors.Pink500, LauncherColors.Pink50),
    ThemeColor("red", R.string.color_red, LauncherColors.Red500, LauncherColors.Red50),
    ThemeColor("gray", R.string.color_gray, LauncherColors.Gray600, LauncherColors.Gray100),
    ThemeColor("teal", R.string.color_teal, Color(0xFF14B8A6), Color(0xFFF0FDFA)),
    ThemeColor("amber", R.string.color_amber, Color(0xFFF59E0B), Color(0xFFFFFBEB)),
    ThemeColor("indigo", R.string.color_indigo, Color(0xFF6366F1), Color(0xFFEEF2FF)),
    ThemeColor("cyan", R.string.color_cyan, Color(0xFF06B6D4), Color(0xFFECFEFF)),
    ThemeColor("lime", R.string.color_lime, Color(0xFF84CC16), Color(0xFFF7FEE7)),
)

@Composable
fun localizedDisplayName(color: ThemeColor): String {
    return stringResource(color.displayNameResId)
}

@Composable
fun ColorPickerDialog(
    currentColor: String,
    onColorSelected: (String) -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(
                modifier = Modifier.padding(24.dp)
            ) {
                Text(
                    text = stringResource(R.string.color_picker_title),
                    style = MaterialTheme.typography.headlineMedium,
                    color = LauncherColors.Gray800
                )

                Spacer(modifier = Modifier.height(24.dp))

                LazyVerticalGrid(
                    columns = GridCells.Fixed(4),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(availableThemeColors) { color ->
                        ColorOption(
                            color = color,
                            isSelected = color.name == currentColor,
                            onClick = {
                                onColorSelected(color.name)
                                onDismiss()
                            }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Cancel button
                OutlinedButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text(
                        text = stringResource(R.string.cancel),
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            }
        }
    }
}

@Composable
fun ColorOption(
    color: ThemeColor,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val colorDisplayName = localizedDisplayName(color)
    val selectedText = stringResource(R.string.status_selected)
    val description = stringResource(
        R.string.color_option_description,
        colorDisplayName,
        if (isSelected) selectedText else ""
    )

    Box(
        modifier = Modifier
            .size(64.dp)
            .clip(CircleShape)
            .background(color.primary)
            .then(
                if (isSelected) {
                    Modifier.border(4.dp, LauncherColors.Gray800, CircleShape)
                } else {
                    Modifier
                }
            )
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(),
                onClick = onClick
            )
            .semantics { contentDescription = description },
        contentAlignment = Alignment.Center
    ) {
        if (isSelected) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = null, // Parent has description
                tint = Color.White,
                modifier = Modifier.size(32.dp)
            )
        }
    }
}

@Composable
fun ColorPreview(
    colorName: String,
    modifier: Modifier = Modifier
) {
    val color = availableThemeColors.find { it.name == colorName }
        ?: availableThemeColors.first()

    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(CircleShape)
                .background(color.primary)
        )
        Text(
            text = localizedDisplayName(color),
            style = MaterialTheme.typography.bodyLarge,
            color = LauncherColors.Gray600
        )
    }
}
