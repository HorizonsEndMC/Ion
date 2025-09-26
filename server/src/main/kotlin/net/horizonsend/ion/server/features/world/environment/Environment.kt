package net.horizonsend.ion.server.features.world.environment

import net.horizonsend.ion.common.database.schema.misc.PlayerSettings
import net.horizonsend.ion.server.features.cache.PlayerSettingsCache.getSetting
import net.horizonsend.ion.server.features.starship.active.ActiveStarships
import net.horizonsend.ion.server.miscellaneous.utils.listen
import org.bukkit.GameMode
import org.bukkit.World
import org.bukkit.entity.FallingBlock
import org.bukkit.entity.Player
import org.bukkit.event.entity.EntityChangeBlockEvent
import org.bukkit.event.entity.EntityDamageEvent
import org.bukkit.event.entity.ItemSpawnEvent
import org.bukkit.event.player.PlayerMoveEvent

enum class Environment {
	NO_GRAVITY {
		override fun tickPlayer(player: Player) {
			if (player.gameMode != GameMode.SURVIVAL || player.isDead || !player.hasGravity()) return

			// do not update fly speed if the player is piloting and is in direct control
			if (ActiveStarships.findByPilot(player)?.isDirectControlEnabled == true && player.getSetting(PlayerSettings::floatWhileDc) == true) return

			if (isInside(player.eyeLocation, 1)) {
				player.allowFlight = true
				player.flySpeed = 0.06f

				return
			}

			player.allowFlight = true

			if (!player.isFlying && !player.isOnGround) {
				player.isFlying = true
			}

			player.flySpeed = 0.02f

			if (player.isSprinting) {
				player.isSprinting = false
			}
		}

		override fun setup() {
			listen<ItemSpawnEvent> { event ->
				val entity = event.entity

				if (!entity.world.hasEnvironment()) return@listen

				entity.setGravity(false)
				entity.velocity = entity.velocity.multiply(0.05)
			}

			listen<PlayerMoveEvent> { event ->
				val player = event.player

				if (!player.world.hasEnvironment()) return@listen

				val isPositiveChange = event.to.y - event.from.y > event.player.world.minHeight

				if (event.to.y < event.player.world.minHeight && !isPositiveChange || event.to.y > event.player.world.maxHeight && isPositiveChange) {
					event.isCancelled = true
				}
			}

			listen<EntityDamageEvent> { event ->
				if (!event.entity.world.hasEnvironment()) return@listen
				if (event.cause != EntityDamageEvent.DamageCause.FALL) return@listen

				event.isCancelled = true
			}

			listen<EntityChangeBlockEvent> { event ->
				val entity = event.entity

				if (!entity.world.hasEnvironment()) return@listen
				if (entity !is FallingBlock) return@listen

				event.isCancelled = true
				event.block.setBlockData(event.blockData, false)
			}
		}
	}

	;

	open fun tickPlayer(player: Player) {}
	open fun setup() {}

	protected fun World.hasEnvironment(): Boolean = false // this.ion.environments.contains(this@Environment)
}
