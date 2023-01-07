package net.starlegacy.feature.starship.control

import net.horizonsend.ion.server.legacy.feedback.FeedbackType
import net.horizonsend.ion.server.legacy.feedback.sendFeedbackAction
import net.horizonsend.ion.server.starships.control.LegacyController
import net.starlegacy.SLComponent
import net.starlegacy.feature.space.Space
import net.starlegacy.feature.starship.PilotedStarships
import net.starlegacy.feature.starship.StarshipType.PLATFORM
import net.starlegacy.feature.starship.active.ActivePlayerStarship
import net.starlegacy.feature.starship.active.ActiveStarship
import net.starlegacy.feature.starship.active.ActiveStarships
import net.starlegacy.feature.starship.hyperspace.Hyperspace
import net.starlegacy.feature.starship.movement.StarshipTeleportation
import net.starlegacy.feature.starship.movement.TranslateMovement
import net.starlegacy.feature.starship.subsystem.weapon.StarshipWeapons
import net.starlegacy.feature.starship.subsystem.weapon.TurretWeaponSubsystem
import net.starlegacy.feature.starship.subsystem.weapon.WeaponSubsystem
import net.starlegacy.feature.starship.subsystem.weapon.interfaces.HeavyWeaponSubsystem
import net.starlegacy.feature.starship.subsystem.weapon.interfaces.ManualWeaponSubsystem
import net.starlegacy.listen
import net.starlegacy.util.PerPlayerCooldown
import net.starlegacy.util.Tasks
import net.starlegacy.util.d
import net.starlegacy.util.isLava
import net.starlegacy.util.isSign
import net.starlegacy.util.isWater
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.block.BlockFace
import org.bukkit.block.Sign
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.block.Action
import org.bukkit.event.block.BlockPlaceEvent
import org.bukkit.event.player.PlayerDropItemEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.event.player.PlayerMoveEvent
import org.bukkit.event.player.PlayerSwapHandItemsEvent
import org.bukkit.event.player.PlayerToggleSneakEvent
import org.bukkit.inventory.EquipmentSlot
import org.bukkit.util.Vector
import java.util.LinkedList
import java.util.UUID
import java.util.concurrent.ThreadLocalRandom
import java.util.concurrent.TimeUnit
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow
import kotlin.math.roundToInt
import kotlin.math.sin

object StarshipControl : SLComponent() {
	val CONTROLLER_TYPE = Material.CLOCK

	fun isHoldingController(player: Player): Boolean {
		val inventory = player.inventory
		return inventory.itemInMainHand.type == CONTROLLER_TYPE || inventory.itemInOffHand.type == CONTROLLER_TYPE
	}

	override fun onEnable() {
		manualFlight()
		rotation()
	}

	private fun manualFlight() {
		Tasks.syncRepeat(1, 1) {
			for (starship in ActiveStarships.allPlayerShips()) {
				processManualFlight(starship)
			}
		}

		listen<PlayerToggleSneakEvent> { event ->
			PilotedStarships[event.player]?.sneakMovements = 0
		}

		listen<PlayerMoveEvent> { event ->
			if (event.player.walkSpeed == 0.009f && PilotedStarships[event.player] == null) {
				event.player.walkSpeed = 0.2f
			}
		}
	}

	private fun processManualFlight(starship: ActivePlayerStarship) {
		if (starship.controller !is LegacyController) return

		if (starship.type == PLATFORM) {
			starship.pilot?.sendFeedbackAction(FeedbackType.USER_ERROR, "This ship type is not capable of moving.")
			return
		}

		if (starship.isTeleporting) {
			return
		}

		val pilot = starship.pilot ?: return

		if (pilot.isSneaking) {
			processSneakFlight(pilot, starship)
		}
	}

	private fun processSneakFlight(pilot: Player, starship: ActivePlayerStarship) {
		if (starship.type == PLATFORM) {
			pilot.sendFeedbackAction(FeedbackType.USER_ERROR, "This ship type is not capable of moving.")
			return
		}

		if (Hyperspace.isWarmingUp(starship)) {
			starship.pilot?.sendFeedbackAction(FeedbackType.USER_ERROR, "Cannot move while in hyperspace warmup.")
			return
		}

		if (!isHoldingController(pilot)) {
			return
		}

		val now = System.currentTimeMillis()
		if (now - starship.lastManualMove < starship.manualMoveCooldownMillis) {
			return
		}
		starship.lastManualMove = now

		starship.sneakMovements++
		val sneakMovements = starship.sneakMovements

		val maxAccel = starship.data.starshipType.maxSneakFlyAccel
		val accelDistance = starship.data.starshipType.sneakFlyAccelDistance

		val yawRadians = Math.toRadians(pilot.location.yaw.toDouble())
		val pitchRadians = Math.toRadians(pilot.location.pitch.toDouble())
		val distance = max(min(maxAccel, sneakMovements / min(1, accelDistance)), 1)

		val vertical = abs(pitchRadians) >= PI * 5 / 12 // 75 degrees

		val dx = if (vertical) 0 else sin(-yawRadians).roundToInt() * distance
		val dy = sin(-pitchRadians).roundToInt() * distance
		val dz = if (vertical) 0 else cos(yawRadians).roundToInt() * distance

		if (locationCheck(starship, dx, dy, dz)) {
			return
		}

		TranslateMovement.loadChunksAndMove(starship, dx, dy, dz)
	}

