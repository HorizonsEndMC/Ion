package net.horizonsend.ion.server.features.space.body.planet

import net.horizonsend.ion.common.database.Oid
import net.horizonsend.ion.common.database.schema.space.ParentPlanet
import net.horizonsend.ion.common.utils.miscellaneous.d
import net.horizonsend.ion.server.features.space.body.CelestialBody
import net.horizonsend.ion.server.features.space.body.EnterableCelestialBody
import net.horizonsend.ion.server.features.space.body.NamedCelestialBody
import net.horizonsend.ion.server.miscellaneous.registrations.legacy.CustomItem
import net.horizonsend.ion.server.miscellaneous.registrations.legacy.CustomItems
import net.horizonsend.ion.server.miscellaneous.utils.Vec3i
import net.horizonsend.ion.server.miscellaneous.utils.getSphereBlocks
import net.horizonsend.ion.server.miscellaneous.utils.nms
import net.kyori.adventure.text.Component
import net.minecraft.world.level.block.Blocks
import net.minecraft.world.level.block.state.BlockState
import org.bukkit.block.data.BlockData
import org.bukkit.util.noise.SimplexNoiseGenerator
import java.util.Locale

abstract class CachedPlanet(
	open val databaseId: Oid<out ParentPlanet>,
	final override val name: String,

	override val enteredWorldName: String,
	location: Vec3i,
	spaceWorldName: String,
	val size: Double,
	val seed: Long,
	val crustMaterials: List<BlockData>,
	val crustNoise: Double,
	val cloudDensity: Double,
	val cloudMaterials: List<BlockData>,
	val cloudDensityNoise: Double,
	val cloudThreshold: Double,
	val cloudNoise: Double,
	var description: String
) : CelestialBody(spaceWorldName, location), NamedCelestialBody, EnterableCelestialBody {
	companion object {
		const val CRUST_RADIUS_MAX = 180
	}

	val planetIcon: CustomItem = CustomItems["planet_icon_${name.lowercase(Locale.getDefault()).replace(" ", "")}"]
		?: CustomItems.BATTERY_LARGE // TODO: When porting over planet icons, change the legacy uranium icon too

	init {
		require(size > 0 && size <= 1)
		require(cloudDensity in 0.0..1.0)
	}

	abstract fun changeDescription(newDescription: String)

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

	abstract fun setSeed(newSeed: Long)
	abstract fun setCloudMaterials(newMaterials: List<String>)
	abstract fun setCloudDensity(newDensity: Double)
	abstract fun setAtmosphereNoise(newNoise: Double)
	abstract fun setCloudThreshold(newThreshold: Double)
	abstract fun setCloudNoise(newNoise: Double)
	abstract fun setCrustNoise(newNoise: Double)
	abstract fun setCrustMaterials(newMaterials: List<String>)
	abstract fun delete()

	abstract fun formatInformation(): Component
}
