package net.horizonsend.ion.server.features.world.generation.generators.configuration

import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import net.horizonsend.ion.server.configuration.util.Pos
import net.horizonsend.ion.server.features.world.IonWorld
import net.horizonsend.ion.server.features.world.generation.generators.IonWorldGenerator
import net.horizonsend.ion.server.features.world.generation.generators.configuration.SpaceGenerationConfiguration.Palette
import net.horizonsend.ion.server.features.world.generation.generators.space.SpaceGenerator
import net.horizonsend.ion.server.miscellaneous.utils.WeightedRandomList
import net.horizonsend.ion.server.miscellaneous.utils.nms
import net.minecraft.world.level.block.state.BlockState
import org.bukkit.Bukkit

/**
 * @param baseAsteroidDensity: Roughly a base level of the number of asteroids per chunk
 * @param maxAsteroidSize: Maximum Size for an Asteroid
 * @param blockPalettes: list of Palettes use for the asteroid materials
 * @param features List of StandardAsteroidFeature
 * @see Palette
 */
@Serializable
data class SpaceGenerationConfiguration(
	val baseAsteroidDensity: Double = 0.25,
	val maxAsteroidSize: Double = 14.0,
	val blockPalettes: ArrayList<Palette>,
	val oreRatio: Double,
	val features: List<AsteroidFeature>,
	val wreckClasses: ArrayList<WreckClass>,
	val wreckMultiplier: Double = 0.01
) : GenerationConfiguration {
	override fun buildGenerator(world: IonWorld): IonWorldGenerator<*> {
		return SpaceGenerator(world, this)
	}

	/**
	 * @param weight: Number of rolls for this Palette
	 * @param materials: Map of Materials to their Weight
	 * @param ores:  list of Palettes used for ore placement
	 *
	 * Each Palette is a set of materials, and their weights that might make up an asteroid. Asteroids may pick from a list of Palettes.
	 */
	@Serializable
	data class Palette(
		val weight: Int,
		val materials: List<PaletteEntry>,
		val ores: Set<Ore>
	) {
		@Serializable
		data class PaletteEntry(
			val material: String,
			val weight: Int
		)

		fun getMaterial(material: String) = Bukkit.createBlockData(material)

		fun blockStateWeightedList(): WeightedRandomList<BlockState> {
			val list = WeightedRandomList<BlockState>()
			list.addMany(materials.map { getMaterial(it.material).nms to it.weight })

			return list
		}
	}

	/**
	 * @param material: String representation of the blockstate
	 * @param rolls: Number of rolls for this Palette
	 *
	 * Each Palette is a set of materials, and their weights that might make up an asteroid. Asteroids may pick from a list of Palettes.
	 */
	@Serializable
	data class Ore(
		val material: String, // Serialized BlockData
		val rolls: Int
	) {
		@Transient
		val blockData = Bukkit.createBlockData(this.material)

		@Transient
		val blockState = blockData.nms
	}

	/**
	 * Asteroid Feature
	 *
	 * All asteroid features are stored as tauruses, but the values can be manipulated to make spheres or other shapes.
	 * @param name Unused, for readability.
	 * @param baseDensity Asteroid density inside the tube
	 * @param tubeSize Distance from the center of the tube to the center of the taurus
	 * @param tubeRadius Radius of the tube
	 * @param origin Origin position
	 * @see Pos
	 * @see baseAsteroidDensity
	 */
	@Serializable
	data class AsteroidFeature(
		val name: String = "",
		val baseDensity: Double = 1.0,
		val tubeSize: Double = 0.0,
		val tubeRadius: Double = 0.0,
		val origin: Pos
	)

	/**
	 * @param wrecks: List of Wrecks
	 * @param weight: Weight of the wreck class
	 **/
	@Serializable
	data class WreckClass(
		val className: String,
		val wrecks: List<Wreck>,
		val weight: Int
	) {
		/**
		 * The Wreck data class contains its schematic, and weight
		 * @param wreckSchematicName: Name of the wreck schematic
		 * @param weight: Number of rolls for this wreck
		 * @param encounters: Map of possible scenarios to information about them
		 **/
		@Serializable
		data class Wreck(
			val wreckSchematicName: String,
			val weight: Int,
			val encounters: Map<String, Int>
		) {
			@Transient
			val encounterWeightedRandomList = WeightedRandomList<String>().apply {
				this.addMany(this@Wreck.encounters)
			}
		}

		@Transient
		val weightedWrecks = WeightedRandomList<Wreck>().apply {
			this.addMany(
				wrecks.associateWith { it.weight }
			)
		}
	}

	@Transient
	val weightedWreckList = WeightedRandomList<WeightedRandomList<WreckClass.Wreck>>().apply {
		this.addMany(
			wreckClasses.associate { wreckClass -> wreckClass.weightedWrecks to wreckClass.weight }
		)
	}

	fun paletteWeightedList(): WeightedRandomList<Pair<Int, WeightedRandomList<BlockState>>> {
		val list = WeightedRandomList<Pair<Int, WeightedRandomList<BlockState>>>()
		val transformed = blockPalettes.associate {
			(blockPalettes.indexOf(it) to it.blockStateWeightedList()) to it.weight
		}.toMap()

		list.addMany(transformed)

		return list
	}
}
