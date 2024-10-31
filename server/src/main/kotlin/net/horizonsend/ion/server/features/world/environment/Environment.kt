package net.horizonsend.ion.server.features.world.environment

import net.horizonsend.ion.server.features.gear.getPower
import net.horizonsend.ion.server.features.gear.powerarmor.PowerArmorManager
import net.horizonsend.ion.server.features.gear.powerarmor.PowerArmorModule
import net.horizonsend.ion.server.features.gear.removePower
import net.horizonsend.ion.server.features.world.IonWorld.Companion.ion
import net.horizonsend.ion.server.miscellaneous.utils.PerPlayerCooldown
import net.horizonsend.ion.server.miscellaneous.utils.isInside
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
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import java.util.concurrent.TimeUnit

enum class Environment {
	VACUUM {
		private fun checkSuffocation(player: Player) {
			if (isWearingSpaceSuit(player)) return
			if (checkPressureField(player)) return

			if (isInside(player.eyeLocation, 1)) return

			player.damage(0.5)
		}

		private val pressureFieldPowerCooldown = PerPlayerCooldown(1, TimeUnit.SECONDS)

		private fun checkPressureField(player: Player): Boolean {
			val helmet = player.inventory.helmet ?: return false

			if (!PowerArmorManager.hasModule(helmet, PowerArmorModule.PRESSURE_FIELD)) return false

			val powerUsage = 10

			if (getPower(helmet) < powerUsage) return false

			pressureFieldPowerCooldown.tryExec(player) {
				removePower(helmet, powerUsage)
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
	},

	LOW_GRAVITY {
		override fun tickPlayer(player: Player) {
			if (player.gameMode != GameMode.SURVIVAL || player.isDead) return

			if (!isInside(player.eyeLocation, 1)) {
				player.addPotionEffect(PotionEffect(
					PotionEffectType.LEVITATION,
					PotionEffect.INFINITE_DURATION,
					-1,
					false,
					false
				))
			} else {
				player.removePotionEffect(PotionEffectType.LEVITATION)
			}
		}

		override fun setup() {
			listen<EntityDamageEvent> { event ->
				if (!event.entity.world.hasEnvironment()) return@listen
				if (event.cause != EntityDamageEvent.DamageCause.FALL) return@listen

				event.isCancelled = true
			}
		}
	},

	VERY_HOT {
		private fun shouldBurn(player: Player): Boolean {
			if (isWearingSpaceSuit(player)) return false
			if (checkEnvironment(player)) return false

			if (isInside(player.eyeLocation, 1)) return false

			return true
		}

		private fun tickBurning(player: Player) {
			player.addPotionEffect(PotionEffect(
				PotionEffectType.HUNGER,
				PotionEffect.INFINITE_DURATION,
				2,
				false,
				false
			))
			player.fireTicks = 300
		}

		override fun tickPlayer(player: Player) {
			if (player.gameMode != GameMode.SURVIVAL || player.isDead) return

			if (shouldBurn(player)) tickBurning(player) else cleanseBurning(player)
		}
	},

	HOT {
		override fun tickPlayer(player: Player) {
			if (player.gameMode != GameMode.SURVIVAL || player.isDead) return

			if (isDay(player)) VERY_HOT.tickPlayer(player) else cleanseBurning(player)
		}
	},

	VERY_COLD {
		private fun shouldFreeze(player: Player): Boolean {
			if (isWearingSpaceSuit(player)) return false
			if (checkEnvironment(player)) return false

			if (isInside(player.eyeLocation, 1)) return false

			return true
		}

		private fun tickFreezing(player: Player) {
			player.addPotionEffect(PotionEffect(
				PotionEffectType.SLOW_DIGGING,
				PotionEffect.INFINITE_DURATION,
				1,
				false,
				false
			))
			player.lockFreezeTicks(true)
			player.freezeTicks += 20
		}

		override fun tickPlayer(player: Player) {
			if (player.gameMode != GameMode.SURVIVAL || player.isDead) return

			if (shouldFreeze(player)) tickFreezing(player) else cleanseFreezing(player)
		}
	},

	COLD {
		override fun tickPlayer(player: Player) {
			if (player.gameMode != GameMode.SURVIVAL || player.isDead) return

			if (isNight(player)) VERY_COLD.tickPlayer(player) else cleanseFreezing(player)
		}
	}

	;

	open fun tickPlayer(player: Player) {}
	open fun setup() {}

	protected fun World.hasEnvironment(): Boolean = this.ion.environments.contains(this@Environment)

	companion object {
		private const val SUNSET_TIME = 12610
		private const val SUNRISE_TIME = 23041

		fun isWearingSpaceSuit(player: Player): Boolean {
			val inventory = player.inventory

			return inventory.helmet?.type == Material.CHAINMAIL_HELMET &&
				inventory.chestplate?.type == Material.CHAINMAIL_CHESTPLATE &&
				inventory.leggings?.type == Material.CHAINMAIL_LEGGINGS &&
				inventory.boots?.type == Material.CHAINMAIL_BOOTS
		}

		fun checkEnvironment(player: Player): Boolean {
			val helmet = player.inventory.helmet ?: return false

			if (!PowerArmorManager.hasModule(helmet, PowerArmorModule.ENVIRONMENT)) return false

			val powerUsage = 10

			return getPower(helmet) >= powerUsage
		}

		fun isNight(player: Player): Boolean {
			val world = player.world
			return world.time in SUNSET_TIME..SUNRISE_TIME
		}

		fun isDay(player: Player): Boolean {
			val world = player.world
			return world.time > SUNRISE_TIME || world.time < SUNSET_TIME
		}

		fun cleanseBurning(player: Player) {
			player.removePotionEffect(PotionEffectType.HUNGER)
		}

		fun cleanseFreezing(player: Player) {
			player.removePotionEffect(PotionEffectType.SLOW_DIGGING)
			player.lockFreezeTicks(false)
		}
	}
}
