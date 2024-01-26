package net.horizonsend.ion.server.features.space.encounters

import net.horizonsend.ion.server.features.space.encounters.SecondaryChest.Companion.giveReward
import net.horizonsend.ion.server.features.world.generation.WorldGenerationManager
import net.horizonsend.ion.server.listener.SLEventListener
import org.bukkit.block.Chest
import org.bukkit.event.EventHandler
import org.bukkit.event.player.PlayerInteractEvent

class EncounterManager : SLEventListener() {
	@EventHandler
	fun onPlayerInteract(event: PlayerInteractEvent) {
		val clickedBlock = (event.clickedBlock?.state as? Chest) ?: return

		val world = event.player.world

		if (!WorldGenerationManager.worldGenerators.containsKey(world)) return

		Encounters[clickedBlock]?.onChestInteract(event)
		giveReward(event.player, clickedBlock)
	}
}
