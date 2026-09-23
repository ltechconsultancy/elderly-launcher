package com.elderlylauncher.ui.volume

import android.content.Context
import android.media.AudioManager
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.ripple
import androidx.compose.runtime.*
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
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
import androidx.lifecycle.viewmodel.compose.viewModel
import com.elderlylauncher.R
import com.elderlylauncher.ui.LauncherViewModel
import com.elderlylauncher.ui.volumeStackFit
import com.elderlylauncher.ui.rememberDeviceLayout
import com.elderlylauncher.ui.ButtonPagedColumn
import com.elderlylauncher.ui.theme.LauncherColors
import com.elderlylauncher.util.BrightnessHelper

private const val TAG = "VolumeScreen"

private val LocalVolumeWide = staticCompositionLocalOf { false }

@Composable
fun VolumeScreen(
    viewModel: LauncherViewModel = viewModel()
) {
    val context = LocalContext.current
    val audioManager = remember {
        context.getSystemService(Context.AUDIO_SERVICE) as? AudioManager
    }
    val brightnessPercent by viewModel.brightnessPercent.collectAsState()
    val brightnessMin by viewModel.brightnessMinPercent.collectAsState()
    val layout = rememberDeviceLayout()
    val compact = !layout.isTablet
    var showBrightnessFloor by remember { mutableStateOf(false) }

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
    val twoColumn = layout.isLandscape

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(LauncherColors.White)
    ) {
        // Header
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = if (compact) 8.dp else 24.dp)
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
                .weight(1f)
                .padding(horizontal = 16.dp)
                .padding(bottom = 8.dp),
            verticalArrangement = Arrangement.spacedBy(if (compact) 8.dp else 12.dp)
        ) {
            val mediaControl: @Composable (Modifier) -> Unit = { itemModifier ->
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
                    modifier = itemModifier,
                    compact = compact,
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
            }
            val notificationControl: @Composable (Modifier) -> Unit = { itemModifier ->
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
                    modifier = itemModifier,
                    compact = compact,
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
            }
            val alarmControl: @Composable (Modifier) -> Unit = { itemModifier ->
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
                    modifier = itemModifier,
                    compact = compact,
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
            }
            val brightnessControl: @Composable (Modifier) -> Unit = { itemModifier ->
                VolumeControl(
                    title = stringResource(R.string.volume_brightness),
                    subtitle = stringResource(R.string.volume_brightness_desc, brightnessMin),
                    icon = Icons.Default.BrightnessHigh,
                    backgroundColor = LauncherColors.Green50,
                    borderColor = LauncherColors.Green200,
                    iconBackgroundColor = LauncherColors.Green500,
                    accentColor = LauncherColors.Green500,
                    textColor = LauncherColors.Green700,
                    currentVolume = brightnessPercent,
                    maxVolume = BrightnessHelper.MAX_PERCENT,
                    decreaseDescription = stringResource(R.string.settings_brightness_decrease),
                    increaseDescription = stringResource(R.string.settings_brightness_increase),
                    modifier = itemModifier,
                    compact = compact,
                    onVolumeUp = {
                        if (brightnessPercent < BrightnessHelper.MAX_PERCENT) {
                            viewModel.setBrightnessPercent(
                                brightnessPercent + BrightnessHelper.STEP
                            )
                        }
                    },
                    onVolumeDown = {
                        if (brightnessPercent > brightnessMin) {
                            viewModel.setBrightnessPercent(
                                brightnessPercent - BrightnessHelper.STEP
                            )
                        } else {
                            showBrightnessFloor = true
                        }
                    }
                )
            }

            VolumeCardGrid(
                brightness = brightnessControl,
                media = mediaControl,
                notification = notificationControl,
                alarm = alarmControl,
                tablet = layout.isTablet,
                landscape = twoColumn,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            )
        }
    }
    if (showBrightnessFloor) {
        com.elderlylauncher.ui.BrightnessFloorDialog(
            minPercent = brightnessMin,
            onDismiss = { showBrightnessFloor = false }
        )
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
internal fun VolumeCardGrid(
    brightness: @Composable (Modifier) -> Unit,
    media: @Composable (Modifier) -> Unit,
    notification: @Composable (Modifier) -> Unit,
    alarm: @Composable (Modifier) -> Unit,
    tablet: Boolean = false,
    landscape: Boolean = false,
    modifier: Modifier = Modifier
) {
    if (tablet) {
        Column(
            modifier = modifier,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            val rowModifier = Modifier
                .fillMaxWidth()
                .weight(1f)
            if (landscape) {
                Row(modifier = rowModifier, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    media(Modifier.weight(1f).fillMaxHeight())
                    notification(Modifier.weight(1f).fillMaxHeight())
                }
                Row(modifier = rowModifier, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    alarm(Modifier.weight(1f).fillMaxHeight())
                    brightness(Modifier.weight(1f).fillMaxHeight())
                }
            } else {
                Row(modifier = rowModifier, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    brightness(Modifier.weight(1f).fillMaxHeight())
                    media(Modifier.weight(1f).fillMaxHeight())
                }
                Row(modifier = rowModifier, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    notification(Modifier.weight(1f).fillMaxHeight())
                    alarm(Modifier.weight(1f).fillMaxHeight())
                }
            }
        }
        return
    }
    BoxWithConstraints(modifier = modifier) {
        val fit = volumeStackFit(maxWidth, maxHeight, tablet = false)
        val wideCards = !tablet && fit.columns == 1
        CompositionLocalProvider(LocalVolumeWide provides wideCards) {
            if (fit.columns == 1) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(fit.gap, Alignment.CenterVertically)
                ) {
                    brightness(Modifier.fillMaxWidth().height(fit.item))
                    media(Modifier.fillMaxWidth().height(fit.item))
                    notification(Modifier.fillMaxWidth().height(fit.item))
                    alarm(Modifier.fillMaxWidth().height(fit.item))
                }
            } else {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(fit.gap, Alignment.CenterVertically)
                ) {
                    val leftTop = if (tablet && landscape) media else brightness
                    val rightTop = if (tablet && landscape) notification else media
                    val leftBottom = if (tablet && landscape) alarm else notification
                    val rightBottom = if (tablet && landscape) brightness else alarm
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(fit.item),
                        horizontalArrangement = Arrangement.spacedBy(fit.gap)
                    ) {
                        leftTop(Modifier.weight(1f).fillMaxHeight())
                        rightTop(Modifier.weight(1f).fillMaxHeight())
                    }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(fit.item),
                        horizontalArrangement = Arrangement.spacedBy(fit.gap)
                    ) {
                        leftBottom(Modifier.weight(1f).fillMaxHeight())
                        rightBottom(Modifier.weight(1f).fillMaxHeight())
                    }
                }
            }
        }
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
    modifier: Modifier = Modifier,
    compact: Boolean = false,
    onVolumeUp: () -> Unit,
    onVolumeDown: () -> Unit
) {
    val pad = if (compact) 10.dp else 24.dp
    val iconBox = if (compact) 40.dp else 80.dp
    val buttonSize = if (compact) 48.dp else 72.dp
    val titleSize = if (compact) 16.sp else 26.sp
    val subtitleSize = if (compact) 12.sp else 18.sp
    val percentSize = if (compact) 22.sp else 36.sp
    val gap = if (compact) 8.dp else 16.dp
    val wide = compact && LocalVolumeWide.current
    val percentage = if (maxVolume > 0) {
        val raw = (currentVolume.toFloat() / maxVolume) * 100
        ((raw / 5).toInt() * 5) // Round to nearest 5%
    } else 0

    val volumeDescription = "$title: $percentage%"

    Column(
        modifier = modifier
            .fillMaxWidth()
            .fillMaxHeight()
            .clip(RoundedCornerShape(24.dp))
            .background(backgroundColor)
            .border(2.dp, borderColor, RoundedCornerShape(24.dp))
            .padding(pad)
            .semantics { contentDescription = volumeDescription },
        verticalArrangement = Arrangement.Center
    ) {
        // Top row: Icon + Title/Subtitle
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon - 56dp for better fit
            Box(
                modifier = Modifier
                    .size(iconBox)
                    .shadow(4.dp, RoundedCornerShape(16.dp))
                    .clip(RoundedCornerShape(16.dp))
                    .background(iconBackgroundColor),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(iconBox * 0.5f)
                )
            }

            Spacer(modifier = Modifier.width(gap))

            // Title and subtitle - takes remaining space
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge,
                    color = textColor,
                    fontSize = titleSize,
                    maxLines = 1
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = textColor.copy(alpha = 0.7f),
                    fontSize = subtitleSize,
                    maxLines = 2,
                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                )
            }

            if (wide) {
                Spacer(modifier = Modifier.width(gap))
                VolumeStepButton(
                    label = "−",
                    size = buttonSize,
                    borderColor = borderColor,
                    accentColor = accentColor,
                    textColor = textColor,
                    description = decreaseDescription,
                    onClick = onVolumeDown
                )
                Text(
                    text = "$percentage%",
                    color = textColor,
                    modifier = Modifier.width(64.dp),
                    textAlign = TextAlign.Center,
                    fontSize = percentSize,
                    maxLines = 1,
                    fontWeight = FontWeight.Bold
                )
                VolumeStepButton(
                    label = "+",
                    size = buttonSize,
                    borderColor = borderColor,
                    accentColor = accentColor,
                    textColor = textColor,
                    description = increaseDescription,
                    onClick = onVolumeUp
                )
            }
        }

        if (!wide) {
            Spacer(modifier = Modifier.height(gap))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                VolumeStepButton(
                    label = "−",
                    size = buttonSize,
                    borderColor = borderColor,
                    accentColor = accentColor,
                    textColor = textColor,
                    description = decreaseDescription,
                    onClick = onVolumeDown
                )
                Text(
                    text = "$percentage%",
                    style = MaterialTheme.typography.headlineMedium,
                    color = textColor,
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 12.dp),
                    textAlign = TextAlign.Center,
                    fontSize = percentSize,
                    maxLines = 1,
                    fontWeight = FontWeight.Bold
                )
                VolumeStepButton(
                    label = "+",
                    size = buttonSize,
                    borderColor = borderColor,
                    accentColor = accentColor,
                    textColor = textColor,
                    description = increaseDescription,
                    onClick = onVolumeUp
                )
            }
        }

        Spacer(modifier = Modifier.height(if (compact) 6.dp else gap))

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
                        .height(if (compact) 8.dp else 12.dp)
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

@Composable
private fun VolumeStepButton(
    label: String,
    size: androidx.compose.ui.unit.Dp,
    borderColor: Color,
    accentColor: Color,
    textColor: Color,
    description: String,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(size)
            .clip(RoundedCornerShape(16.dp))
            .background(Color.White)
            .border(2.dp, borderColor, RoundedCornerShape(16.dp))
            .clickable(
                indication = ripple(color = accentColor),
                interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
                onClick = onClick
            )
            .semantics { contentDescription = description },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = textColor
        )
    }
}
