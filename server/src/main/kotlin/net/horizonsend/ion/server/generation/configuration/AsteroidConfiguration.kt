package net.horizonsend.ion.server.generation.configuration

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
 * @param wreckRatio: Proportion of wrecks to asteroids
 * @see Palette
 */
@ConfigSerializable
data class AsteroidConfiguration(
	val baseAsteroidDensity: Double = 0.25,
	val maxAsteroidSize: Double = 14.0,
	val maxAsteroidOctaves: Int = 4,
	val blockPalettes: ArrayList<Palette> = arrayListOf(Palette(1, mapOf(Material.STONE to 1, Material.ANDESITE to 1))),
	val ores: Set<Ore> = setOf(Ore(Material.IRON_ORE.createBlockData().getAsString(true), 3, 3), Ore(Material.LAPIS_ORE.createBlockData().getAsString(true), 2, 3)),
	val oreRatio: Double = 0.25,
	val wreckRatio: Double = 0.2
)

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
