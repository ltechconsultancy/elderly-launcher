package com.elderlylauncher.ui.home

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.drawable.toBitmap
import androidx.lifecycle.viewmodel.compose.viewModel
import com.elderlylauncher.R
import com.elderlylauncher.data.AppInfo
import com.elderlylauncher.data.QuickContact
import com.elderlylauncher.ui.LauncherViewModel
import com.elderlylauncher.ui.rememberDeviceLayout
import com.elderlylauncher.ui.theme.LauncherColors
import java.time.LocalDate
import java.time.LocalTime
import kotlin.math.cos
import kotlin.math.sin
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.Locale

// Default app configurations
data class DefaultAppConfig(
    val title: Int, // String resource
    val icon: ImageVector,
    val backgroundColor: Color,
    val borderColor: Color,
    val iconBackgroundColor: Color,
    val textColor: Color,
    val launchIntent: () -> Intent,
    val requiresTelephony: Boolean = false
)

private val defaultApps = listOf(
    DefaultAppConfig(
        title = R.string.home_call,
        icon = Icons.Default.Phone,
        backgroundColor = LauncherColors.Green50,
        borderColor = LauncherColors.Green200,
        iconBackgroundColor = LauncherColors.Green500,
        textColor = LauncherColors.Green700,
        launchIntent = { Intent(Intent.ACTION_DIAL) },
        requiresTelephony = true
    ),
    DefaultAppConfig(
        title = R.string.home_messages,
        icon = Icons.Default.Chat,
        backgroundColor = LauncherColors.Blue50,
        borderColor = LauncherColors.Blue200,
        iconBackgroundColor = LauncherColors.Blue500,
        textColor = LauncherColors.Blue700,
        launchIntent = { Intent(Intent.ACTION_MAIN).apply { addCategory(Intent.CATEGORY_APP_MESSAGING) } },
        requiresTelephony = true
    ),
    DefaultAppConfig(
        title = R.string.home_camera,
        icon = Icons.Default.CameraAlt,
        backgroundColor = LauncherColors.Purple50,
        borderColor = LauncherColors.Purple200,
        iconBackgroundColor = LauncherColors.Purple500,
        textColor = LauncherColors.Purple700,
        launchIntent = { Intent("android.media.action.IMAGE_CAPTURE") }
    ),
    DefaultAppConfig(
        title = R.string.home_photos,
        icon = Icons.Default.Photo,
        backgroundColor = LauncherColors.Pink50,
        borderColor = LauncherColors.Pink200,
        iconBackgroundColor = LauncherColors.Pink500,
        textColor = LauncherColors.Pink700,
        launchIntent = { Intent(Intent.ACTION_VIEW).apply { type = "image/*" } }
    )
)

// Color schemes for custom apps
private val appColorSchemes = listOf(
    Triple(LauncherColors.Green50, LauncherColors.Green200, LauncherColors.Green500),
    Triple(LauncherColors.Blue50, LauncherColors.Blue200, LauncherColors.Blue500),
    Triple(LauncherColors.Purple50, LauncherColors.Purple200, LauncherColors.Purple500),
    Triple(LauncherColors.Pink50, LauncherColors.Pink200, LauncherColors.Pink500),
    Triple(LauncherColors.Orange50, LauncherColors.Orange200, LauncherColors.Orange500),
    Triple(LauncherColors.Red50, LauncherColors.Red200, LauncherColors.Red500)
)

