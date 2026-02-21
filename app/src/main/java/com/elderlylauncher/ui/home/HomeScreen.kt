package com.elderlylauncher.ui.home

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.ripple
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.elderlylauncher.R
import com.elderlylauncher.ui.theme.LauncherColors
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.Locale

@Composable
fun HomeScreen() {
    val context = LocalContext.current
    var showEmergencyConfirm by rememberSaveable { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(LauncherColors.White)
            .padding(horizontal = 16.dp)
    ) {
        // Clock & Date
        ClockDisplay(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 32.dp, bottom = 24.dp)
        )

        // 2x2 App Grid
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                AppTile(
                    title = stringResource(R.string.home_call),
                    icon = Icons.Default.Phone,
                    backgroundColor = LauncherColors.Green50,
                    borderColor = LauncherColors.Green200,
                    iconBackgroundColor = LauncherColors.Green500,
                    textColor = LauncherColors.Green700,
                    modifier = Modifier.weight(1f),
                    onClick = {
                        safeStartActivity(context) {
                            Intent(Intent.ACTION_DIAL)
                        }
                    }
                )
                AppTile(
                    title = stringResource(R.string.home_messages),
                    icon = Icons.Default.Chat,
                    backgroundColor = LauncherColors.Blue50,
                    borderColor = LauncherColors.Blue200,
                    iconBackgroundColor = LauncherColors.Blue500,
                    textColor = LauncherColors.Blue700,
                    badgeCount = 3,
                    modifier = Modifier.weight(1f),
                    onClick = {
                        safeStartActivity(context) {
                            Intent(Intent.ACTION_MAIN).apply {
                                addCategory(Intent.CATEGORY_APP_MESSAGING)
                            }
                        }
                    }
                )
            }
            Row(
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                AppTile(
                    title = stringResource(R.string.home_camera),
                    icon = Icons.Default.CameraAlt,
                    backgroundColor = LauncherColors.Purple50,
                    borderColor = LauncherColors.Purple200,
                    iconBackgroundColor = LauncherColors.Purple500,
                    textColor = LauncherColors.Purple700,
                    modifier = Modifier.weight(1f),
                    onClick = {
                        safeStartActivity(context) {
                            Intent("android.media.action.IMAGE_CAPTURE")
                        }
                    }
                )
                AppTile(
                    title = stringResource(R.string.home_photos),
                    icon = Icons.Default.Photo,
                    backgroundColor = LauncherColors.Pink50,
                    borderColor = LauncherColors.Pink200,
                    iconBackgroundColor = LauncherColors.Pink500,
                    textColor = LauncherColors.Pink700,
                    modifier = Modifier.weight(1f),
                    onClick = {
                        safeStartActivity(context) {
                            Intent(Intent.ACTION_VIEW).apply {
                                type = "image/*"
                            }
                        }
                    }
                )
            }
        }

        // Emergency Button
        EmergencyButton(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
            onClick = { showEmergencyConfirm = true }
        )
    }

    // Emergency call confirmation dialog
    if (showEmergencyConfirm) {
        AlertDialog(
            onDismissRequest = { showEmergencyConfirm = false },
            title = {
                Text(
                    text = stringResource(R.string.home_emergency_confirm_title),
                    style = MaterialTheme.typography.headlineMedium
                )
            },
            text = {
                Text(
                    text = stringResource(R.string.home_emergency_confirm_message),
                    style = MaterialTheme.typography.bodyLarge,
                    fontSize = 20.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showEmergencyConfirm = false
                        safeStartActivity(context) {
                            Intent(Intent.ACTION_DIAL).apply {
                                data = Uri.parse("tel:112")
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = LauncherColors.Red500
                    )
                ) {
                    Text(
                        text = stringResource(R.string.confirm),
                        style = MaterialTheme.typography.titleMedium,
                        fontSize = 20.sp
                    )
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { showEmergencyConfirm = false }
                ) {
                    Text(
                        text = stringResource(R.string.cancel),
                        style = MaterialTheme.typography.titleMedium,
                        fontSize = 20.sp
                    )
                }
            },
            shape = RoundedCornerShape(24.dp)
        )
    }
}

/**
 * Safely start an activity with error handling
 */
