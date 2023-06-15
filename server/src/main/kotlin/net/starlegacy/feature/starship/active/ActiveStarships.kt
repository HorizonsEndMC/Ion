package net.starlegacy.feature.starship.active

import com.google.common.cache.CacheBuilder
import com.google.common.cache.CacheLoader
import com.google.common.cache.LoadingCache
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet
import net.starlegacy.SLComponent
import net.horizonsend.ion.server.database.Oid
import net.horizonsend.ion.server.database.schema.starships.PlayerStarshipData
import net.starlegacy.feature.starship.PilotedStarships
import net.starlegacy.feature.starship.StarshipDestruction
import net.starlegacy.feature.starship.StarshipType.SPEEDER
import net.starlegacy.feature.starship.event.StarshipActivatedEvent
import net.starlegacy.feature.starship.event.StarshipDeactivatedEvent
import net.starlegacy.util.Tasks
import net.starlegacy.util.blockKey
import net.starlegacy.util.blockKeyX
import net.starlegacy.util.blockKeyY
import net.starlegacy.util.blockKeyZ
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.World
import org.bukkit.block.Block
import org.bukkit.block.BlockFace
import org.bukkit.block.data.Directional
import org.bukkit.entity.Player
import kotlin.collections.set

object ActiveStarships : SLComponent() {
	private val set = ObjectOpenHashSet<ActiveStarship>()
	private val playerShipIdMap = Object2ObjectOpenHashMap<Oid<PlayerStarshipData>, ActivePlayerStarship>()
	private val playerShipLocationMap: LoadingCache<World, Long2ObjectOpenHashMap<PlayerStarshipData>> = CacheBuilder
		.newBuilder()
		.weakKeys()
		.build(CacheLoader.from { w -> Long2ObjectOpenHashMap() })
	private val worldMap: LoadingCache<World, MutableSet<ActiveStarship>> = CacheBuilder
		.newBuilder()
		.weakKeys()
		.build(CacheLoader.from { w -> mutableSetOf() })

	fun all(): List<ActiveStarship> = set.toList()

	fun allPlayerShips(): List<ActivePlayerStarship> = playerShipIdMap.values.toList()

	fun add(starship: ActiveStarship) {
		Tasks.checkMainThread()
		val world = starship.serverLevel.world

		require(starship !is ActivePlayerStarship || !playerShipIdMap.containsKey(starship.dataId)) {
			"Starship is already in the active id map"
		}
		require(starship !is ActivePlayerStarship || !playerShipLocationMap[world].containsKey(starship.data.blockKey)) {
			"Another starship is already at that location"
		}

		set.add(starship)

		if (starship is ActivePlayerStarship) {
			playerShipIdMap[starship.dataId] = starship
			playerShipLocationMap[world][starship.data.blockKey] = starship.data
		}

		worldMap[world].add(starship)

		starship.iterateBlocks { x, y, z ->
			val block = starship.serverLevel.world.getBlockAt(x, y, z)
			if (block.type == Material.REDSTONE_BLOCK) {
				val below = block.getRelative(BlockFace.DOWN).blockData
				if (below.material == Material.PISTON && (below as Directional).facing == BlockFace.DOWN) {
					block.type = Material.LAPIS_BLOCK
				}
			}
		}

		StarshipActivatedEvent(starship).callEvent()
	}

	fun remove(starship: ActiveStarship) {
		Tasks.checkMainThread()

		set.remove(starship)

		if (starship is ActivePlayerStarship) {
			playerShipIdMap.remove(starship.dataId)
			val blockKey: Long = starship.data.blockKey
			val data: PlayerStarshipData = starship.data
			playerShipLocationMap[starship.serverLevel.world].remove(blockKey, data as Any)
		}

		worldMap[starship.serverLevel.world].remove(starship)

		if (starship.serverLevel.world.name == "SpaceArena" && !starship.isExploding) {
			StarshipDestruction.vanish(starship)
		}

		StarshipDeactivatedEvent(starship).callEvent()

		starship.destroy()
	}

	fun updateLocation(playerStarshipData: PlayerStarshipData, newWorld: World, newKey: Long) {
		Tasks.checkMainThread()

		val oldKey = playerStarshipData.blockKey
		if (oldKey == newKey) {
			return
		}

		val oldWorld: World = playerStarshipData.bukkitWorld()
		val oldMap: Long2ObjectOpenHashMap<PlayerStarshipData> = playerShipLocationMap[oldWorld]
		val newMap: Long2ObjectOpenHashMap<PlayerStarshipData> = playerShipLocationMap[newWorld]

		val notYetInNewWorld = !newMap.containsKey(newKey)
		val successfullyRemoved = oldMap.remove(oldKey, playerStarshipData as Any)
		check(notYetInNewWorld && successfullyRemoved) {
			"Not all conditions ($notYetInNewWorld, $successfullyRemoved) were true when moving computer from " +
				"${oldWorld.name}@${blockKeyX(oldKey)},${blockKeyY(oldKey)},${blockKeyZ(oldKey)}" +
				" to ${newWorld.name}@${blockKeyX(newKey)},${blockKeyY(newKey)},${blockKeyZ(newKey)}"
		}

		playerStarshipData.blockKey = newKey
		playerStarshipData.levelName = newWorld.name
		newMap[newKey] = playerStarshipData
	}

	fun updateWorld(starship: ActiveStarship, oldWorld: World, newWorld: World) {
		worldMap[oldWorld].remove(starship)
		worldMap[newWorld].add(starship)

		if (starship.type == SPEEDER && newWorld.name == "Space") StarshipDestruction.destroy(starship)
	}

	operator fun get(playerShipId: Oid<PlayerStarshipData>) = playerShipIdMap[playerShipId]

	fun getByComputerLocation(world: World, x: Int, y: Int, z: Int): PlayerStarshipData? {
		return playerShipLocationMap[world][blockKey(x, y, z)]
	}

	fun getInWorld(world: World): Collection<ActiveStarship> = worldMap[world]

	fun findByPassenger(player: Player): ActiveStarship? = set.firstOrNull { it.isPassenger(player.uniqueId) }

	fun findByPilot(player: Player): ActivePlayerStarship? = PilotedStarships[player]

	fun findByBlock(block: Block): ActiveStarship? {
		return findByBlock(block.world, block.x, block.y, block.z)
	}

	fun findByBlock(location: Location): ActiveStarship? {
		return findByBlock(location.world, location.blockX, location.blockY, location.blockZ)
	}

	fun findByBlock(world: World, x: Int, y: Int, z: Int): ActiveStarship? {
		return getInWorld(world).firstOrNull { it.contains(x, y, z) }
	}

	fun isActive(starship: ActiveStarship) = worldMap[starship.serverLevel.world].contains(starship)
}
