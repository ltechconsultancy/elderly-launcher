package com.elderlylauncher

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.elderlylauncher.ui.LauncherApp
import com.elderlylauncher.ui.LauncherViewModel
import com.elderlylauncher.ui.theme.ElderlyLauncherTheme
import com.elderlylauncher.ui.theme.LauncherColors
import com.elderlylauncher.utils.PermissionHelper

class MainActivity : ComponentActivity() {

    // Permission request launcher
    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        // Permissions result handled - UI will update via recomposition
        // No need to do anything here as the UI observes permission state
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            ElderlyLauncherTheme {
                val viewModel: LauncherViewModel = viewModel()

                // Check if we need to show permission request
                val missingPermissions = remember { PermissionHelper.getMissingPermissions(this) }
                var showPermissionUI by remember { mutableStateOf(missingPermissions.isNotEmpty()) }

                // Re-check permissions when activity resumes
                LaunchedEffect(Unit) {
                    showPermissionUI = PermissionHelper.getMissingPermissions(this@MainActivity).isNotEmpty()
                }

                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = LauncherColors.White
                ) {
                    if (showPermissionUI && missingPermissions.isNotEmpty()) {
                        PermissionRequestScreen(
                            onRequestPermissions = {
                                permissionLauncher.launch(missingPermissions.toTypedArray())
                            },
                            onSkip = {
                                showPermissionUI = false
                            }
                        )
                    } else {
                        LauncherApp(viewModel = viewModel)
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        // Reload data when returning to the launcher (e.g., after app install/uninstall)
    }

    // Override back button to prevent leaving the launcher
    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        // Do nothing - we're the home screen
    }
}

/**
 * Permission request screen with elderly-friendly design
 */
@Composable
fun PermissionRequestScreen(
    onRequestPermissions: () -> Unit,
    onSkip: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(LauncherColors.White)
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Icon
        Box(
            modifier = Modifier
                .size(100.dp)
                .background(LauncherColors.Blue100, RoundedCornerShape(24.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Security,
                contentDescription = null,
                modifier = Modifier.size(56.dp),
                tint = LauncherColors.Blue500
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Title
        Text(
            text = stringResource(R.string.permission_title),
            style = MaterialTheme.typography.headlineLarge,
            color = LauncherColors.Gray800,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Description
        Text(
            text = stringResource(R.string.permission_description),
            style = MaterialTheme.typography.bodyLarge,
            color = LauncherColors.Gray600,
            textAlign = TextAlign.Center,
            fontSize = 18.sp,
            lineHeight = 26.sp
        )

        Spacer(modifier = Modifier.height(48.dp))

        // Allow button - large for elderly
        Button(
            onClick = onRequestPermissions,
            modifier = Modifier
                .fillMaxWidth()
                .height(72.dp),
            shape = RoundedCornerShape(20.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = LauncherColors.Blue500
            )
        ) {
            Text(
                text = stringResource(R.string.permission_allow),
                fontSize = 22.sp
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Skip button
        TextButton(
            onClick = onSkip,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
        ) {
            Text(
                text = stringResource(R.string.permission_skip),
                fontSize = 18.sp,
                color = LauncherColors.Gray500
            )
        }
    }
}
