package net.starlegacy.feature.starship.control

import java.util.Collections
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
import kotlin.math.round
import kotlin.math.roundToInt
import kotlin.math.sign
import kotlin.math.sin
import net.horizonsend.ion.core.feedback.FeedbackType
import net.horizonsend.ion.core.feedback.sendFeedbackMessage
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
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
import net.starlegacy.util.ConnectionUtils
import net.starlegacy.util.PerPlayerCooldown
import net.starlegacy.util.Tasks
import net.starlegacy.util.d
import net.starlegacy.util.isLava
import net.starlegacy.util.isSign
import net.starlegacy.util.isWater
import net.starlegacy.util.nms
import org.bukkit.ChatColor
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
import org.bukkit.event.player.PlayerItemHeldEvent
import org.bukkit.event.player.PlayerMoveEvent
import org.bukkit.event.player.PlayerSwapHandItemsEvent
import org.bukkit.event.player.PlayerToggleSneakEvent
import org.bukkit.inventory.EquipmentSlot
import org.bukkit.util.Vector

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

	private const val DIRECT_CONTROL_DIVISOR = 1.75

	private fun manualFlight() {

		Tasks.syncRepeat(1, 1) {
			for (starship in ActiveStarships.allPlayerShips()) {
				processManualFlight(starship)
			}
		}

		subscribe<PlayerToggleSneakEvent> { event ->
			PilotedStarships[event.player]?.sneakMovements = 0
		}

		subscribe<PlayerItemHeldEvent>(EventPriority.MONITOR, ignoreCancelled = true) { event ->
			val player = event.player
			val starship = ActiveStarships.findByPilot(player)
			if (starship == null || !starship.isDirectControlEnabled) {
				return@subscribe
			}
			val inventory = player.inventory
			val previousSlot = event.previousSlot
			val oldItem = inventory.getItem(previousSlot)
			val newSlot = event.newSlot
			val newItem = player.inventory.getItem(newSlot)
			inventory.setItem(newSlot, oldItem)
			inventory.setItem(previousSlot, newItem)
			val baseSpeed = calculateSpeed(newSlot)
			val cooldown: Long = calculateCooldown(starship.directControlCooldown, newSlot)
			val speed = (10.0f * baseSpeed * (1000.0f / cooldown)).roundToInt() / 10.0f
			player.sendActionBar(ChatColor.AQUA.toString() + "Speed: " + speed)
		}

		subscribe<PlayerMoveEvent> { event ->
			if (event.player.walkSpeed == 0.009f && PilotedStarships[event.player] == null) {
				event.player.walkSpeed = 0.2f
			}
		}
	}

	private fun processManualFlight(starship: ActivePlayerStarship) {
		if (starship.type == PLATFORM) {
			starship.pilot?.sendMessage(Component.text("This ship type is not capable of moving.", NamedTextColor.RED))
			return
		}

		if (starship.isTeleporting) {
			return
		}

		val pilot = starship.pilot ?: return

		if (starship.isDirectControlEnabled) {
			processDirectControl(starship)
		} else if (pilot.isSneaking) {
			processSneakFlight(pilot, starship)
		}
	}

	private fun processDirectControl(starship: ActivePlayerStarship) {
		if (starship.type == PLATFORM) {
			starship.pilot!!.sendMessage(Component.text("This ship type is not capable of moving.", NamedTextColor.RED))
			return
		}

		if (starship.isInterdicting || Hyperspace.isWarmingUp(starship) || Hyperspace.isMoving(starship)) {
			starship.setDirectControlEnabled(false)
			return
		}

		val pilot = starship.pilot ?: return
		val ping = getPing(pilot)
		val movementCooldown = starship.directControlCooldown
		val speedFac = if (ping > movementCooldown) 2 else 1
		val heldItemSlot = pilot.inventory.heldItemSlot
		val cooldown = calculateCooldown(movementCooldown, heldItemSlot) * speedFac
		val currentTime = System.currentTimeMillis()
		val lastManualMove = starship.lastManualMove
		val elapsedSinceLastMove = currentTime - lastManualMove
		if (elapsedSinceLastMove < cooldown) {
			return
		}
		starship.lastManualMove = currentTime

		var dx = 0
		var dy = 0
		var dz = 0
		val direction = starship.getTargetForward()
		val targetSpeed = calculateSpeed(heldItemSlot)
		dx += (targetSpeed * direction.modX)
		dz += (targetSpeed * direction.modZ)
		if (pilot.isSneaking) {
			dx *= 2
			dz *= 2
		}
		var center = starship.directControlCenter
		if (center == null) {
			center = pilot.location.toBlockLocation().add(0.5, 0.0, 0.5)
			starship.directControlCenter = center
		}
		var vector = pilot.location.toVector().subtract(center.toVector())
		vector.setY(0)
		vector.normalize()

		val directionWrapper = center.clone()
		directionWrapper.direction = Vector(direction.modX, direction.modY, direction.modZ)

		val playerDirectionWrapper = center.clone()
		playerDirectionWrapper.direction = pilot.location.direction

		val vectorWrapper = center.clone()
		vectorWrapper.direction = vector

		vectorWrapper.yaw = vectorWrapper.yaw - (playerDirectionWrapper.yaw - directionWrapper.yaw)
		vector = vectorWrapper.direction

		vector.x = round(vector.x)
		vector.setY(0)
		vector.z = round(vector.z)

		val vectors = starship.directControlPreviousVectors
		if (vectors.size > 3) {
			vectors.poll()
		}
		vectors.add(vector)

		if (vector.x != 0.0 || vector.z != 0.0) {
			ConnectionUtils.teleport(pilot, center)
		}

		var highestFrequency = Collections.frequency(vectors, vector)
		for (previousVector in vectors) {
			val frequency = Collections.frequency(vectors, previousVector)
			if (previousVector != vector && frequency > highestFrequency) {
				highestFrequency = frequency
				vector = previousVector
			}
		}

		val forwardZ = direction.modZ != 0
		val strafeAxis = if (forwardZ) vector.x else vector.z
		val strafe = sign(strafeAxis).toInt() * abs(targetSpeed)
		val ascensionAxis = if (forwardZ) vector.z * -direction.modZ else vector.x * -direction.modX
		val ascension = sign(ascensionAxis).toInt() * abs(targetSpeed)
		if (forwardZ) dx += strafe else dz += strafe
		dy += ascension

		val deltaTime = elapsedSinceLastMove / 1000.0
		val maxChange = 15 * starship.reactor.powerDistributor.thrusterPortion * deltaTime

		val directControlVec = starship.directControlVector
		val offset = Vector(dx, dy, dz).subtract(directControlVec)
		if (offset.length() > maxChange) {
			offset.normalize().multiply(maxChange)
		}
		directControlVec.add(offset)
		dx = directControlVec.blockX
		dy = directControlVec.blockY
		dz = directControlVec.blockZ

		dx *= speedFac
		dy *= speedFac
		dz *= speedFac

		when {
			dy < 0 && starship.min.y + dy < 0 -> {
				dy = -starship.min.y
			}

			dy > 0 && starship.max.y + dy > starship.world.maxHeight -> {
				dy = starship.world.maxHeight - starship.max.y
			}
		}

		if (locationCheck(starship, dx, dy, dz)) {
			return
		}

		if (dx == 0 && dy == 0 && dz == 0) {
			return
		}

		pilot.walkSpeed = 0.009f
		TranslateMovement.loadChunksAndMove(starship, dx, dy, dz)
	}

	private fun processSneakFlight(pilot: Player, starship: ActivePlayerStarship) {
		if (starship.type == PLATFORM) {
			pilot.sendMessage(Component.text("This ship type is not capable of moving.", NamedTextColor.RED))
			return
		}

		if (Hyperspace.isWarmingUp(starship)) {
			starship.pilot?.sendFeedbackMessage(FeedbackType.USER_ERROR, "Cannot move while in hyperspace warmup.")
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

		val maxAccel = starship.data.type.maxSneakFlyAccel
		val accelDistance = starship.data.type.sneakFlyAccelDistance

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
		val newCenter = starship.centerOfMass.toLocation(world).add(dx.d(), dy.d(), dz.d())

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

	private fun calculateCooldown(movementCooldown: Long, heldItemSlot: Int) = movementCooldown - heldItemSlot * 8

	private fun calculateSpeed(slot: Int) = if (slot == 0) -1 else (slot / DIRECT_CONTROL_DIVISOR).toInt()

	private fun accel(old: Double, new: Double, maxChange: Double): Double {
		val diff = new - old
		if (diff < maxChange) {
			return new
		}

		return old + min(maxChange, abs(diff)) * sign(diff)
	}

	private fun getPing(player: Player): Int {
		return player.nms.connection.player.latency
	}

	private fun rotation() {
		subscribe<PlayerDropItemEvent>(priority = EventPriority.LOWEST) { event ->
			val starship = PilotedStarships[event.player] ?: return@subscribe
			if (event.itemDrop.itemStack.type != CONTROLLER_TYPE) return@subscribe

			event.isCancelled = true
			starship.tryRotate(false)
		}

		subscribe<PlayerSwapHandItemsEvent>(priority = EventPriority.LOWEST) { event ->
			val starship = PilotedStarships[event.player] ?: return@subscribe
			if (event.offHandItem?.type != CONTROLLER_TYPE) return@subscribe

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
