package net.starlegacy.feature.starship.active

import com.destroystokyo.paper.Title
import com.google.common.collect.HashBiMap
import com.google.common.collect.HashMultimap
import it.unimi.dsi.fastutil.longs.LongOpenHashSet
import net.horizonsend.ion.server.legacy.feedback.FeedbackType
import net.horizonsend.ion.server.legacy.feedback.sendFeedbackAction
import net.horizonsend.ion.server.legacy.feedback.sendFeedbackMessage
import net.horizonsend.ion.server.starships.Starship
import net.kyori.adventure.audience.Audience
import net.kyori.adventure.audience.ForwardingAudience
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.server.level.ServerLevel
import net.starlegacy.feature.multiblock.gravitywell.GravityWellMultiblock
import net.starlegacy.feature.progression.ShipKillXP
import net.starlegacy.feature.space.CachedPlanet
import net.starlegacy.feature.starship.StarshipType
import net.starlegacy.feature.starship.movement.StarshipMovement
import net.starlegacy.feature.starship.subsystem.GravityWellSubsystem
import net.starlegacy.feature.starship.subsystem.HyperdriveSubsystem
import net.starlegacy.feature.starship.subsystem.MagazineSubsystem
import net.starlegacy.feature.starship.subsystem.NavCompSubsystem
import net.starlegacy.feature.starship.subsystem.StarshipSubsystem
import net.starlegacy.feature.starship.subsystem.reactor.ReactorSubsystem
import net.starlegacy.feature.starship.subsystem.shield.ShieldSubsystem
import net.starlegacy.feature.starship.subsystem.thruster.ThrustData
import net.starlegacy.feature.starship.subsystem.thruster.ThrusterSubsystem
import net.starlegacy.feature.starship.subsystem.weapon.TurretWeaponSubsystem
import net.starlegacy.feature.starship.subsystem.weapon.WeaponSubsystem
import net.starlegacy.util.CARDINAL_BLOCK_FACES
import net.starlegacy.util.Tasks
import net.starlegacy.util.Vec3i
import net.starlegacy.util.blockKey
import net.starlegacy.util.blockKeyX
import net.starlegacy.util.blockKeyY
import net.starlegacy.util.blockKeyZ
import net.starlegacy.util.d
import net.starlegacy.util.getBlockTypeSafe
import net.starlegacy.util.msg
import net.starlegacy.util.nms
import net.starlegacy.util.squared
import net.starlegacy.util.title
import org.bukkit.Bukkit
import org.bukkit.Color
import org.bukkit.Location
import org.bukkit.World
import org.bukkit.block.BlockFace
import org.bukkit.block.Sign
import org.bukkit.craftbukkit.v1_19_R2.CraftWorld
import org.bukkit.craftbukkit.v1_19_R2.block.CraftBlock
import org.bukkit.entity.Entity
import org.bukkit.entity.Player
import org.bukkit.util.NumberConversions
import org.bukkit.util.Vector
import java.util.LinkedList
import java.util.UUID
import java.util.concurrent.CompletableFuture
import java.util.concurrent.atomic.AtomicInteger
import kotlin.collections.set
import kotlin.math.ln
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow
import kotlin.math.roundToInt
import kotlin.math.sqrt

