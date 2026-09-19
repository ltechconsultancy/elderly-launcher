package com.elderlylauncher.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import com.elderlylauncher.R
import com.elderlylauncher.data.AppInfo
import com.elderlylauncher.data.QuickContact
import com.elderlylauncher.ui.ButtonPagedColumn
import com.elderlylauncher.ui.LauncherViewModel
import com.elderlylauncher.ui.rememberDeviceLayout
import com.elderlylauncher.ui.theme.LauncherColors
import com.elderlylauncher.util.BrightnessHelper
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.core.graphics.drawable.toBitmap
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun SettingsScreen(
    viewModel: LauncherViewModel = viewModel()
) {
    var isUnlocked by rememberSaveable { mutableStateOf(false) }
    var showPasswordDialog by rememberSaveable { mutableStateOf(false) }
    var password by rememberSaveable { mutableStateOf("") }
    var showError by rememberSaveable { mutableStateOf(false) }
    var failedAttempts by rememberSaveable { mutableIntStateOf(0) }
    var lockoutEndTime by rememberSaveable { mutableLongStateOf(0L) }

    // Rate limiting: lock for 30 seconds after 5 failed attempts
    var isLockedOut by rememberSaveable { mutableStateOf(false) }

    // Coroutine scope for password validation
    val coroutineScope = rememberCoroutineScope()

    // Auto-refresh lockout timer
    LaunchedEffect(lockoutEndTime) {
        if (lockoutEndTime > 0L) {
            val remaining = lockoutEndTime - System.currentTimeMillis()
            if (remaining > 0L) {
                isLockedOut = true
                delay(remaining)
            }
            isLockedOut = false
        }
    }

    if (showPasswordDialog && !isLockedOut) {
        PasswordDialog(
            password = password,
            onPasswordChange = { password = it; showError = false },
            showError = showError,
            failedAttempts = failedAttempts,
            onDismiss = { showPasswordDialog = false; password = "" },
            onConfirm = {
                coroutineScope.launch {
                    if (viewModel.validatePassword(password)) {
                        isUnlocked = true
                        showPasswordDialog = false
                        password = ""
                        failedAttempts = 0
                    } else {
                        showError = true
                        failedAttempts++
                        if (failedAttempts >= 5) {
                            lockoutEndTime = System.currentTimeMillis() + 30_000
                            showPasswordDialog = false
                            password = ""
                        }
                    }
                }
            }
        )
    }

    if (isUnlocked) {
        SettingsContent(
            viewModel = viewModel,
            onLock = { isUnlocked = false }
        )
    } else {
        LockedSettingsScreen(
            onUnlockClick = { showPasswordDialog = true },
            isLockedOut = isLockedOut,
            lockoutEndTime = lockoutEndTime
        )
    }
}

@Composable
fun LockedSettingsScreen(
    onUnlockClick: () -> Unit,
    isLockedOut: Boolean = false,
    lockoutEndTime: Long = 0L
) {
    val layout = rememberDeviceLayout()
    val lockDescription = stringResource(R.string.settings_lock_description)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(LauncherColors.White),
        contentAlignment = Alignment.Center
    ) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .then(if (layout.isTablet) Modifier.widthIn(max = layout.contentMaxWidth) else Modifier)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Lock icon
        Box(
            modifier = Modifier
                .size(120.dp)
                .shadow(12.dp, RoundedCornerShape(32.dp))
                .clip(RoundedCornerShape(32.dp))
                .background(LauncherColors.Gray100)
                .semantics { contentDescription = lockDescription },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Lock,
                contentDescription = lockDescription,
                tint = LauncherColors.Gray500,
                modifier = Modifier.size(60.dp)
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = stringResource(R.string.settings_title),
            style = MaterialTheme.typography.headlineLarge,
            color = LauncherColors.Gray800
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = stringResource(R.string.settings_password_title),
            style = MaterialTheme.typography.bodyLarge,
            color = LauncherColors.Gray500,
            textAlign = TextAlign.Center
        )

        if (isLockedOut) {
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = stringResource(R.string.settings_locked_out),
                style = MaterialTheme.typography.bodyLarge,
                color = LauncherColors.Red500,
                textAlign = TextAlign.Center
            )
        }

        Spacer(modifier = Modifier.height(40.dp))

        // Unlock button - 64dp+ for elderly
        Button(
            onClick = onUnlockClick,
            enabled = !isLockedOut,
            modifier = Modifier
                .fillMaxWidth()
                .height(72.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = LauncherColors.Blue500,
                disabledContainerColor = LauncherColors.Gray300
            ),
            shape = RoundedCornerShape(20.dp)
        ) {
            Icon(
                imageVector = Icons.Default.LockOpen,
                contentDescription = null,
                modifier = Modifier.size(32.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = stringResource(R.string.settings_unlock),
                style = MaterialTheme.typography.titleMedium,
                fontSize = 22.sp
            )
        }
    }
    }
}

