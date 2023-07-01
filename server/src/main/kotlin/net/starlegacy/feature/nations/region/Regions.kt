package net.starlegacy.feature.nations.region

import com.google.common.collect.HashMultimap
import com.google.common.collect.Multimap
import net.horizonsend.ion.server.IonServer
import net.horizonsend.ion.server.IonComponent
import net.starlegacy.cache.nations.PlayerCache
import net.starlegacy.cache.nations.SettlementCache
import net.horizonsend.ion.server.database.DbObject
import net.horizonsend.ion.server.database.Oid
import net.horizonsend.ion.server.database.OidDbObjectCompanion
import net.horizonsend.ion.server.database.containsUpdated
import net.horizonsend.ion.server.database.oid
import net.horizonsend.ion.server.database.schema.nations.CapturableStation
import net.horizonsend.ion.server.database.schema.nations.Settlement
import net.horizonsend.ion.server.database.schema.nations.SettlementRole
import net.horizonsend.ion.server.database.schema.nations.SettlementZone
import net.horizonsend.ion.server.database.schema.nations.Territory
import net.horizonsend.ion.server.database.schema.nations.spacestation.PlayerSpaceStation
import net.horizonsend.ion.server.database.schema.nations.spacestation.SettlementSpaceStation
import net.horizonsend.ion.server.database.schema.nations.spacestation.NationSpaceStation
import net.horizonsend.ion.server.database.schema.nations.moonsieges.ForwardOperatingBase
import net.horizonsend.ion.server.database.schema.nations.territories.Territory
import net.horizonsend.ion.server.database.schema.nations.territories.ForwardOperatingBase
import net.horizonsend.ion.server.database.schema.nations.moonsieges.SiegeBeacon
import net.horizonsend.ion.server.database.schema.nations.territories.SiegeTerritory
import net.starlegacy.feature.nations.region.types.Region
import net.starlegacy.feature.nations.region.types.RegionCapturableStation
import net.starlegacy.feature.nations.region.types.RegionForwardOperatingBase
import net.starlegacy.feature.nations.region.types.RegionParent
import net.starlegacy.feature.nations.region.types.RegionSettlementZone
import net.starlegacy.feature.nations.region.types.RegionSiegeBeacon
import net.starlegacy.feature.nations.region.types.RegionSiegeTerritory
import net.starlegacy.feature.nations.region.types.RegionSpaceStation
import net.starlegacy.feature.nations.region.types.RegionTerritory
import net.starlegacy.feature.nations.region.types.RegionTopLevel
import net.starlegacy.listen
import net.starlegacy.util.Tasks
import org.bson.types.ObjectId
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.event.player.PlayerChangedWorldEvent
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.litote.kmongo.id.WrappedObjectId
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap
import kotlin.collections.set
import kotlin.reflect.KClass

object Regions : IonComponent() {
	override fun supportsVanilla(): Boolean {
		return true
	}

	@Synchronized
	private fun locked(block: () -> Unit) = block()

	private val cache = RegionCache()

	override fun onEnable() {
		registerRegionType(Territory.Companion) { RegionTerritory(it) }

		registerRegionType(SettlementZone.Companion) { RegionSettlementZone(it) }

		registerRegionType(CapturableStation.Companion) { RegionCapturableStation(it) }

		registerRegionType(NationSpaceStation.Companion) { RegionSpaceStation(it) }

		registerRegionType(SettlementSpaceStation.Companion) { RegionSpaceStation(it) }

		registerRegionType(PlayerSpaceStation.Companion) { RegionSpaceStation(it) }

		registerRegionType(SiegeTerritory.Companion) { RegionSiegeTerritory(it) }

		registerRegionType(ForwardOperatingBase.Companion) { RegionForwardOperatingBase(it) }

		registerRegionType(SiegeBeacon.Companion) { RegionSiegeBeacon(it) }

		cache.forEach { it.refreshAccessCache() }

		// cache when players join
		listen<PlayerJoinEvent> { event ->
			val player = event.player

			Tasks.async {
				if (player.isOnline) {
					cache.forEach(player.world.name) { it.cacheAccess(player) }
				}
			}
		}

		// cache is per world, remove from old and cache for new when a player moves between worlds
		listen<PlayerChangedWorldEvent> { event ->
			val player = event.player
			cache.forEach(event.from.name) { it.remove(player) }

			Tasks.async {
				if (player.isOnline) {
					cache.forEach(player.world.name) { it.cacheAccess(player) }
				}
			}
		}

		// clear cache when player quits
		listen<PlayerQuitEvent> { event ->
			cache.forEach(event.player.world.name) { it.remove(event.player) }
		}

		SettlementRole.watchUpdates { change ->
			val id: Oid<SettlementRole> = change.oid

			// Since the BUILD permission can change, update the settlement territory if
			// the permissions are updated or the list of members is updated
			if (change.containsUpdated(SettlementRole::permissions) || change.containsUpdated(SettlementRole::members)) {
				val settlementId = SettlementRole.findPropById(id, SettlementRole::parent)
					?: return@watchUpdates

				refreshSettlementTerritoryLocally(settlementId)
			}
		}

		// When a role is deleted it may have had the BUILD permission, so refresh the settlement territory if so
		SettlementRole.watchDeletes(fullDocument = true) { change ->
			// if the settlement role is mass deleted, this will be null,
			// but theoretically it should be fine as
			// all members of the settlement are updated anyway in
			// the only known case of this happening: a settlement being deleted
			val fullDocument = change.fullDocument ?: return@watchDeletes

			if (!fullDocument.permissions.contains(SettlementRole.Permission.BUILD)) {
				return@watchDeletes
			}

			val settlementId = fullDocument.parent
			refreshSettlementTerritoryLocally(settlementId)
		}
	}

