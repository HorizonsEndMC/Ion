package net.horizonsend.ion.server.features.ai.convoys

import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.World
import kotlin.random.Random

class RandomConvoyRoute private constructor(
	private val worldList : List<String>,
) : ConvoyRoute {
	private val source: Location = randomLocation()
	private val destinations: ArrayDeque<Location> =
		ArrayDeque(List(5) { randomLocation() })

	override fun advanceDestination(): Location? = destinations.removeFirstOrNull()
	override fun getSourceLocation(): Location      = source

	private fun randomLocation(): Location {
		val validWorlds = worldList.filter { Bukkit.getWorld(it) != null}
		val world = Bukkit.getWorld(validWorlds.random())!!
		val border = world.worldBorder
		val r = border.size / 2.0
		return Location(
			world,
			Random.nextDouble(border.center.x - r, border.center.x + r),
			192.0,
			Random.nextDouble(border.center.z - r, border.center.z + r)
		)
	}

	companion object {
		fun sameWorld(worldName: String) = RandomConvoyRoute(listOf(worldName))
		fun anyWorld()                   = RandomConvoyRoute(Bukkit.getWorlds().map { it.name })
		fun fromList(worldList: List<String>) = RandomConvoyRoute(worldList)
	}
}
