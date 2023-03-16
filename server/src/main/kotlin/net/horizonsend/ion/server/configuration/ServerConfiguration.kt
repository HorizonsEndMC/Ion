package net.horizonsend.ion.server.configuration

import kotlinx.serialization.Serializable
import net.horizonsend.ion.server.miscellaneous.WeightedRandomList
import net.minecraft.core.BlockPos
import net.minecraft.world.level.block.state.BlockState
import net.starlegacy.util.nms
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.World

@Serializable
data class ServerConfiguration(
	val serverName: String? = null,
	val particleColourChoosingMoneyRequirement: Double? = 5.0,
	val beacons: List<HyperspaceBeacon> = listOf(),
	val spaceGenConfig: Map<String, AsteroidConfig> = mapOf(
		"space2" to AsteroidConfig(
			0.25,
			14.0,
			4,
			arrayListOf(
				AsteroidConfig.Palette(
					1,
					listOf(AsteroidConfig.Palette.PaletteEntry(Material.STONE.createBlockData().getAsString(false), 1)),
					setOf(AsteroidConfig.Ore(Material.IRON_ORE.createBlockData().getAsString(true), 3, 3), AsteroidConfig.Ore(Material.LAPIS_ORE.createBlockData().getAsString(true), 2, 3))
				)
			),
			0.25,
			arrayListOf(AsteroidConfig.AsteroidFeature("Example", 1.0, 100.0, 10.0, Pos("ExampleWorld", 420, 100, 69000))),
			arrayListOf(
				AsteroidConfig.WreckClass(
					"biiiihg",
					wrecks = listOf(
						AsteroidConfig.WreckClass.Wreck(
							"megayacht",
							1,
							mapOf("ITS_A_TRAP" to 1)
						)
					),
					1
				)
			),
			1.0
		)
	)
) {
	/**
	 * @param baseAsteroidDensity: Roughly a base level of the number of asteroids per chunk
	 * @param maxAsteroidSize: Maximum Size for an Asteroid
	 * @param maxAsteroidOctaves: Maximum number of octaves for noise generation
	 * @param blockPalettes: list of Palettes use for the asteroid materials
	 * @param oreRatio: Number of attempts to place an ore blob per chunk
	 * @param features List of AsteroidFeature
	 * @see Palette
	 */
	@Serializable
	data class AsteroidConfig(
		val baseAsteroidDensity: Double = 0.25,
		val maxAsteroidSize: Double = 14.0,
		val maxAsteroidOctaves: Int = 4,
		val blockPalettes: ArrayList<Palette> = arrayListOf(Palette(1, listOf(Palette.PaletteEntry(Material.STONE.createBlockData().getAsString(false), 1)), setOf(Ore(Material.IRON_ORE.createBlockData().getAsString(true), 3, 3), Ore(Material.LAPIS_ORE.createBlockData().getAsString(true), 2, 3)))),
		val oreRatio: Double = 0.25,
		val features: List<AsteroidFeature> = listOf(AsteroidFeature("Example", 1.0, 100.0, 10.0, Pos("ExampleWorld", 420, 100, 69000))),
		val wreckClasses: ArrayList<WreckClass>,
		val wreckMultiplier: Double = 0.01
	) {
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
		 * @param maxBlobSize: Size of the ore blob (Official Mojang term)
		 * @param rolls: Number of rolls for this Palette
		 *
		 * Each Palette is a set of materials, and their weights that might make up an asteroid. Asteroids may pick from a list of Palettes.
		 */
		@Serializable
		data class Ore(
			val material: String, // Serialized BlockData
			val maxBlobSize: Int,
			val rolls: Int
		) {
			@kotlinx.serialization.Transient
			val blockData = Bukkit.createBlockData(this.material)

			@kotlinx.serialization.Transient
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
			 * The Wreck data class contains its schematic, weight and additional information
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
				@kotlinx.serialization.Transient
				val encounterWeightedRandomList = WeightedRandomList<String>().apply {
					this.addMany(this@Wreck.encounters)
				}
			}

			@kotlinx.serialization.Transient
			val weightedWrecks = WeightedRandomList<Wreck>().apply {
				this.addMany(
					wrecks.associateWith { it.weight }
				)
			}
		}

		@kotlinx.serialization.Transient
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

	@Serializable
	data class HyperspaceBeacon(
		val name: String,
		val radius: Double,
		val spaceLocation: Pos,
		val destination: Pos,
		val destinationName: String? = null,
		val prompt: String? = null
	)

	@Serializable
	data class Pos(
		val world: String,
		val x: Int,
		val y: Int,
		val z: Int
	) {
		fun bukkitWorld(): World = Bukkit.getWorld(world) ?: throw
		java.lang.NullPointerException("Could not find world $world")

		fun toBlockPos(): BlockPos = BlockPos(x, y, z)

		fun toLocation(): Location = Location(bukkitWorld(), x.toDouble(), y.toDouble(), z.toDouble())
	}
}
