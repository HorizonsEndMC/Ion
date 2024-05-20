package net.horizonsend.ion.server.features.starship.active

import com.google.common.collect.HashBiMap
import com.google.common.collect.HashMultimap
import it.unimi.dsi.fastutil.longs.LongOpenHashSet
import net.horizonsend.ion.common.extensions.hint
import net.horizonsend.ion.common.extensions.informationAction
import net.horizonsend.ion.common.extensions.success
import net.horizonsend.ion.common.utils.miscellaneous.d
import net.horizonsend.ion.common.utils.miscellaneous.squared
import net.horizonsend.ion.common.utils.text.MessageFactory
import net.horizonsend.ion.common.utils.text.plainText
import net.horizonsend.ion.common.utils.text.randomString
import net.horizonsend.ion.server.command.admin.debug
import net.horizonsend.ion.server.configuration.StarshipBalancing
import net.horizonsend.ion.server.features.multiblock.gravitywell.GravityWellMultiblock
import net.horizonsend.ion.server.features.progression.ShipKillXP
import net.horizonsend.ion.server.features.space.CachedPlanet
import net.horizonsend.ion.server.features.starship.AutoTurretTargeting
import net.horizonsend.ion.server.features.starship.PilotedStarships
import net.horizonsend.ion.server.features.starship.StarshipType
import net.horizonsend.ion.server.features.starship.control.controllers.Controller
import net.horizonsend.ion.server.features.starship.control.controllers.NoOpController
import net.horizonsend.ion.server.features.starship.control.controllers.ai.AIController
import net.horizonsend.ion.server.features.starship.control.controllers.player.ActivePlayerController
import net.horizonsend.ion.server.features.starship.control.controllers.player.PlayerController
import net.horizonsend.ion.server.features.starship.control.controllers.player.UnpilotedController
import net.horizonsend.ion.server.features.starship.damager.Damager
import net.horizonsend.ion.server.features.starship.modules.RewardsProvider
import net.horizonsend.ion.server.features.starship.movement.StarshipMovement
import net.horizonsend.ion.server.features.starship.subsystem.FuelTankSubsystem
import net.horizonsend.ion.server.features.starship.subsystem.GravityWellSubsystem
import net.horizonsend.ion.server.features.starship.subsystem.HyperdriveSubsystem
import net.horizonsend.ion.server.features.starship.subsystem.MagazineSubsystem
import net.horizonsend.ion.server.features.starship.subsystem.NavCompSubsystem
import net.horizonsend.ion.server.features.starship.subsystem.PlanetDrillSubsystem
import net.horizonsend.ion.server.features.starship.subsystem.StarshipSubsystem
import net.horizonsend.ion.server.features.starship.subsystem.reactor.ReactorSubsystem
import net.horizonsend.ion.server.features.starship.subsystem.shield.ShieldSubsystem
import net.horizonsend.ion.server.features.starship.subsystem.thruster.ThrustData
import net.horizonsend.ion.server.features.starship.subsystem.thruster.ThrusterSubsystem
import net.horizonsend.ion.server.features.starship.subsystem.weapon.TurretWeaponSubsystem
import net.horizonsend.ion.server.features.starship.subsystem.weapon.WeaponSubsystem
import net.horizonsend.ion.server.miscellaneous.IonWorld
import net.horizonsend.ion.server.miscellaneous.utils.CARDINAL_BLOCK_FACES
import net.horizonsend.ion.server.miscellaneous.utils.Tasks
import net.horizonsend.ion.server.miscellaneous.utils.Vec3i
import net.horizonsend.ion.server.miscellaneous.utils.blockKey
import net.horizonsend.ion.server.miscellaneous.utils.blockKeyX
import net.horizonsend.ion.server.miscellaneous.utils.blockKeyY
import net.horizonsend.ion.server.miscellaneous.utils.blockKeyZ
import net.horizonsend.ion.server.miscellaneous.utils.getBlockTypeSafe
import net.horizonsend.ion.server.miscellaneous.utils.minecraft
import net.kyori.adventure.audience.Audience
import net.kyori.adventure.audience.ForwardingAudience
import net.kyori.adventure.text.Component
import net.starlegacy.feature.starship.active.ActiveStarshipHitbox
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.World
import org.bukkit.block.BlockFace
import org.bukkit.block.Sign
import org.bukkit.entity.Entity
import org.bukkit.entity.Player
import org.bukkit.util.NumberConversions
import org.bukkit.util.Vector
import java.util.LinkedList
import java.util.UUID
import java.util.concurrent.CompletableFuture
import kotlin.collections.set
import kotlin.math.ln
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow
import kotlin.math.roundToInt
import kotlin.math.sqrt

