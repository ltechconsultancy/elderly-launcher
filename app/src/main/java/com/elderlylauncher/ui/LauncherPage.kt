package com.elderlylauncher.ui

enum class LauncherPage {
    HOME,
    APPS,
    GAMES,
    VOLUME,
    NOTIFICATIONS,
    PHOTOS,
    SETTINGS;

    companion object {
        val DEFAULT = listOf(HOME, APPS, GAMES, VOLUME, NOTIFICATIONS, PHOTOS, SETTINGS)

        fun decode(raw: String?): List<LauncherPage> {
            if (raw.isNullOrBlank()) return DEFAULT
            val parsed = raw.split(",").mapNotNull { name ->
                entries.firstOrNull { it.name == name }
            }
            val missing = DEFAULT.filter { it !in parsed }
            return (parsed + missing).distinct()
        }

        fun encode(pages: List<LauncherPage>): String = pages.joinToString(",") { it.name }
    }
}