@Composable
fun PasswordDialog(
    password: String,
    onPasswordChange: (String) -> Unit,
    showError: Boolean,
    failedAttempts: Int,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
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
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = stringResource(R.string.settings_password_title),
                    style = MaterialTheme.typography.headlineMedium,
                    color = LauncherColors.Gray800
                )

                Spacer(modifier = Modifier.height(24.dp))

                OutlinedTextField(
                    value = password,
                    onValueChange = onPasswordChange,
                    label = { Text(stringResource(R.string.settings_password_hint)) },
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                    isError = showError,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    textStyle = MaterialTheme.typography.titleLarge.copy(
                        textAlign = TextAlign.Center,
                        letterSpacing = 8.sp
                    )
                )

                if (showError) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = stringResource(R.string.settings_password_wrong),
                        color = LauncherColors.Red500,
                        style = MaterialTheme.typography.bodyLarge
                    )
                    if (failedAttempts >= 3) {
                        val remaining = 5 - failedAttempts
                        Text(
                            text = pluralStringResource(R.plurals.settings_attempts_remaining, remaining, remaining),
                            color = LauncherColors.Orange500,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // 64dp+ height for elderly
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .weight(1f)
                            .height(64.dp),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.cancel),
                            style = MaterialTheme.typography.titleMedium,
                            fontSize = 16.sp,
                            maxLines = 1
                        )
                    }

                    Button(
                        onClick = onConfirm,
                        modifier = Modifier
                            .weight(1f)
                            .height(64.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = LauncherColors.Blue500
                        )
                    ) {
                        Text(
                            text = stringResource(R.string.confirm),
                            style = MaterialTheme.typography.titleMedium,
                            fontSize = 16.sp,
                            maxLines = 1
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SettingsContent(
    viewModel: LauncherViewModel,
    onLock: () -> Unit
) {
    val lockDescription = stringResource(R.string.action_lock)
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    // Dialog states
    var showEmergencyDialog by rememberSaveable { mutableStateOf(false) }
    var showLanguageDialog by rememberSaveable { mutableStateOf(false) }
    var showPasswordChangeDialog by rememberSaveable { mutableStateOf(false) }
    var showColorsDialog by rememberSaveable { mutableStateOf(false) }
    var showAppsDialog by rememberSaveable { mutableStateOf(false) }
    var showAppsPageDialog by rememberSaveable { mutableStateOf(false) }
    var showContactsDialog by rememberSaveable { mutableStateOf(false) }
    var showGamesDialog by rememberSaveable { mutableStateOf(false) }
    var showPhotosDialog by rememberSaveable { mutableStateOf(false) }
    var showBrightnessDialog by rememberSaveable { mutableStateOf(false) }

    val brightnessPercent by viewModel.brightnessPercent.collectAsState()
    val brightnessLocked by viewModel.brightnessLocked.collectAsState()
    val layout = rememberDeviceLayout()

    val storedEmergency by viewModel.emergencyNumber.collectAsState()

    // Password change state
    var currentPassword by rememberSaveable { mutableStateOf("") }
    var newPassword by rememberSaveable { mutableStateOf("") }
    var confirmPassword by rememberSaveable { mutableStateOf("") }
    var passwordError by rememberSaveable { mutableStateOf<String?>(null) }

    // Emergency Number Dialog
    if (showEmergencyDialog) {
        EmergencyNumberDialog(
            currentNumber = storedEmergency,
            onDismiss = { showEmergencyDialog = false },
            onConfirm = { newNumber ->
                viewModel.updateEmergencyNumber(newNumber)
                showEmergencyDialog = false
            }
        )
    }

    // Language Dialog
    if (showLanguageDialog) {
        LanguageDialog(
            onDismiss = { showLanguageDialog = false },
            onSelectLanguage = { languageCode ->
                // Save language preference and apply locale
                viewModel.updateLanguage(languageCode)
                showLanguageDialog = false
                // Recreate activity to apply new locale
                (context as? android.app.Activity)?.let { activity ->
                    com.elderlylauncher.util.LocaleHelper.applyLocale(activity, languageCode)
                }
            }
        )
    }

    // Password Change Dialog
    if (showPasswordChangeDialog) {
        PasswordChangeDialog(
            currentPassword = currentPassword,
            newPassword = newPassword,
            confirmPassword = confirmPassword,
            error = passwordError,
            onCurrentPasswordChange = { currentPassword = it; passwordError = null },
            onNewPasswordChange = { newPassword = it; passwordError = null },
            onConfirmPasswordChange = { confirmPassword = it; passwordError = null },
            onDismiss = {
                showPasswordChangeDialog = false
                currentPassword = ""
                newPassword = ""
                confirmPassword = ""
                passwordError = null
            },
            onConfirm = {
                coroutineScope.launch {
                    when {
                        !viewModel.validatePassword(currentPassword) -> {
                            passwordError = context.getString(R.string.settings_password_wrong)
                        }
                        newPassword.length < 4 -> {
                            passwordError = context.getString(R.string.settings_password_too_short)
                        }
                        newPassword != confirmPassword -> {
                            passwordError = context.getString(R.string.settings_password_mismatch)
                        }
                        else -> {
                            viewModel.changePassword(newPassword)
                            showPasswordChangeDialog = false
                            currentPassword = ""
                            newPassword = ""
                            confirmPassword = ""
                            passwordError = null
                        }
                    }
                }
            }
        )
    }

    // Colors Dialog
    if (showColorsDialog) {
        val currentColor by viewModel.primaryColor.collectAsState()
        ColorsDialog(
            currentColor = currentColor,
            onDismiss = { showColorsDialog = false },
            onSelectColor = { color ->
                viewModel.updatePrimaryColor(color)
                showColorsDialog = false
            }
        )
    }

    // Apps Dialog - Select home screen apps
    if (showAppsDialog) {
        val installedApps by viewModel.installedApps.collectAsState()
        val homeApps by viewModel.homeApps.collectAsState()

        HomeAppsDialog(
            installedApps = installedApps,
            currentHomeApps = homeApps,
            onDismiss = { showAppsDialog = false },
            onAppSelected = { position, packageName ->
                viewModel.setHomeApp(position, packageName)
            },
            onAppCleared = { position ->
                viewModel.clearHomeApp(position)
            }
        )
    }

    // Contacts Dialog
    if (showContactsDialog) {
        val quickContacts by viewModel.quickContacts.collectAsState()
        val allContacts by viewModel.contacts.collectAsState()

        // Reload contacts when dialog opens (in case permission was just granted)
        LaunchedEffect(showContactsDialog) {
            viewModel.loadContacts()
        }

        QuickContactsDialog(
            quickContacts = quickContacts,
            allContacts = allContacts,
            onDismiss = { showContactsDialog = false },
            onAddContact = { contact ->
                viewModel.addQuickContact(contact)
            },
            onRemoveContact = { contact ->
                viewModel.removeQuickContact(contact)
            }
        )
    }

    // Apps Page visibility dialog
    if (showAppsPageDialog) {
        val installedApps by viewModel.installedApps.collectAsState()
        val hiddenApps by viewModel.hiddenApps.collectAsState()

        AppsPageDialog(
            installedApps = installedApps,
            hiddenApps = hiddenApps,
            onDismiss = { showAppsPageDialog = false },
            onToggleApp = { packageName ->
                viewModel.toggleAppVisibility(packageName)
            }
        )
    }

    // Games selection dialog
    if (showGamesDialog) {
        val installedApps by viewModel.installedApps.collectAsState()
        val gameApps by viewModel.gameApps.collectAsState()

        GamesSelectionDialog(
            installedApps = installedApps,
            gameApps = gameApps,
            onDismiss = { showGamesDialog = false },
            onToggleApp = { packageName ->
                viewModel.toggleGameApp(packageName)
            }
        )
    }

    // Photos selection dialog
    if (showPhotosDialog) {
        val carouselPhotos by viewModel.carouselPhotos.collectAsState()

        PhotosSelectionDialog(
            selectedPhotos = carouselPhotos,
            onDismiss = { showPhotosDialog = false },
            onAddPhoto = { uri ->
                viewModel.addCarouselPhoto(uri)
            },
            onRemovePhoto = { uri ->
                viewModel.removeCarouselPhoto(uri)
            }
        )
    }

    if (showBrightnessDialog) {
        BrightnessDialog(
            percent = brightnessPercent,
            locked = brightnessLocked,
            onDismiss = { showBrightnessDialog = false },
            onPercentChange = { viewModel.setBrightnessPercent(it) },
            onLockedChange = { viewModel.setBrightnessLocked(it) }
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(LauncherColors.White),
        contentAlignment = Alignment.TopCenter
    ) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .then(if (layout.isTablet) Modifier.widthIn(max = layout.contentMaxWidth) else Modifier)
    ) {
        // Header with lock button
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 24.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = stringResource(R.string.settings_title),
                    style = MaterialTheme.typography.headlineLarge,
                    color = LauncherColors.Gray800
                )
            }

            // Lock button - 64dp for elderly
            IconButton(
                onClick = onLock,
                modifier = Modifier
                    .size(64.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(LauncherColors.Gray100)
            ) {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = lockDescription,
                    tint = LauncherColors.Gray600,
                    modifier = Modifier.size(32.dp)
                )
            }
        }

        val settingsKeys = listOf(
            "colors", "language", "brightness", "apps",
            "apps_page", "games", "photos", "contacts",
            "emergency", "password"
        )
        ButtonPagedColumn(
            items = settingsKeys,
            pageSize = 4,
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) { key ->
            when (key) {
                "colors" -> SettingsItem(
                    title = stringResource(R.string.settings_colors),
                    subtitle = stringResource(R.string.settings_colors_subtitle),
                    icon = Icons.Default.Palette,
                    iconColor = LauncherColors.Purple500,
                    onClick = { showColorsDialog = true }
                )
                "language" -> SettingsItem(
                    title = stringResource(R.string.settings_language),
                    subtitle = stringResource(R.string.language_current),
                    icon = Icons.Default.Language,
                    iconColor = LauncherColors.Blue500,
                    onClick = { showLanguageDialog = true }
                )
                "brightness" -> SettingsItem(
                    title = stringResource(R.string.settings_brightness),
                    subtitle = if (brightnessLocked) {
                        stringResource(R.string.settings_brightness_locked_value, brightnessPercent)
                    } else {
                        stringResource(R.string.settings_brightness_value, brightnessPercent)
                    },
                    icon = Icons.Default.BrightnessHigh,
                    iconColor = LauncherColors.Orange500,
                    onClick = { showBrightnessDialog = true }
                )
                "apps" -> SettingsItem(
                    title = stringResource(R.string.settings_apps),
                    subtitle = stringResource(R.string.settings_apps_subtitle),
                    icon = Icons.Default.Apps,
                    iconColor = LauncherColors.Green500,
                    onClick = { showAppsDialog = true }
                )
                "apps_page" -> SettingsItem(
                    title = stringResource(R.string.settings_apps_page),
                    subtitle = stringResource(R.string.settings_apps_page_subtitle),
                    icon = Icons.Default.GridView,
                    iconColor = LauncherColors.Teal500,
                    onClick = { showAppsPageDialog = true }
                )
                "games" -> SettingsItem(
                    title = stringResource(R.string.settings_games),
                    subtitle = stringResource(R.string.settings_games_subtitle),
                    icon = Icons.Default.SportsEsports,
                    iconColor = LauncherColors.Purple500,
                    onClick = { showGamesDialog = true }
                )
                "photos" -> SettingsItem(
                    title = stringResource(R.string.settings_photos),
                    subtitle = stringResource(R.string.settings_photos_subtitle),
                    icon = Icons.Default.PhotoLibrary,
                    iconColor = LauncherColors.Pink500,
                    onClick = { showPhotosDialog = true }
                )
                "contacts" -> SettingsItem(
                    title = stringResource(R.string.settings_contacts),
                    subtitle = stringResource(R.string.settings_contacts_subtitle),
                    icon = Icons.Default.Contacts,
                    iconColor = LauncherColors.Orange500,
                    onClick = { showContactsDialog = true }
                )
                "emergency" -> SettingsItem(
                    title = stringResource(R.string.settings_emergency),
                    subtitle = storedEmergency,
                    icon = Icons.Default.Emergency,
                    iconColor = LauncherColors.Red500,
                    onClick = { showEmergencyDialog = true }
                )
                "password" -> SettingsItem(
                    title = stringResource(R.string.settings_password_change),
                    subtitle = stringResource(R.string.settings_password_change_subtitle),
                    icon = Icons.Default.Key,
                    iconColor = LauncherColors.Gray600,
                    onClick = { showPasswordChangeDialog = true }
                )
            }
        }
    }
    }
}

