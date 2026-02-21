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
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.elderlylauncher.ui.theme.LauncherColors

data class ThemeColor(
    val name: String,
    val displayName: String,
    val primary: Color,
    val light: Color
)

val availableThemeColors = listOf(
    ThemeColor("blue", "Blauw", LauncherColors.Blue500, LauncherColors.Blue50),
    ThemeColor("green", "Groen", LauncherColors.Green500, LauncherColors.Green50),
    ThemeColor("purple", "Paars", LauncherColors.Purple500, LauncherColors.Purple50),
    ThemeColor("orange", "Oranje", LauncherColors.Orange500, LauncherColors.Orange50),
    ThemeColor("pink", "Roze", LauncherColors.Pink500, LauncherColors.Pink50),
    ThemeColor("red", "Rood", LauncherColors.Red500, LauncherColors.Red50),
    ThemeColor("gray", "Grijs", LauncherColors.Gray600, LauncherColors.Gray100),
    ThemeColor("teal", "Turquoise", Color(0xFF14B8A6), Color(0xFFF0FDFA)),
    ThemeColor("amber", "Amber", Color(0xFFF59E0B), Color(0xFFFFFBEB)),
    ThemeColor("indigo", "Indigo", Color(0xFF6366F1), Color(0xFFEEF2FF)),
    ThemeColor("cyan", "Cyaan", Color(0xFF06B6D4), Color(0xFFECFEFF)),
    ThemeColor("lime", "Limoen", Color(0xFF84CC16), Color(0xFFF7FEE7)),
)

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
                    text = "Kies een kleur",
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
                        text = "Annuleren",
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
                indication = null,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        if (isSelected) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = "Selected",
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
            text = color.displayName,
            style = MaterialTheme.typography.bodyLarge,
            color = LauncherColors.Gray600
        )
    }
}
