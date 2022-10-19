package net.starlegacy.feature.multiblock.starshipweapon.turret

import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap
import it.unimi.dsi.fastutil.longs.LongOpenHashSet
import kotlin.collections.component1
import kotlin.collections.component2
import kotlin.collections.set
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin
import net.minecraft.world.level.block.Rotation
import net.starlegacy.cache.nations.NationCache
import net.starlegacy.cache.nations.PlayerCache
import net.starlegacy.database.Oid
import net.starlegacy.database.schema.nations.Nation
import net.starlegacy.feature.multiblock.Multiblocks
import net.starlegacy.feature.multiblock.starshipweapon.StarshipWeaponMultiblock
import net.starlegacy.feature.starship.active.ActiveStarship
import net.starlegacy.feature.starship.active.ActiveStarships
import net.starlegacy.feature.starship.subsystem.weapon.TurretWeaponSubsystem
import net.starlegacy.feature.starship.subsystem.weapon.projectile.TurretLaserProjectile
import net.starlegacy.feature.starship.subsystem.weapon.projectile.flagcolors
import net.starlegacy.util.CARDINAL_BLOCK_FACES
import net.starlegacy.util.Vec3i
import net.starlegacy.util.blockKey
import net.starlegacy.util.leftFace
import net.starlegacy.util.nms
import net.starlegacy.util.rightFace
import org.bukkit.Color
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.World
import org.bukkit.block.BlockFace
import org.bukkit.block.Sign
import org.bukkit.block.data.BlockData
import org.bukkit.entity.Player
import org.bukkit.util.Vector

abstract class TurretMultiblock : StarshipWeaponMultiblock<TurretWeaponSubsystem>() {
	init {
		shape.signCentered()
		shape.ignoreDirection()
	}

	override val name: String = "turret"
	override val signText = createSignText("&8Turret", null, null, null)

	abstract val cooldownNanos: Long
	abstract val range: Double
	abstract val sound: String

	abstract val projectileSpeed: Int
	abstract val projectileParticleThickness: Double
	abstract val projectileExplosionPower: Float
	abstract val projectileShieldDamageMultiplier: Int

	protected abstract fun buildFirePointOffsets(): List<Vec3i>
	protected abstract fun getPilotOffset(): Vec3i

	private val firePointOffsets: Map<BlockFace, List<Vec3i>> = CARDINAL_BLOCK_FACES.associate { inward ->
		val right = inward.rightFace

		val newOffsets: List<Vec3i> = buildFirePointOffsets().map { (x, y, z) ->
			Vec3i(x = right.modX * x + inward.modX * z, y = y, z = right.modZ * x + inward.modZ * z)
		}

		return@associate inward to newOffsets
	}

	fun getFirePoints(face: BlockFace): List<Vec3i> {
		return firePointOffsets.getValue(face)
	}

	// the mean coordinates of the fire points
	private val meanFirePoints: Map<BlockFace, Vec3i> = CARDINAL_BLOCK_FACES.associate { inward ->
		var x = 0
		var y = 0
		var z = 0
		val firePoints = getFirePoints(inward)

		for (point in firePoints) {
			x += point.x
			y += point.y
			z += point.z
		}

		x /= firePoints.size
		y /= firePoints.size
		z /= firePoints.size

		return@associate inward to Vec3i(x, y, z)
	}

	fun getMeanFirePoint(face: BlockFace): Vec3i {
		return meanFirePoints.getValue(face)
	}

	private val pilotOffsets: Map<BlockFace, Vec3i> = CARDINAL_BLOCK_FACES.associate { inward ->
		val right = inward.rightFace
		val (x, y, z) = getPilotOffset()
		val vec = Vec3i(x = right.modX * x + inward.modX * z, y = y, z = right.modZ * x + inward.modZ * z)
		return@associate inward to vec
	}

	fun getPilotLoc(sign: Sign, face: BlockFace): Location {
		return getPilotLoc(sign.world, sign.x, sign.y, sign.z, face)
	}

	fun getPilotLoc(world: World, x: Int, y: Int, z: Int, face: BlockFace): Location {
		return pilotOffsets.getValue(face).toLocation(world).add(x + 0.5, y + 0.0, z + 0.5)
	}

	fun getSignFromPilot(player: Player): Sign? {
		for (face in CARDINAL_BLOCK_FACES) {
			val (x, y, z) = pilotOffsets.getValue(face)
			val loc = player.location.subtract(x.toDouble(), y.toDouble(), z.toDouble())
			val sign = loc.block.getState(false) as? Sign
				?: continue

			if (Multiblocks[sign] === this) {
				return sign
			}
		}

		return null
	}

	fun getFacing(sign: Sign): BlockFace {
		val block = sign.block

		for (face in CARDINAL_BLOCK_FACES) {
			if (!shape.checkRequirementsSpecific(block, face, loadChunks = true, particles = false)) {
				continue
			}

			return face
		}

		error("Failed to find a face for sign at ${sign.location}")
	}

	fun getFacing(signPos: Vec3i, starship: ActiveStarship): BlockFace {
		val block = signPos.toLocation(starship.world).block
		val sign = block.state as Sign
		return getFacing(sign)
	}

