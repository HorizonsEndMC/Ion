package net.horizonsend.ion.server.features.starship.control

import io.papermc.paper.entity.TeleportFlag
import net.horizonsend.ion.common.extensions.userErrorAction
import net.horizonsend.ion.common.utils.miscellaneous.d
import net.horizonsend.ion.server.IonServerComponent
import net.horizonsend.ion.server.command.admin.debug
import net.horizonsend.ion.server.command.admin.debugBanner
import net.horizonsend.ion.server.features.space.Space
import net.horizonsend.ion.server.features.starship.PilotedStarships
import net.horizonsend.ion.server.features.starship.StarshipType.PLATFORM
import net.horizonsend.ion.server.features.starship.active.ActiveControlledStarship
import net.horizonsend.ion.server.features.starship.active.ActiveStarship
import net.horizonsend.ion.server.features.starship.active.ActiveStarships
import net.horizonsend.ion.server.features.starship.controllers.Controller
import net.horizonsend.ion.server.features.starship.hyperspace.Hyperspace
import net.horizonsend.ion.server.features.starship.movement.StarshipTeleportation
import net.horizonsend.ion.server.features.starship.movement.TranslateMovement
import net.horizonsend.ion.server.features.starship.subsystem.weapon.StarshipWeapons
import net.horizonsend.ion.server.features.starship.subsystem.weapon.TurretWeaponSubsystem
import net.horizonsend.ion.server.features.starship.subsystem.weapon.WeaponSubsystem
import net.horizonsend.ion.server.features.starship.subsystem.weapon.interfaces.HeavyWeaponSubsystem
import net.horizonsend.ion.server.features.starship.subsystem.weapon.interfaces.ManualWeaponSubsystem
import net.horizonsend.ion.server.miscellaneous.utils.PerPlayerCooldown
import net.horizonsend.ion.server.miscellaneous.utils.Tasks
import net.horizonsend.ion.server.miscellaneous.utils.actualType
import net.horizonsend.ion.server.miscellaneous.utils.displayNameString
import net.horizonsend.ion.server.miscellaneous.utils.isLava
import net.horizonsend.ion.server.miscellaneous.utils.isSign
import net.horizonsend.ion.server.miscellaneous.utils.isWater
import net.horizonsend.ion.server.miscellaneous.utils.listen
import net.horizonsend.ion.server.miscellaneous.utils.minecraft
import net.kyori.adventure.text.minimessage.MiniMessage
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
import org.bukkit.event.player.PlayerTeleportEvent
import org.bukkit.event.player.PlayerToggleSneakEvent
import org.bukkit.inventory.EquipmentSlot
import org.bukkit.inventory.ItemStack
import org.bukkit.util.Vector
import java.util.Collections
import java.util.LinkedList
import java.util.UUID
import java.util.concurrent.ThreadLocalRandom
import java.util.concurrent.TimeUnit
import kotlin.collections.set
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

object StarshipControl : IonServerComponent() {
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

		listen<PlayerToggleSneakEvent> { event ->
			PilotedStarships[event.player]?.sneakMovements = 0
		}

		listen<PlayerItemHeldEvent>(EventPriority.MONITOR, ignoreCancelled = true) { event ->
			val player = event.player
			val starship = ActiveStarships.findByPilot(player)
			if (starship == null || !starship.isDirectControlEnabled) {
				return@listen
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

		listen<PlayerMoveEvent> { event ->
			if (event.player.walkSpeed == 0.009f && PilotedStarships[event.player] == null) {
				event.player.walkSpeed = 0.2f
			}
		}
	}

	private fun processManualFlight(starship: ActiveControlledStarship) {
		if (starship.type == PLATFORM) {
			starship.controller?.userErrorAction("This ship type is not capable of moving.")
			return
		}

		if (starship.isTeleporting) {
			return
		}

		val playerPilot = starship.playerPilot ?: return

		if (starship.isDirectControlEnabled) {
			processDirectControl(starship)
		} else if (playerPilot.isSneaking) {
			processSneakFlight(playerPilot, starship)
		}
	}

	private fun processDirectControl(starship: ActiveControlledStarship) {
		if (starship.type == PLATFORM) {
			starship.controller!!.userErrorAction("This ship type is not capable of moving.")
			return
		}

		if (Hyperspace.isWarmingUp(starship) || Hyperspace.isMoving(starship)) {
			starship.setDirectControlEnabled(false)
			return
		}

		val playerPilot = starship.playerPilot ?: return
		val ping = getPing(playerPilot)
		val movementCooldown = starship.directControlCooldown
		val speedFac = if (ping > movementCooldown) 2 else 1
		val heldItemSlot = playerPilot.inventory.heldItemSlot
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
		if (playerPilot.isSneaking) {
			dx *= 2
			dz *= 2
		}
		var center = starship.directControlCenter
		if (center == null) {
			center = playerPilot.location.toBlockLocation().add(0.5, 0.0, 0.5)
			starship.directControlCenter = center
		}
		var vector = playerPilot.location.toVector().subtract(center.toVector())
		vector.setY(0)
		vector.normalize()

		val directionWrapper = center.clone()
		directionWrapper.direction = Vector(direction.modX, direction.modY, direction.modZ)

		val playerDirectionWrapper = center.clone()
		playerDirectionWrapper.direction = playerPilot.location.direction

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
			val newLoc = center.clone()
			newLoc.pitch = playerPilot.location.pitch
			newLoc.yaw = playerPilot.location.yaw
			playerPilot.teleport(
				newLoc,
				PlayerTeleportEvent.TeleportCause.PLUGIN,
				*TeleportFlag.Relative.values(),
				TeleportFlag.EntityState.RETAIN_OPEN_INVENTORY
			)
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

			dy > 0 && starship.max.y + dy > starship.serverLevel.world.maxHeight -> {
				dy = starship.serverLevel.world.maxHeight - starship.max.y
			}
		}

		if (locationCheck(starship, dx, dy, dz)) {
			return
		}

		if (dx == 0 && dy == 0 && dz == 0) {
			return
		}

		playerPilot.walkSpeed = 0.009f
		TranslateMovement.loadChunksAndMove(starship, dx, dy, dz)
	}