@Composable
fun EmergencyNumberDialog(
    currentNumber: String,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var number by rememberSaveable { mutableStateOf(currentNumber) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Default.Emergency,
                    contentDescription = null,
                    tint = LauncherColors.Red500,
                    modifier = Modifier.size(48.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = stringResource(R.string.settings_emergency),
                    style = MaterialTheme.typography.headlineMedium,
                    color = LauncherColors.Gray800
                )

                Spacer(modifier = Modifier.height(24.dp))

                OutlinedTextField(
                    value = number,
                    onValueChange = {
                        if (it.length <= 15) number = it.filter { ch -> ch.isDigit() || ch == '+' || ch == ' ' }
                    },
                    label = { Text(stringResource(R.string.settings_emergency_hint)) },
                    isError = number.isNotBlank() && LauncherViewModel.sanitizeEmergencyNumber(number) == null,
                    supportingText = {
                        if (number.isNotBlank() && LauncherViewModel.sanitizeEmergencyNumber(number) == null) {
                            Text(stringResource(R.string.settings_emergency_invalid))
                        }
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    textStyle = MaterialTheme.typography.titleLarge.copy(
                        textAlign = TextAlign.Center
                    ),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .weight(1f)
                            .height(56.dp),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.cancel),
                            fontSize = 16.sp,
                            maxLines = 1
                        )
                    }

                    Button(
                        onClick = {
                            val sanitized = LauncherViewModel.sanitizeEmergencyNumber(number)
                            if (sanitized != null) onConfirm(sanitized)
                        },
                        enabled = LauncherViewModel.sanitizeEmergencyNumber(number) != null,
                        modifier = Modifier
                            .weight(1f)
                            .height(56.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = LauncherColors.Red500
                        )
                    ) {
                        Text(
                            text = stringResource(R.string.confirm),
                            fontSize = 16.sp,
                            maxLines = 1
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun LanguageDialog(
    onDismiss: () -> Unit,
    onSelectLanguage: (String) -> Unit
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
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Default.Language,
                    contentDescription = null,
                    tint = LauncherColors.Blue500,
                    modifier = Modifier.size(48.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = stringResource(R.string.settings_language),
                    style = MaterialTheme.typography.headlineMedium,
                    color = LauncherColors.Gray800
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Dutch
                LanguageOption(
                    flag = "🇳🇱",
                    name = "Nederlands",
                    onClick = { onSelectLanguage("nl") }
                )

                Spacer(modifier = Modifier.height(12.dp))

                // English
                LanguageOption(
                    flag = "🇬🇧",
                    name = "English",
                    onClick = { onSelectLanguage("en") }
                )

                Spacer(modifier = Modifier.height(24.dp))

                OutlinedButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text(
                        text = stringResource(R.string.cancel),
                        fontSize = 16.sp
                    )
                }
            }
        }
    }
}

