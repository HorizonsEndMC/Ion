package net.starlegacy.feature.machine

import net.horizonsend.ion.common.extensions.information
import net.horizonsend.ion.common.extensions.userError
import java.util.concurrent.TimeUnit
import net.horizonsend.ion.server.IonComponent
import net.horizonsend.ion.server.features.multiblock.landsieges.AntiAirCannonBaseMultiblock
import net.horizonsend.ion.server.features.multiblock.landsieges.AntiAirCannonTurretMultiblock
import net.kyori.adventure.text.Component
import net.starlegacy.feature.starship.control.StarshipControl
import net.starlegacy.util.Notify
import net.starlegacy.util.PerPlayerCooldown
import net.starlegacy.util.Tasks
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
import java.util.UUID

object AntiAirCannons : IonComponent() {
	val cooldown = PerPlayerCooldown(AntiAirCannonTurretMultiblock.cooldownMillis)
	val lastBarrel = mutableMapOf<UUID, Boolean>()

	override fun onEnable() {
		Tasks.syncRepeat(0L, 20L * 60L * 5L) {
			lastBarrel.clear()
		}
	}

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
					player.information("Exiting Turret")
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

		AntiAirCannonBaseMultiblock.getTurretPivotPoint(baseSign)
		AntiAirCannonBaseMultiblock.turretIntact(baseSign) ?: return player.userError("Turret not intact!")

		cooldown.tryExec(player, AntiAirCannonTurretMultiblock.cooldownMillis, TimeUnit.NANOSECONDS) {
			val oldFace = AntiAirCannonTurretMultiblock.getFacing(baseSign)
			val newFace = player.facing

			// only fire if it can align properly
			val resultFace = AntiAirCannonTurretMultiblock.rotate(
				AntiAirCannonBaseMultiblock.getTurretPivotPoint(baseSign),
				baseSign.world,
				oldFace,
				newFace
			) { _, _, _, _ ->
				AntiAirCannonTurretMultiblock.moveEntitiesInWindow(baseSign, oldFace, newFace)
			}

			if (resultFace != newFace) return@tryExec

			AntiAirCannonTurretMultiblock.shoot(
				player,
				resultFace,
				baseSign
			)

			Notify.all(Component.text("pew pew pew"))
		}
	}

	@EventHandler
	fun onPlayerMove(event: PlayerMoveEvent) {
		val player = event.player

		if (!event.from.block.type.isGlass) return

		val sign = AntiAirCannonTurretMultiblock.getSignFromPilot(player) ?: return

		handleTurretMovement(event, sign)
	}

	private fun handleTurretMovement(event: PlayerMoveEvent, sign: Sign) {
		val newTo = event.to

		if (event.from.distanceSquared(event.to) > 0) {
			newTo.x = newTo.blockX + 0.5
			newTo.y = newTo.blockY + 0.0
			newTo.z = newTo.blockZ + 0.5
		}

		val oldFace = AntiAirCannonTurretMultiblock.getFacing(sign)
		val newFace = event.player.facing

		if (oldFace == newFace) return

		AntiAirCannonTurretMultiblock.rotate(
			AntiAirCannonBaseMultiblock.getTurretPivotPoint(sign),
			sign.world,
			oldFace = oldFace,
			newFace = newFace
		) { _, _, _, _ ->
			AntiAirCannonTurretMultiblock.moveEntitiesInWindow(sign, oldFace, newFace)
		}
	}
}
