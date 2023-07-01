package net.starlegacy.feature.machine

import java.util.concurrent.TimeUnit
import net.horizonsend.ion.server.IonComponent
import net.horizonsend.ion.server.features.multiblock.Multiblocks
import net.horizonsend.ion.server.features.multiblock.landsieges.AAGunMultiblock
import net.starlegacy.feature.starship.control.StarshipControl
import net.starlegacy.listen
import net.starlegacy.util.PerPlayerCooldown
import net.starlegacy.util.Vec3i
import net.starlegacy.util.isGlass
import org.bukkit.Location
import org.bukkit.block.Sign
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.block.Action
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.event.player.PlayerMoveEvent
import org.bukkit.inventory.EquipmentSlot

object AATurret : IonComponent() {
	private lateinit var turretMultiblocks: List<AAGunMultiblock>

	val cooldown = PerPlayerCooldown(AAGunMultiblock.cooldownMillis)

	@EventHandler
	fun onPlayerInteract(event: PlayerInteractEvent) {
	}



	override fun onEnable() {

		listen<PlayerInteractEvent>(EventPriority.NORMAL) { event ->
			// prevent double call
			if (event.hand != EquipmentSlot.HAND) {
				return@listen
			}

			val player = event.player
			val block = player.location.block

			if (!block.type.isGlass) {
				return@listen
			}

			var sign: Sign? = null
			var multiblock: AAGunMultiblock? = null

			for (turret in turretMultiblocks) {
				sign = turret.getSignFromPilot(player) ?: continue
				multiblock = turret
				break
			}

			if (sign == null || multiblock == null) return@listen

			when (event.action) {
				Action.RIGHT_CLICK_AIR, Action.RIGHT_CLICK_BLOCK -> {
					cooldown.tryExec(player) {
						player.teleport(sign.location.add(0.5, 0.0, 0.5))
					}
				}

				Action.LEFT_CLICK_AIR, Action.LEFT_CLICK_BLOCK -> {
					if (!StarshipControl.isHoldingController(player)) {
						return@listen
					}

					cooldown.tryExec(player, multiblock.cooldownMillis, TimeUnit.NANOSECONDS) {
						val oldFace = multiblock.getFacing(sign)
						val newFace = player.facing

						// only fire if it can align properly
						if (multiblock.rotate(sign, oldFace, newFace) == newFace) {
							val world = player.world
							val pos = Vec3i(sign.location.toBlockKey())
							val dir = player.location.direction

							multiblock.shoot(world, pos, newFace, dir, player)
						}
					}
				}

				else -> return@listen
			}
			event.isCancelled = true
		}

		listen<PlayerMoveEvent> { event ->
			val player = event.player

			if (!event.from.block.type.isGlass) {
				return@listen
			}

			for (turret in turretMultiblocks) {
				val sign = turret.getSignFromPilot(player) ?: continue
				handleTurretMovement(event, turret, sign)
				return@listen
			}
		}
	}

	private fun handleTurretMovement(event: PlayerMoveEvent, turret: AAGunMultiblock, sign: Sign): Location {
		val newTo = event.to

		if (event.from.distanceSquared(event.to) > 0) {
			newTo.x = newTo.blockX + 0.5
			newTo.y = newTo.blockY + 0.0
			newTo.z = newTo.blockZ + 0.5
		}

		val oldFace = turret.getFacing(sign)
		val newFace = event.player.facing
		if (oldFace != newFace) {
			val finalFace = turret.rotate(sign, oldFace, newFace)
			if (finalFace != oldFace) {
				val pilotLocation = turret.getPilotLoc(sign, finalFace)
				newTo.x = pilotLocation.x
				newTo.y = pilotLocation.y
				newTo.z = pilotLocation.z
			}
		}

		return newTo
	}
}