@Composable
fun LanguageOption(
    flag: String,
    name: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(LauncherColors.Gray50)
            .border(1.dp, LauncherColors.Gray200, RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = flag,
            fontSize = 32.sp
        )
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = name,
            style = MaterialTheme.typography.titleLarge,
            color = LauncherColors.Gray800
        )
    }
}

@Composable
fun PasswordChangeDialog(
    currentPassword: String,
    newPassword: String,
    confirmPassword: String,
    error: String?,
    onCurrentPasswordChange: (String) -> Unit,
    onNewPasswordChange: (String) -> Unit,
    onConfirmPasswordChange: (String) -> Unit,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
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
                modifier = Modifier
                    .padding(24.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Default.Key,
                    contentDescription = null,
                    tint = LauncherColors.Gray600,
                    modifier = Modifier.size(48.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = stringResource(R.string.settings_password_change),
                    style = MaterialTheme.typography.headlineMedium,
                    color = LauncherColors.Gray800
                )

                Spacer(modifier = Modifier.height(24.dp))

                OutlinedTextField(
                    value = currentPassword,
                    onValueChange = onCurrentPasswordChange,
                    label = { Text(stringResource(R.string.settings_password_current)) },
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = newPassword,
                    onValueChange = onNewPasswordChange,
                    label = { Text(stringResource(R.string.settings_password_new)) },
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = confirmPassword,
                    onValueChange = onConfirmPasswordChange,
                    label = { Text(stringResource(R.string.settings_password_confirm)) },
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    singleLine = true,
                    isError = error != null
                )

                if (error != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = error,
                        color = LauncherColors.Red500,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .weight(1f)
                            .height(56.dp),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.cancel),
                            fontSize = 16.sp,
                            maxLines = 1
                        )
                    }

                    Button(
                        onClick = onConfirm,
                        modifier = Modifier
                            .weight(1f)
                            .height(56.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = LauncherColors.Blue500
                        )
                    ) {
                        Text(
                            text = stringResource(R.string.confirm),
                            fontSize = 16.sp,
                            maxLines = 1
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun AppsPageDialog(
    installedApps: List<AppInfo>,
    hiddenApps: Set<String>,
    onDismiss: () -> Unit,
    onToggleApp: (String) -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.85f)
                .padding(8.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(R.string.settings_apps_page),
                        style = MaterialTheme.typography.headlineSmall,
                        color = LauncherColors.Gray800
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = stringResource(R.string.cancel),
                            tint = LauncherColors.Gray600
                        )
                    }
                }

                Text(
                    text = stringResource(R.string.settings_apps_page_hint),
                    style = MaterialTheme.typography.bodyMedium,
                    color = LauncherColors.Gray500
                )

                Spacer(modifier = Modifier.height(12.dp))

                ButtonPagedColumn(
                    items = installedApps,
                    pageSize = 6,
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) { app ->
                        val isHidden = app.packageName in hiddenApps
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (isHidden) LauncherColors.Gray100 else LauncherColors.Green50)
                                .clickable { onToggleApp(app.packageName) }
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Image(
                                bitmap = app.icon.toBitmap(96, 96).asImageBitmap(),
                                contentDescription = app.label,
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(RoundedCornerShape(10.dp))
                            )

                            Spacer(modifier = Modifier.width(12.dp))

                            Text(
                                text = app.label,
                                style = MaterialTheme.typography.titleMedium,
                                color = if (isHidden) LauncherColors.Gray500 else LauncherColors.Gray800,
                                modifier = Modifier.weight(1f)
                            )

                            Icon(
                                imageVector = if (isHidden) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                contentDescription = if (isHidden) "Hidden" else "Visible",
                                tint = if (isHidden) LauncherColors.Gray400 else LauncherColors.Green500
                            )
                        }
                }
            }
        }
    }
}

