package net.horizonsend.ion.server.features.starship

import com.google.common.collect.HashBiMap
import com.google.common.collect.HashMultimap
import it.unimi.dsi.fastutil.longs.LongOpenHashSet
import java.util.LinkedList
import java.util.UUID
import java.util.concurrent.CompletableFuture
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.TimeUnit
import kotlin.collections.set
import kotlin.math.cbrt
import kotlin.math.ln
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow
import kotlin.math.roundToInt
import kotlin.math.sqrt
import net.horizonsend.ion.common.database.Oid
import net.horizonsend.ion.common.database.schema.starships.StarshipData
import net.horizonsend.ion.common.extensions.hint
import net.horizonsend.ion.common.extensions.informationAction
import net.horizonsend.ion.common.extensions.serverError
import net.horizonsend.ion.common.extensions.success
import net.horizonsend.ion.common.utils.miscellaneous.d
import net.horizonsend.ion.common.utils.miscellaneous.squared
import net.horizonsend.ion.common.utils.text.MessageFactory
import net.horizonsend.ion.common.utils.text.bracketed
import net.horizonsend.ion.common.utils.text.colors.HEColorScheme
import net.horizonsend.ion.common.utils.text.ofChildren
import net.horizonsend.ion.common.utils.text.plainText
import net.horizonsend.ion.common.utils.text.randomString
import net.horizonsend.ion.server.IonServer
import net.horizonsend.ion.server.command.admin.debug
import net.horizonsend.ion.server.configuration.ServerConfiguration
import net.horizonsend.ion.server.features.multiblock.gravitywell.GravityWellMultiblock
import net.horizonsend.ion.server.features.progression.ShipKillXP
import net.horizonsend.ion.server.features.space.CachedPlanet
import net.horizonsend.ion.server.features.starship.PilotedStarships.isPiloted
import net.horizonsend.ion.server.features.starship.active.ActiveStarships
import net.horizonsend.ion.server.features.starship.control.controllers.Controller
import net.horizonsend.ion.server.features.starship.control.controllers.NoOpController
import net.horizonsend.ion.server.features.starship.control.controllers.ai.AIController
import net.horizonsend.ion.server.features.starship.control.controllers.player.ActivePlayerController
import net.horizonsend.ion.server.features.starship.control.controllers.player.PlayerController
import net.horizonsend.ion.server.features.starship.control.controllers.player.UnpilotedController
import net.horizonsend.ion.server.features.starship.control.movement.StarshipControl
import net.horizonsend.ion.server.features.starship.control.movement.StarshipCruising
import net.horizonsend.ion.server.features.starship.damager.Damager
import net.horizonsend.ion.server.features.starship.event.movement.StarshipMoveEvent
import net.horizonsend.ion.server.features.starship.event.movement.StarshipRotateEvent
import net.horizonsend.ion.server.features.starship.event.movement.StarshipTranslateEvent
import net.horizonsend.ion.server.features.starship.modules.RewardsProvider
import net.horizonsend.ion.server.features.starship.modules.SinkMessageFactory
import net.horizonsend.ion.server.features.starship.movement.RotationMovement
import net.horizonsend.ion.server.features.starship.movement.StarshipBlockedException
import net.horizonsend.ion.server.features.starship.movement.StarshipMovement
import net.horizonsend.ion.server.features.starship.movement.StarshipMovementException
import net.horizonsend.ion.server.features.starship.movement.TranslateMovement
import net.horizonsend.ion.server.features.starship.subsystem.StarshipSubsystem
import net.horizonsend.ion.server.features.starship.subsystem.checklist.FuelTankSubsystem
import net.horizonsend.ion.server.features.starship.subsystem.misc.GravityWellSubsystem
import net.horizonsend.ion.server.features.starship.subsystem.misc.HyperdriveSubsystem
import net.horizonsend.ion.server.features.starship.subsystem.misc.MagazineSubsystem
import net.horizonsend.ion.server.features.starship.subsystem.misc.NavCompSubsystem
import net.horizonsend.ion.server.features.starship.subsystem.misc.PlanetDrillSubsystem
import net.horizonsend.ion.server.features.starship.subsystem.reactor.ReactorSubsystem
import net.horizonsend.ion.server.features.starship.subsystem.shield.ShieldSubsystem
import net.horizonsend.ion.server.features.starship.subsystem.thruster.ThrustData
import net.horizonsend.ion.server.features.starship.subsystem.thruster.ThrusterSubsystem
import net.horizonsend.ion.server.features.starship.subsystem.weapon.TurretWeaponSubsystem
import net.horizonsend.ion.server.features.starship.subsystem.weapon.WeaponSubsystem
import net.horizonsend.ion.server.features.world.IonWorld
import net.horizonsend.ion.server.miscellaneous.utils.CARDINAL_BLOCK_FACES
import net.horizonsend.ion.server.miscellaneous.utils.Tasks
import net.horizonsend.ion.server.miscellaneous.utils.Vec3i
import net.horizonsend.ion.server.miscellaneous.utils.actualType
import net.horizonsend.ion.server.miscellaneous.utils.blockKey
import net.horizonsend.ion.server.miscellaneous.utils.blockKeyX
import net.horizonsend.ion.server.miscellaneous.utils.blockKeyY
import net.horizonsend.ion.server.miscellaneous.utils.blockKeyZ
import net.horizonsend.ion.server.miscellaneous.utils.bukkitWorld
import net.horizonsend.ion.server.miscellaneous.utils.getBlockTypeSafe
import net.horizonsend.ion.server.miscellaneous.utils.leftFace
import net.horizonsend.ion.server.miscellaneous.utils.minecraft
import net.horizonsend.ion.server.miscellaneous.utils.rightFace
import net.kyori.adventure.audience.Audience
import net.kyori.adventure.audience.ForwardingAudience
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.Component.space
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.event.ClickEvent
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.NamedTextColor.RED
import net.kyori.adventure.text.minimessage.MiniMessage.miniMessage
import net.starlegacy.feature.starship.active.ActiveStarshipHitbox
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.World
import org.bukkit.block.BlockFace
import org.bukkit.block.Sign
import org.bukkit.boss.BossBar
import org.bukkit.entity.Entity
import org.bukkit.entity.Player
import org.bukkit.util.NumberConversions
import org.bukkit.util.Vector

