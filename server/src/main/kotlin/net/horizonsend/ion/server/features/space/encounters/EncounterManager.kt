package net.horizonsend.ion.server.features.space.encounters

import net.horizonsend.ion.server.features.space.generation.SpaceGenerationManager
import net.horizonsend.ion.server.miscellaneous.NamespacedKeys
import org.bukkit.craftbukkit.v1_19_R2.CraftWorld
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.persistence.PersistentDataType

object EncounterManager : Listener {
	@EventHandler
	fun onPlayerInteract(event: PlayerInteractEvent) {
		val serverLevel = (event.player.world as CraftWorld).handle
		// Quick check if the world of the event would contain a wreck
		if (!SpaceGenerationManager.worldGenerators.containsKey(serverLevel)) return

		// Check if the chunk of the event contains an encounter, if not, return
		if (
			event.player.chunk.persistentDataContainer.get(
				NamespacedKeys.WRECK_DATA,
				PersistentDataType.BYTE_ARRAY
			) == null
		) {
			return
		}

		// TODO get the encounter, execute its interact event
	}
}
