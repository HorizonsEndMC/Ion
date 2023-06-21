package net.starlegacy.feature.space

import net.horizonsend.ion.server.database.Oid
import net.horizonsend.ion.server.database.schema.space.Moon
import net.minecraft.world.level.block.Blocks
import net.minecraft.world.level.block.state.BlockState
import net.starlegacy.feature.misc.CustomItem
import net.starlegacy.feature.misc.CustomItems
import net.starlegacy.util.Vec3i
import net.starlegacy.util.d
import net.starlegacy.util.getSphereBlocks
import net.starlegacy.util.i
import net.starlegacy.util.nms
import org.bukkit.block.data.BlockData
import org.bukkit.util.noise.SimplexNoiseGenerator
import java.util.Locale
import kotlin.math.cos
import kotlin.math.sin

class CachedMoon(
	val databaseId: Oid<Moon>,
	override val name: String,
	parent: CachedPlanet,
	override val worldName: String,
	val size: Double,
	orbitDistance: Int,
	private val orbitSpeed: Double,
	orbitProgress: Double,
	val seed: Long,
	val crustMaterials: List<BlockData>,
	val crustNoise: Double
): EnterableCelestialBody(worldName, parent.spaceWorldName, calculateOrbitLocation(parent, orbitDistance, orbitProgress)) {
	override val outerRadius: Int get() = crustRadius + 3

	companion object {
		private const val CRUST_RADIUS_MAX = 115

		fun calculateOrbitLocation(planet: CachedPlanet, orbitDistance: Int, orbitProgress: Double): Vec3i {
			val (x, y, z) = planet.location

			val radians = Math.toRadians(orbitProgress)

			return Vec3i(
				x = x + (cos(radians) * orbitDistance.d()).i(),
				y = y,
				z = z + (sin(radians) * orbitDistance.d()).i()
			)
		}
	}

	var parent = parent; private set
	var orbitDistance: Int = orbitDistance; private set
	var orbitProgress: Double = orbitProgress; private set

	val planetIcon: CustomItem = CustomItems["planet_icon_${name.lowercase(Locale.getDefault()).replace(" ", "")}"]
		?: CustomItems.DETONATOR

	init {
		require(size > 0 && size <= 1)
	}

	fun changeSun(newSun: CachedPlanet) {
		val world = checkNotNull(newSun.spaceWorld)
		val newLocation = CachedMoon.calculateOrbitLocation(newSun, orbitDistance, orbitProgress)
		move(newLocation, world)

		parent = newSun

		Moon.setParent(databaseId, newSun.databaseId)
	}

	fun setOrbitProgress(progress: Double) {
		val newLocation = CachedMoon.calculateOrbitLocation(parent, orbitDistance, progress)
		move(newLocation)

		orbitProgress = progress
		Moon.setOrbitProgress(databaseId, progress)
	}

	fun changeOrbitDistance(newDistance: Int) {
		val newLocation = CachedMoon.calculateOrbitLocation(parent, newDistance, orbitProgress)
		move(newLocation)

		orbitDistance = newDistance

		Moon.setOrbitDistance(databaseId, newDistance)
	}

	fun orbit(urgent: Boolean = false): Unit = orbit(urgent, updateDb = true)

	fun orbit(urgent: Boolean = false, updateDb: Boolean = true) {
		val newProgress = (orbitProgress + orbitSpeed) % 360
		val newLocation = CachedMoon.calculateOrbitLocation(parent, orbitDistance, newProgress)
		move(newLocation, urgent = urgent)

		orbitProgress = newProgress

		if (updateDb) {
			Moon.setOrbitProgress(databaseId, newProgress)
		}
	}

	val crustRadius = (CRUST_RADIUS_MAX * size).toInt()

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

		return crust
	}
}
