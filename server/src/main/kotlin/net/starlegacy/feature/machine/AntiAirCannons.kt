package net.starlegacy.feature.machine

import net.horizonsend.ion.common.extensions.userError
import java.util.concurrent.TimeUnit
import net.horizonsend.ion.server.IonComponent
import net.horizonsend.ion.server.features.multiblock.landsieges.AntiAirCannonBaseMultiblock
import net.horizonsend.ion.server.features.multiblock.landsieges.AntiAirCannonTurretMultiblock
import net.starlegacy.feature.starship.control.StarshipControl
import net.starlegacy.util.PerPlayerCooldown
import net.starlegacy.util.Vec3i
import net.starlegacy.util.isGlass
import org.bukkit.Location
import org.bukkit.block.Sign
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.block.Action
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.event.player.PlayerMoveEvent
import org.bukkit.inventory.EquipmentSlot

object AntiAirCannons : IonComponent() {
	val cooldown = PerPlayerCooldown(AntiAirCannonTurretMultiblock.cooldownMillis)

	@EventHandler
	fun onPlayerInteract(event: PlayerInteractEvent) {
		// prevent double call
		if (event.hand != EquipmentSlot.HAND) return

		val player = event.player
		val block = player.location.block

		if (!block.type.isGlass) return

		val sign = AntiAirCannonTurretMultiblock.getSignFromPilot(player) ?: return

		when (event.action) {
			// Leave the turret on right click
			Action.RIGHT_CLICK_AIR, Action.RIGHT_CLICK_BLOCK -> {
				cooldown.tryExec(player) {
					player.teleport(sign.location.add(0.5, 0.0, 0.5))
				}
			}

			// Shoot on left click
			Action.LEFT_CLICK_AIR, Action.LEFT_CLICK_BLOCK -> tryShoot(player, sign)

			else -> return
		}
		event.isCancelled = true
	}

	fun tryShoot(player: Player, baseSign: Sign) {
		if (!StarshipControl.isHoldingController(player)) return

		// if base gets blown up return
		if (!AntiAirCannonBaseMultiblock.signMatchesStructure(baseSign, true)) return player.userError("Turret not intact!")

		val turretOrigin = AntiAirCannonBaseMultiblock.getTurretPivotPoint(baseSign)
		val turretDirection = AntiAirCannonBaseMultiblock.turretIntact(baseSign) ?: return player.userError("Turret not intact!")

		cooldown.tryExec(player, AntiAirCannonTurretMultiblock.cooldownMillis, TimeUnit.NANOSECONDS) {
			val oldFace = AntiAirCannonTurretMultiblock.getFacing(baseSign)
			val newFace = player.facing

			// only fire if it can align properly
			val resultFace = AntiAirCannonTurretMultiblock.rotate(baseSign, oldFace, newFace) { _, _, _, _ ->
				AntiAirCannonTurretMultiblock.moveEntitiesInWindow(baseSign, oldFace, newFace)
			}

			if (resultFace != newFace) return@tryExec

			val dir = player.location.direction

			AntiAirCannonTurretMultiblock.shoot(
				player.world,
				Vec3i(baseSign.location),
				newFace,
				dir,
				player
			)
		}
	}

	@EventHandler
	fun onPlayerMove(event: PlayerMoveEvent) {
		val player = event.player

		if (!event.from.block.type.isGlass) {
			return
		}

		val sign = AntiAirCannonTurretMultiblock.getSignFromPilot(player) ?: return
		handleTurretMovement(event, sign)
	}

	private fun handleTurretMovement(event: PlayerMoveEvent, sign: Sign): Location {
		val newTo = event.to

		if (event.from.distanceSquared(event.to) > 0) {
			newTo.x = newTo.blockX + 0.5
			newTo.y = newTo.blockY + 0.0
			newTo.z = newTo.blockZ + 0.5
		}

		val oldFace = AntiAirCannonTurretMultiblock.getFacing(sign)
		val newFace = event.player.facing
		if (oldFace != newFace) {
			val finalFace = AntiAirCannonTurretMultiblock.rotate(sign, oldFace, newFace)
			if (finalFace != oldFace) {
				val pilotLocation = AntiAirCannonTurretMultiblock.getPilotLoc(sign, finalFace)
				newTo.x = pilotLocation.x
				newTo.y = pilotLocation.y
				newTo.z = pilotLocation.z
			}
		}

		return newTo
	}
}
