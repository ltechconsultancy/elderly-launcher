package com.elderlylauncher.ui.home

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
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
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
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
    val launchIntent: () -> Intent
)

private val defaultApps = listOf(
    DefaultAppConfig(
        title = R.string.home_call,
        icon = Icons.Default.Phone,
        backgroundColor = LauncherColors.Green50,
        borderColor = LauncherColors.Green200,
        iconBackgroundColor = LauncherColors.Green500,
        textColor = LauncherColors.Green700,
        launchIntent = { Intent(Intent.ACTION_DIAL) }
    ),
    DefaultAppConfig(
        title = R.string.home_messages,
        icon = Icons.Default.Chat,
        backgroundColor = LauncherColors.Blue50,
        borderColor = LauncherColors.Blue200,
        iconBackgroundColor = LauncherColors.Blue500,
        textColor = LauncherColors.Blue700,
        launchIntent = { Intent(Intent.ACTION_MAIN).apply { addCategory(Intent.CATEGORY_APP_MESSAGING) } }
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
    val emergencyNumber by viewModel.emergencyNumber.collectAsState()
    val dialNumber = LauncherViewModel.sanitizeEmergencyNumber(emergencyNumber) ?: "112"
    val landscapeTablet = layout.isTablet && layout.isLandscape

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(LauncherColors.White)
            .padding(horizontal = if (layout.isTablet) 24.dp else 16.dp)
    ) {
        ClockDisplay(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    top = if (landscapeTablet) 8.dp else 32.dp,
                    bottom = if (landscapeTablet) 12.dp else 24.dp
                )
        )

        HomeAppGrid(
            homeApps = homeApps,
            installedApps = installedApps,
            context = context,
            viewModel = viewModel,
            columns = when {
                layout.isTablet && layout.isLandscape -> 4
                layout.isTablet -> 3
                else -> 2
            },
            modifier = Modifier.weight(1f)
        )

        if (layout.hasTelephony && quickContacts.isNotEmpty()) {
            QuickContactsRow(
                contacts = quickContacts,
                maxVisible = if (layout.isTablet) 6 else 4,
                onCallContact = { contact ->
                    viewModel.callContact(contact)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp)
            )
        }

        if (layout.hasTelephony) {
            EmergencyButton(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                onClick = { showEmergencyConfirm = true }
            )
        } else {
            NoTelephonyBanner(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp)
            )
        }
    }

    // Emergency call confirmation dialog
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
            .fillMaxSize()
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
fun HomeAppGrid(
    homeApps: Map<Int, String>,
    installedApps: List<AppInfo>,
    context: Context,
    viewModel: LauncherViewModel,
    columns: Int,
    modifier: Modifier = Modifier
) {
    val positions = remember(homeApps) {
        (0 until LauncherViewModel.DEFAULT_HOME_SLOTS) +
            homeApps.keys.filter { it >= LauncherViewModel.DEFAULT_HOME_SLOTS }.sorted()
    }

    LazyVerticalGrid(
        columns = GridCells.Fixed(columns),
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(positions, key = { it }) { position ->
            HomeAppTile(
                position = position,
                homeApps = homeApps,
                installedApps = installedApps,
                context = context,
                viewModel = viewModel,
                modifier = Modifier.aspectRatio(1f)
            )
        }
    }
}

@Composable
fun NoTelephonyBanner(modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .height(80.dp)
            .clip(RoundedCornerShape(24.dp))
            .background(LauncherColors.Gray100)
            .border(2.dp, LauncherColors.Gray200, RoundedCornerShape(24.dp))
            .padding(horizontal = 20.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.PhoneDisabled,
            contentDescription = null,
            tint = LauncherColors.Gray500,
            modifier = Modifier.size(32.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = stringResource(R.string.tablet_no_telephony),
            style = MaterialTheme.typography.titleMedium,
            color = LauncherColors.Gray600,
            fontSize = 18.sp,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun HomeAppTile(
    position: Int,
    homeApps: Map<Int, String>,
    installedApps: List<AppInfo>,
    context: Context,
    viewModel: LauncherViewModel,
    modifier: Modifier = Modifier
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
            onClick = {
                viewModel.launchApp(customApp.packageName)
            }
        )
    } else if (position < defaultApps.size) {
        val defaultConfig = defaultApps[position]
        AppTile(
            title = stringResource(defaultConfig.title),
            icon = defaultConfig.icon,
            backgroundColor = defaultConfig.backgroundColor,
            borderColor = defaultConfig.borderColor,
            iconBackgroundColor = defaultConfig.iconBackgroundColor,
            textColor = defaultConfig.textColor,
            modifier = modifier,
            onClick = {
                safeStartActivity(context) {
                    defaultConfig.launchIntent()
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
            .padding(16.dp)
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
                    .size(80.dp)
                    .shadow(8.dp, CircleShape)
                    .clip(CircleShape)
                    .background(Color.White),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    bitmap = appInfo.icon.toBitmap(128, 128).asImageBitmap(),
                    contentDescription = null,
                    modifier = Modifier.size(56.dp)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = appInfo.label,
                style = MaterialTheme.typography.titleLarge,
                color = iconTint,
                textAlign = TextAlign.Center,
                fontSize = 22.sp,
                maxLines = 1
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