	fun locationCheck(starship: ActivePlayerStarship, dx: Int, dy: Int, dz: Int): Boolean {
		val world = starship.world
		val newCenter = starship.centerOfMassVec3i.toLocation(world).add(dx.d(), dy.d(), dz.d())

		val planet = Space.getPlanets().asSequence()
			.filter { it.spaceWorld == world }
			.filter {
				it.location.toLocation(world).distanceSquared(newCenter) < starship.getEntryRange(it).toDouble().pow(2)
			}
			.firstOrNull()
			?: return false

		val border = planet.planetWorld?.worldBorder
			?.takeIf { it.size < 60_000_000 } // don't use if it's the default, giant border
		val halfLength = if (border == null) 2500.0 else border.size / 2.0
		val centerX = border?.center?.x ?: halfLength
		val centerZ = border?.center?.z ?: halfLength

		val distance = (halfLength - 250) * max(0.15, newCenter.y / starship.world.maxHeight)
		val offset = newCenter.toVector()
			.subtract(planet.location.toVector())
			.normalize().multiply(distance)

		val x = centerX + offset.x
		val y = 250.0 - (starship.max.y - starship.min.y)
		val z = centerZ + offset.z
		val target = Location(planet.planetWorld, x, y, z).toBlockLocation()

		starship.sendMessage("&7&oEntering &2&o${planet.name}&7&o...")

		StarshipTeleportation.teleportStarship(starship, target)
		return true
	}

	private fun rotation() {
		listen<PlayerDropItemEvent>(priority = EventPriority.LOWEST) { event ->
			val starship = PilotedStarships[event.player] ?: return@listen
			if (event.itemDrop.itemStack.type != CONTROLLER_TYPE) return@listen

			event.isCancelled = true
			starship.tryRotate(false)
		}

		listen<PlayerSwapHandItemsEvent>(priority = EventPriority.LOWEST) { event ->
			val starship = PilotedStarships[event.player] ?: return@listen
			if (event.offHandItem?.type != CONTROLLER_TYPE) return@listen

			event.isCancelled = true
			starship.tryRotate(true)
		}
	}

	private val rightClickTimes = mutableMapOf<UUID, Long>()

	@EventHandler
	fun onSignClick(event: PlayerInteractEvent) {
		if (event.hand != EquipmentSlot.HAND) {
			return
		}

		val rightClick = event.action == Action.RIGHT_CLICK_BLOCK

		val player = event.player

		val block = event.clickedBlock ?: return
		val sign = block.state as? Sign ?: return

		clickSign(player, rightClick, sign)
	}

	@EventHandler
	fun onSignPlace(event: BlockPlaceEvent) {
		val sign = event.block as? Sign ?: return
		Tasks.syncDelay(1) {
			clickSign(event.player, true, sign)
		}
	}

	private fun clickSign(player: Player, rightClick: Boolean, sign: Sign) {
		for (signType in StarshipSigns.values()) {
			if (rightClick && firstLineMatchesUndetected(sign, signType)) {
				detectSign(signType, player, sign)
				return
			}

			if (!matchesDetectedSign(signType, sign)) {
				continue
			}

			signType.onClick(player, sign, rightClick)
			return
		}
	}

	private fun firstLineMatchesUndetected(sign: Sign, signType: StarshipSigns): Boolean {
		return sign.getLine(0) == signType.undetectedText
	}

	private fun matchesDetectedSign(signType: StarshipSigns, sign: Sign): Boolean {
		val baseLines = signType.baseLines
		return baseLines.withIndex().none { (index, line) ->
			line != null && sign.getLine(index) != line
		}
	}

	private fun detectSign(signType: StarshipSigns, player: Player, sign: Sign) {
		if (signType.onDetect(player, sign)) {
			for ((index, line) in signType.baseLines.withIndex()) {
				if (line == null) {
					continue
				}
				sign.setLine(index, line)
			}
			sign.update()
		}
	}

	private val cooldown = PerPlayerCooldown(250L, TimeUnit.MILLISECONDS)

	@EventHandler(priority = EventPriority.LOW)
	fun onClick(event: PlayerInteractEvent) {
		val player = event.player

		if (!isHoldingController(player)) {
			return
		}

		val starship = ActiveStarships.findByPassenger(player) ?: return

		val leftClick = event.action == Action.LEFT_CLICK_BLOCK || event.action == Action.LEFT_CLICK_AIR

		if (leftClick) {
			event.isCancelled = true
		}

		if (!leftClick) {
			val uuid = player.uniqueId
			val elapsedSinceRightClick = System.nanoTime() - rightClickTimes.getOrDefault(uuid, 0)
			if (elapsedSinceRightClick > TimeUnit.MILLISECONDS.toNanos(250)) {
				rightClickTimes[uuid] = System.nanoTime()
				return
			}
			rightClickTimes.remove(uuid)
		}

		if (event.clickedBlock?.type?.isSign == true) {
			return
		}

		cooldown.tryExec(player) {
			manualFire(player, starship, leftClick)
		}
	}

	private fun manualFire(player: Player, starship: ActiveStarship, leftClick: Boolean) {
		val loc = player.eyeLocation
		val playerFacing = player.facing
		val dir = loc.direction.normalize()

		val target: Vector? = getTarget(loc, dir, starship)

		val weaponSet = starship.weaponSetSelections[player.uniqueId]
		if (weaponSet == null && PilotedStarships[player] != starship) {
			return
		}

		val weapons = (if (weaponSet == null) starship.weapons else starship.weaponSets[weaponSet])
			.shuffled(ThreadLocalRandom.current())

		val queuedShots = queueShots(player, weapons, leftClick, playerFacing, dir, target)
		StarshipWeapons.fireQueuedShots(queuedShots, starship)
	}

	private fun getTarget(loc: Location, dir: Vector, starship: ActiveStarship): Vector? {
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
		player: Player,
		weapons: List<WeaponSubsystem>,
		leftClick: Boolean,
		playerFacing: BlockFace,
		dir: Vector,
		target: Vector?
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