@Composable
fun QuickContactsDialog(
    quickContacts: List<QuickContact>,
    allContacts: List<QuickContact>,
    onDismiss: () -> Unit,
    onAddContact: (QuickContact) -> Unit,
    onRemoveContact: (QuickContact) -> Unit
) {
    var showAddScreen by rememberSaveable { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.85f)
                .padding(8.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (showAddScreen) {
                        IconButton(onClick = { showAddScreen = false }) {
                            Icon(
                                imageVector = Icons.Default.ArrowBack,
                                contentDescription = stringResource(R.string.back),
                                tint = LauncherColors.Gray600
                            )
                        }
                        Text(
                            text = stringResource(R.string.contacts_add),
                            style = MaterialTheme.typography.headlineSmall,
                            color = LauncherColors.Gray800
                        )
                    } else {
                        Text(
                            text = stringResource(R.string.settings_contacts),
                            style = MaterialTheme.typography.headlineSmall,
                            color = LauncherColors.Gray800
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = stringResource(R.string.cancel),
                            tint = LauncherColors.Gray600
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                if (showAddScreen) {
                    // Show all contacts to add
                    if (allContacts.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    imageVector = Icons.Default.Contacts,
                                    contentDescription = null,
                                    tint = LauncherColors.Gray400,
                                    modifier = Modifier.size(64.dp)
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    text = stringResource(R.string.contacts_empty),
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = LauncherColors.Gray500
                                )
                            }
                        }
                    } else {
                        ButtonPagedColumn(
                            items = allContacts,
                            pageSize = 6,
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) { contact ->
                                val isAlreadyAdded = quickContacts.any { it.id == contact.id }
                                ContactListItem(
                                    contact = contact,
                                    isAdded = isAlreadyAdded,
                                    onClick = {
                                        if (!isAlreadyAdded) {
                                            onAddContact(contact)
                                            showAddScreen = false
                                        }
                                    }
                                )
                        }
                    }
                } else {
                    // Show current quick contacts
                    if (quickContacts.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    imageVector = Icons.Default.PersonAdd,
                                    contentDescription = null,
                                    tint = LauncherColors.Gray400,
                                    modifier = Modifier.size(64.dp)
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    text = stringResource(R.string.contacts_empty),
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = LauncherColors.Gray500,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    } else {
                        ButtonPagedColumn(
                            items = quickContacts,
                            pageSize = 6,
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) { contact ->
                                QuickContactItem(
                                    contact = contact,
                                    onRemove = { onRemoveContact(contact) }
                                )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Add button
                    Button(
                        onClick = { showAddScreen = true },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = LauncherColors.Green500
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.PersonAdd,
                            contentDescription = null,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = stringResource(R.string.contacts_add),
                            fontSize = 16.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun QuickContactItem(
    contact: QuickContact,
    onRemove: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(LauncherColors.Orange50)
            .border(1.dp, LauncherColors.Orange200, RoundedCornerShape(16.dp))
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Contact avatar
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(LauncherColors.Orange500),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = contact.name.firstOrNull()?.uppercase() ?: "?",
                style = MaterialTheme.typography.titleLarge,
                color = Color.White
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = contact.name,
                style = MaterialTheme.typography.titleMedium,
                color = LauncherColors.Gray800
            )
            Text(
                text = contact.phoneNumber,
                style = MaterialTheme.typography.bodyMedium,
                color = LauncherColors.Gray600
            )
        }

        IconButton(
            onClick = onRemove,
            modifier = Modifier.size(40.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Delete,
                contentDescription = stringResource(R.string.settings_apps_clear),
                tint = LauncherColors.Red500
            )
        }
    }
}

@Composable
fun ContactListItem(
    contact: QuickContact,
    isAdded: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(if (isAdded) LauncherColors.Gray100 else Color.Transparent)
            .clickable(enabled = !isAdded, onClick = onClick)
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Contact avatar
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(if (isAdded) LauncherColors.Gray400 else LauncherColors.Blue500),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = contact.name.firstOrNull()?.uppercase() ?: "?",
                style = MaterialTheme.typography.titleMedium,
                color = Color.White
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = contact.name,
                style = MaterialTheme.typography.titleMedium,
                color = if (isAdded) LauncherColors.Gray500 else LauncherColors.Gray800
            )
            Text(
                text = contact.phoneNumber,
                style = MaterialTheme.typography.bodySmall,
                color = LauncherColors.Gray500
            )
        }

        if (isAdded) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = stringResource(R.string.status_selected),
                tint = LauncherColors.Green500
            )
        }
    }
}

