package net.horizonsend.ion.server.generation.configuration

import org.spongepowered.configurate.objectmapping.ConfigSerializable

/** Asteroid Features
 * @param features List of AsteroidFeature
 * @see	AsteroidFeature
 */
@ConfigSerializable
data class AsteroidFeatures(
	val features: List<AsteroidFeature> = listOf(AsteroidFeature("Example", "ExampleWorld",1.0, 100.0, 10.0, 420, 100, 69000))
)

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
 * @see AsteroidConfiguration.baseAsteroidDensity
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