class Starship (
	val data: StarshipData,
	var blocks: LongOpenHashSet,
	val mass: Double,
	var centerOfMass: Vec3i,
	private val hitbox: ActiveStarshipHitbox,
	carriedShips: Map<StarshipData, LongOpenHashSet> // map of carried ship to its blocks
) : ForwardingAudience {
	// Data Aliases
	val dataId: Oid<out StarshipData> = data._id
	val type: StarshipType = data.starshipType.actualType
	val balancing = type.balancingSupplier.get()
	val interdictionRange: Int = balancing.interdictionRange
	val charIdentifier = randomString(5L) // Created once
	/** Name is misleading, would be more accurate to call this `activationTime` */
	val creationTime = System.currentTimeMillis()
	val initialBlockCount: Int = blocks.size
	var currentBlockCount: Int = initialBlockCount
	var lastUnpilotTime: Long = 0
	val minutesUnpiloted get() = if (isPiloted(this) || controller is NoOpController) 0 else TimeUnit.NANOSECONDS.toMinutes(System.nanoTime() - lastUnpilotTime)
	var pilotDisconnectLocation: Vec3i? = null
	val carriedShips: MutableMap<StarshipData, LongOpenHashSet> = carriedShips.toMutableMap()

	var world: World = data.bukkitWorld()
		set(value) {
			ActiveStarships.updateWorld(this, field, value)
			field = value
		}

	/** Called on each server tick. */
	fun tick() {
		controller.tick()

		subsystems.forEach { it.tick() }
	}

	/** Called when a starship is removed. Any cleanup logic should be done here. */
	fun destroy() {
		IonWorld[world].starships.remove(this)
		controller.destroy()
	}

	//region Pilot & Controller
	/**
	 * If the controller is an active player controller, get the player.
	 * It just makes a lot of things less verbose.
	 *
	 * Try not to use, most starship code should not rely on players.
	 **/
	val playerPilot: Player? get() = (controller as? ActivePlayerController)?.player

	fun requirePlayerController(): Player = requireNotNull((controller as? PlayerController)?.player) { "Starship must be piloted!" }

	var controller: Controller = NoOpController(this, null); private set

	fun setController(value: Controller, updateMap: Boolean = true) {
		if (updateMap) PilotedStarships.changeController(this, value)
		value.hint("Updated control mode to ${value.name}.")
		controller.destroy()
		controller = value
	}
	//endregion

	//region Ship Blocks & Hitbox
	var hullIntegrity = 1.0
	fun updateHullIntegrity() {
		currentBlockCount = blocks.count {
			getBlockTypeSafe(world, blockKeyX(it), blockKeyY(it), blockKeyZ(it))?.isAir != true
		}
		hullIntegrity = currentBlockCount.toDouble() / initialBlockCount.toDouble()
	}

	inline fun iterateBlocks(x: (Int, Int, Int) -> Unit) {
		for (key in blocks.iterator()) {
			x(blockKeyX(key), blockKeyY(key), blockKeyZ(key))
		}
	}

	val min: Vec3i get() = hitbox.min
	val max: Vec3i get() = hitbox.max

	fun calculateHitbox() {
		this.hitbox.calculate(this.blocks)
	}

	fun calculateMinMax() {
		this.hitbox.calculateMinMax(this.blocks)
	}

	fun isInBounds(x: Int, y: Int, z: Int): Boolean {
		return x >= min.x && y >= min.y && z >= min.z && x <= max.x && y <= max.y && z <= max.z
	}

	fun isWithinHitbox(x: Int, y: Int, z: Int, tolerance: Int = 2): Boolean {
		return isInBounds(x, y, z) && hitbox.contains(x, y, z, min.x, min.y, min.z, tolerance)
	}

	fun isWithinHitbox(loc: Location, tolerance: Int = 2): Boolean {
		return world == loc.world && isWithinHitbox(loc.blockX, loc.blockY, loc.blockZ, tolerance)
	}

	fun isWithinHitbox(entity: Entity, tolerance: Int = 2): Boolean {
		val loc = entity.location
		return isWithinHitbox(loc, tolerance)
	}

	fun contains(x: Int, y: Int, z: Int): Boolean {
		return isInBounds(x, y, z) && blocks.contains(blockKey(x, y, z))
	}

	fun isInternallyObstructed(origin: Vec3i, dir: Vector, maxDistance: Int? = null): Boolean {
		var x = origin.x.toDouble() + 0.5
		var y = origin.y.toDouble() + 0.5
		var z = origin.z.toDouble() + 0.5
		var distance = 0
		while (maxDistance == null || distance <= maxDistance) {
			val blockX = NumberConversions.floor(x)
			val blockY = NumberConversions.floor(y)
			val blockZ = NumberConversions.floor(z)
			if (!isInBounds(
					blockX,
					blockY,
					blockZ
				) && distance > (max.x - min.x) && distance > (max.y - min.y) && distance > (max.z - min.z)
			) {
				break
			}

			if (contains(blockX, blockY, blockZ)) {
				return true
			}
			x += dir.x
			y += dir.y
			z += dir.z
			distance++
		}
		return false
	}
	//endregion

	//region Rotation
	data class PendingRotation(val clockwise: Boolean)

	val pendingRotations = LinkedBlockingQueue<PendingRotation>()
	private val rotationTime get() = TimeUnit.MILLISECONDS.toNanos(50L + initialBlockCount / 30L)

	fun getTargetForward(): BlockFace {
		val rotation = pendingRotations.peek()
		return when {
			rotation == null -> forward
			rotation.clockwise -> forward.rightFace
			else -> forward.leftFace
		}
	}

	fun tryRotate(clockwise: Boolean) {
//		Throwable("WHOS TRYING TO ROTATE").printStackTrace()
		pendingRotations.add(PendingRotation(clockwise))

		if (pendingRotations.size > 1) {
			return
		}

		scheduleRotation()
	}

	private fun scheduleRotation() {
		val rotationTimeTicks = TimeUnit.NANOSECONDS.toMillis(rotationTime) / 50L
		Tasks.sync {
			(controller as? ActivePlayerController)?.player?.setCooldown(StarshipControl.CONTROLLER_TYPE, rotationTimeTicks.toInt())
		}
		Tasks.syncDelay(rotationTimeTicks) {
			if (pendingRotations.none()) {
				return@syncDelay
			}

			val rotation = pendingRotations.poll()

			if (pendingRotations.any()) {
				scheduleRotation()
			}

			moveAsync(RotationMovement(this, rotation.clockwise))
		}
	}
	//endregion

	//region Movement
	var cruiseData = StarshipCruising.CruiseData(this)
	var lastBlockedTime: Long = 0
	val manualMoveCooldownMillis: Long = (cbrt(initialBlockCount.toDouble()) * 40).toLong()
	var speedLimit = -1
	// manual move is sneak/direct control
	var lastManualMove = System.nanoTime() / 1_000_000
	var sneakMovements = 0

	/**
	 * Non-normalized vector containing the ships velocity
	 * Used for target lead / speed estimations
	 */
	var velocity: Vector = Vector(0.0, 0.0, 0.0)

	fun moveAsync(movement: StarshipMovement): CompletableFuture<Boolean> {
		if (!ActiveStarships.isActive(this)) {
			return CompletableFuture.completedFuture(false)
		}

		val pilot = this.controller

		val event: StarshipMoveEvent = when (movement) {
			is TranslateMovement -> StarshipTranslateEvent(this, pilot, movement)
			is RotationMovement -> StarshipRotateEvent(this, pilot, movement)
			else -> error("Unrecognized movement type ${movement.javaClass.name}")
		}

		if (!event.callEvent()) {
			return CompletableFuture.completedFuture(false)
		}

		val future = CompletableFuture<Boolean>()
		Tasks.async {
			val result = executeMovement(movement, pilot)
			future.complete(result)
			controller.onMove(movement)
			subsystems.forEach { runCatching { it.onMovement(movement) } }
		}

		return future
	}

	@Synchronized
	private fun executeMovement(movement: StarshipMovement, controller: Controller): Boolean {
		try {
			movement.execute()
		} catch (e: StarshipMovementException) {
			val location = if (e is StarshipBlockedException) e.location else null
			controller.onBlocked(movement, e, location)
			controller.sendMessage(e.formatMessage())

			sneakMovements = 0
			lastBlockedTime = System.currentTimeMillis()
			return false
		} catch (e: Throwable) {
			serverError("There was an unhandled exception during movement! Please forward this to staff")
			val stackTrace = "$e\n" + e.stackTrace.joinToString(separator = "\n")

			val exceptionMessage = ofChildren(text(e.message ?: "No message provided", RED), space(), bracketed(text("Hover for info", HEColorScheme.HE_LIGHT_GRAY)))
				.hoverEvent(text(stackTrace))
				.clickEvent(ClickEvent.copyToClipboard(stackTrace))

			sendMessage(exceptionMessage)

			IonServer.slF4JLogger.error(e.message)
			e.printStackTrace()

			return false
		}

		return true
	}
	//endregion

	//region Direct Control
	val directControlCooldown get() = 300L + (initialBlockCount / 700) * 30
	var isDirectControlEnabled: Boolean = false
		private set
	val directControlPreviousVectors = LinkedBlockingQueue<Vector>(4)
	val directControlVector: Vector = Vector()
	var directControlCenter: Location? = null

	fun setDirectControlEnabled(enabled: Boolean) {
		isDirectControlEnabled = enabled
		if (enabled) {
			val dcMessage = text()
				.append(text("Direct Control: ", NamedTextColor.GRAY))
				.append(text("ON ", NamedTextColor.GRAY))
				.append(text("[Use /dc to turn it off - scroll or use hotbar keys to adjust speed - use W/A/S/D to maneuver - hold sneak (", NamedTextColor.YELLOW))
				.append(Component.keybind("key.sneak", NamedTextColor.YELLOW))
				.append(text(") for a boost]", NamedTextColor.YELLOW))
				.build()

			sendMessage(dcMessage)

			val player: Player = (controller as? PlayerController)?.player ?: return

			player.walkSpeed = 0.009f
			val playerLoc = player.location
			directControlCenter = playerLoc.toBlockLocation().add(0.5, playerLoc.y.rem(1)+0.001, 0.5)
			player.teleport(directControlCenter!!)
		} else {
			sendMessage(
				text()
					.append(text("Direct Control: ", NamedTextColor.GRAY))
					.append(text("OFF ", NamedTextColor.RED))
					.append(text("[Use /dc to turn it on]", NamedTextColor.YELLOW))
					.build()
			)

			directControlVector.x = 0.0
			directControlVector.y = 0.0
			directControlVector.z = 0.0

			val player: Player = (controller as? PlayerController)?.player ?: return
			player.walkSpeed = 0.2f // default
		}
	}
	//endregion

	//region Subsystems
	val subsystems = LinkedList<StarshipSubsystem>()
	var drillCount = 0

	lateinit var reactor: ReactorSubsystem
	val shields = LinkedList<ShieldSubsystem>()
	val weapons = LinkedList<WeaponSubsystem>()
	val turrets = LinkedList<TurretWeaponSubsystem>()
	val hyperdrives = LinkedList<HyperdriveSubsystem>()
	val navComps = LinkedList<NavCompSubsystem>()
	val thrusters = LinkedList<ThrusterSubsystem>()
	val magazines = LinkedList<MagazineSubsystem>()
	val gravityWells = LinkedList<GravityWellSubsystem>()
	val drills = LinkedList<PlanetDrillSubsystem>()
	val fuelTanks = LinkedList<FuelTankSubsystem>()

	val shieldBars = mutableMapOf<String, BossBar>()

	val weaponSets: HashMultimap<String, WeaponSubsystem> = HashMultimap.create()
	val weaponSetSelections: HashBiMap<UUID, String> = HashBiMap.create()

	val autoTurretTargets = mutableMapOf<String, AutoTurretTargeting.AutoTurretTarget<*>>()

	val shieldEfficiency: Double
		get() = (shields.size.d().pow(0.9) / (initialBlockCount / 500.0).coerceAtLeast(1.0).pow(0.7))
			.coerceAtMost(1.0)

	val maxShields: Int = (0.00671215 * initialBlockCount.toDouble().pow(0.836512) - 0.188437).toInt()
		get() = if (initialBlockCount < 500) (1 - field + (1)) else field

	val thrusterMap = mutableMapOf<BlockFace, ThrustData>()

	// used to identify the ship to auto turrets
	val identifier get() = getAutoTurretIdentifier()

	var isTeleporting: Boolean = false

	var lastTick = System.nanoTime()

	/** Ignore weapon color, use rainbows for pride month **/
	var rainbowToggle = false

	var targetedPosition: Location? = null
	var beacon: ServerConfiguration.HyperspaceBeacon? = null
	var forward: BlockFace = BlockFace.NORTH
	var isExploding = false

	var isInterdicting = false; private set

	fun setIsInterdicting(value: Boolean) {
		Tasks.checkMainThread()
		isInterdicting = value

		gravityWells
			.filter { it.isIntact() }
			.map { it.pos.toLocation(world).block.state }
			.filterIsInstance<Sign>()
			.forEach { GravityWellMultiblock.setEnabled(it, value) }

		if (!value) {
			onlinePassengers.forEach { player -> player.success("Gravity well disabled") }

			return
		}

		onlinePassengers.forEach { player -> player.success("Gravity well enabled") }
	}

	val disabledThrusterRatio: Double get() =
		thrusters.count { it.lastIonTurretLimited < (System.currentTimeMillis() - 5000L) } / thrusters.size.toDouble()

	fun generateThrusterMap() {
		for (face in CARDINAL_BLOCK_FACES) {
			val faceThrusters = thrusters.filter { it.face == face }
			val data = buildThrustData(faceThrusters)
			thrusterMap[face] = data
		}
	}

	private fun buildThrustData(faceThrusters: List<ThrusterSubsystem>): ThrustData {
		if (faceThrusters.none()) {
			return ThrustData(0.0, 0)
		}

		val baseSpeedFactor = 50.0
		val speedExponent = 0.5
		val massExponent = 0.2
		val reductionBase = 0.85
		val finalSpeedFactor = 1.0

		val mass = this.mass
		val totalAccel = 1.0 + faceThrusters.sumOf { it.type.accel }
		val totalWeight = faceThrusters.sumOf { it.type.weight }.toDouble()
		val reduction = reductionBase.pow(sqrt(totalWeight))
		val totalSpeed = faceThrusters.sumOf { it.type.speed } * reduction

		val calculatedSpeed = totalSpeed.pow(speedExponent) / mass.pow(massExponent) * baseSpeedFactor

		val maxSpeed = reactor.output * .4 / totalSpeed

		val speed = (min(maxSpeed, calculatedSpeed) * finalSpeedFactor).roundToInt()

		val acceleration = ln(2.0 + totalAccel) * ln(2.0 + totalWeight) / ln(mass.squared()) * reduction * 30.0
		return ThrustData(acceleration, speed)
	}

	/** get the thruster data for this direction. if it's diagonal, it returns the faster side's speed. */
	fun getThrustData(dx: Int, dz: Int): ThrustData {
		val xDirection = if (dx > 0) BlockFace.EAST else BlockFace.WEST
		val zDirection = if (dz > 0) BlockFace.SOUTH else BlockFace.NORTH
		val xData = thrusterMap.getValue(xDirection)
		val zData = thrusterMap.getValue(zDirection)
		return when {
			dx != 0 && dz != 0 -> when {
				xData.maxSpeed > zData.maxSpeed -> xData
				zData.maxSpeed > xData.maxSpeed -> zData
				xData.accel > zData.accel -> xData
				zData.accel > xData.accel -> zData
				else -> xData
			}

			dx != 0 -> xData
			dz != 0 -> zData
			else -> error("Can't get thruster data for $dx $dz")
		}
	}

	fun updatePower(sender: String, shield: Int, weapon: Int, thruster: Int) {
		reactor.powerDistributor.setDivision(shield / 100.0, weapon / 100.0, thruster / 100.0)

		onlinePassengers.forEach { player ->
			player.informationAction(
				"<green>$sender</green> updated the power mode to <aqua>$shield% shield <red>$weapon% weapon <yellow>$thruster% thruster"
			)
		}
	}

	fun getEntryRange(planet: CachedPlanet): Int {
		return planet.atmosphereRadius + max(max.x - min.x, max.z - min.z) / 2 + 10
	}

	private fun getAutoTurretIdentifier(): String = when (controller) {
		is UnpilotedController -> "UnpilotedShip:$charIdentifier"

		is PlayerController -> (controller as PlayerController).player.name

		is AIController -> "${getDisplayNamePlain().replace(' ', '_')}:$charIdentifier"

		is NoOpController -> "${getDisplayNamePlain()}:$charIdentifier"

		else -> throw NotImplementedError("${controller::class.java.simpleName} does not have an auto turret identifier!")
	}
	//endregion

	//region Kill Credit
	val rewardsProviders: LinkedList<RewardsProvider> = LinkedList<RewardsProvider>()
	var sinkMessageFactory: MessageFactory = SinkMessageFactory(this)

	val damagers = mutableMapOf<Damager, ShipKillXP.ShipDamageData>()
	fun lastDamagedOrNull(): Long? = damagers.maxOfOrNull { it.value.lastDamaged }

	fun addDamager(damager: Damager, points: Int = 1) {
		damagers.getOrPut(damager) {
			ShipKillXP.ShipDamageData()
		}.incrementPoints(points)

		debug("$damager added to damagers")
		damager.debug("$damager added to $identifier's damagers")

		controller.onDamaged(damager)
	}
	//endregion

	//region Passengers
	private val passengers = HashSet<UUID>()
	val passengerIDs get() = passengers.toList()
	val onlinePassengers get() = passengers.mapNotNull(Bukkit::getPlayer)

	fun isPassenger(playerID: UUID): Boolean {
		return passengers.contains(playerID)
	}

	fun addPassenger(playerID: UUID) {
		passengers.add(playerID)
	}

	fun removePassenger(playerID: UUID) {
		passengers.remove(playerID)
		val player = Bukkit.getPlayer(playerID) ?: return
		for (shieldBar in shieldBars.values) {
			shieldBar.removePlayer(player)
		}
	}

	fun clearPassengers() {
		for (passenger in onlinePassengers) {
			for (shieldBar in shieldBars.values) {
				shieldBar.removePlayer(passenger)
			}
		}
		passengers.clear()
	}

	override fun audiences(): Iterable<Audience> = onlinePassengers
	//endregion

	//region Display Name
	/** Gets the minimessage display name of this starship */
	fun getDisplayNameMiniMessage(): String = this.data.name ?: this.type.displayNameMiniMessage

	/** Gets the component display name of this starship */
	fun getDisplayName(): Component {
		val name = this.data.name ?: return type.displayNameComponent

		return miniMessage().deserialize(name)
	}

	/** Gets the plain text serialized version of this starship's display name */
	fun getDisplayNamePlain(): String = getDisplayName().plainText()
	//endregion

	init {
		IonWorld[world].starships.add(this)
	}
}
