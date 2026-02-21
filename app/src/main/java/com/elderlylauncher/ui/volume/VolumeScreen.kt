package com.elderlylauncher.ui.volume

import android.content.Context
import android.media.AudioManager
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.ripple
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import kotlinx.coroutines.delay
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.elderlylauncher.R
import com.elderlylauncher.ui.theme.LauncherColors

private const val TAG = "VolumeScreen"

@Composable
fun VolumeScreen() {
    val context = LocalContext.current
    val audioManager = remember {
        context.getSystemService(Context.AUDIO_SERVICE) as? AudioManager
    }

    if (audioManager == null) {
        // Fallback UI if AudioManager is unavailable
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = stringResource(R.string.volume_unavailable),
                style = MaterialTheme.typography.headlineMedium,
                color = LauncherColors.Gray500
            )
        }
        return
    }

    // Get max volumes for each stream (removed STREAM_RING as it didn't work correctly)
    val maxMedia = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
    val maxNotification = audioManager.getStreamMaxVolume(AudioManager.STREAM_NOTIFICATION)
    val maxAlarm = audioManager.getStreamMaxVolume(AudioManager.STREAM_ALARM)

    // Current volumes as state
    var mediaVolume by remember { mutableIntStateOf(audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)) }
    var notificationVolume by remember { mutableIntStateOf(audioManager.getStreamVolume(AudioManager.STREAM_NOTIFICATION)) }
    var alarmVolume by remember { mutableIntStateOf(audioManager.getStreamVolume(AudioManager.STREAM_ALARM)) }

    // Periodically sync volume state from system (e.g., if user changes via hardware buttons)
    LaunchedEffect(audioManager) {
        while (true) {
            delay(2000)
            mediaVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)
            notificationVolume = audioManager.getStreamVolume(AudioManager.STREAM_NOTIFICATION)
            alarmVolume = audioManager.getStreamVolume(AudioManager.STREAM_ALARM)
        }
    }

    val decreaseDesc = stringResource(R.string.volume_decrease)
    val increaseDesc = stringResource(R.string.volume_increase)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(LauncherColors.White)
    ) {
        // Header
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 24.dp)
        ) {
            Text(
                text = stringResource(R.string.volume_title),
                style = MaterialTheme.typography.headlineLarge,
                color = LauncherColors.Gray800
            )
            Text(
                text = stringResource(R.string.volume_subtitle),
                style = MaterialTheme.typography.bodyLarge,
                color = LauncherColors.Gray600, // Darker for contrast
                fontSize = 18.sp
            )
        }

        // Volume controls
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Media Volume
            VolumeControl(
                title = stringResource(R.string.volume_media),
                subtitle = stringResource(R.string.volume_media_desc),
                icon = Icons.Default.MusicNote,
                backgroundColor = LauncherColors.Blue50,
                borderColor = LauncherColors.Blue200,
                iconBackgroundColor = LauncherColors.Blue500,
                accentColor = LauncherColors.Blue500,
                textColor = LauncherColors.Blue700,
                currentVolume = mediaVolume,
                maxVolume = maxMedia,
                decreaseDescription = decreaseDesc,
                increaseDescription = increaseDesc,
                onVolumeUp = {
                    if (mediaVolume < maxMedia) {
                        mediaVolume++
                        safeSetVolume(audioManager, AudioManager.STREAM_MUSIC, mediaVolume)
                    }
                },
                onVolumeDown = {
                    if (mediaVolume > 0) {
                        mediaVolume--
                        safeSetVolume(audioManager, AudioManager.STREAM_MUSIC, mediaVolume)
                    }
                }
            )

            // Notification Volume
            VolumeControl(
                title = stringResource(R.string.volume_notifications),
                subtitle = stringResource(R.string.volume_notifications_desc),
                icon = Icons.Default.Notifications,
                backgroundColor = LauncherColors.Orange50,
                borderColor = LauncherColors.Orange200,
                iconBackgroundColor = LauncherColors.Orange500,
                accentColor = LauncherColors.Orange500,
                textColor = LauncherColors.Orange700,
                currentVolume = notificationVolume,
                maxVolume = maxNotification,
                decreaseDescription = decreaseDesc,
                increaseDescription = increaseDesc,
                onVolumeUp = {
                    if (notificationVolume < maxNotification) {
                        notificationVolume++
                        safeSetVolume(audioManager, AudioManager.STREAM_NOTIFICATION, notificationVolume)
                    }
                },
                onVolumeDown = {
                    if (notificationVolume > 0) {
                        notificationVolume--
                        safeSetVolume(audioManager, AudioManager.STREAM_NOTIFICATION, notificationVolume)
                    }
                }
            )

            // Alarm Volume
            VolumeControl(
                title = stringResource(R.string.volume_alarm),
                subtitle = stringResource(R.string.volume_alarm_desc),
                icon = Icons.Default.Alarm,
                backgroundColor = LauncherColors.Red50,
                borderColor = LauncherColors.Red200,
                iconBackgroundColor = LauncherColors.Red500,
                accentColor = LauncherColors.Red500,
                textColor = LauncherColors.Red700,
                currentVolume = alarmVolume,
                maxVolume = maxAlarm,
                decreaseDescription = decreaseDesc,
                increaseDescription = increaseDesc,
                onVolumeUp = {
                    if (alarmVolume < maxAlarm) {
                        alarmVolume++
                        safeSetVolume(audioManager, AudioManager.STREAM_ALARM, alarmVolume)
                    }
                },
                onVolumeDown = {
                    if (alarmVolume > 0) {
                        alarmVolume--
                        safeSetVolume(audioManager, AudioManager.STREAM_ALARM, alarmVolume)
                    }
                }
            )

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

