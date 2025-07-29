package net.horizonsend.ion.server.features.starship.active

import com.google.common.cache.CacheBuilder
import com.google.common.cache.CacheLoader
import com.google.common.cache.LoadingCache
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet
import net.horizonsend.ion.common.database.Oid
import net.horizonsend.ion.common.database.schema.starships.StarshipData
import net.horizonsend.ion.server.IonServerComponent
import net.horizonsend.ion.server.features.starship.PilotedStarships
import net.horizonsend.ion.server.features.starship.Starship
import net.horizonsend.ion.server.features.starship.StarshipType.SPEEDER
import net.horizonsend.ion.server.features.starship.destruction.StarshipDestruction
import net.horizonsend.ion.server.features.starship.event.StarshipActivatedEvent
import net.horizonsend.ion.server.features.starship.event.StarshipDeactivatedEvent
import net.horizonsend.ion.server.features.world.IonWorld.Companion.ion
import net.horizonsend.ion.server.features.world.WorldFlag
import net.horizonsend.ion.server.miscellaneous.utils.Tasks
import net.horizonsend.ion.server.miscellaneous.utils.bukkitWorld
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.Vec3i
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.blockKey
import org.bukkit.Location
import org.bukkit.World
import org.bukkit.block.Block
import org.bukkit.entity.Player
import java.util.UUID

object ActiveStarships : IonServerComponent() {
	private val set = ObjectOpenHashSet<ActiveStarship>()
	private val shipIdMap = Object2ObjectOpenHashMap<Oid<*>, ActiveControlledStarship>()
	private val shipLocationMap: LoadingCache<World, Long2ObjectOpenHashMap<StarshipData>> = CacheBuilder
		.newBuilder()
		.weakKeys()
		.build(CacheLoader.from { w -> Long2ObjectOpenHashMap() })
	private val worldMap: LoadingCache<World, MutableSet<ActiveStarship>> = CacheBuilder
		.newBuilder()
		.weakKeys()
		.build(CacheLoader.from { w -> mutableSetOf() })

	fun all(): List<ActiveStarship> = set.toList()

	fun allControlledStarships(): List<ActiveControlledStarship> = shipIdMap.values.toList()

	fun add(starship: ActiveStarship) {
		Tasks.checkMainThread()
		val world = starship.world

		require(starship !is ActiveControlledStarship || !shipIdMap.containsKey(starship.dataId)) {
			"Starship is already in the active id map"
		}
		require(starship !is ActiveControlledStarship || !shipLocationMap[world].containsKey(starship.data.blockKey)) {
			"Another starship is already at that location"
		}

		set.add(starship)

		if (starship is ActiveControlledStarship) {
			shipIdMap[starship.dataId] = starship
			shipLocationMap[world][starship.data.blockKey] = starship.data
		}

		worldMap[world].add(starship)

		StarshipActivatedEvent(starship).callEvent()
	}

	fun remove(starship: ActiveStarship) {
		Tasks.checkMainThread()

		set.remove(starship)

		if (starship is ActiveControlledStarship) {
			shipIdMap.remove(starship.dataId)

			val blockKey: Long = starship.data.blockKey
			val data: StarshipData = starship.data

			shipLocationMap[starship.world].remove(blockKey, data as Any)
		}

		worldMap[starship.world].remove(starship)

		StarshipDeactivatedEvent(starship).callEvent()

		starship.destroy()
	}

	fun updateLocation(starshipData: StarshipData, newWorld: World, newKey: Long) {
		Tasks.checkMainThread()

		val oldKey = starshipData.blockKey

		if (oldKey == newKey) return

		val oldWorld: World = starshipData.bukkitWorld()
		val oldMap: Long2ObjectOpenHashMap<StarshipData> = shipLocationMap[oldWorld]
		val newMap: Long2ObjectOpenHashMap<StarshipData> = shipLocationMap[newWorld]

		val notYetInNewWorld = !newMap.containsKey(newKey)
		val successfullyRemoved = oldMap.remove(oldKey, starshipData as Any)

		check(notYetInNewWorld && successfullyRemoved) {
			"Not all conditions ($notYetInNewWorld, $successfullyRemoved) were true when moving computer from " +
				"${oldWorld.name}@${Vec3i(oldKey)}" +
				" to ${newWorld.name}@${Vec3i(newKey)}"
		}

		starshipData.blockKey = newKey
		starshipData.levelName = newWorld.name
		newMap[newKey] = starshipData
	}

	fun updateWorld(starship: ActiveStarship, oldWorld: World, newWorld: World) {
		if (oldWorld.uid == newWorld.uid) return

		worldMap[oldWorld].remove(starship)
		worldMap[newWorld].add(starship)

		if (starship.type == SPEEDER && newWorld.ion.hasFlag(WorldFlag.SPEEDERS_EXPLODE)) StarshipDestruction.destroy(starship)
	}

	operator fun get(playerShipId: Oid<out StarshipData>) = shipIdMap[playerShipId]

	fun getByCharIdentifier(charIdentifier: String) = set.firstOrNull { it.charIdentifier == charIdentifier }

	fun getByIdentifier(identifier: String): ActiveStarship? = set.firstOrNull { it.identifier == identifier }

	fun getByComputerLocation(world: World, x: Int, y: Int, z: Int): StarshipData? {
		return shipLocationMap[world][blockKey(x, y, z)]
	}

	fun getInWorld(world: World): Collection<Starship> = worldMap[world]

	fun findByPassenger(player: Player): Starship? = set.firstOrNull { it.isPassenger(player.uniqueId) }

	fun findByPilot(player: Player): Starship? = PilotedStarships[player]
	fun findByPilot(player: UUID): Starship? = PilotedStarships[player]

	fun findByBlock(block: Block): Starship? {
		return findByBlock(block.world, block.x, block.y, block.z)
	}

	fun findByBlock(location: Location): Starship? {
		return findByBlock(location.world, location.blockX, location.blockY, location.blockZ)
	}

	fun findByBlock(world: World, x: Int, y: Int, z: Int): Starship? {
		return getInWorld(world).firstOrNull { it.contains(x, y, z) }
	}

	fun isActive(starship: Starship) = worldMap[starship.world].contains(starship)
}
