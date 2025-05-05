package net.horizonsend.ion.server.features.ai.convoys

import org.bukkit.Bukkit
import org.bukkit.Location
import kotlin.random.Random

class RandomConvoyRoute private constructor(
	private val sameWorld : Boolean,
	private val srcWorld  : String
) : ConvoyRoute {

	private val source: Location = randomLocation(srcWorld)
	private val destinations: ArrayDeque<Location> =
		ArrayDeque(List(5) { randomLocation(if (sameWorld) srcWorld else null) })

	override fun advanceDestination(): Location? = destinations.removeFirstOrNull()
	override fun getSourceLocation(): Location      = source

	companion object {
		fun sameWorld(worldName: String) = RandomConvoyRoute(true, worldName)
		fun anyWorld()                   = RandomConvoyRoute(false, Bukkit.getWorlds().random().name)

		private fun randomLocation(worldName: String?): Location {
			val world = if (worldName != null) Bukkit.getWorld(worldName)!!
			else Bukkit.getWorlds().random()
			val border = world.worldBorder
			val r = border.size / 2.0
			return Location(
				world,
				Random.nextDouble(border.center.x - r, border.center.x + r),
				192.0,
				Random.nextDouble(border.center.z - r, border.center.z + r)
			)
		}
	}
}
