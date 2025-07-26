package net.horizonsend.ion.server.features.npcs

import net.horizonsend.ion.server.IonServerComponent
import net.horizonsend.ion.server.features.npcs.database.UniversalNPCWrapper
import org.bukkit.Bukkit
import org.dynmap.bukkit.DynmapPlugin
import org.dynmap.markers.Marker
import org.dynmap.markers.MarkerAPI
import org.dynmap.markers.MarkerIcon
import org.dynmap.markers.MarkerSet
import java.util.UUID

object NPCDisplay : IonServerComponent(true) {
	private lateinit var npcMarkers: MarkerSet
	private val markerAPI: MarkerAPI get() = DynmapPlugin.plugin.markerAPI

	override fun onEnable() {
		if (!Bukkit.getPluginManager().isPluginEnabled("dynmap")) return
		npcMarkers = markerAPI.createMarkerSet("npc-icons", "NPCs", null, false)
	}

	private val displayed: MutableSet<UUID> = mutableSetOf()

	fun createMarker(npc: UniversalNPCWrapper<*, *>, label: String, icon: MarkerIcon, description: String): Marker? {
		if (displayed.contains(npc.npc.uniqueId)) {
			removeMarker(npc)
		}

		val loction = npc.npc.storedLocation

		val marker: Marker? = npcMarkers.createMarker(
			npc.npc.uniqueId.toString(),
			label,
			true, // HTML label support
			loction.world.name,
			loction.x,
			loction.y,
			loction.z,
			icon,
			false
		)

		marker?.description = description

		displayed.add(npc.npc.uniqueId)
		return marker
	}

	fun removeMarker(npc: UniversalNPCWrapper<*, *>) {
		npcMarkers.findMarker(npc.npc.uniqueId.toString()).deleteMarker()
		displayed.remove(npc.npc.uniqueId)
	}

	fun getMarkerIcon(name: String): MarkerIcon? {
		if (!Bukkit.getPluginManager().isPluginEnabled("dynmap")) return null
		return markerAPI.getMarkerIcon(name)
	}
}