	fun rotate(sign: Sign, oldFace: BlockFace, newFace: BlockFace): BlockFace {
		val i = when (newFace) {
			oldFace -> return oldFace
			oldFace.rightFace -> 1
			oldFace.oppositeFace -> 2
			oldFace.leftFace -> 3
			else -> error("Failed to calculate rotation iteration count from $oldFace to $newFace")
		}

		val nmsRotation: Rotation = when (i) {
			1 -> Rotation.CLOCKWISE_90
			2 -> Rotation.CLOCKWISE_180
			3 -> Rotation.COUNTERCLOCKWISE_90
			else -> return oldFace // can only be 0
		}

		val theta: Double = 90.0 * i
		val radians: Double = Math.toRadians(theta)
		val cosFactor: Double = cos(radians)
		val sinFactor: Double = sin(radians)
		val locations = shape.getLocations(oldFace)
		val oldKeys = LongOpenHashSet(locations.size)
		val newKeys = LongOpenHashSet(locations.size)
		val placements = Long2ObjectOpenHashMap<BlockData>()

		val world = sign.world

		val air = Material.AIR.createBlockData()

		for ((x0, y0, z0) in locations) {
			val x = x0 + sign.x
			val y = y0 + sign.y
			val z = z0 + sign.z
			val block = world.getBlockAt(x, y, z)
			val data = block.blockData
			val newData = data.nms.rotate(nmsRotation).createCraftBlockData()
			val nx0 = (x0.toDouble() * cosFactor - z0.toDouble() * sinFactor).roundToInt()
			val nz0 = (x0.toDouble() * sinFactor + z0.toDouble() * cosFactor).roundToInt()
			val nx = nx0 + sign.x
			val nz = nz0 + sign.z

			if (!locations.contains(Vec3i(nx0, y0, nz0)) && !world.getBlockAt(nx, y, nz).type.isAir) {
				return oldFace
			}

			val oldKey = blockKey(x, y, z)
			oldKeys.add(oldKey)
			placements.putIfAbsent(oldKey, air) // old block, may have been removed

			val newKey = blockKey(nx, y, nz)
			newKeys.add(newKey)
			placements[newKey] = newData
		}

		placeBlocks(placements, world)

		moveEntitiesInWindow(sign, oldFace, newFace)

		updateSubsystem(sign, oldKeys, newKeys, newFace)

		return newFace
	}

	private fun placeBlocks(placements: Long2ObjectOpenHashMap<BlockData>, world: World) {
		for ((key, data) in placements) {
			world.getBlockAtKey(key).setBlockData(data, false)
		}
	}

	private fun moveEntitiesInWindow(sign: Sign, oldFace: BlockFace, newFace: BlockFace) {
		val oldPilotBlock = getPilotLoc(sign, oldFace).block
		val newPilotLoc = getPilotLoc(sign, newFace)
		for (entity in oldPilotBlock.chunk.entities) {
			val entityLoc = entity.location

			if (entityLoc.block != oldPilotBlock) {
				continue
			}

			val loc = newPilotLoc.clone()
			loc.direction = entityLoc.direction
			entity.teleport(newPilotLoc)
		}
	}

	private fun updateSubsystem(sign: Sign, oldKeys: LongOpenHashSet, newKeys: LongOpenHashSet, newFace: BlockFace) {
		val starship = ActiveStarships.findByBlock(sign.block) ?: return
		val signPos = Vec3i(sign.location)
		val subsystem = starship.turrets.firstOrNull { it.pos == signPos } ?: return
		starship.blocks.removeAll(oldKeys)
		starship.blocks.addAll(newKeys)
		starship.calculateHitbox()
		subsystem.face = newFace
	}

	/** gets the points relative to the sign pos */
	private fun getAdjustedFirePoints(pos: Vec3i, face: BlockFace) = getFirePoints(face)
		.map { Vec3i(it.x + pos.x, it.y + pos.y, it.z + pos.z) }

	fun shoot(world: World, pos: Vec3i, face: BlockFace, dir: Vector, starship: ActiveStarship?, shooter: Player?) {
		val color: Color = getColor(starship, shooter)
		val speed = projectileSpeed.toDouble()

		for (point: Vec3i in getAdjustedFirePoints(pos, face)) {
			if (starship?.isInternallyObstructed(point, dir) == true) {
				continue
			}

			val loc = point.toLocation(world).toCenterLocation()

			TurretLaserProjectile(
				starship,
				loc,
				dir,
				speed,
				color,
				range,
				projectileParticleThickness,
				projectileExplosionPower,
				projectileShieldDamageMultiplier,
				sound,
				shooter
			).fire()
		}
	}

	private fun getColor(starship: ActiveStarship?, shooter: Player?): Color {
		var counter = 0
		if (starship != null) {
			if (starship.rainbowtoggle) flagcolors.random()
			else return starship.weaponColor
		}

		if (shooter != null) {
			val nation: Oid<Nation>? = PlayerCache[shooter].nation

			if (nation != null) {
				return Color.fromRGB(NationCache[nation].color)
			}
		}

		return Color.FUCHSIA
	}
}