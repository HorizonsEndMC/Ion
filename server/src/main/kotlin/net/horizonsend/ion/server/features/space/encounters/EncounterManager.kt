package net.horizonsend.ion.server.features.space.encounters

import net.horizonsend.ion.server.features.space.generation.SpaceGenerationManager
import net.horizonsend.ion.server.miscellaneous.NamespacedKeys
import net.minecraft.nbt.CompoundTag
import net.minecraft.nbt.NbtIo
import net.starlegacy.util.toBlockPos
import org.bukkit.craftbukkit.v1_19_R2.CraftWorld
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.persistence.PersistentDataType
import java.io.ByteArrayInputStream

object EncounterManager : Listener {
	@EventHandler
	fun onPlayerInteract(event: PlayerInteractEvent) {
		val clickedBlock = event.clickedBlock ?: return

		val serverLevel = (event.player.world as CraftWorld).handle
		// Quick check if the world of the event would contain a wreck
		if (!SpaceGenerationManager.worldGenerators.containsKey(serverLevel)) return

		val pdc = event.clickedBlock!!.location.chunk.persistentDataContainer.get(
			NamespacedKeys.WRECK_DATA,
			PersistentDataType.BYTE_ARRAY
		) ?: return

		// Check if the chunk of the event contains an encounter, if not, return

		val wreckData = try { // get existing asteroid data
			NbtIo.readCompressed(
				ByteArrayInputStream(
					pdc,
					0,
					pdc.size
				)
			)
		} catch (error: Error) {
			error.printStackTrace(); return
		}

		val existingWrecks = wreckData.getList("wrecks", 10) // list of compound tags (10)

		for (wreck in existingWrecks) {
			wreck as CompoundTag
			val block = clickedBlock.location.toBlockPos()

			if (wreck.getInt("x") == block.x &&
				wreck.getInt("y") == block.y &&
				wreck.getInt("z") == block.z
			) {
				val encounter = Encounters.getByIdentifier(wreck.getString("Encounter Identifier"))

				if (encounter == null) {
					val exception = NoSuchElementException("could not find wreck $wreck")
					exception.printStackTrace()
					throw exception
				}

				encounter.onChestInteract(event)
			}
		}
	}
}