@Composable
fun ColorsDialog(
    currentColor: String,
    onDismiss: () -> Unit,
    onSelectColor: (String) -> Unit
) {
    val colorOptions = listOf(
        "blue" to LauncherColors.Blue500,
        "green" to LauncherColors.Green500,
        "purple" to LauncherColors.Purple500,
        "orange" to LauncherColors.Orange500,
        "red" to LauncherColors.Red500,
        "teal" to Color(0xFF14B8A6)
    )

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Default.Palette,
                    contentDescription = null,
                    tint = LauncherColors.Purple500,
                    modifier = Modifier.size(48.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = stringResource(R.string.settings_colors),
                    style = MaterialTheme.typography.headlineMedium,
                    color = LauncherColors.Gray800
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Color grid - 3 columns
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    for (row in colorOptions.chunked(3)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            for ((colorName, colorValue) in row) {
                                val isSelected = currentColor == colorName
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .aspectRatio(1f)
                                        .clip(RoundedCornerShape(16.dp))
                                        .background(colorValue)
                                        .then(
                                            if (isSelected) Modifier.border(
                                                4.dp,
                                                LauncherColors.Gray800,
                                                RoundedCornerShape(16.dp)
                                            ) else Modifier
                                        )
                                        .clickable { onSelectColor(colorName) },
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (isSelected) {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = stringResource(R.string.status_selected),
                                            tint = Color.White,
                                            modifier = Modifier.size(32.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                OutlinedButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text(
                        text = stringResource(R.string.cancel),
                        fontSize = 16.sp
                    )
                }
            }
        }
    }
}

@Composable
fun ComingSoonDialog(
    title: String,
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
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Default.Construction,
                    contentDescription = null,
                    tint = LauncherColors.Orange500,
                    modifier = Modifier.size(64.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = title,
                    style = MaterialTheme.typography.headlineMedium,
                    color = LauncherColors.Gray800
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = stringResource(R.string.settings_coming_soon),
                    style = MaterialTheme.typography.bodyLarge,
                    color = LauncherColors.Gray500,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = onDismiss,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = LauncherColors.Blue500
                    )
                ) {
                    Text(
                        text = stringResource(R.string.ok),
                        fontSize = 16.sp,
                        maxLines = 1
                    )
                }
            }
        }
    }
}

@Composable
fun SettingsItem(
    title: String,
    subtitle: String,
    icon: ImageVector,
    iconColor: Color,
    onClick: () -> Unit
) {
    val itemDescription = "$title: $subtitle"

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(LauncherColors.Gray50)
            .border(1.dp, LauncherColors.Gray200, RoundedCornerShape(20.dp))
            .clickable(
                indication = ripple(color = iconColor),
                interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
                onClick = onClick
            )
            .padding(20.dp)
            .semantics { contentDescription = itemDescription },
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Icon - 64dp for elderly
        Box(
            modifier = Modifier
                .size(64.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(iconColor.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null, // Parent has description
                tint = iconColor,
                modifier = Modifier.size(32.dp)
            )
        }

        Spacer(modifier = Modifier.width(16.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                color = LauncherColors.Gray800,
                fontSize = 20.sp
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyLarge,
                color = LauncherColors.Gray600,
                fontSize = 16.sp
            )
        }

        Icon(
            imageVector = Icons.Default.ChevronRight,
            contentDescription = stringResource(R.string.action_open),
            tint = LauncherColors.Gray600,
            modifier = Modifier.size(32.dp)
        )
    }
}

@Composable
private fun homeSlotLabel(position: Int): String = when (position) {
    0 -> stringResource(R.string.settings_app_slot_1)
    1 -> stringResource(R.string.settings_app_slot_2)
    2 -> stringResource(R.string.settings_app_slot_3)
    3 -> stringResource(R.string.settings_app_slot_4)
    else -> stringResource(R.string.settings_app_slot_n, position + 1)
}