abstract class ActiveStarship(
	serverLevel: ServerLevel,

	var blocks: LongOpenHashSet,
	val mass: Double,
	centerOfMass: BlockPos,
	private val hitbox: ActiveStarshipHitbox
) : Starship(serverLevel), ForwardingAudience {
	private var _world: World = serverLevel.world

	override var serverLevel: ServerLevel
		get() = super.serverLevel
		set(value) {
			ActiveStarships.updateWorld(this, value.world, value.world)
			super.serverLevel = value
			_world = value.world
		}

	@Deprecated("Prefer Minecraft - `net.minecraft.server.level.ServerLevel`")
	var world: World
		get() = _world
		set(value) {
			ActiveStarships.updateWorld(this, value, value)
			super.serverLevel = (value as CraftWorld).handle
			_world = value
		}

	private var _centerOfMassBlockPos: BlockPos = centerOfMass
	private var _centerOfMassVec3i: Vec3i = Vec3i(centerOfMass.x, centerOfMass.y, centerOfMass.z)

	var centerOfMassBlockPos: BlockPos
		get() = _centerOfMassBlockPos
		set(value) {
			_centerOfMassBlockPos = value
			_centerOfMassVec3i = Vec3i(value.x, value.y, value.z)
		}

	@Deprecated("Prefer Minecraft - `net.minecraft.core.BlockPos`")
	var centerOfMassVec3i: Vec3i
		get() = _centerOfMassVec3i
		set(value) {
			_centerOfMassBlockPos = BlockPos(value.x, value.y, value.z)
			_centerOfMassVec3i = value
		}

	private var _forwardBlockFace: BlockFace = BlockFace.NORTH

	override var facingDirection: Direction
		get() = super.facingDirection
		set(value) {
			_forwardBlockFace = CraftBlock.notchToBlockFace(value)
			super.facingDirection = value
		}

	@Deprecated("Prefer Minecraft - `net.minecraft.core.Direction`")
	var forwardBlockFace: BlockFace
		get() = _forwardBlockFace
		set(value) {
			_forwardBlockFace = value
			super.facingDirection = CraftBlock.blockFaceToNotch(value)
		}

	override fun audiences(): Iterable<Audience> = onlinePassengers
	abstract val type: StarshipType

	var isTeleporting: Boolean = false

	val initialBlockCount: Int = blocks.size

	val subsystems = LinkedList<StarshipSubsystem>()
	lateinit var reactor: ReactorSubsystem
	val shields = LinkedList<ShieldSubsystem>()
	val weapons = LinkedList<WeaponSubsystem>()
	val turrets = LinkedList<TurretWeaponSubsystem>()
	val hyperdrives = LinkedList<HyperdriveSubsystem>()
	val navComps = LinkedList<NavCompSubsystem>()
	val thrusters = LinkedList<ThrusterSubsystem>()
	val magazines = LinkedList<MagazineSubsystem>()
	val gravityWells = LinkedList<GravityWellSubsystem>()

	val weaponSets: HashMultimap<String, WeaponSubsystem> = HashMultimap.create()
	val weaponSetSelections: HashBiMap<UUID, String> = HashBiMap.create()
	val autoTurretTargets = mutableMapOf<String, UUID>()

	val shieldEfficiency: Double
		get() = (shields.size.d().pow(0.9) / (initialBlockCount / 500.0).coerceAtLeast(1.0).pow(0.7))
			.coerceAtMost(1.0)

	val maxShields: Int = (0.00671215 * initialBlockCount.toDouble().pow(0.836512) - 0.188437).toInt()
		get() = if (initialBlockCount < 500) (1 - field + (1)) else field

	val thrusterMap = mutableMapOf<BlockFace, ThrustData>()

	var lastTick = System.nanoTime()

	var isInterdicting = false
		private set

	var rainbowtoggle = false

	var randomTarget = false

	var randomTargetBlacklist: MutableSet<UUID> = mutableSetOf()

	fun setIsInterdicting(value: Boolean) {
		Tasks.checkMainThread()
		isInterdicting = value

		gravityWells
			.filter { it.isIntact() }
			.map { it.pos.toLocation(serverLevel.world).block.state }
			.filterIsInstance<Sign>()
			.forEach { GravityWellMultiblock.setEnabled(it, value) }

		if (!value) {
			onlinePassengers.forEach { player -> player.sendFeedbackMessage(FeedbackType.SUCCESS, "Gravity well disabled") }

			return
		}

		onlinePassengers.forEach { player -> player.sendFeedbackMessage(FeedbackType.SUCCESS, "Gravity well enabled") }
	}

	abstract val interdictionRange: Int

	abstract val weaponColor: Color

	var isExploding = false

	val damagers = mutableMapOf<ShipKillXP.Damager, AtomicInteger>()

	val min: Vec3i get() = hitbox.min
	val max: Vec3i get() = hitbox.max

	inline fun iterateBlocks(x: (Int, Int, Int) -> Unit) {
		for (key in blocks.iterator()) {
			x(blockKeyX(key), blockKeyY(key), blockKeyZ(key))
		}
	}

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
		return serverLevel == loc.world.nms && isWithinHitbox(loc.blockX, loc.blockY, loc.blockZ, tolerance)
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

	@Deprecated("Deprecated in favour of Adventure text components.")
	fun sendTitle(title: Title) {
		onlinePassengers.asSequence().forEach { it title title }
	}

	@Deprecated("Deprecated in favour of Adventure text components.")
	fun sendMessage(message: String) {
		onlinePassengers.asSequence().forEach { it msg message }
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

	fun updatePower(sender: Player, shield: Int, weapon: Int, thruster: Int) {
		reactor.powerDistributor.setDivision(shield / 100.0, weapon / 100.0, thruster / 100.0)
		val name = sender.name

		onlinePassengers.forEach { player ->
			player.sendFeedbackAction(
				FeedbackType.INFORMATION,
				"<green>$name</green> updated the power mode to <aqua>$shield% shield <red>$weapon% weapon <yellow>$thruster% thruster"
			)
		}
	}

	fun hullIntegrity(): Double {
		val nonAirBlocks = blocks.count {
			getBlockTypeSafe(serverLevel, blockKeyX(it), blockKeyY(it), blockKeyZ(it))?.isAir != true
		}
		return nonAirBlocks.toDouble() / initialBlockCount.toDouble()
	}

	fun getEntryRange(planet: CachedPlanet): Int {
		return planet.atmosphereRadius + max(max.x - min.x, max.z - min.z) / 2 + 10
	}
}
