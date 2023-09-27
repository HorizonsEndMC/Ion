package net.horizonsend.ion.server.features.space.encounters

import net.horizonsend.ion.common.extensions.alert
import net.horizonsend.ion.common.extensions.hint
import net.horizonsend.ion.common.extensions.information
import net.horizonsend.ion.common.extensions.success
import net.horizonsend.ion.common.extensions.userError
import net.horizonsend.ion.server.IonServer
import net.horizonsend.ion.server.miscellaneous.registrations.NamespacedKeys.INACTIVE
import net.horizonsend.ion.server.miscellaneous.registrations.NamespacedKeys.LOCKED
import net.horizonsend.ion.server.miscellaneous.utils.Vec3i
import net.horizonsend.ion.server.miscellaneous.utils.highlightBlock
import net.horizonsend.ion.server.miscellaneous.utils.runnable
import net.horizonsend.ion.server.miscellaneous.utils.spherePoints
import net.horizonsend.ion.server.miscellaneous.utils.toBlockPos
import net.minecraft.nbt.CompoundTag
import org.bukkit.Material
import org.bukkit.Sound
import org.bukkit.block.Block
import org.bukkit.block.BlockFace
import org.bukkit.block.Chest
import org.bukkit.block.data.FaceAttachable
import org.bukkit.block.data.type.Switch
import org.bukkit.event.player.PlayerInteractEvent

object DefuseBomb: Encounter(identifier = "defuse_bomb") {
	private val validColors = listOf(
		Material.ORANGE_GLAZED_TERRACOTTA,
		Material.BLUE_GLAZED_TERRACOTTA,
		Material.RED_GLAZED_TERRACOTTA,
		Material.MAGENTA_GLAZED_TERRACOTTA
	)

	private val displayColorMap = mapOf(
		Material.ORANGE_GLAZED_TERRACOTTA to "<#eb9111>Orange Stripes",
		Material.BLUE_GLAZED_TERRACOTTA to "<#3c44aa>Blue Gem",
		Material.RED_GLAZED_TERRACOTTA to "<#b82f27>Red Swirl",
		Material.MAGENTA_GLAZED_TERRACOTTA to "<#d460cf>Magenta Arrow"
	)

