package net.horizonsend.ion.server.configuration

import kotlinx.serialization.Serializable
import net.minecraft.core.BlockPos
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
	val asteroidConfig: AsteroidConfig
) {
	/**
	 * @param baseAsteroidDensity: Roughly a base level of the number of asteroids per chunk
	 * @param maxAsteroidSize: Maximum Size for an Asteroid
	 * @param maxAsteroidOctaves: Maximum number of octaves for noise generation
	 * @param blockPalettes: list of Palettes use for the asteroid materials
	 * @param ores:  list of Palettes used for ore placement
	 * @param oreRatio: Number of attempts to place an ore blob per chunk
	 * @param features List of AsteroidFeature
	 * @see Palette
	 */
	@ConfigSerializable
	data class AsteroidConfig(
		val baseAsteroidDensity: Double = 0.25,
		val maxAsteroidSize: Double = 14.0,
		val maxAsteroidOctaves: Int = 4,
		val blockPalettes: ArrayList<Palette> = arrayListOf(Palette(1, mapOf(Material.STONE.createBlockData().getAsString(false) to 1))),
		val ores: Set<Ore> = setOf(Ore(Material.IRON_ORE.createBlockData().getAsString(true), 3, 3), Ore(Material.LAPIS_ORE.createBlockData().getAsString(true), 2, 3)),
		val oreRatio: Double = 0.25,
		val features: List<AsteroidFeature> = listOf(AsteroidFeature("Example", 1.0, 100.0, 10.0, Pos("ExampleWorld", 420, 100, 69000))),
		val wrecks: Map<String, Int>
	) {
		val mappedWrecks = wrecks.mapKeys { (name, _) ->
			name
		}

		/**
		 * @param weight: Number of rolls for this Palette
		 * @param materials: Map of Materials to their Weight
		 *
		 * Each Palette is a set of materials, and their weights that might make up an asteroid. Asteroids may pick from a list of Palettes.
		 */
		@Serializable
		data class Palette(
			val weight: Int,
			val materials: Map<String, Int>
		) {
			fun getMaterial(material: String) = Bukkit.createBlockData(material)
		}

		/**
		 * @param material: Map of Materials to their Weight
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
			val blockData = Bukkit.createBlockData(this.material)
			val blockState = blockData.nms
		}

		/**Asteroid Feature
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
		java.lang.NullPointerException("Hyperspace Beacons | Could not find world $world")

		fun toBlockPos(): BlockPos = BlockPos(x, y, z)

		fun toLocation(): Location = Location(bukkitWorld(), x.toDouble(), y.toDouble(), z.toDouble())
	}

	/**Asteroid Feature
	 * All asteroid features are stored as tauruses, but the values can be manipulated to make spheres or other shapes.
	 * @param name Unused, for readability.
	 * @param worldName World to place the feature
	 * @param baseDensity Asteroid density inside the tube
	 * @param tubeSize Distance from the center of the tube to the center of the taurus
	 * @param tubeRadius Radius of the tube
	 * @param x X Asis Location
	 * @param y Y Asis Location
	 * @param z Z Asis Location
	 * @see baseAsteroidDensity
	 */
	@Serializable
	data class AsteroidFeature(
		val name: String = "",
		val worldName: String = "",
		val baseDensity: Double = 1.0,
		val tubeSize: Double = 0.0,
		val tubeRadius: Double = 0.0,
		val x: Int,
		val y: Int,
		val z: Int
	)
}
