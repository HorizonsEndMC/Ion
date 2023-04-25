package net.horizonsend.ion.server.features.space.encounters

import fr.skytasul.guardianbeam.Laser
import net.horizonsend.ion.common.extensions.alert
import net.horizonsend.ion.server.IonServer
import net.horizonsend.ion.server.miscellaneous.NamespacedKeys
import net.horizonsend.ion.server.miscellaneous.castSpawnEntity
import net.horizonsend.ion.server.miscellaneous.runnable
import net.minecraft.nbt.CompoundTag
import net.starlegacy.util.distance
import net.starlegacy.util.toBlockPos
import org.bukkit.FluidCollisionMode
import org.bukkit.block.Chest
import org.bukkit.entity.EntityType
import org.bukkit.entity.ShulkerBullet
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.util.Vector

object DefenseMatrix : Encounter(identifier = "defense_matrix") { // TODO
	override fun onChestInteract(event: PlayerInteractEvent) {
		val targetedBlock = event.clickedBlock!!

		event.isCancelled = true
		val chest = (targetedBlock.state as? Chest) ?: return

		Encounters.setChestFlag(chest, NamespacedKeys.LOCKED, "true")
		event.player.alert("[INTRUSION DETECTED] ... [ACTIVATING DEFENSE MATRIX]")

		var iteration = 0

		val playerLocation = event.player.eyeLocation.toCenterLocation().toVector()

		val blocks = Encounters.getBlocks(
			event.player.world,
			chest.location.toBlockPos(),
			30.0,
		) {
			val rayCast = if (!it.isEmpty) {
				val blockLocation = it.location.toCenterLocation()
				val vector = blockLocation.toVector().subtract(playerLocation)

				it.world.rayTrace(
					blockLocation,
					vector,
					distance(
						blockLocation.x,
						blockLocation.y,
						blockLocation.z,
						playerLocation.x,
						playerLocation.y,
						playerLocation.z
					),
					FluidCollisionMode.NEVER,
					true,
					0.5,
					null
				)?.hitBlock
			} else it

			!it.isEmpty && rayCast == null
		}.shuffled().subList(0, 5)

		val defenseNodes = mutableListOf<ShulkerBullet>()

		for (block in blocks) {
			defenseNodes.add(
				event.player.world.castSpawnEntity<ShulkerBullet>(
					block.location.toCenterLocation(),
					EntityType.SHULKER_BULLET
				).apply {
					this.target = event.player

					this.flightSteps = 0
					this.targetDelta = Vector(0.0, 0.0, 0.0)
				}
			)
		}

		runnable {
			iteration++

			// 3 minute timeout
			if (iteration > 60) {
				cancel()
			}

			defenseNodes.removeAll { it.isDead }

			for (defenseNode in defenseNodes) {

				Laser.GuardianLaser(
					defenseNode.location,
					event.player,
					50,
					30
				).durationInTicks().apply { start(IonServer) }
			}

		}.runTaskTimer(IonServer, 20L, 60L)
	}

	override fun constructChestNBT(): CompoundTag {
		return Encounters.createLootChest("horizonsend:chests/guns")
	}
}
