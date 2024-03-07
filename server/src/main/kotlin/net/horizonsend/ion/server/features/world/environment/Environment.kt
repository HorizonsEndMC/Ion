package net.horizonsend.ion.server.features.world.environment

import net.horizonsend.ion.server.features.custom.items.CustomItemRegistry.customItem
import net.horizonsend.ion.server.features.custom.items.component.CustomComponentTypes
import net.horizonsend.ion.server.features.custom.items.type.tool.mods.ItemModRegistry
import net.horizonsend.ion.server.features.world.IonWorld.Companion.ion
import net.horizonsend.ion.server.miscellaneous.utils.PerPlayerCooldown
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.isInside
import net.horizonsend.ion.server.miscellaneous.utils.listen
import org.bukkit.GameMode
import org.bukkit.Material
import org.bukkit.World
import org.bukkit.entity.FallingBlock
import org.bukkit.entity.Player
import org.bukkit.event.entity.EntityChangeBlockEvent
import org.bukkit.event.entity.EntityDamageEvent
import org.bukkit.event.entity.ItemSpawnEvent
import org.bukkit.event.player.PlayerMoveEvent
import java.util.concurrent.TimeUnit

enum class Environment {
	VACUUM {
		private fun checkSuffocation(player: Player) {
			if (isWearingSpaceSuit(player)) return

			if (isInside(player.eyeLocation, 1)) return

			if (checkPressureField(player)) return

			player.damage(0.5)
		}

		private val pressureFieldPowerCooldown = PerPlayerCooldown(1, TimeUnit.SECONDS)

		private fun checkPressureField(player: Player): Boolean {
			val helmet = player.inventory.helmet ?: return false
			val customItem = helmet.customItem ?: return false

			if (customItem.hasComponent(CustomComponentTypes.MOD_MANAGER)) return false
			val mods = customItem.getComponent(CustomComponentTypes.MOD_MANAGER).getMods(helmet)
			if (!mods.contains(ItemModRegistry.PRESSURE_FIELD)) return false

			val powerUsage = 10

			val power = customItem.getComponent(CustomComponentTypes.POWER_STORAGE)
			if (power.getPower(helmet) < powerUsage) return false

			pressureFieldPowerCooldown.tryExec(player) {
				power.removePower(helmet, customItem, powerUsage)
			}

			return true
		}

		override fun tickPlayer(player: Player) {
			if (player.gameMode != GameMode.SURVIVAL || player.isDead) return

			checkSuffocation(player)
		}
	},

	NO_GRAVITY {
		override fun tickPlayer(player: Player) {
			if (player.gameMode != GameMode.SURVIVAL || player.isDead || !player.hasGravity()) return

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

	protected fun World.hasEnvironment(): Boolean = this.ion.environments.contains(this@Environment)

	companion object {
		fun isWearingSpaceSuit(player: Player): Boolean {
			val inventory = player.inventory

			return inventory.helmet?.type == Material.CHAINMAIL_HELMET &&
				inventory.chestplate?.type == Material.CHAINMAIL_CHESTPLATE &&
				inventory.leggings?.type == Material.CHAINMAIL_LEGGINGS &&
				inventory.boots?.type == Material.CHAINMAIL_BOOTS
		}
	}
}
