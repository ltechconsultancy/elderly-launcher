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
import com.elderlylauncher.ui.LauncherViewModel
import com.elderlylauncher.ui.theme.LauncherColors
import androidx.compose.foundation.Image
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.ui.graphics.asImageBitmap
import androidx.core.graphics.drawable.toBitmap
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
    val lockDescription = stringResource(R.string.settings_lock_description)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(LauncherColors.White)
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
                            fontSize = 18.sp
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
                            fontSize = 18.sp
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
    var showContactsDialog by rememberSaveable { mutableStateOf(false) }

    // Emergency number state
    var emergencyNumber by rememberSaveable { mutableStateOf("112") }

    // Password change state
    var currentPassword by rememberSaveable { mutableStateOf("") }
    var newPassword by rememberSaveable { mutableStateOf("") }
    var confirmPassword by rememberSaveable { mutableStateOf("") }
    var passwordError by rememberSaveable { mutableStateOf<String?>(null) }

    // Emergency Number Dialog
    if (showEmergencyDialog) {
        EmergencyNumberDialog(
            currentNumber = emergencyNumber,
            onDismiss = { showEmergencyDialog = false },
            onConfirm = { newNumber ->
                emergencyNumber = newNumber
                showEmergencyDialog = false
            }
        )
    }

    // Language Dialog
    if (showLanguageDialog) {
        LanguageDialog(
            onDismiss = { showLanguageDialog = false },
            onSelectLanguage = { languageCode ->
                // Change app locale
                val locale = java.util.Locale(languageCode)
                val config = context.resources.configuration
                config.setLocale(locale)
                context.createConfigurationContext(config)
                showLanguageDialog = false
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

    // Colors Dialog (Coming Soon)
    if (showColorsDialog) {
        ComingSoonDialog(
            title = stringResource(R.string.settings_colors),
            onDismiss = { showColorsDialog = false }
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

    // Contacts Dialog (Coming Soon)
    if (showContactsDialog) {
        ComingSoonDialog(
            title = stringResource(R.string.settings_contacts),
            onDismiss = { showContactsDialog = false }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(LauncherColors.White)
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

        // Settings list
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            SettingsItem(
                title = stringResource(R.string.settings_colors),
                subtitle = stringResource(R.string.settings_colors_subtitle),
                icon = Icons.Default.Palette,
                iconColor = LauncherColors.Purple500,
                onClick = { showColorsDialog = true }
            )

            SettingsItem(
                title = stringResource(R.string.settings_language),
                subtitle = stringResource(R.string.language_current),
                icon = Icons.Default.Language,
                iconColor = LauncherColors.Blue500,
                onClick = { showLanguageDialog = true }
            )

            SettingsItem(
                title = stringResource(R.string.settings_apps),
                subtitle = stringResource(R.string.settings_apps_subtitle),
                icon = Icons.Default.Apps,
                iconColor = LauncherColors.Green500,
                onClick = { showAppsDialog = true }
            )

            SettingsItem(
                title = stringResource(R.string.settings_contacts),
                subtitle = stringResource(R.string.settings_contacts_subtitle),
                icon = Icons.Default.Contacts,
                iconColor = LauncherColors.Orange500,
                onClick = { showContactsDialog = true }
            )

            SettingsItem(
                title = stringResource(R.string.settings_emergency),
                subtitle = emergencyNumber,
                icon = Icons.Default.Emergency,
                iconColor = LauncherColors.Red500,
                onClick = { showEmergencyDialog = true }
            )

            SettingsItem(
                title = stringResource(R.string.settings_password_change),
                subtitle = stringResource(R.string.settings_password_change_subtitle),
                icon = Icons.Default.Key,
                iconColor = LauncherColors.Gray600,
                onClick = { showPasswordChangeDialog = true }
            )

            Spacer(modifier = Modifier.height(16.dp))
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
                    onValueChange = { if (it.length <= 15) number = it },
                    label = { Text(stringResource(R.string.settings_emergency_hint)) },
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
                            fontSize = 16.sp
                        )
                    }

                    Button(
                        onClick = { onConfirm(number) },
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
                            fontSize = 16.sp
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
                            fontSize = 16.sp
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
                            fontSize = 16.sp
                        )
                    }
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
                        fontSize = 18.sp
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
fun HomeAppsDialog(
    installedApps: List<AppInfo>,
    currentHomeApps: Map<Int, String>,
    onDismiss: () -> Unit,
    onAppSelected: (Int, String) -> Unit,
    onAppCleared: (Int) -> Unit
) {
    var selectedPosition by rememberSaveable { mutableIntStateOf(-1) }
    val positionNames = listOf(
        stringResource(R.string.settings_app_slot_1),
        stringResource(R.string.settings_app_slot_2),
        stringResource(R.string.settings_app_slot_3),
        stringResource(R.string.settings_app_slot_4)
    )

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
                modifier = Modifier.padding(16.dp)
            ) {
                // Header
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
                    // Show 4 app slots
                    Text(
                        text = stringResource(R.string.settings_apps_choose_slot),
                        style = MaterialTheme.typography.bodyLarge,
                        color = LauncherColors.Gray600
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    Column(
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        for (position in 0..3) {
                            val packageName = currentHomeApps[position]
                            val appInfo = installedApps.find { it.packageName == packageName }

                            AppSlotItem(
                                slotName = positionNames[position],
                                appInfo = appInfo,
                                onClick = { selectedPosition = position },
                                onClear = if (packageName != null) {
                                    { onAppCleared(position) }
                                } else null
                            )
                        }
                    }
                } else {
                    // Show app list for selection
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
                            text = positionNames[selectedPosition],
                            style = MaterialTheme.typography.titleLarge,
                            color = LauncherColors.Gray800
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        items(installedApps) { app ->
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
