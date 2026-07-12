package net.horizonsend.ion.server.features.world.environment.modules

import net.horizonsend.ion.common.database.schema.misc.PlayerSettings
import net.horizonsend.ion.server.core.registration.keys.WrappedListenerTypeKeys
import net.horizonsend.ion.server.features.cache.PlayerSettingsCache.getSetting
import net.horizonsend.ion.server.features.starship.active.ActiveStarships
import net.horizonsend.ion.server.features.world.environment.WorldEnvironmentManager
import net.horizonsend.ion.server.features.world.environment.isInside
import net.horizonsend.ion.server.features.world.environment.listener.WrappedListener
import org.bukkit.GameMode
import org.bukkit.entity.FallingBlock
import org.bukkit.entity.Player
import org.bukkit.event.entity.EntityDamageEvent

class NoGravityEnvironmentModule(manager: WorldEnvironmentManager, val ignoreIndoors: Boolean) : EnvironmentModule(manager) {
	var interval = 0

	override fun tickSync() {
		interval++
		// It's not time to tick yet
		if (interval % 10 != 0) return

		for (player in world.players) {
			// the GOAT Codex made me realize that returns brick the entire function, not just every player
			if (player.gameMode != GameMode.SURVIVAL || player.isDead || !player.hasGravity()) continue

			// do not update fly speed if the player is piloting and is in direct control
			if (ActiveStarships.findByPilot(player)?.isDirectControlEnabled == true && player.getSetting(PlayerSettings::floatWhileDc) == true) continue

			// only allow players to fly at full speed if ignoreIndoors is false and the player is inside
			if (!ignoreIndoors && isInside(player.eyeLocation, 1)) {
				removeEffects(player)

				continue
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
	}

	override fun removeEffects(player: Player) {
		player.allowFlight = true
		player.flySpeed = 0.06f
	}

	override fun getWrappedListeners(): Collection<WrappedListener<*>> = listOf(
		WrappedListenerTypeKeys.ITEM_SPAWN_EVENT.getValue().createInstance { event ->
			val entity = event.entity

			if (!entity.world.hasEnvironment()) return@createInstance

			entity.setGravity(false)
			entity.velocity = entity.velocity.multiply(0.05)
		},
		WrappedListenerTypeKeys.PLAYER_MOVE_EVENT.getValue().createInstance { event ->
			val player = event.player

			if (!player.world.hasEnvironment()) return@createInstance

			val isPositiveChange = event.to.y - event.from.y > event.player.world.minHeight

			if (event.to.y < event.player.world.minHeight && !isPositiveChange || event.to.y > event.player.world.maxHeight && isPositiveChange) {
				event.isCancelled = true
			}
		},
		WrappedListenerTypeKeys.ENTITY_DAMAGE_EVENT.getValue().createInstance { event ->
			if (!event.entity.world.hasEnvironment()) return@createInstance
			if (event.cause != EntityDamageEvent.DamageCause.FALL) return@createInstance

			event.isCancelled = true
		},
		WrappedListenerTypeKeys.ENTITY_CHANGE_BLOCK.getValue().createInstance { event ->
			val entity = event.entity

			if (!entity.world.hasEnvironment()) return@createInstance
			if (entity !is FallingBlock) return@createInstance

			event.isCancelled = true
			event.block.setBlockData(event.blockData, false)
		}
	)
}
