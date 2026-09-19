package com.elderlylauncher.util

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import androidx.core.content.FileProvider
import org.json.JSONObject
import java.io.File
import java.net.HttpURLConnection
import java.net.URL

data class LatestRelease(
    val tagName: String,
    val assetName: String,
    val downloadUrl: String
) {
    val displayVersion: String
        get() = tagName.removePrefix("v").removePrefix("V")
}

object AppUpdater {
    private const val RELEASES_URL =
        "https://api.github.com/repos/ltechconsultancy/elderly-launcher/releases/latest"
    private const val USER_AGENT = "ElderlyLauncher"
    private val mainHandler = Handler(Looper.getMainLooper())

    fun currentVersionName(context: Context): String {
        return try {
            val info = context.packageManager.getPackageInfo(context.packageName, 0)
            info.versionName?.removePrefix("v")?.removePrefix("V").orEmpty()
        } catch (_: Exception) {
            ""
        }
    }

    fun isNewer(remoteTag: String, currentVersion: String): Boolean {
        return compareVersions(remoteTag, currentVersion) > 0
    }

    fun compareVersions(left: String, right: String): Int {
        val a = versionParts(left)
        val b = versionParts(right)
        val size = maxOf(a.size, b.size)
        for (i in 0 until size) {
            val av = a.getOrElse(i) { 0 }
            val bv = b.getOrElse(i) { 0 }
            if (av != bv) return av.compareTo(bv)
        }
        return 0
    }

    private fun versionParts(raw: String): List<Int> {
        return raw.removePrefix("v").removePrefix("V")
            .split(Regex("[^0-9]+"))
            .filter { it.isNotBlank() }
            .map { it.toIntOrNull() ?: 0 }
    }

    fun canInstallPackages(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.packageManager.canRequestPackageInstalls()
        } else {
            true
        }
    }

    fun requestInstallPermission(context: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val intent = Intent(
            Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES,
            Uri.parse("package:${context.packageName}")
        )
        if (context !is Activity) {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
    }

    fun fetchLatest(): LatestRelease {
        val body = httpGetText(RELEASES_URL)
        val json = JSONObject(body)
        val tag = json.getString("tag_name")
        val assets = json.getJSONArray("assets")
        for (i in 0 until assets.length()) {
            val asset = assets.getJSONObject(i)
            val name = asset.optString("name")
            val url = asset.optString("browser_download_url")
            if (name.endsWith(".apk", ignoreCase = true) && url.isNotBlank()) {
                return LatestRelease(tag, name, url)
            }
        }
        throw IllegalStateException("No APK in latest release")
    }

    fun downloadApk(
        context: Context,
        release: LatestRelease,
        onProgress: (Int) -> Unit
    ): File {
        val dir = File(context.cacheDir, "updates").apply { mkdirs() }
        val dest = File(dir, "update.apk")
        val part = File(dir, "update.apk.part")
        if (part.exists()) part.delete()
        if (dest.exists()) dest.delete()

        val connection = open(release.downloadUrl)
        try {
            if (connection.responseCode !in 200..299) {
                throw IllegalStateException("Download HTTP ${connection.responseCode}")
            }
            val length = connection.contentLengthLong
            connection.inputStream.use { input ->
                part.outputStream().use { output ->
                    val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
                    var copied = 0L
                    var lastPercent = -1
                    while (true) {
                        val read = input.read(buffer)
                        if (read < 0) break
                        output.write(buffer, 0, read)
                        copied += read
                        if (length > 0) {
                            val percent = ((copied * 100) / length).toInt().coerceIn(0, 100)
                            if (percent != lastPercent) {
                                lastPercent = percent
                                mainHandler.post { onProgress(percent) }
                            }
                        }
                    }
                }
            }
        } finally {
            connection.disconnect()
        }
        if (!part.renameTo(dest)) {
            part.copyTo(dest, overwrite = true)
            part.delete()
        }
        mainHandler.post { onProgress(100) }
        return dest
    }

    fun installApk(context: Context, apk: File) {
        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            apk
        )
        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, "application/vnd.android.package-archive")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
    }

    private fun httpGetText(url: String): String {
        val connection = open(url)
        try {
            if (connection.responseCode !in 200..299) {
                throw IllegalStateException("GitHub HTTP ${connection.responseCode}")
            }
            return connection.inputStream.bufferedReader().use { it.readText() }
        } finally {
            connection.disconnect()
        }
    }

    private fun open(url: String): HttpURLConnection {
        var current = url
        repeat(8) {
            val connection = URL(current).openConnection() as HttpURLConnection
            connection.instanceFollowRedirects = false
            connection.requestMethod = "GET"
            connection.connectTimeout = 20_000
            connection.readTimeout = 60_000
            connection.setRequestProperty("User-Agent", USER_AGENT)
            if (current.contains("api.github.com")) {
                connection.setRequestProperty("Accept", "application/vnd.github+json")
            } else {
                connection.setRequestProperty("Accept", "*/*")
            }
            val code = connection.responseCode
            if (code in 300..399) {
                val location = connection.getHeaderField("Location")
                connection.disconnect()
                if (location.isNullOrBlank()) {
                    throw IllegalStateException("Redirect without Location")
                }
                current = URL(URL(current), location).toString()
            } else {
                return connection
            }
        }
        throw IllegalStateException("Too many redirects")
    }
}
