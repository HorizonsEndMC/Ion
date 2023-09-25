package net.horizonsend.ion.server.miscellaneous.utils

import net.horizonsend.ion.server.IonServer
import org.bukkit.Bukkit
import org.dynmap.bukkit.DynmapPlugin
import org.dynmap.markers.MarkerAPI
import java.io.FileInputStream
import java.util.Locale

/** Function must only be called after dynmap has been enabled */
val markerAPI: MarkerAPI get() = DynmapPlugin.plugin.markerAPI

/**
 * Registers an icon stored in the icons folder inside the plugin data folder
 * Icons are stored as PNGs
 * Their display name will be the file name, without the extension, and formatted to remove _'s and capitalize the first letter of words
 **/
fun registerIcon(iconName: String) {
	if (!isDynmapEnabled()) return

	val iconsFolder = IonServer.dataFolder.resolve("icons")

	// Take the name, split it at _'s, replace the first letter of each with capital, and join together with spaces
	// medium_freighter becomes Medium Freighter
	val displayName = iconName.split("_")
		.map { separated ->
			separated.lowercase().replaceFirstChar { firstChar ->
				firstChar.uppercase(Locale.getDefault())
			}
		}
		.joinToString { " " }

	val file = iconsFolder.resolve("$iconName.png")
	if (!file.exists()) return

	val input = FileInputStream(file)
	markerAPI.createMarkerIcon(iconName, displayName, input)
}

fun isDynmapEnabled(): Boolean = Bukkit.getPluginManager().isPluginEnabled("dynmap")