private fun safeStartActivity(context: Context, intentBuilder: () -> Intent) {
    try {
        context.startActivity(intentBuilder())
    } catch (e: ActivityNotFoundException) {
        Toast.makeText(
            context,
            context.getString(R.string.error_app_not_found),
            Toast.LENGTH_SHORT
        ).show()
    } catch (e: SecurityException) {
        Toast.makeText(
            context,
            context.getString(R.string.error_no_permission),
            Toast.LENGTH_SHORT
        ).show()
    }
}

@Composable
fun ClockDisplay(modifier: Modifier = Modifier) {
    var currentTime by remember { mutableStateOf(getCurrentTime()) }
    var currentDate by remember { mutableStateOf(getCurrentDate()) }

    LaunchedEffect(Unit) {
        while (true) {
            currentTime = getCurrentTime()
            currentDate = getCurrentDate()
            kotlinx.coroutines.delay(1000)
        }
    }

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = currentTime,
            style = MaterialTheme.typography.displayLarge,
            color = LauncherColors.Gray800
        )
        Text(
            text = currentDate,
            style = MaterialTheme.typography.titleLarge,
            color = LauncherColors.Gray600, // Darker for better contrast
            fontWeight = FontWeight.Normal,
            fontSize = 22.sp
        )
    }
}

@Composable
fun AppTile(
    title: String,
    icon: ImageVector,
    backgroundColor: Color,
    borderColor: Color,
    iconBackgroundColor: Color,
    textColor: Color,
    modifier: Modifier = Modifier,
    badgeCount: Int? = null,
    onClick: () -> Unit
) {
    val badgeDescription = stringResource(R.string.home_badge_description, title, badgeCount ?: 0)
    val tileDescription = if (badgeCount != null && badgeCount > 0) {
        badgeDescription
    } else {
        title
    }

    Box(
        modifier = modifier
            .fillMaxHeight()
            .clip(RoundedCornerShape(24.dp))
            .background(backgroundColor)
            .border(2.dp, borderColor, RoundedCornerShape(24.dp))
            .clickable(
                indication = ripple(color = iconBackgroundColor),
                interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
                onClick = onClick
            )
            .padding(16.dp)
            .semantics { contentDescription = tileDescription },
        contentAlignment = Alignment.Center
    ) {
        // Badge
        if (badgeCount != null && badgeCount > 0) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .size(32.dp) // Larger badge
                    .clip(CircleShape)
                    .background(LauncherColors.Red500),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = badgeCount.toString(),
                    color = Color.White,
                    fontSize = 18.sp, // Larger text
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Icon circle - 80dp touch target
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .shadow(8.dp, CircleShape)
                    .clip(CircleShape)
                    .background(iconBackgroundColor),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null, // Parent has description
                    tint = Color.White,
                    modifier = Modifier.size(40.dp)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                color = textColor,
                textAlign = TextAlign.Center,
                fontSize = 22.sp // Larger for elderly
            )
        }
    }
}

@Composable
fun EmergencyButton(
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val emergencyDescription = stringResource(R.string.home_emergency)

    Button(
        onClick = onClick,
        modifier = modifier
            .height(80.dp) // Even larger for emergency
            .semantics { contentDescription = emergencyDescription },
        colors = ButtonDefaults.buttonColors(
            containerColor = LauncherColors.Red500
        ),
        shape = RoundedCornerShape(24.dp),
        elevation = ButtonDefaults.buttonElevation(
            defaultElevation = 8.dp
        )
    ) {
        Icon(
            imageVector = Icons.Default.Phone,
            contentDescription = null,
            modifier = Modifier.size(36.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = stringResource(R.string.home_emergency),
            style = MaterialTheme.typography.titleMedium,
            fontSize = 26.sp
        )
    }
}

private fun getCurrentTime(): String {
    val formatter = DateTimeFormatter.ofPattern("HH:mm")
    return LocalTime.now().format(formatter)
}

private fun getCurrentDate(): String {
    val formatter = DateTimeFormatter.ofLocalizedDate(FormatStyle.FULL)
        .withLocale(Locale.getDefault())
    return LocalDate.now().format(formatter)
        .replaceFirstChar { it.uppercase() }
}
