package net.horizonsend.ion.server.features.space.encounters

import net.horizonsend.ion.common.extensions.alert
import net.horizonsend.ion.common.extensions.information
import net.horizonsend.ion.common.extensions.success
import net.horizonsend.ion.common.extensions.userError
import net.horizonsend.ion.server.IonServer
import net.horizonsend.ion.server.miscellaneous.registrations.NamespacedKeys.INACTIVE
import net.horizonsend.ion.server.miscellaneous.registrations.NamespacedKeys.LOCKED
import net.horizonsend.ion.server.miscellaneous.registrations.NamespacedKeys.X
import net.horizonsend.ion.server.miscellaneous.registrations.NamespacedKeys.Y
import net.horizonsend.ion.server.miscellaneous.registrations.NamespacedKeys.Z
import net.horizonsend.ion.server.miscellaneous.highlightBlock
import net.horizonsend.ion.server.miscellaneous.runnable
import net.minecraft.core.BlockPos
import net.minecraft.nbt.CompoundTag
import net.starlegacy.util.spherePoints
import net.starlegacy.util.toBlockPos
import org.bukkit.Material
import org.bukkit.Particle
import org.bukkit.Sound
import org.bukkit.block.Block
import org.bukkit.block.BlockFace
import org.bukkit.block.Chest
import org.bukkit.block.data.FaceAttachable
import org.bukkit.block.data.type.Switch
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.persistence.PersistentDataType

object CoolantLeak : Encounter(identifier = "coolant_leak") {
	private const val BLOCKS_PER_ITERATION = 0.10
	private const val MAX_RADIUS = 15.0
	private const val MAX_ATTEMPTS = 500

	private fun getLever(chest: Chest): BlockPos = BlockPos(
		(Encounters.getChestFlag(chest, X))!!.toInt(),
		(Encounters.getChestFlag(chest, Y))!!.toInt(),
		(Encounters.getChestFlag(chest, Z))!!.toInt()
	)

	private fun placeLever(chest: Chest) {
		val chestPos = BlockPos(chest.x, chest.y, chest.z)

		fun checkAir(block: Block): Boolean {
			val up1 = block.getRelative(BlockFace.UP)
			val up2 = up1.getRelative(BlockFace.UP)

			return up1.isEmpty && up2.isEmpty
		}

		val surroundingBlocks = Encounters.getBlocks(chest.world, chestPos, 8.0) {
			checkAir(it) && it.isSolid && it.type != Material.CHEST && it.type != Material.REINFORCED_DEEPSLATE
		}

		val leverOn = surroundingBlocks.random()
		leverOn.type = Material.REINFORCED_DEEPSLATE
		val leverBlock = leverOn.getRelative(BlockFace.UP)

		val blockData = (Material.LEVER.createBlockData() as Switch)
		blockData.attachedFace = FaceAttachable.AttachedFace.FLOOR

		leverBlock.blockData = blockData

		Encounters.setChestFlag(chest, X, (leverBlock.x).toString())
		Encounters.setChestFlag(chest, Y, (leverBlock.y).toString())
		Encounters.setChestFlag(chest, Z, (leverBlock.z).toString())
	}

	override fun onChestInteract(event: PlayerInteractEvent) {
		val targetedBlock = event.clickedBlock!!

		event.isCancelled = true
		val chest = (targetedBlock.state as? Chest) ?: return

		if (Encounters.getChestFlag(chest, LOCKED) == "true") {
			event.player.information("The chest is frozen shut! Find the lever to stop the leak!")
			event.isCancelled = true
			return
		}

		placeLever(chest)
		Encounters.setChestFlag(chest, LOCKED, "true")

		var iteration = 0
		val leverPos = getLever(chest)

		highlightBlock(event.player, leverPos.below(), (MAX_ATTEMPTS * 2).toLong())

		val iceTypes = listOf(
			Material.ICE,
			Material.PACKED_ICE,
			Material.BLUE_ICE,
			Material.PACKED_ICE,
			Material.ICE,
		)

		var attempts = 0

		event.player.alert("The chest triggered a coolant leak! Find the lever to stop the leak!")
		targetedBlock.location.world.playSound(targetedBlock.location, Sound.BLOCK_FIRE_EXTINGUISH, 5.0f, 0.0f)

		runnable {
			val currentSize = iteration * BLOCKS_PER_ITERATION

			if (attempts > MAX_ATTEMPTS) {
				event.player.userError("Coolant leak expired.")
				cancel()
			}
			attempts++

			val leverState = chest.world.getBlockAt(leverPos.x, leverPos.y, leverPos.z).state
			if ((leverState.blockData as Switch).isPowered) {
				chest.persistentDataContainer.set(LOCKED, PersistentDataType.STRING, "false")
				chest.persistentDataContainer.set(INACTIVE, PersistentDataType.STRING, "true")

				Encounters.setChestFlag(chest, LOCKED, "false")
				Encounters.setChestFlag(chest, INACTIVE, "true")
				event.player.success("Coolant leak deactivated! The chest is now unlocked.")
				cancel()
			}

			for (block in Encounters.getBlocks(
				chest.world,
				chest.location.toCenterLocation().toBlockPos(),
				currentSize
			) {
				!it.isEmpty && it.isSolid && !iceTypes.contains(it.type) && it.type != Material.CHEST
			} ) {
				block.type = iceTypes.random()
			}

			if (currentSize <= MAX_RADIUS) iteration++

			val spherePoints = chest.location.toCenterLocation().spherePoints(currentSize, 500)

			for (player in chest.world.players) {
				if (player.location.distance(chest.location) >= maxOf(currentSize, 100.0)) continue

				for (spherePoint in spherePoints) {
					player.spawnParticle(
						Particle.SNOWFLAKE,
						spherePoint.x,
						spherePoint.y,
						spherePoint.z,
						1,
						0.0,
						0.0,
						0.0,
						0.1,
						null
					)
				}

				if (player.location.distance(chest.location) >= currentSize) continue

				player.freezeTicks = player.freezeTicks + 10
			}
		}.runTaskTimer(IonServer, 0L, 2L)
	}

	override fun constructChestNBT(): CompoundTag {
		return Encounters.createLootChest("horizonsend:chests/starship_resource")
	}
}
