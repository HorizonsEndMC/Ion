package net.starlegacy.feature.space

import net.horizonsend.ion.server.IonServerComponent
import net.starlegacy.feature.gear.powerarmor.PowerArmorManager
import net.starlegacy.feature.gear.powerarmor.PowerArmorModule
import net.starlegacy.feature.misc.getPower
import net.starlegacy.feature.misc.removePower
import net.horizonsend.ion.server.miscellaneous.utils.listen
import net.starlegacy.util.PerPlayerCooldown
import net.starlegacy.util.Tasks
import net.horizonsend.ion.server.miscellaneous.utils.Vec3i
import net.starlegacy.util.distanceSquared
import net.starlegacy.util.isInside
import net.starlegacy.util.squared
import org.bukkit.Bukkit
import org.bukkit.GameMode
import org.bukkit.Material
import org.bukkit.World
import org.bukkit.entity.FallingBlock
import org.bukkit.entity.Player
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.entity.EntityChangeBlockEvent
import org.bukkit.event.entity.EntityDamageEvent
import org.bukkit.event.entity.ItemSpawnEvent
import org.bukkit.event.player.PlayerMoveEvent
import java.util.concurrent.TimeUnit

object SpaceMechanics : IonServerComponent() {
	override fun onEnable() {
		Tasks.syncRepeat(10, 10) {
			for (player in Bukkit.getOnlinePlayers()) {
				if (player.gameMode != GameMode.SURVIVAL || player.isDead || !player.hasGravity()) {
					continue
				}

				val space = SpaceWorlds.contains(player.world)

				if (!space) {
					continue
				}

				if (!space || isInside(player.eyeLocation, 1)) {
					player.allowFlight = true
					player.flySpeed = 0.06f
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

				checkSuffocation(player)
			}
		}

		listen<ItemSpawnEvent> { event ->
			val entity = event.entity

			if (!SpaceWorlds.contains(entity.world)) {
				return@listen
			}

			entity.setGravity(false)
			entity.velocity = entity.velocity.multiply(0.05)
		}

		listen<PlayerMoveEvent> { event ->
			val player = event.player

			if (!SpaceWorlds.contains(player.world)) {
				return@listen
			}

			val isPositiveChange = event.to.y - event.from.y > event.player.world.minHeight

			if (event.to.y < event.player.world.minHeight && !isPositiveChange || event.to.y > event.player.world.maxHeight && isPositiveChange) {
				event.isCancelled = true
			}
		}

		listen<EntityDamageEvent> { event ->
			if (SpaceWorlds.contains(event.entity.world) && event.cause == EntityDamageEvent.DamageCause.FALL) {
				event.isCancelled = true
			}
		}

		listen<EntityChangeBlockEvent> { event ->
			val entity = event.entity
			if (entity is FallingBlock && SpaceWorlds.contains(event.block.world)) {
				event.isCancelled = true
				event.block.setBlockData(event.blockData, false)
			}
		}

		listen<BlockBreakEvent> { event ->
			val world = event.block.world
			if (!SpaceWorlds.contains(world)) {
				return@listen
			}

			val x = event.block.x.toDouble()
			val y = event.block.y.toDouble()
			val z = event.block.z.toDouble()

			fun check(world: World?, loc: Vec3i, radius: Int): Boolean {
				if (world != world) {
					return false
				}

				return distanceSquared(
					x,
					y,
					z,
					loc.x.toDouble(),
					loc.y.toDouble(),
					loc.z.toDouble()
				) <= radius.squared()
			}

			for (star in Space.getStars()) {
				if (check(star.spaceWorld, star.location, star.sphereRadius)) {
					event.isCancelled = true
					return@listen
				}
			}

			for (planet in Space.getPlanets()) {
				if (check(planet.spaceWorld, planet.location, planet.atmosphereRadius)) {
					event.isCancelled = true
					return@listen
				}
			}
		}
	}

	private fun checkSuffocation(player: Player) {
		if (isWearingSpaceSuit(player)) {
			return
		}

		if (checkPressureField(player)) {
			return
		}

		player.damage(0.5)
	}

	fun isWearingSpaceSuit(player: Player): Boolean {
		val inventory = player.inventory
		return inventory.helmet?.type == Material.CHAINMAIL_HELMET &&
			inventory.chestplate?.type == Material.CHAINMAIL_CHESTPLATE &&
			inventory.leggings?.type == Material.CHAINMAIL_LEGGINGS &&
			inventory.boots?.type == Material.CHAINMAIL_BOOTS
	}

	private val pressureFieldPowerCooldown = PerPlayerCooldown(1, TimeUnit.SECONDS)

	private fun checkPressureField(player: Player): Boolean {
		val helmet = player.inventory.helmet
			?: return false

		if (!PowerArmorManager.hasModule(helmet, PowerArmorModule.PRESSURE_FIELD)) {
			return false
		}

		val powerUsage = 10

		if (getPower(helmet) < powerUsage) {
			return false
		}

		pressureFieldPowerCooldown.tryExec(player) {
			removePower(helmet, powerUsage)
		}
		return true
	}
}