abstract class ActiveStarship (
	world: World,
	var blocks: LongOpenHashSet,
	val mass: Double,
	var centerOfMass: Vec3i,
	private val hitbox: ActiveStarshipHitbox
) : ForwardingAudience {
	override fun audiences(): Iterable<Audience> = onlinePassengers

	abstract val type: StarshipType
	abstract val balancing: StarshipBalancing

	var world: World = world
		set(value) {
			ActiveStarships.updateWorld(this, field, value)
			field = value
		}

	var controller: Controller = NoOpController(this, null); private set

	fun setController(value: Controller, updateMap: Boolean = true) {
		if (this is ActiveControlledStarship && updateMap) PilotedStarships.changeController(this, value)
		value.hint("Updated control mode to ${value.name}.")
		controller.destroy()
		controller = value
	}

	var pilotDisconnectLocation: Vec3i? = null

	abstract val rewardsProviders: LinkedList<RewardsProvider>
	abstract var sinkMessageFactory: MessageFactory

	/**
	 * If the controller is an active player controller, get the player.
	 * It just makes a lot of things less verbose.
	 *
	 * Try not to use, most starship code should not rely on players.
	 **/
	val playerPilot: Player? get() = (controller as? ActivePlayerController)?.player

	/** Called on each server tick. */
	fun tick() {
		controller.tick()

		subsystems.forEach { it.tick() }
	}

	/** Called when a starship is removed. Any cleanup logic should be done here. */
	fun destroy() {
		IonWorld[world.minecraft].starships.remove(this)
		controller.destroy()
	}

	init {
		@Suppress("LeakingThis") // This is done right at the end of the class's initialization, it *should* be fine
		IonWorld[world.minecraft].starships.add(this)
	}

	// Created once
	val charIdentifier = randomString(5L)
	// used to identify the ship to auto turrets
	val identifier get() = getAutoTurretIdentifier()

	var isTeleporting: Boolean = false

	val initialBlockCount: Int = blocks.size
	var currentBlockCount: Int = initialBlockCount

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

	val weaponSets: HashMultimap<String, WeaponSubsystem> = HashMultimap.create()
	val weaponSetSelections: HashBiMap<UUID, String> = HashBiMap.create()

	val autoTurretTargets = mutableMapOf<String, AutoTurretTargeting.AutoTurretTarget<*>>()

	// Non-normalized vector containing the ships velocity
	// Used for target lead / speed estimations
	var velocity: Vector = Vector(0.0, 0.0, 0.0)

	val shieldEfficiency: Double
		get() = (shields.size.d().pow(0.9) / (initialBlockCount / 500.0).coerceAtLeast(1.0).pow(0.7))
			.coerceAtMost(1.0)

	val maxShields: Int = (0.00671215 * initialBlockCount.toDouble().pow(0.836512) - 0.188437).toInt()
		get() = if (initialBlockCount < 500) (1 - field + (1)) else field

	val thrusterMap = mutableMapOf<BlockFace, ThrustData>()

	var lastTick = System.nanoTime()

	/** Ignore weapon color, use rainbows for pride month **/
	var rainbowToggle = false

	var targetedPosition: Location? = null

	var forward: BlockFace = BlockFace.NORTH
	var isExploding = false

	var isInterdicting = false; private set
	abstract val interdictionRange: Int

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

	val min: Vec3i get() = hitbox.min
	val max: Vec3i get() = hitbox.max

	inline fun iterateBlocks(x: (Int, Int, Int) -> Unit) {
		for (key in blocks.iterator()) {
			x(blockKeyX(key), blockKeyY(key), blockKeyZ(key))
		}
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

	protected val passengers = HashSet<UUID>()
	val passengerIDs get() = passengers.toList()
	val onlinePassengers get() = passengers.mapNotNull(Bukkit::getPlayer)

	fun isPassenger(playerID: UUID): Boolean {
		return passengers.contains(playerID)
	}

	open fun addPassenger(playerID: UUID) {
		passengers.add(playerID)
	}

	open fun removePassenger(playerID: UUID) {
		passengers.remove(playerID)
	}

	open fun clearPassengers() {
		passengers.clear()
	}

	abstract fun moveAsync(movement: StarshipMovement): CompletableFuture<Boolean>

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

	var hullIntegrity = 1.0

	fun updateHullIntegrity() {
		currentBlockCount = blocks.count {
			getBlockTypeSafe(world, blockKeyX(it), blockKeyY(it), blockKeyZ(it))?.isAir != true
		}
		hullIntegrity = currentBlockCount.toDouble() / initialBlockCount.toDouble()
	}

	fun getEntryRange(planet: CachedPlanet): Int {
		return planet.atmosphereRadius + max(max.x - min.x, max.z - min.z) / 2 + 10
	}

	private fun getAutoTurretIdentifier(): String = when (controller) {
		is UnpilotedController -> "UnpilotedShip:$charIdentifier"

		is PlayerController -> (controller as PlayerController).player.name

		is AIController -> "${controller.getPilotName().plainText()}:$charIdentifier"

		is NoOpController -> "${getDisplayNamePlain()}:$charIdentifier"

		else -> throw NotImplementedError("${controller::class.java.simpleName} does not have an auto turret identifier!")
	}

	val damagers = mutableMapOf<Damager, ShipKillXP.ShipDamageData>()
	fun lastDamagedOrNull(): Long? = damagers.maxOfOrNull { it.value.lastDamaged }

	fun addToDamagers(damager: Damager, points: Int = 1) {
		val data = damagers.getOrPut(damager) { ShipKillXP.ShipDamageData() }
		data.points.getAndAdd(points)
		data.lastDamaged = System.currentTimeMillis()

		debug("$damager added to damagers")
		damager.debug("$damager added to $identifier's damagers")

		controller.onDamaged(damager)
	}

	/** Gets the minimessage display name of this starship */
	open fun getDisplayNameMiniMessage(): String = type.displayNameMiniMessage

	/** Gets the component display name of this starship */
	open fun getDisplayName(): Component = type.displayNameComponent

	/** Gets the plain text serialized version of this starship's display name */
	open fun getDisplayNamePlain(): String = type.displayName
}
