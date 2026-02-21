package com.elderlylauncher.util

import android.app.Activity
import android.content.Context
import android.content.res.Configuration
import android.os.Build
import java.util.Locale

object LocaleHelper {

    fun setLocale(context: Context, languageCode: String): Context {
        val locale = Locale(languageCode)
        Locale.setDefault(locale)

        val config = Configuration(context.resources.configuration)
        config.setLocale(locale)

        return context.createConfigurationContext(config)
    }

    fun applyLocale(activity: Activity, languageCode: String) {
        val locale = Locale(languageCode)
        Locale.setDefault(locale)

        val config = Configuration(activity.resources.configuration)
        config.setLocale(locale)

        activity.resources.updateConfiguration(config, activity.resources.displayMetrics)

        // Recreate to apply the new locale
        activity.recreate()
    }

    fun getCurrentLocale(context: Context): String {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            context.resources.configuration.locales[0].language
        } else {
            @Suppress("DEPRECATION")
            context.resources.configuration.locale.language
        }
    }
}
