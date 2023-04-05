package net.horizonsend.ion.server.features.space.encounters

import net.horizonsend.ion.common.extensions.information
import net.horizonsend.ion.common.extensions.success
import net.horizonsend.ion.server.IonServer
import net.horizonsend.ion.server.features.space.generation.SpaceGenerationManager
import net.horizonsend.ion.server.miscellaneous.NamespacedKeys
import net.minecraft.nbt.CompoundTag
import net.minecraft.nbt.NbtIo
import net.starlegacy.util.VAULT_ECO
import net.starlegacy.util.toBlockPos
import org.bukkit.craftbukkit.v1_19_R2.CraftWorld
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.persistence.PersistentDataType
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream

class EncounterManager : Listener {

	// Handles primary chests
	@EventHandler
	fun onPlayerInteractA(event: PlayerInteractEvent) {
		val clickedBlock = event.clickedBlock ?: return

		val serverLevel = (event.player.world as CraftWorld).handle
		// Quick check if the world of the event would contain a wreck
		if (!SpaceGenerationManager.worldGenerators.containsKey(serverLevel)) return

		val chunk = event.clickedBlock!!.location.chunk

		val pdc = chunk.persistentDataContainer.get(
			NamespacedKeys.WRECK_ENCOUNTER_DATA,
			PersistentDataType.BYTE_ARRAY
		) ?: return

		// Check if the chunk of the event contains an encounter, if not, return

		// get existing asteroid data
		val wreckData = NbtIo.readCompressed(
			ByteArrayInputStream(
				pdc,
				0,
				pdc.size
			)
		)

		val existingWrecks = wreckData.getList("Wrecks", 10) // list of compound tags (10)

		for (wreck in existingWrecks) {
			wreck as CompoundTag
			val block = clickedBlock.location.toBlockPos()

			if (wreck.getInt("x") == block.x &&
				wreck.getInt("y") == block.y &&
				wreck.getInt("z") == block.z
			) {
				if (wreck.getBoolean("inactive")) {
					event.player.information("The chest was empty...")
					continue
				}

				val encounter = Encounters.getByIdentifier(wreck.getString("Encounter Identifier"))

				if (encounter == null) {
					IonServer.slF4JLogger.error("could not find wreck encounter $wreck")
					continue
				}

				event.isCancelled = true
				encounter.onChestInteract(event)
			}
		}
	}

	// Handles secondary chests
	@EventHandler
	fun onPlayerInteractB(event: PlayerInteractEvent) {
		val clickedBlock = event.clickedBlock ?: return

		val serverLevel = (event.player.world as CraftWorld).handle
		// Quick check if the world of the event would contain a wreck
		if (!SpaceGenerationManager.worldGenerators.containsKey(serverLevel)) return

		val chunk = event.clickedBlock!!.location.chunk

		val pdc = chunk.persistentDataContainer.get(
			NamespacedKeys.WRECK_ENCOUNTER_DATA,
			PersistentDataType.BYTE_ARRAY
		) ?: return
		// Check if the chunk of the event contains an encounter, if not, return

		// get existing wreck data
		val wreckData =
			NbtIo.readCompressed(
				ByteArrayInputStream(
					pdc,
					0,
					pdc.size
				)
			)

		val player = event.player
		val secondaryChests = wreckData.getList("SecondaryChests", 10) // list of compound tags (10)
		wreckData.remove("SecondaryChests")

		for (secondaryChest in secondaryChests) {
			secondaryChest as CompoundTag
			val block = clickedBlock.location.toBlockPos()

			if (secondaryChest.getInt("x") == block.x &&
				secondaryChest.getInt("y") == block.y &&
				secondaryChest.getInt("z") == block.z
			) {
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
		}

		wreckData.put("SecondaryChests", secondaryChests)

		val wreckDataOutputStream = ByteArrayOutputStream()
		NbtIo.writeCompressed(wreckData, wreckDataOutputStream)

		chunk.persistentDataContainer.set(
			NamespacedKeys.WRECK_ENCOUNTER_DATA,
			PersistentDataType.BYTE_ARRAY,
			wreckDataOutputStream.toByteArray()
		)
	}
}