@Composable
fun HomeAppsDialog(
    installedApps: List<AppInfo>,
    currentHomeApps: Map<Int, String>,
    onDismiss: () -> Unit,
    onAppSelected: (Int, String) -> Unit,
    onAppCleared: (Int) -> Unit
) {
    var selectedPosition by rememberSaveable { mutableIntStateOf(-1) }
    val extraSlots = currentHomeApps.keys
        .filter { it >= LauncherViewModel.DEFAULT_HOME_SLOTS }
        .sorted()
    val nextExtraSlot = (LauncherViewModel.DEFAULT_HOME_SLOTS until LauncherViewModel.MAX_HOME_APPS)
        .firstOrNull { it !in currentHomeApps }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.9f)
                .padding(8.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(R.string.settings_apps),
                        style = MaterialTheme.typography.headlineSmall,
                        color = LauncherColors.Gray800
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = stringResource(R.string.cancel),
                            tint = LauncherColors.Gray600
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                if (selectedPosition == -1) {
                    Text(
                        text = stringResource(R.string.settings_apps_choose_slot),
                        style = MaterialTheme.typography.bodyLarge,
                        color = LauncherColors.Gray600
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    ButtonPagedColumn(
                        items = (0 until LauncherViewModel.DEFAULT_HOME_SLOTS).toList() + extraSlots,
                        pageSize = 6,
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) { position ->
                            val packageName = currentHomeApps[position]
                            val appInfo = installedApps.find { it.packageName == packageName }

                            AppSlotItem(
                                slotName = homeSlotLabel(position),
                                appInfo = appInfo,
                                onClick = { selectedPosition = position },
                                onClear = if (packageName != null) {
                                    { onAppCleared(position) }
                                } else null
                            )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    if (nextExtraSlot != null) {
                        Button(
                            onClick = { selectedPosition = nextExtraSlot },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(64.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = LauncherColors.Green500
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = null,
                                modifier = Modifier.size(28.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = stringResource(R.string.settings_apps_add),
                                fontSize = 20.sp
                            )
                        }
                    } else {
                        Text(
                            text = stringResource(R.string.settings_apps_full),
                            style = MaterialTheme.typography.bodyLarge,
                            color = LauncherColors.Gray500,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                } else {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = { selectedPosition = -1 }) {
                            Icon(
                                imageVector = Icons.Default.ArrowBack,
                                contentDescription = stringResource(R.string.back),
                                tint = LauncherColors.Gray600
                            )
                        }
                        Text(
                            text = homeSlotLabel(selectedPosition),
                            style = MaterialTheme.typography.titleLarge,
                            color = LauncherColors.Gray800
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    ButtonPagedColumn(
                        items = installedApps,
                        pageSize = 6,
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) { app ->
                            AppListItem(
                                appInfo = app,
                                isSelected = currentHomeApps[selectedPosition] == app.packageName,
                                onClick = {
                                    onAppSelected(selectedPosition, app.packageName)
                                    selectedPosition = -1
                                }
                            )
                    }
                }
            }
        }
    }
}

@Composable
fun AppSlotItem(
    slotName: String,
    appInfo: AppInfo?,
    onClick: () -> Unit,
    onClear: (() -> Unit)?
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(LauncherColors.Gray50)
            .border(1.dp, LauncherColors.Gray200, RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // App icon or placeholder
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(if (appInfo != null) Color.Transparent else LauncherColors.Gray200),
            contentAlignment = Alignment.Center
        ) {
            if (appInfo != null) {
                Image(
                    bitmap = appInfo.icon.toBitmap(96, 96).asImageBitmap(),
                    contentDescription = appInfo.label,
                    modifier = Modifier.size(48.dp)
                )
            } else {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = null,
                    tint = LauncherColors.Gray500,
                    modifier = Modifier.size(24.dp)
                )
            }
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = slotName,
                style = MaterialTheme.typography.bodyMedium,
                color = LauncherColors.Gray500,
                fontSize = 12.sp
            )
            Text(
                text = appInfo?.label ?: stringResource(R.string.settings_apps_tap_to_select),
                style = MaterialTheme.typography.titleMedium,
                color = if (appInfo != null) LauncherColors.Gray800 else LauncherColors.Gray500
            )
        }

        if (onClear != null) {
            IconButton(
                onClick = onClear,
                modifier = Modifier.size(40.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Clear,
                    contentDescription = stringResource(R.string.settings_apps_clear),
                    tint = LauncherColors.Gray500
                )
            }
        }
    }
}

@Composable
fun AppListItem(
    appInfo: AppInfo,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(if (isSelected) LauncherColors.Green50 else Color.Transparent)
            .border(
                width = if (isSelected) 2.dp else 0.dp,
                color = if (isSelected) LauncherColors.Green500 else Color.Transparent,
                shape = RoundedCornerShape(12.dp)
            )
            .clickable(onClick = onClick)
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(
            bitmap = appInfo.icon.toBitmap(96, 96).asImageBitmap(),
            contentDescription = appInfo.label,
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(10.dp))
        )

        Spacer(modifier = Modifier.width(12.dp))

        Text(
            text = appInfo.label,
            style = MaterialTheme.typography.titleMedium,
            color = LauncherColors.Gray800,
            modifier = Modifier.weight(1f)
        )

        if (isSelected) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = stringResource(R.string.status_selected),
                tint = LauncherColors.Green500
            )
        }
    }
}

@Composable
fun GamesSelectionDialog(
    installedApps: List<AppInfo>,
    gameApps: Set<String>,
    onDismiss: () -> Unit,
    onToggleApp: (String) -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.85f)
                .padding(8.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(R.string.settings_games),
                        style = MaterialTheme.typography.headlineSmall,
                        color = LauncherColors.Gray800
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = stringResource(R.string.cancel),
                            tint = LauncherColors.Gray600
                        )
                    }
                }

                Text(
                    text = stringResource(R.string.settings_games_hint),
                    style = MaterialTheme.typography.bodyMedium,
                    color = LauncherColors.Gray500
                )

                Spacer(modifier = Modifier.height(12.dp))

                ButtonPagedColumn(
                    items = installedApps,
                    pageSize = 6,
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) { app ->
                        val isGame = app.packageName in gameApps
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (isGame) LauncherColors.Purple50 else LauncherColors.Gray50)
                                .clickable { onToggleApp(app.packageName) }
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Image(
                                bitmap = app.icon.toBitmap(96, 96).asImageBitmap(),
                                contentDescription = app.label,
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(RoundedCornerShape(10.dp))
                            )

                            Spacer(modifier = Modifier.width(12.dp))

                            Text(
                                text = app.label,
                                style = MaterialTheme.typography.titleMedium,
                                color = if (isGame) LauncherColors.Gray800 else LauncherColors.Gray500,
                                modifier = Modifier.weight(1f)
                            )

                            Icon(
                                imageVector = if (isGame) Icons.Default.SportsEsports else Icons.Default.Add,
                                contentDescription = if (isGame) "Game" else "Add",
                                tint = if (isGame) LauncherColors.Purple500 else LauncherColors.Gray400
                            )
                        }
                }
            }
        }
    }
}

