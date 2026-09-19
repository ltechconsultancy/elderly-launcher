package com.elderlylauncher.util

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageInstaller
import android.os.Build
import android.widget.Toast
import com.elderlylauncher.R

class UpdateInstallReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val status = intent.getIntExtra(
            PackageInstaller.EXTRA_STATUS,
            PackageInstaller.STATUS_FAILURE
        )
        when (status) {
            PackageInstaller.STATUS_PENDING_USER_ACTION -> {
                val confirm = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    intent.getParcelableExtra(Intent.EXTRA_INTENT, Intent::class.java)
                } else {
                    @Suppress("DEPRECATION")
                    intent.getParcelableExtra(Intent.EXTRA_INTENT)
                } ?: return
                confirm.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(confirm)
            }
            PackageInstaller.STATUS_SUCCESS -> {
                Toast.makeText(
                    context,
                    context.getString(R.string.settings_update_success),
                    Toast.LENGTH_LONG
                ).show()
            }
            PackageInstaller.STATUS_FAILURE_CONFLICT,
            PackageInstaller.STATUS_FAILURE_INCOMPATIBLE -> {
                Toast.makeText(
                    context,
                    context.getString(R.string.settings_update_signature),
                    Toast.LENGTH_LONG
                ).show()
            }
            else -> {
                Toast.makeText(
                    context,
                    context.getString(R.string.settings_update_failed),
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    companion object {
        const val ACTION_INSTALL_COMPLETE = "com.elderlylauncher.INSTALL_COMPLETE"
    }
}