	override fun onChestInteract(event: PlayerInteractEvent) {
		val targetedBlock = event.clickedBlock!!

		event.isCancelled = true
		val chest = (targetedBlock.state as? Chest) ?: return

		if (Encounters.getChestFlag(chest, LOCKED) == "true") {
			event.player.userError("You must defuse the bomb before opening the chest!")
			return
		}

		Encounters.setChestFlag(chest, LOCKED, "true")

		val timeLimit = 60 // seconds
		var iteration = 0 // ticks
		event.player.alert("Defusable bomb activated! Press the buttons in the correct order within $timeLimit seconds!")

		val surroundingBlocks = Encounters.getBlocks(chest.world, chest.location.toBlockPos(), 10.0) {
			Encounters.checkAir(it) && it.isSolid && it.type != Material.CHEST && it.type !in validColors
		}

		val buttonList = mutableListOf<Block>()

		// Button placer
		for (color in validColors) {
			val buttonOn = surroundingBlocks.random()
			buttonOn.type = color
			val buttonBlock = buttonOn.getRelative(BlockFace.UP)

			val blockData = (Material.STONE_BUTTON.createBlockData() as Switch)
			blockData.attachedFace = FaceAttachable.AttachedFace.FLOOR

			buttonBlock.blockData = blockData
			buttonList.add(buttonBlock)
			highlightBlock(event.player, Vec3i(buttonOn.location), (timeLimit * 20).toLong())
		}

		val correctOrder = validColors.shuffled()
		val selected = mutableListOf<Material>()
		var failed = false
		event.player.information(
			"Search for colored buttons in the wreck and press them in the right order:\n" +
					"  <gray>1: ${displayColorMap[correctOrder[0]]}\n" +
					"  <gray>2: ${displayColorMap[correctOrder[1]]}\n" +
					"  <gray>3: ${displayColorMap[correctOrder[2]]}\n" +
					"  <gray>4: ${displayColorMap[correctOrder[3]]}"
		)
		event.player.information("Do not attempt to break the buttons!")

		runnable {
			// timer sounds
			if (iteration % 20 == 0) {
				targetedBlock.location.world.playSound(targetedBlock.location, Sound.BLOCK_NOTE_BLOCK_BELL, 5.0f, 1.0f)
			}
			if (iteration >= timeLimit * 20 - 300 && iteration % 5 == 0) { // 15 seconds left
				targetedBlock.location.world.playSound(targetedBlock.location, Sound.BLOCK_NOTE_BLOCK_BELL, 5.0f, 1.0f)
			}
			if (iteration >= timeLimit * 20 - 100) { // 5 seconds left
				targetedBlock.location.world.playSound(targetedBlock.location, Sound.BLOCK_NOTE_BLOCK_BELL, 5.0f, 1.0f)
			}

			// explosion
			if (timeLimit * 20 == iteration) {
				val explosionRadius = 15.0 // For spawning actualStyle explosions
				val explosionDamage = 100.0
				val explosionDamageRadius = 30.0 // For entity damage calculation
				targetedBlock.location.spherePoints(explosionRadius / 2, 10).forEach {
					it.createExplosion(10.0f) // inner explosion
				}
				targetedBlock.location.spherePoints(explosionRadius, 20).forEach {
					it.createExplosion(15.0f) // outer explosion
				}
				targetedBlock.location.getNearbyLivingEntities(explosionDamageRadius).forEach {
					it.damage(explosionDamage * (explosionDamageRadius - it.location.distance(targetedBlock.location)) / explosionDamageRadius)
				}
				Encounters.setChestFlag(chest, LOCKED, "false")
				event.player.userError("You failed to defuse the bomb!")
				failed = true
				cancel()
			}

			if (!failed) {
				// button logic
				buttonList.forEach { button ->
					if (button.type != Material.STONE_BUTTON) {
						iteration = (timeLimit - 1) * 20
						chest.location.world.playSound(chest.location, Sound.ENTITY_WARDEN_SONIC_CHARGE, 10.0f, 1.0f)
						event.player.userError("You tampered with the bomb's disarming mechanism!")
						failed = true
						return@forEach
					}
					val buttonData = button.blockData as Switch
					if (buttonData.isPowered) {
						buttonData.isPowered = false
						selected.add(button.getRelative(BlockFace.DOWN).type)
						button.location.world.playSound(button.location, Sound.ITEM_FLINTANDSTEEL_USE, 5.0f, 1.0f)
						event.player.hint("The bomb mechanism clicks...")
					}
					button.blockData = buttonData
				}

				// failure
				if (selected.isNotEmpty() && (selected.size > correctOrder.size || selected.last() != correctOrder[selected.size - 1])
				) {
					iteration = (timeLimit - 1) * 20
					chest.location.world.playSound(chest.location, Sound.ENTITY_WARDEN_SONIC_CHARGE, 10.0f, 1.0f)
					event.player.userError("You pressed the button in the wrong order!")
					failed = true
				}

				// success
				if (selected == correctOrder) {
					Encounters.setChestFlag(chest, LOCKED, "false")
					Encounters.setChestFlag(chest, INACTIVE, "true")
					event.player.success("You successfully defused the bomb! The chest is now unlocked.")
					chest.location.world.playSound(chest.location, Sound.BLOCK_FIRE_EXTINGUISH, 5.0f, 0.0f)
					cancel()
				}
			}
			iteration++
		}.runTaskTimer(IonServer, 0L, 1L)
	}

	override fun constructChestNBT(): CompoundTag {
		return Encounters.createLootChest("horizonsend:chests/gun_parts")
	}
}
