package net.horizonsend.ion.server.features.ai.convoys

import net.horizonsend.ion.server.features.nations.NationsBalancing
import net.horizonsend.ion.server.features.space.spacestations.SpaceStationCache
import org.bukkit.Bukkit
import org.bukkit.Location
import kotlin.random.Random

class RandomConvoyRoute private constructor(
	private val worldList: List<String>,
	val numDestinations: Int,
	val generator : RandomConvoyRoute.() -> Location = randomLocation
) : ConvoyRoute {
	private val source: Location = randomLocation()
	private val destinations: ArrayDeque<Location> =
		ArrayDeque(List(numDestinations) { randomLocation() })

	override fun advanceDestination(): Location? = destinations.removeFirstOrNull()
	override fun getSourceLocation(): Location = source

	private fun randomLocation(): Location {
		val validWorlds = worldList.filter { Bukkit.getWorld(it) != null }
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
		fun sameWorld(worldName: String, numDestinations: Int = 5) = RandomConvoyRoute(listOf(worldName), numDestinations)
		fun anyWorld(numDestinations: Int = 5) = RandomConvoyRoute(Bukkit.getWorlds().map { it.name }, numDestinations)
		fun fromList(worldList: List<String>, numDestinations: Int = 5) = RandomConvoyRoute(worldList, numDestinations)
		fun fromAnyStation(numDestinations: Int = 5) = RandomConvoyRoute(Bukkit.getWorlds().map { it.name }, numDestinations, randomStation)

		private val randomLocation : RandomConvoyRoute.() -> Location = lamb@{
			val validWorlds = worldList.filter { Bukkit.getWorld(it) != null }
			val world = Bukkit.getWorld(validWorlds.random())!!
			val border = world.worldBorder
			val r = border.size / 2.0
			return@lamb Location(
				world,
				Random.nextDouble(border.center.x - r, border.center.x + r),
				192.0,
				Random.nextDouble(border.center.z - r, border.center.z + r)
			)
		}

		private val randomStation : RandomConvoyRoute.() -> Location = lamb@{
			val validWorlds = worldList.filter { Bukkit.getWorld(it) != null }
			val stations = SpaceStationCache.all().filter { validWorlds.contains(it.world) }
			if (stations.isEmpty()) return@lamb randomLocation() //fallback
			val station = stations.random()
			val world = Bukkit.getWorld(station.world)!!
			return@lamb Location(
				world,
				station.x.toDouble(),
				192.0,
				station.z.toDouble(),
			)
		}

	}
}
