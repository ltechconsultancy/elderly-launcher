package com.elderlylauncher.data

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.graphics.drawable.Drawable
import android.os.Build
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

private const val TAG = "AppRepository"

/**
 * Repository for getting installed apps.
 * Uses applicationContext to prevent memory leaks.
 */
class AppRepository(context: Context) {

    // Use applicationContext to prevent Activity/Fragment context leaks
    private val appContext: Context = context.applicationContext
    private val packageManager: PackageManager = appContext.packageManager

    /**
     * Get all launchable apps
     */
    suspend fun getInstalledApps(): List<AppInfo> = withContext(Dispatchers.IO) {
        try {
            val intent = Intent(Intent.ACTION_MAIN).apply {
                addCategory(Intent.CATEGORY_LAUNCHER)
            }

            val resolveInfoList: List<ResolveInfo> = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                packageManager.queryIntentActivities(
                    intent,
                    PackageManager.ResolveInfoFlags.of(0)
                )
            } else {
                @Suppress("DEPRECATION")
                packageManager.queryIntentActivities(intent, 0)
            }

            resolveInfoList
                .filter { it.activityInfo.packageName != appContext.packageName } // Exclude self
                .mapNotNull { resolveInfo ->
                    try {
                        AppInfo(
                            packageName = resolveInfo.activityInfo.packageName,
                            label = resolveInfo.loadLabel(packageManager).toString(),
                            icon = resolveInfo.loadIcon(packageManager),
                            isSystemApp = isSystemApp(resolveInfo.activityInfo.packageName)
                        )
                    } catch (e: Exception) {
                        Log.e(TAG, "Error loading app info for ${resolveInfo.activityInfo.packageName}", e)
                        null
                    }
                }
                .sortedBy { it.label.lowercase() }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting installed apps", e)
            emptyList()
        }
    }

    /**
     * Get app icon for a package
     */
    fun getAppIcon(packageName: String): Drawable? {
        return try {
            packageManager.getApplicationIcon(packageName)
        } catch (e: PackageManager.NameNotFoundException) {
            Log.w(TAG, "App icon not found for $packageName")
            null
        }
    }

    /**
     * Get the default app for a category
     */
    fun getDefaultApp(category: AppCategory): String? {
        val intent = when (category) {
            AppCategory.PHONE -> Intent(Intent.ACTION_DIAL)
            AppCategory.MESSAGES -> Intent(Intent.ACTION_MAIN).apply {
                addCategory(Intent.CATEGORY_APP_MESSAGING)
            }
            AppCategory.CAMERA -> Intent("android.media.action.IMAGE_CAPTURE")
            AppCategory.PHOTOS -> Intent(Intent.ACTION_VIEW).apply {
                type = "image/*"
            }
            AppCategory.BROWSER -> Intent(Intent.ACTION_VIEW).apply {
                data = android.net.Uri.parse("https://")
            }
            AppCategory.SETTINGS -> Intent(android.provider.Settings.ACTION_SETTINGS)
            AppCategory.OTHER -> return null
        }

        return try {
            val resolveInfo = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                packageManager.resolveActivity(
                    intent,
                    PackageManager.ResolveInfoFlags.of(PackageManager.MATCH_DEFAULT_ONLY.toLong())
                )
            } else {
                @Suppress("DEPRECATION")
                packageManager.resolveActivity(intent, PackageManager.MATCH_DEFAULT_ONLY)
            }
            resolveInfo?.activityInfo?.packageName
        } catch (e: Exception) {
            Log.e(TAG, "Error getting default app for $category", e)
            null
        }
    }

    /**
     * Launch an app by package name
     * @return true if app was launched successfully, false otherwise
     */
    fun launchApp(packageName: String): Boolean {
        return try {
            val intent = packageManager.getLaunchIntentForPackage(packageName)
            if (intent != null) {
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                appContext.startActivity(intent)
                true
            } else {
                Log.w(TAG, "No launch intent for $packageName")
                false
            }
        } catch (e: ActivityNotFoundException) {
            Log.e(TAG, "Activity not found for $packageName", e)
            false
        } catch (e: SecurityException) {
            Log.e(TAG, "Security exception launching $packageName", e)
            false
        }
    }

    private fun isSystemApp(packageName: String): Boolean {
        return try {
            val appInfo = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                packageManager.getApplicationInfo(
                    packageName,
                    PackageManager.ApplicationInfoFlags.of(0)
                )
            } else {
                @Suppress("DEPRECATION")
                packageManager.getApplicationInfo(packageName, 0)
            }
            (appInfo.flags and android.content.pm.ApplicationInfo.FLAG_SYSTEM) != 0
        } catch (e: PackageManager.NameNotFoundException) {
            false
        }
    }
}
