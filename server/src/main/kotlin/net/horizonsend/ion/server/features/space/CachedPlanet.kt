package net.horizonsend.ion.server.features.space

import net.horizonsend.ion.common.database.Oid
import net.horizonsend.ion.common.database.schema.space.Planet
import net.horizonsend.ion.common.database.schema.space.Planet.Companion.setX
import net.horizonsend.ion.common.database.schema.space.Planet.Companion.setZ
import net.horizonsend.ion.common.utils.miscellaneous.d
import net.horizonsend.ion.common.utils.miscellaneous.i
import net.horizonsend.ion.server.miscellaneous.registrations.legacy.CustomItem
import net.horizonsend.ion.server.miscellaneous.registrations.legacy.CustomItems
import net.horizonsend.ion.server.miscellaneous.utils.Vec3i
import net.horizonsend.ion.server.miscellaneous.utils.getSphereBlocks
import net.horizonsend.ion.server.miscellaneous.utils.nms
import net.minecraft.world.level.block.Blocks
import net.minecraft.world.level.block.state.BlockState
import org.bukkit.Bukkit
import org.bukkit.World
import org.bukkit.block.data.BlockData
import org.bukkit.util.noise.SimplexNoiseGenerator
import java.util.Locale
import kotlin.math.cos
import kotlin.math.sin

class CachedPlanet(
    val databaseId: Oid<Planet>,
    override val name: String,
    sun: CachedStar,
    val planetWorldName: String,
    var rogue: Boolean,
    var x: Int,
    var z: Int,
    val size: Double,
    orbitDistance: Int,
    private val orbitSpeed: Double,
    orbitProgress: Double,
    val seed: Long,
    val crustMaterials: List<BlockData>,
    val crustNoise: Double,
    val cloudDensity: Double,
    val cloudMaterials: List<BlockData>,
    val cloudDensityNoise: Double,
    val cloudThreshold: Double,
    val cloudNoise: Double
) : CelestialBody(sun.spaceWorldName, calculateOrbitLocation(sun, orbitDistance, orbitProgress)), NamedCelestialBody {
	companion object {
		private const val CRUST_RADIUS_MAX = 180

		fun calculateOrbitLocation(sun: CachedStar, orbitDistance: Int, orbitProgress: Double): Vec3i {
			val (x, y, z) = sun.location

			val radians = Math.toRadians(orbitProgress)

			return Vec3i(
				x = x + (Math.cos(radians) * orbitDistance.d()).i(),
				y = y,
				z = z + (Math.sin(radians) * orbitDistance.d()).i()
			)
		}

		fun calculateLocation(sun: CachedStar, orbitDistance: Int, orbitProgress: Double): Vec3i {
			val (x, y, z) = sun.location

			val radians = Math.toRadians(orbitProgress)

			return Vec3i(
				x = x + (cos(radians) * orbitDistance.d()).i(),
				y = y,
				z = z + (sin(radians) * orbitDistance.d()).i()
			)
		}
	}

	var sun = sun; private set
	val planetWorld: World? get() = Bukkit.getWorld(planetWorldName)
	var orbitDistance: Int = orbitDistance; private set
	var orbitProgress: Double = orbitProgress; private set

	val planetIcon: CustomItem = CustomItems["planet_icon_${name.lowercase(Locale.getDefault()).replace(" ", "")}"]
		?: CustomItems.DETONATOR

	init {
		require(size > 0 && size <= 1)
		require(cloudDensity in 0.0..1.0)
	}

	fun changeSun(newSun: CachedStar) {
		val world = checkNotNull(newSun.spaceWorld)
		val newLocation = calculateOrbitLocation(newSun, orbitDistance, orbitProgress)
		move(newLocation, world)

		sun = newSun

		Planet.setSun(databaseId, newSun.databaseId)
	}

	fun toggleRogue(rogue: Boolean) {
		this.rogue = rogue

		Planet.setRogue(databaseId, rogue)
	}

	fun setOrbitProgress(progress: Double) {
		val newLocation = calculateOrbitLocation(sun, orbitDistance, progress)
		move(newLocation)

		orbitProgress = progress
		Planet.setOrbitProgress(databaseId, progress)
	}

	fun changeX(x: Int) {
		this.x = x
		setX(databaseId, x)
		setLocation(true)
	}

	fun changeZ(z: Int) {
		this.z = z
		setZ(databaseId, z)
		setLocation(true)
	}

	fun changeOrbitDistance(newDistance: Int) {
		val newLocation = calculateOrbitLocation(sun, newDistance, orbitProgress)
		move(newLocation)

		orbitDistance = newDistance

		Planet.setOrbitDistance(databaseId, newDistance)
	}


	fun orbit(): Unit = orbit(updateDb = true)

	fun orbit(updateDb: Boolean = true) {
		val newProgress = (orbitProgress + orbitSpeed) % 360
		val newLocation = calculateLocation(sun, orbitDistance, newProgress)
		move(newLocation)

		orbitProgress = newProgress

		if (updateDb) {
			Planet.setOrbitProgress(databaseId, newProgress)
		}
	}

	fun setLocation(updateDb: Boolean = true) {
		move(Vec3i(x, sun.location.y, z))

		if (updateDb) {
			setX(databaseId, x)
			setZ(databaseId, z)
		}
	}

	val crustRadius = (CRUST_RADIUS_MAX * size).toInt()
	val atmosphereRadius = crustRadius + 3

	override fun createStructure(): Map<Vec3i, BlockState> {
		val random = SimplexNoiseGenerator(seed)

		val crustPalette: List<BlockState> = crustMaterials.map(BlockData::nms)

		val crust: Map<Vec3i, BlockState> = getSphereBlocks(crustRadius).associateWith { (x, y, z) ->
			// number from -1 to 1
			val simplexNoise = random.noise(x.d() * crustNoise, y.d() * crustNoise, z.d() * crustNoise)

			val noise = (simplexNoise / 2.0 + 0.5)

			return@associateWith when {
				crustPalette.isEmpty() -> Blocks.DIRT.defaultBlockState()
				else -> crustPalette[(noise * crustPalette.size).toInt()]
			}
		}

		val atmospherePalette: List<BlockState> = cloudMaterials.map(BlockData::nms)

		val atmosphere: Map<Vec3i, BlockState> = getSphereBlocks(atmosphereRadius).associateWith { (x, y, z) ->
			if (atmospherePalette.isEmpty()) {
				return@associateWith Blocks.AIR.defaultBlockState()
			}

			val atmosphereSimplex = random.noise(
				x.d() * cloudDensityNoise,
				y.d() * cloudDensityNoise,
				z.d() * cloudDensityNoise
			)

			if ((atmosphereSimplex / 2.0 + 0.5) > cloudDensity) {
				return@associateWith Blocks.AIR.defaultBlockState()
			}

			val cloudSimplex = random.noise(
				-x.d() * cloudNoise,
				-y.d() * cloudNoise,
				-z.d() * cloudNoise
			)

			val noise = (cloudSimplex / 2.0) + 0.5

			if (noise > cloudThreshold) {
				return@associateWith Blocks.AIR.defaultBlockState()
			}

			return@associateWith atmospherePalette[(noise * atmospherePalette.size).toInt()]
		}

		return crust + atmosphere
	}
}