/**
 * Safely set volume with error handling
 */
private fun safeSetVolume(audioManager: AudioManager, streamType: Int, volume: Int) {
    try {
        audioManager.setStreamVolume(streamType, volume, 0)
    } catch (e: SecurityException) {
        Log.e(TAG, "SecurityException setting volume", e)
    } catch (e: Exception) {
        Log.e(TAG, "Error setting volume", e)
    }
}

@Composable
fun VolumeControl(
    title: String,
    subtitle: String,
    icon: ImageVector,
    backgroundColor: Color,
    borderColor: Color,
    iconBackgroundColor: Color,
    accentColor: Color,
    textColor: Color,
    currentVolume: Int,
    maxVolume: Int,
    decreaseDescription: String,
    increaseDescription: String,
    onVolumeUp: () -> Unit,
    onVolumeDown: () -> Unit
) {
    val percentage = if (maxVolume > 0) {
        ((currentVolume.toFloat() / maxVolume) * 100).toInt()
    } else 0

    val volumeDescription = "$title: $percentage%"

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(backgroundColor)
            .border(2.dp, borderColor, RoundedCornerShape(24.dp))
            .padding(20.dp)
            .semantics { contentDescription = volumeDescription }
    ) {
        // Top row: Icon + Title/Subtitle
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon - 56dp for better fit
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .shadow(4.dp, RoundedCornerShape(16.dp))
                    .clip(RoundedCornerShape(16.dp))
                    .background(iconBackgroundColor),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(28.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Title and subtitle - takes remaining space
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge,
                    color = textColor,
                    fontSize = 22.sp
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = textColor.copy(alpha = 0.7f),
                    fontSize = 14.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Volume controls row - full width, centered
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            // Minus button - 56dp touch target
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color.White)
                    .border(2.dp, borderColor, RoundedCornerShape(16.dp))
                    .clickable(
                        indication = ripple(color = accentColor),
                        interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
                        onClick = onVolumeDown
                    )
                    .semantics { contentDescription = decreaseDescription },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "−",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = textColor
                )
            }

            // Percentage - centered with fixed width
            Text(
                text = "$percentage%",
                style = MaterialTheme.typography.headlineMedium,
                color = textColor,
                modifier = Modifier.padding(horizontal = 24.dp),
                textAlign = TextAlign.Center,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold
            )

            // Plus button - 56dp touch target
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color.White)
                    .border(2.dp, borderColor, RoundedCornerShape(16.dp))
                    .clickable(
                        indication = ripple(color = accentColor),
                        interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
                        onClick = onVolumeUp
                    )
                    .semantics { contentDescription = increaseDescription },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "+",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = textColor
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Volume bars with accessibility
        val filledBars = if (maxVolume > 0) {
            (currentVolume.toFloat() / maxVolume * 10).toInt()
        } else 0

        val barsDescription = stringResource(R.string.volume_bars_description, filledBars)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .semantics { contentDescription = barsDescription },
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            repeat(10) { index ->
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(12.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(
                            if (index < filledBars) accentColor
                            else accentColor.copy(alpha = 0.2f)
                        )
                )
            }
        }
    }
}