	override fun onDisable() {
		cache.clear()
	}

	fun refreshSettlementTerritoryLocally(settlementId: Oid<Settlement>) {
		locked {
			cache.get<Region<Territory>>(SettlementCache[settlementId].territory)?.refreshAccessCache()
		}
	}

	fun refreshSettlementMembersLocally(settlementId: Oid<Settlement>) {
		locked {
			for (player in IonServer.server.onlinePlayers) {
				if (PlayerCache[player].settlementOid == settlementId) {
					cache.forEach(player.world.name) { region ->
						region.cacheAccess(player)
					}
				}
			}
		}
	}

	fun refreshPlayerLocally(uuid: UUID) {
		locked {
			val player = Bukkit.getPlayer(uuid) ?: return@locked

			cache.forEach(player.world.name) { region ->
				region.cacheAccess(player)
			}
		}
	}

	fun find(loc: Location): Iterable<Region<*>> = find(loc.world.name, loc.blockX, loc.blockY, loc.blockZ)

	fun find(world: String, x: Int, y: Int, z: Int): Iterable<Region<*>> = cache.find(world, x, y, z)

	/** Used like: <code>val territory = Regions.findFirstOf<RegionTerritory>(location)</code> */
	inline fun <reified T : Region<*>> findFirstOf(loc: Location): T? {
		val first: Region<*> = find(loc).firstOrNull { it is T } ?: return null
		return first as T
	}

	operator fun <B : Region<*>> get(id: Oid<*>): B = cache[id] ?: error("$id is not cached!")

	fun <T : Region<*>> getAllOf(clazz: KClass<T>): Iterable<T> = cache.getAllOf(clazz)

	inline fun <reified T : Region<*>> getAllOf(): Iterable<T> = getAllOf(T::class)

	private inline fun <reified A : DbObject, reified B : Region<A>> registerRegionType(
        objectCompanion: OidDbObjectCompanion<A>,
        crossinline createNew: (A) -> B
	) {
		// cache all existing ones
		objectCompanion.all().map(createNew).forEach(cache::add)

		// add new cache when one is inserted
		objectCompanion.watchInserts { change ->
			val fullDocument = change.fullDocument ?: return@watchInserts

			locked {
				val new: B = createNew(fullDocument)
				new.onCreate()
				new.refreshAccessCache()
				cache.add(new)
			}
		}

		// call the change's update method when the properties listened for are changed
		objectCompanion.watchUpdates { change ->
			locked {
				cache.get<B>(change.oid)?.let { region ->
					region.update(change)

					Tasks.async {
						region.refreshAccessCache()
					}
				}
			}
		}

		// remove the cache and call its delete method when its database mirror is removed
		objectCompanion.watchDeletes { change ->
			locked {
				cache.remove(change.oid)?.onDelete()
			}
		}
	}
}

private class RegionCache {
	private val idMap: MutableMap<ObjectId, Region<*>> = ConcurrentHashMap()

	@Suppress("UnstableApiUsage") // our standards are very low
	private val worldRegions: Multimap<String, Region<*>> = HashMultimap.create()

	@Suppress("UnstableApiUsage") // our standards are very low
	private val classMap: Multimap<Class<Region<*>>, Region<*>> = HashMultimap.create()

	fun forEach(action: (Region<*>) -> Unit): Unit = idMap.values.forEach(action)

	fun forEach(world: String, action: (Region<*>) -> Unit) {
		for (region in worldRegions[world]) {
			region.apply(action)

			if (region is RegionParent) {
				region.children.forEach(action)
			}
		}
	}

	fun add(region: Region<*>) {
		idMap[(region.id as WrappedObjectId<*>).id] = region

		classMap[region.javaClass].add(region)

		if (region is RegionTopLevel) {
			worldRegions[region.world].add(region)
		}
	}

	@Suppress("UNCHECKED_CAST")
	operator fun <B : Region<*>> get(id: Oid<*>): B? = idMap[id.id] as B?

	@Suppress("UNCHECKED_CAST")
	fun <T : Region<*>> getAllOf(clazz: KClass<T>): Iterable<T> =
		classMap[clazz.java as Class<Region<*>>] as Iterable<T>

	fun remove(region: Region<*>) {
		idMap.remove((region.id as WrappedObjectId<*>).id)

		classMap[region.javaClass].remove(region)

		if (region is RegionTerritory) {
			worldRegions[region.world].remove(region)
		}
	}

	fun remove(objectId: Oid<*>): Region<*>? {
		val region: Region<*> = idMap[objectId.id] ?: return null
		remove(region)
		return region
	}

	/** Search all the world regions in the world, plus their children */
	fun find(world: String, x: Int, y: Int, z: Int): List<Region<*>> {
		val regions: MutableList<Region<*>> = worldRegions[world].asSequence()
			.filter { it.contains(x, y, z) }
			.toMutableList()

		for (region in regions.toList()) {
			if (region is RegionParent) {
				region.children.filterTo(regions) { it.contains(x, y, z) }
			}
		}

		return regions
	}

	fun clear() {
		idMap.clear()
		classMap.clear()
		worldRegions.clear()
	}
}