@Composable
fun PhotosSelectionDialog(
    selectedPhotos: Set<String>,
    onDismiss: () -> Unit,
    onAddPhoto: (String) -> Unit,
    onRemovePhoto: (String) -> Unit
) {
    val context = LocalContext.current

    // Photo picker launcher
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenMultipleDocuments()
    ) { uris: List<Uri> ->
        uris.forEach { uri ->
            try {
                context.contentResolver.takePersistableUriPermission(
                    uri,
                    android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
            } catch (_: Exception) {
                // Some providers do not support persistable grants
            }
            onAddPhoto(uri.toString())
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.85f)
                .padding(8.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(R.string.settings_photos),
                        style = MaterialTheme.typography.headlineSmall,
                        color = LauncherColors.Gray800
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = stringResource(R.string.cancel),
                            tint = LauncherColors.Gray600
                        )
                    }
                }

                Text(
                    text = stringResource(R.string.settings_photos_hint),
                    style = MaterialTheme.typography.bodyMedium,
                    color = LauncherColors.Gray500
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Add photo button
                Button(
                    onClick = { photoPickerLauncher.launch(arrayOf("image/*")) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = LauncherColors.Pink500
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.AddPhotoAlternate,
                        contentDescription = null,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = stringResource(R.string.photos_add),
                        fontSize = 16.sp
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                if (selectedPhotos.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.PhotoLibrary,
                                contentDescription = null,
                                tint = LauncherColors.Gray400,
                                modifier = Modifier.size(64.dp)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = stringResource(R.string.photos_none_selected),
                                style = MaterialTheme.typography.bodyLarge,
                                color = LauncherColors.Gray500,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                } else {
                    ButtonPagedColumn(
                        items = selectedPhotos.toList(),
                        pageSize = 4,
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) { photoUri ->
                            PhotoListItem(
                                photoUri = photoUri,
                                onRemove = { onRemovePhoto(photoUri) }
                            )
                    }
                }
            }
        }
    }
}

@Composable
fun PhotoListItem(
    photoUri: String,
    onRemove: () -> Unit
) {
    val context = LocalContext.current
    val painter = rememberAsyncImagePainter(
        ImageRequest.Builder(context)
            .data(Uri.parse(photoUri))
            .crossfade(true)
            .build()
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(LauncherColors.Pink50)
            .border(1.dp, LauncherColors.Pink200, RoundedCornerShape(16.dp))
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Photo thumbnail
        Image(
            painter = painter,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(64.dp)
                .clip(RoundedCornerShape(12.dp))
        )

        Spacer(modifier = Modifier.width(12.dp))

        Text(
            text = stringResource(R.string.photos_favorite),
            style = MaterialTheme.typography.titleMedium,
            color = LauncherColors.Gray800,
            modifier = Modifier.weight(1f)
        )

        IconButton(
            onClick = onRemove,
            modifier = Modifier.size(40.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Delete,
                contentDescription = stringResource(R.string.settings_apps_clear),
                tint = LauncherColors.Red500
            )
        }
    }
}

@Composable
fun BrightnessDialog(
    percent: Int,
    locked: Boolean,
    onDismiss: () -> Unit,
    onPercentChange: (Int) -> Unit,
    onLockedChange: (Boolean) -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    var canWrite by remember { mutableStateOf(BrightnessHelper.canWriteSettings(context)) }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                canWrite = BrightnessHelper.canWriteSettings(context)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Default.BrightnessHigh,
                    contentDescription = null,
                    tint = LauncherColors.Orange500,
                    modifier = Modifier.size(48.dp)
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = stringResource(R.string.settings_brightness),
                    style = MaterialTheme.typography.headlineSmall,
                    color = LauncherColors.Gray800
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = stringResource(R.string.settings_brightness_subtitle),
                    style = MaterialTheme.typography.bodyMedium,
                    color = LauncherColors.Gray500,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(24.dp))

                if (!canWrite) {
                    Text(
                        text = stringResource(R.string.settings_brightness_permission),
                        style = MaterialTheme.typography.bodyLarge,
                        color = LauncherColors.Gray700,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = { BrightnessHelper.requestWriteSettings(context) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = LauncherColors.Orange500
                        )
                    ) {
                        Text(
                            text = stringResource(R.string.settings_brightness_permission_button),
                            fontSize = 18.sp
                        )
                    }
                } else {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        val decreaseDesc = stringResource(R.string.settings_brightness_decrease)
                        val increaseDesc = stringResource(R.string.settings_brightness_increase)
                        IconButton(
                            onClick = {
                                onPercentChange(percent - BrightnessHelper.STEP)
                            },
                            enabled = percent > BrightnessHelper.MIN_PERCENT,
                            modifier = Modifier
                                .size(64.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(LauncherColors.Gray100)
                                .semantics { contentDescription = decreaseDesc }
                        ) {
                            Text(
                                text = "−",
                                fontSize = 32.sp,
                                color = LauncherColors.Gray800
                            )
                        }

                        Text(
                            text = stringResource(R.string.settings_brightness_value, percent),
                            style = MaterialTheme.typography.displaySmall,
                            color = LauncherColors.Gray800,
                            fontWeight = FontWeight.Bold
                        )

                        IconButton(
                            onClick = {
                                onPercentChange(percent + BrightnessHelper.STEP)
                            },
                            enabled = percent < BrightnessHelper.MAX_PERCENT,
                            modifier = Modifier
                                .size(64.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(LauncherColors.Gray100)
                                .semantics { contentDescription = increaseDesc }
                        ) {
                            Text(
                                text = "+",
                                fontSize = 32.sp,
                                color = LauncherColors.Gray800
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(if (locked) LauncherColors.Orange50 else LauncherColors.Gray50)
                            .clickable { onLockedChange(!locked) }
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (locked) Icons.Default.Lock else Icons.Default.LockOpen,
                            contentDescription = null,
                            tint = if (locked) LauncherColors.Orange500 else LauncherColors.Gray500,
                            modifier = Modifier.size(32.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = stringResource(R.string.settings_brightness_lock),
                                style = MaterialTheme.typography.titleMedium,
                                color = LauncherColors.Gray800
                            )
                            Text(
                                text = stringResource(R.string.settings_brightness_lock_desc),
                                style = MaterialTheme.typography.bodySmall,
                                color = LauncherColors.Gray500
                            )
                        }
                        Switch(
                            checked = locked,
                            onCheckedChange = onLockedChange,
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = LauncherColors.Orange500
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text(
                        text = stringResource(R.string.cancel),
                        fontSize = 16.sp
                    )
                }
            }
        }
    }
}
