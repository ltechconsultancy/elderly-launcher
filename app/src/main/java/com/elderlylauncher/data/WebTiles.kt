package com.elderlylauncher.data

import android.content.Context
import androidx.core.content.ContextCompat
import com.elderlylauncher.R

/**
 * Website tiles that can be chosen like an app (Home and Apps page).
 * They are stored under a "web:" key instead of a package name.
 */
object WebTiles {
    const val PREFIX = "web:"
    const val PUIK = "web:puik"
    const val PUZZELWOORDEN = "web:puzzelwoorden"

    private data class Tile(val key: String, val label: String, val url: String, val icon: Int)

    private val tiles = listOf(
        // The magazine root always redirects to the newest weekly issue.
        Tile(PUIK, "Puik", "https://magazine.puiklokaal.nl/", R.drawable.web_puik),
        Tile(
            PUZZELWOORDEN,
            "Puzzelwoorden",
            "https://www.mijnwoordenboek.nl/puzzelwoordenboek/",
            R.drawable.web_puzzelwoorden
        )
    )

    /** Tiles placed on Home once, on the first start after they were added. */
    val homeDefaults: List<String> = listOf(PUIK, PUZZELWOORDEN)

    fun isWebTile(key: String): Boolean = key.startsWith(PREFIX)

    fun urlFor(key: String): String? = tiles.firstOrNull { it.key == key }?.url

    fun all(context: Context): List<AppInfo> = tiles.mapNotNull { tile ->
        ContextCompat.getDrawable(context, tile.icon)?.let { icon ->
            AppInfo(packageName = tile.key, label = tile.label, icon = icon)
        }
    }
}
