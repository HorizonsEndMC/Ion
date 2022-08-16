package net.starlegacy.feature.space

import com.mongodb.client.result.UpdateResult
import java.util.Locale
import net.minecraft.world.level.block.Blocks
import net.minecraft.world.level.block.state.BlockState
import net.starlegacy.database.Oid
import net.starlegacy.database.schema.space.Planet
import net.starlegacy.feature.misc.CustomItem
import net.starlegacy.feature.misc.CustomItems
import net.starlegacy.util.Vec3i
import net.starlegacy.util.d
import net.starlegacy.util.getSphereBlocks
import net.starlegacy.util.i
import net.starlegacy.util.nms
import org.apache.logging.log4j.core.pattern.AbstractStyleNameConverter.Yellow
import org.bukkit.Bukkit
import org.bukkit.World
import org.bukkit.block.data.BlockData
import org.bukkit.util.noise.SimplexNoiseGenerator

class CachedPlanet(
	val databaseId: Oid<Planet>,
	override val name: String,
	sun: CachedStar,
	val planetWorldName: String,
	val rogue: Boolean,
	val x: Int,
	val z: Int,
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
		private const val CRUST_RADIUS_MAX = 115

		fun calculateOrbitLocation(sun: CachedStar, orbitDistance: Int, orbitProgress: Double): Vec3i {
			val (x, y, z) = sun.location

			val radians = Math.toRadians(orbitProgress)

			return Vec3i(
				x = x + (Math.cos(radians) * orbitDistance.d()).i(),
				y = y,
				z = z + (Math.sin(radians) * orbitDistance.d()).i()
			)
		}

		fun calculateLocation(sun: CachedStar, x: Int, z: Int): Vec3i {
			val (y) = sun.location

			return Vec3i(
				x = x,
				y = y,
				z = z
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

	fun toggleRogue(rogue: Boolean): UpdateResult = Planet.setRogue(databaseId, rogue)

	fun changeX(x: Int): UpdateResult = Planet.setX(databaseId, x)


	fun changeZ(z: Int): UpdateResult = Planet.setX(databaseId, z)


	fun changeOrbitDistance(newDistance: Int) {
		val newLocation = calculateOrbitLocation(sun, newDistance, orbitProgress)
		move(newLocation)

		orbitDistance = newDistance

		Planet.setOrbitDistance(databaseId, newDistance)
	}

	fun orbit(urgent: Boolean = false): Unit = orbit(urgent, updateDb = true)

	fun orbit(urgent: Boolean = false, updateDb: Boolean = true) {
		val newProgress = (orbitProgress + orbitSpeed) % 360
		val newLocation = calculateOrbitLocation(sun, orbitDistance, newProgress)
		move(newLocation, urgent = urgent)

		orbitProgress = newProgress

		if (updateDb) {
			Planet.setOrbitProgress(databaseId, newProgress)
		}
	}
	fun setLocation(urgent: Boolean = false) {
		val newLocation = calculateLocation(sun, x, z)

		move(newLocation, urgent = urgent)

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