	private fun processSneakFlight(pilot: Player, starship: ActiveControlledStarship) {
		if (starship.type == PLATFORM) {
			pilot.userErrorAction("This ship type is not capable of moving.")
			return
		}

		if (Hyperspace.isWarmingUp(starship)) {
			starship.controller?.userErrorAction("Cannot move while in hyperspace warmup.")
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

		val maxAccel = starship.data.starshipType.actualType.maxSneakFlyAccel
		val accelDistance = starship.data.starshipType.actualType.sneakFlyAccelDistance

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

	fun locationCheck(starship: ActiveControlledStarship, dx: Int, dy: Int, dz: Int): Boolean {
		val world = starship.serverLevel.world
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

		val distance = (halfLength - 250) * max(0.15, newCenter.y / starship.serverLevel.world.maxHeight)
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

	fun calculateCooldown(movementCooldown: Long, heldItemSlot: Int) = movementCooldown - heldItemSlot * 8

	fun calculateSpeed(slot: Int) = if (slot == 0) -1 else (slot / DIRECT_CONTROL_DIVISOR).toInt()

	private fun accel(old: Double, new: Double, maxChange: Double): Double {
		val diff = new - old
		if (diff < maxChange) {
			return new
		}

		return old + min(maxChange, abs(diff)) * sign(diff)
	}

	private fun getPing(player: Player): Int {
		return player.minecraft.connection.player.latency
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

		player.debugBanner("INTERACT EVENT MANUAL FIRE START")
		if (!isHoldingController(player)) {
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

		if (itemStack.type != CONTROLLER_TYPE) return

		val itemName = itemStack.displayNameString.lowercase()

		if (starship.weaponSets.keys().contains(itemName)) {
			event.player.sendActionBar(
				MiniMessage.miniMessage().deserialize(
					"<gray>Now firing <aqua>$itemName<gray> weaponSet"
				)
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
		starship.controller?.playerPilot?.debug("Queuing shots")
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
			player.playerPilot?.debug("Weapon: $weapon")

			if (weapon !is ManualWeaponSubsystem) {
				player.playerPilot?.debug("Weapon is not manual")
				continue
			}

			if (!weapon.isAcceptableDirection(playerFacing)) {
				continue
			}

			if (weapon is HeavyWeaponSubsystem != !leftClick) {
				continue
			}

			if (!weapon.isCooledDown()) {
				player.playerPilot?.debug("Weapon not cooled down")
				continue
			}

			if (!weapon.isIntact()) {
				player.playerPilot?.debug("Weapon not intact")
				continue
			}

			val targetedDir: Vector = weapon.getAdjustedDir(dir, target)

			if (weapon is TurretWeaponSubsystem && !weapon.ensureOriented(targetedDir)) {
				player.playerPilot?.debug("Turret not oriented")
				continue
			}

			if (!weapon.canFire(targetedDir, target)) {
				player.playerPilot?.debug("Can not fire")
				continue
			}

			queuedShots.add(StarshipWeapons.ManualQueuedShot(weapon, player, targetedDir, target))
		}

		return queuedShots
	}
}
