package net.horizonsend.ion.server

import net.minecraft.core.BlockPos
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.World
import org.bukkit.Material
import org.spongepowered.configurate.objectmapping.ConfigSerializable
import java.io.Serializable

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
data class ServerConfiguration(
	val serverName: String? = null,
	val ParticleColourChoosingMoneyRequirement: Double? = 5.0,
	val beacons: List<HyperspaceBeacon> = listOf(
		HyperspaceBeacon(
			"test", 100.0, Pos("space2", 100000, 128, 100000),
			Pos("space2", 0, 128, 0), "zero zero"
		)
	),
	// Asteroid gen start
	val baseAsteroidDensity: Double = 0.25,
	val maxAsteroidSize: Double = 14.0,
	val maxAsteroidOctaves: Int = 4,
	val blockPalettes: ArrayList<Palette> = arrayListOf(Palette(1, mapOf(Material.STONE to 1, Material.ANDESITE to 1))),
	val ores: Set<Ore> = setOf(Ore(Material.IRON_ORE.createBlockData().getAsString(true), 3, 3), Ore(Material.LAPIS_ORE.createBlockData().getAsString(true), 2, 3)),
	val oreRatio: Double = 0.25,
	val features: List<AsteroidFeature> = listOf(AsteroidFeature("Example", "ExampleWorld", 1.0, 100.0, 10.0, 420, 100, 69000))
) {
	/**
	 * @param weight: Number of rolls for this Palette
	 * @param materials: Map of Materials to their Weight
	 *
	 * Each Palette is a set of materials, and their weights that might make up an asteroid. Asteroids may pick from a list of Palettes.
	 */
	@ConfigSerializable
	data class Palette(
		val weight: Int,
		val materials: Map<Material, Int>
	) : Serializable

	/**
	 * @param material: Map of Materials to their Weight
	 * @param maxBlobSize: Size of the ore blob (Official Mojang term)
	 * @param rolls: Number of rolls for this Palette
	 *
	 * Each Palette is a set of materials, and their weights that might make up an asteroid. Asteroids may pick from a list of Palettes.
	 */
	@ConfigSerializable
	data class Ore(
		val material: String, // Serialized BlockData
		val maxBlobSize: Int,
		val rolls: Int
	)

	@ConfigSerializable
	data class HyperspaceBeacon(
		val name: String,
		val radius: Double,
		val spaceLocation: Pos,
		val destination: Pos,
		val destinationName: String? = null
	)

	@ConfigSerializable
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
	@ConfigSerializable
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
