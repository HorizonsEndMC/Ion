package net.horizonsend.ion.server.features.starship.control.weaponry

import net.horizonsend.ion.server.IonServerComponent
import net.horizonsend.ion.server.command.admin.debug
import net.horizonsend.ion.server.command.admin.debugBanner
import net.horizonsend.ion.server.features.starship.PilotedStarships
import net.horizonsend.ion.server.features.starship.active.ActiveStarship
import net.horizonsend.ion.server.features.starship.active.ActiveStarships
import net.horizonsend.ion.server.features.starship.control.PlayerStarshipControl
import net.horizonsend.ion.server.features.starship.control.StarshipControl
import net.horizonsend.ion.server.features.starship.controllers.Controller
import net.horizonsend.ion.server.features.starship.subsystem.weapon.StarshipWeapons
import net.horizonsend.ion.server.features.starship.subsystem.weapon.TurretWeaponSubsystem
import net.horizonsend.ion.server.features.starship.subsystem.weapon.WeaponSubsystem
import net.horizonsend.ion.server.features.starship.subsystem.weapon.interfaces.HeavyWeaponSubsystem
import net.horizonsend.ion.server.features.starship.subsystem.weapon.interfaces.ManualWeaponSubsystem
import net.horizonsend.ion.server.miscellaneous.utils.PerPlayerCooldown
import net.horizonsend.ion.server.miscellaneous.utils.displayNameString
import net.horizonsend.ion.server.miscellaneous.utils.isLava
import net.horizonsend.ion.server.miscellaneous.utils.isSign
import net.horizonsend.ion.server.miscellaneous.utils.isWater
import net.kyori.adventure.text.minimessage.MiniMessage
import org.bukkit.Location
import org.bukkit.block.BlockFace
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.event.player.PlayerItemHeldEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.util.Vector
import java.util.LinkedList
import java.util.UUID
import java.util.concurrent.ThreadLocalRandom
import java.util.concurrent.TimeUnit

object PlayerStarshipWeaponry : IonServerComponent() {
	private val rightClickTimes = mutableMapOf<UUID, Long>()
	private val cooldown = PerPlayerCooldown(250L, TimeUnit.MILLISECONDS)


	@EventHandler(priority = EventPriority.LOW)
	fun onClick(event: PlayerInteractEvent) {
		val player = event.player

		player.debugBanner("INTERACT EVENT MANUAL FIRE START")
		if (!PlayerStarshipControl.isHoldingController(player)) {
			return
		}

		player.debug("player has controller")

		val starship = ActiveStarships.findByPassenger(player) ?: return

		player.debug("player is piloting")

		if (event.action.isLeftClick) {
			event.isCancelled = true
		}

		player.debug("player is rclicking")

		if (event.action.isRightClick) {
			val uuid = player.uniqueId
			val elapsedSinceRightClick = System.nanoTime() - rightClickTimes.getOrDefault(uuid, 0)
			player.debug("elapsedSinceRCLICK = $elapsedSinceRightClick")
			if (elapsedSinceRightClick > TimeUnit.MILLISECONDS.toNanos(250)) {
				player.debug("click isn't doubleclick, adding...")
				rightClickTimes[uuid] = System.nanoTime()
				return
			}
			player.debug("it's a doubleclick, going on")
			rightClickTimes.remove(uuid)
		}

		if (event.clickedBlock?.type?.isSign == true) {
			return
		}

		player.debug("didnt click sign, trying to fire")

		cooldown.tryExec(player) {
			manualFire(player, starship, event.action.isLeftClick, player.inventory.itemInMainHand)
		}

		player.debugBanner("END")
	}

	@EventHandler
	fun onPlayerItemHoldEvent(event: PlayerItemHeldEvent) {
		val itemStack: ItemStack = event.player.inventory.getItem(event.newSlot) ?: return
		val starship = ActiveStarships.findByPassenger(event.player) ?: return

		if (itemStack.type != StarshipControl.CONTROLLER_TYPE) return

		val itemName = itemStack.displayNameString.lowercase()

		if (starship.weaponSets.keys().contains(itemName)) {
			event.player.sendActionBar(
				MiniMessage.miniMessage().deserialize("<gray>Now firing <aqua>$itemName<gray> weaponSet")
			)
		}
	}

	private fun manualFire(player: Player, starship: ActiveStarship, leftClick: Boolean, clock: ItemStack) {
		val loc = player.eyeLocation
		val playerFacing = player.facing
		val dir = loc.direction.normalize()

		val target: Vector = getTarget(loc, dir, starship)

		var weaponSet = starship.weaponSetSelections[player.uniqueId]
		val clockWeaponSet = clock.displayNameString.lowercase()

		if (starship.weaponSets.keys().contains(clockWeaponSet)) weaponSet = clockWeaponSet
		if (weaponSet == null && PilotedStarships[player] != starship) {
			return
		}

		val weapons = (if (weaponSet == null) starship.weapons else starship.weaponSets[weaponSet])
			.shuffled(ThreadLocalRandom.current())

		val queuedShots = queueShots(starship.controller!!, weapons, leftClick, playerFacing, dir, target)
		StarshipWeapons.fireQueuedShots(queuedShots, starship)
	}

	private fun getTarget(loc: Location, dir: Vector, starship: ActiveStarship): Vector {
		val world = loc.world
		var target: Vector = loc.toVector()
		val x = loc.blockX
		val y = loc.blockY
		val z = loc.blockZ
		for (i in 0 until 500) {
			val bx = (x + dir.x * i).toInt()
			val by = (y + dir.y * i).toInt()
			val bz = (z + dir.z * i).toInt()
			if (starship.contains(bx, by, bz)) {
				continue
			}
			if (!world.isChunkLoaded(bx shr 4, bz shr 4)) {
				continue
			}
			val type = world.getBlockAt(bx, by, bz).type
			target = Vector(bx + 0.5, by + 0.5, bz + 0.5)
			if (!type.isAir && !type.isWater && !type.isLava) {
				break
			}
			if (world.getNearbyLivingEntities(target.toLocation(world), 0.5).any { !starship.isWithinHitbox(it) }) {
				break
			}
		}
		return target
	}

	private fun queueShots(
		player: Controller,
		weapons: List<WeaponSubsystem>,
		leftClick: Boolean,
		playerFacing: BlockFace,
		dir: Vector,
		target: Vector
	): LinkedList<StarshipWeapons.ManualQueuedShot> {
		val queuedShots = LinkedList<StarshipWeapons.ManualQueuedShot>()

		for (weapon: WeaponSubsystem in weapons) {
			if (weapon !is ManualWeaponSubsystem) {
				continue
			}

			if (!weapon.isAcceptableDirection(playerFacing)) {
				continue
			}

			if (weapon is HeavyWeaponSubsystem != !leftClick) {
				continue
			}

			if (!weapon.isCooledDown()) {
				continue
			}

			if (!weapon.isIntact()) {
				continue
			}

			val targetedDir: Vector = weapon.getAdjustedDir(dir, target)

			if (weapon is TurretWeaponSubsystem && !weapon.ensureOriented(targetedDir)) {
				continue
			}

			if (!weapon.canFire(targetedDir, target)) {
				continue
			}

			queuedShots.add(StarshipWeapons.ManualQueuedShot(weapon, player, targetedDir, target))
		}

		return queuedShots
	}
}
