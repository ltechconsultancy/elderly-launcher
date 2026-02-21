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
import com.elderlylauncher.ui.LauncherViewModel
import com.elderlylauncher.ui.theme.LauncherColors
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
                onClick = { /* TODO */ }
            )

            SettingsItem(
                title = stringResource(R.string.settings_language),
                subtitle = stringResource(R.string.language_current),
                icon = Icons.Default.Language,
                iconColor = LauncherColors.Blue500,
                onClick = { /* TODO */ }
            )

            SettingsItem(
                title = stringResource(R.string.settings_apps),
                subtitle = stringResource(R.string.settings_apps_subtitle),
                icon = Icons.Default.Apps,
                iconColor = LauncherColors.Green500,
                onClick = { /* TODO */ }
            )

            SettingsItem(
                title = stringResource(R.string.settings_contacts),
                subtitle = stringResource(R.string.settings_contacts_subtitle),
                icon = Icons.Default.Contacts,
                iconColor = LauncherColors.Orange500,
                onClick = { /* TODO */ }
            )

            SettingsItem(
                title = stringResource(R.string.settings_emergency),
                subtitle = stringResource(R.string.emergency_number_default),
                icon = Icons.Default.Emergency,
                iconColor = LauncherColors.Red500,
                onClick = { /* TODO */ }
            )

            SettingsItem(
                title = stringResource(R.string.settings_password_change),
                subtitle = stringResource(R.string.settings_password_change_subtitle),
                icon = Icons.Default.Key,
                iconColor = LauncherColors.Gray600,
                onClick = { /* TODO */ }
            )

            Spacer(modifier = Modifier.height(16.dp))
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
            tint = LauncherColors.Gray400,
            modifier = Modifier.size(32.dp)
        )
    }
}
