package com.elderlylauncher.ui

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.elderlylauncher.R
import com.elderlylauncher.ui.theme.LauncherColors

@Composable
fun BrightnessFloorDialog(onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = stringResource(R.string.brightness_minimum_title),
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = LauncherColors.Gray800
            )
        },
        text = {
            Text(
                text = stringResource(R.string.brightness_minimum_message),
                fontSize = 22.sp,
                color = LauncherColors.Gray700
            )
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text(
                    text = stringResource(R.string.brightness_understood),
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    )
}
