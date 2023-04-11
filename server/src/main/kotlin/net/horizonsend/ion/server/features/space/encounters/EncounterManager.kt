package net.horizonsend.ion.server.features.space.encounters

import net.horizonsend.ion.common.extensions.information
import net.horizonsend.ion.common.extensions.success
import net.horizonsend.ion.server.features.space.generation.BlockSerialization
import net.horizonsend.ion.server.features.space.generation.SpaceGenerationManager
import net.horizonsend.ion.server.miscellaneous.NamespacedKeys
import net.minecraft.nbt.CompoundTag
import net.starlegacy.util.VAULT_ECO
import org.bukkit.block.Chest
import org.bukkit.craftbukkit.v1_19_R2.CraftWorld
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerInteractEvent

class EncounterManager : Listener {

	// Handles primary chests
	@EventHandler
	fun onPlayerInteractA(event: PlayerInteractEvent) {
		val clickedBlock = (event.clickedBlock?.state as? Chest) ?: return

		val serverLevel = (event.player.world as CraftWorld).handle
		// Quick check if the world of the event would contain a wreck
		if (!SpaceGenerationManager.worldGenerators.containsKey(serverLevel)) return

		val encounter = Encounters[clickedBlock]
		encounter?.onChestInteract(event)
	}

	// Handles secondary chests
	@EventHandler
	fun onPlayerInteractB(event: PlayerInteractEvent) {
		val clickedBlock = (event.clickedBlock?.state as? Chest) ?: return

		val serverLevel = (event.player.world as CraftWorld).handle
		// Quick check if the world of the event would contain a wreck
		if (!SpaceGenerationManager.worldGenerators.containsKey(serverLevel)) return

		val chunk = clickedBlock.location.chunk
		val wreckData = BlockSerialization.readChunkCompoundTag(chunk, NamespacedKeys.WRECK_ENCOUNTER_DATA) ?: return
		val player = event.player

		val secondaryChests = wreckData.getList("SecondaryChests", 10) // list of compound tags (10)
		wreckData.remove("SecondaryChests")

		for (secondaryChest in secondaryChests) {
			secondaryChest as CompoundTag
			if (!Encounters.encounterMatchesChest(secondaryChest, clickedBlock)) continue

			if (secondaryChest.getBoolean("inactive")) {
				player.information("The chest was empty...")
				continue
			}

			val money = secondaryChest.getInt("Money")

			if (money != 0) {
				VAULT_ECO.depositPlayer(player, money.toDouble())
				player.success("You found $money credits stashed in the chest!")
				secondaryChests.remove(secondaryChest)
				secondaryChest.putBoolean("inactive", true)
				secondaryChests.add(secondaryChest)
			}
		}

		wreckData.put("SecondaryChests", secondaryChests)

		Encounters.setChunkEncounters(chunk, wreckData)
	}
}