@Composable
fun HomeScreen(viewModel: LauncherViewModel = viewModel()) {
    val context = LocalContext.current
    val layout = rememberDeviceLayout()
    var showEmergencyConfirm by rememberSaveable { mutableStateOf(false) }

    val homeApps by viewModel.homeApps.collectAsState()
    val installedApps by viewModel.installedApps.collectAsState()
    val quickContacts by viewModel.quickContacts.collectAsState()
    val contacts by viewModel.contacts.collectAsState()
    var showCallPicker by rememberSaveable { mutableStateOf(false) }
    val emergencyNumber by viewModel.emergencyNumber.collectAsState()
    val dialNumber = LauncherViewModel.sanitizeEmergencyNumber(emergencyNumber) ?: "112"
    val landscape = layout.isLandscape

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(LauncherColors.White)
            .padding(horizontal = if (layout.isTablet) 24.dp else 16.dp)
    ) {
        ClockDisplay(
            compact = landscape || !layout.isTablet,
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    top = if (landscape) 4.dp else 16.dp,
                    bottom = if (landscape) 4.dp else 12.dp
                )
        )

        HomeAppGrid(
            homeApps = homeApps,
            installedApps = installedApps,
            context = context,
            viewModel = viewModel,
            hasTelephony = layout.hasTelephony,
            landscape = landscape,
            tablet = layout.isTablet,
            onCall = { showCallPicker = true },
            modifier = Modifier.weight(1f)
        )

        if (layout.hasTelephony && quickContacts.isNotEmpty() && !(landscape && !layout.isTablet)) {
            QuickContactsRow(
                contacts = quickContacts,
                maxVisible = if (layout.isTablet) 6 else 4,
                onCallContact = { contact ->
                    viewModel.callContact(contact)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = if (landscape) 4.dp else 12.dp)
            )
        }

        if (layout.hasTelephony) {
            EmergencyButton(
                compact = landscape || !layout.isTablet,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = if (landscape) 8.dp else 16.dp),
                onClick = { showEmergencyConfirm = true }
            )
        }
    }

    if (showCallPicker) {
        CallPickerDialog(
            favorites = quickContacts,
            contacts = contacts,
            onDismiss = { showCallPicker = false },
            onCall = { number ->
                showCallPicker = false
                viewModel.dialNumber(number)
            }
        )
    }

    if (showEmergencyConfirm) {
        AlertDialog(
            onDismissRequest = { showEmergencyConfirm = false },
            title = {
                Text(
                    text = stringResource(R.string.emergency_confirm_title),
                    style = MaterialTheme.typography.headlineMedium
                )
            },
            text = {
                Text(
                    text = stringResource(R.string.emergency_confirm_message, dialNumber),
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
                                data = Uri.parse("tel:$dialNumber")
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
fun ClockDisplay(
    modifier: Modifier = Modifier,
    compact: Boolean = false
) {
    var now by remember { mutableStateOf(LocalTime.now()) }
    var currentDate by remember { mutableStateOf(getCurrentDate()) }

    LaunchedEffect(Unit) {
        while (true) {
            now = LocalTime.now()
            currentDate = getCurrentDate()
            kotlinx.coroutines.delay(1000)
        }
    }

    val clockSize = if (compact) 72.dp else 120.dp
    val timeSize = if (compact) 36.sp else 56.sp

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        AnalogClock(
            time = now,
            modifier = Modifier.size(clockSize)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column(horizontalAlignment = Alignment.Start) {
            Text(
                text = now.format(DateTimeFormatter.ofPattern("HH:mm")),
                style = MaterialTheme.typography.displayLarge,
                color = LauncherColors.Gray800,
                fontSize = timeSize,
                lineHeight = timeSize
            )
            Text(
                text = currentDate,
                style = MaterialTheme.typography.titleLarge,
                color = LauncherColors.Gray600,
                fontWeight = FontWeight.Normal,
                fontSize = if (compact) 16.sp else 20.sp,
                maxLines = 2
            )
        }
    }
}

@Composable
private fun AnalogClock(
    time: LocalTime,
    modifier: Modifier = Modifier
) {
    val face = LauncherColors.Gray100
    val mark = LauncherColors.Gray800
    val hand = LauncherColors.Gray800
    val second = LauncherColors.Red500
    Canvas(modifier = modifier) {
        val radius = size.minDimension / 2f
        val center = Offset(size.width / 2f, size.height / 2f)
        drawCircle(color = face, radius = radius, center = center)
        drawCircle(
            color = mark,
            radius = radius - 2.dp.toPx(),
            center = center,
            style = Stroke(width = 3.dp.toPx())
        )
        for (index in 0 until 12) {
            val angle = Math.toRadians((index * 30.0) - 90.0)
            val inner = if (index % 3 == 0) radius * 0.72f else radius * 0.82f
            val outer = radius * 0.90f
            drawLine(
                color = mark,
                start = Offset(
                    center.x + (cos(angle) * inner).toFloat(),
                    center.y + (sin(angle) * inner).toFloat()
                ),
                end = Offset(
                    center.x + (cos(angle) * outer).toFloat(),
                    center.y + (sin(angle) * outer).toFloat()
                ),
                strokeWidth = if (index % 3 == 0) 4.dp.toPx() else 2.dp.toPx(),
                cap = StrokeCap.Round
            )
        }
        val hourAngle = ((time.hour % 12) + time.minute / 60f) * 30f - 90f
        val minuteAngle = (time.minute + time.second / 60f) * 6f - 90f
        val secondAngle = time.second * 6f - 90f
        fun hand(angleDegrees: Float, length: Float, width: Float, color: Color) {
            val radians = Math.toRadians(angleDegrees.toDouble())
            drawLine(
                color = color,
                start = center,
                end = Offset(
                    center.x + (cos(radians) * length).toFloat(),
                    center.y + (sin(radians) * length).toFloat()
                ),
                strokeWidth = width,
                cap = StrokeCap.Round
            )
        }
        hand(hourAngle, radius * 0.48f, 6.dp.toPx(), hand)
        hand(minuteAngle, radius * 0.70f, 4.dp.toPx(), hand)
        hand(secondAngle, radius * 0.76f, 2.dp.toPx(), second)
        drawCircle(color = hand, radius = 5.dp.toPx(), center = center)
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
    iconBoxSize: Dp = 80.dp,
    glyphSize: Dp = 40.dp,
    labelSize: TextUnit = 22.sp,
    contentPadding: Dp = 16.dp,
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
            .fillMaxSize()
            .clip(RoundedCornerShape(24.dp))
            .background(backgroundColor)
            .border(2.dp, borderColor, RoundedCornerShape(24.dp))
            .clickable(
                indication = ripple(color = iconBackgroundColor),
                interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
                onClick = onClick
            )
            .padding(contentPadding)
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
                    .size(iconBoxSize)
                    .shadow(8.dp, CircleShape)
                    .clip(CircleShape)
                    .background(iconBackgroundColor),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null, // Parent has description
                    tint = Color.White,
                    modifier = Modifier.size(glyphSize)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                color = textColor,
                textAlign = TextAlign.Center,
                fontSize = labelSize,
                maxLines = 2
            )
        }
    }
}

@Composable
fun HomeAppGrid(
    homeApps: Map<Int, String>,
    installedApps: List<AppInfo>,
    context: Context,
    viewModel: LauncherViewModel,
    hasTelephony: Boolean,
    landscape: Boolean,
    tablet: Boolean,
    onCall: () -> Unit,
    modifier: Modifier = Modifier
) {
    val positions = remember(homeApps, hasTelephony) {
        val defaultSlots = defaultApps.mapIndexedNotNull { index, config ->
            if (!config.requiresTelephony || hasTelephony) index else null
        }
        val extraSlots = homeApps.keys
            .filter { it !in defaultSlots }
            .sorted()
        defaultSlots + extraSlots
    }
    val (columns, _) = homeGridShape(positions.size, landscape, tablet)
    val scale = tileScale(positions.size, tablet)
    val rowsOfItems = positions.chunked(columns)

    Column(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        rowsOfItems.forEach { rowItems ->
            Row(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                rowItems.forEach { position ->
                    HomeAppTile(
                        position = position,
                        homeApps = homeApps,
                        installedApps = installedApps,
                        context = context,
                        viewModel = viewModel,
                        scale = scale,
                        onCall = onCall,
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                    )
                }
                repeat(columns - rowItems.size) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

private fun homeGridShape(count: Int, landscape: Boolean, tablet: Boolean): Pair<Int, Int> {
    if (count <= 1) return 1 to 1
    if (count == 2) return 2 to 1
    if (landscape && !tablet) {
        val cols = count.coerceAtMost(4)
        return cols to ((count + cols - 1) / cols)
    }
    if (count <= 4) return 2 to 2
    if (count <= 6) return if (landscape || tablet) 3 to 2 else 2 to 3
    return if (landscape || tablet) 4 to 2 else 2 to 4
}

private data class TileScale(
    val iconBox: Dp,
    val glyph: Dp,
    val label: TextUnit,
    val padding: Dp
)

private fun tileScale(count: Int, tablet: Boolean): TileScale {
    val base = when {
        count <= 4 -> TileScale(80.dp, 40.dp, 22.sp, 16.dp)
        count <= 6 -> TileScale(64.dp, 32.dp, 18.sp, 12.dp)
        else -> TileScale(48.dp, 24.dp, 16.sp, 8.dp)
    }
    if (tablet) return base
    return TileScale(
        iconBox = base.iconBox * 0.72f,
        glyph = base.glyph * 0.72f,
        label = (base.label.value * 0.85f).sp,
        padding = base.padding * 0.6f
    )
}

@Composable
private fun HomeAppTile(
    position: Int,
    homeApps: Map<Int, String>,
    installedApps: List<AppInfo>,
    context: Context,
    viewModel: LauncherViewModel,
    modifier: Modifier = Modifier,
    scale: TileScale = tileScale(4, tablet = true),
    onCall: () -> Unit = {}
) {
    val customPackage = homeApps[position]
    val customApp = customPackage?.let { pkg -> installedApps.find { it.packageName == pkg } }
    val colorScheme = appColorSchemes[position % appColorSchemes.size]

    if (customApp != null) {
        CustomAppTile(
            appInfo = customApp,
            backgroundColor = colorScheme.first,
            borderColor = colorScheme.second,
            iconTint = colorScheme.third,
            modifier = modifier,
            iconBoxSize = scale.iconBox,
            glyphSize = scale.glyph,
            labelSize = scale.label,
            contentPadding = scale.padding,
            onClick = {
                viewModel.launchApp(customApp.packageName)
            }
        )
    } else if (customPackage == null && position < defaultApps.size) {
        val defaultConfig = defaultApps[position]
        AppTile(
            title = stringResource(defaultConfig.title),
            icon = defaultConfig.icon,
            backgroundColor = defaultConfig.backgroundColor,
            borderColor = defaultConfig.borderColor,
            iconBackgroundColor = defaultConfig.iconBackgroundColor,
            textColor = defaultConfig.textColor,
            modifier = modifier,
            iconBoxSize = scale.iconBox,
            glyphSize = scale.glyph,
            labelSize = scale.label,
            contentPadding = scale.padding,
            onClick = {
                if (defaultConfig.title == R.string.home_call) {
                    onCall()
                } else {
                    safeStartActivity(context) {
                        defaultConfig.launchIntent()
                    }
                }
            }
        )
    }
}

@Composable
fun CustomAppTile(
    appInfo: AppInfo,
    backgroundColor: Color,
    borderColor: Color,
    iconTint: Color,
    modifier: Modifier = Modifier,
    iconBoxSize: Dp = 80.dp,
    glyphSize: Dp = 56.dp,
    labelSize: TextUnit = 22.sp,
    contentPadding: Dp = 16.dp,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .clip(RoundedCornerShape(24.dp))
            .background(backgroundColor)
            .border(2.dp, borderColor, RoundedCornerShape(24.dp))
            .clickable(
                indication = ripple(color = iconTint),
                interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
                onClick = onClick
            )
            .padding(contentPadding)
            .semantics { contentDescription = appInfo.label },
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // App icon - 80dp
            Box(
                modifier = Modifier
                    .size(iconBoxSize)
                    .shadow(8.dp, CircleShape)
                    .clip(CircleShape)
                    .background(Color.White),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    bitmap = appInfo.icon.toBitmap(128, 128).asImageBitmap(),
                    contentDescription = null,
                    modifier = Modifier.size(glyphSize)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = appInfo.label,
                style = MaterialTheme.typography.titleLarge,
                color = iconTint,
                textAlign = TextAlign.Center,
                fontSize = labelSize,
                maxLines = 2
            )
        }
    }
}

@Composable
fun EmergencyButton(
    modifier: Modifier = Modifier,
    compact: Boolean = false,
    onClick: () -> Unit
) {
    val emergencyDescription = stringResource(R.string.home_emergency)

    Button(
        onClick = onClick,
        modifier = modifier
            .height(if (compact) 56.dp else 80.dp)
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

private fun getCurrentDate(): String {
    val formatter = DateTimeFormatter.ofLocalizedDate(FormatStyle.FULL)
        .withLocale(Locale.getDefault())
    return LocalDate.now().format(formatter)
        .replaceFirstChar { it.uppercase() }
}

@Composable
fun QuickContactsRow(
    contacts: List<QuickContact>,
    onCallContact: (QuickContact) -> Unit,
    modifier: Modifier = Modifier,
    maxVisible: Int = 4
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        contacts.take(maxVisible).forEach { contact ->
            QuickContactButton(
                contact = contact,
                onClick = { onCallContact(contact) },
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
fun QuickContactButton(
    contact: QuickContact,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(LauncherColors.Orange50)
            .border(2.dp, LauncherColors.Orange200, RoundedCornerShape(16.dp))
            .clickable(
                indication = ripple(color = LauncherColors.Orange500),
                interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
                onClick = onClick
            )
            .padding(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Contact avatar
        Box(
            modifier = Modifier
                .size(48.dp)
                .shadow(4.dp, CircleShape)
                .clip(CircleShape)
                .background(LauncherColors.Orange500),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = contact.name.firstOrNull()?.uppercase() ?: "?",
                style = MaterialTheme.typography.titleLarge,
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Contact name
        Text(
            text = contact.name.split(" ").firstOrNull() ?: contact.name,
            style = MaterialTheme.typography.bodyMedium,
            color = LauncherColors.Orange700,
            textAlign = TextAlign.Center,
            maxLines = 1,
            fontSize = 14.sp
        )

        // Phone icon
        Icon(
            imageVector = Icons.Default.Phone,
            contentDescription = stringResource(R.string.action_call),
            tint = LauncherColors.Orange500,
            modifier = Modifier.size(16.dp)
        )
    }
}
