package net.horizonsend.ion.server.features.nations.region.types

import com.mongodb.client.model.changestream.ChangeStreamDocument
import net.horizonsend.ion.common.database.DbObject
import net.horizonsend.ion.common.database.Oid
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.World
import org.bukkit.entity.Player
import java.util.UUID
import java.util.WeakHashMap

abstract class Region<T : DbObject>(dbObject: DbObject) {
	private val accessCache: MutableMap<UUID, String?> = WeakHashMap()
	abstract val world: String
	val bukkitWorld: World? get() = Bukkit.getWorld(world)

	fun isCached(player: Player): Boolean {
		synchronized(accessCache) {
			return accessCache.contains(player.uniqueId)
		}
	}

	fun getInaccessMessage(player: Player): String? {
		synchronized(accessCache) {
			check(isCached(player))
			return accessCache[player.uniqueId]
		}
	}

	fun cacheAccess(player: Player) {
		synchronized(accessCache) {
			accessCache[player.uniqueId] = calculateInaccessMessage(player)
		}
	}

	fun remove(player: Player) {
		synchronized(accessCache) {
			accessCache.remove(player.uniqueId)
		}
	}

	fun refreshAccessCache() {
		synchronized(accessCache) {
			accessCache.clear()
			bukkitWorld?.players?.forEach(::cacheAccess)
		}
	}

	open fun onFailedToAccess(player: Player) {}

	@Suppress("UNCHECKED_CAST") // should always be the same type, otherwise it's jacked
	val id: Oid<T> = dbObject._id as Oid<T>

	abstract val priority: Int

	abstract fun contains(x: Int, y: Int, z: Int): Boolean

	fun contains(loc: Location) = bukkitWorld == loc.world && contains(loc.blockX, loc.blockY, loc.blockZ)

	/** Changes called the watchers in Regions. In addition to being listened for here, their properties are specified there. */
	abstract fun update(delta: ChangeStreamDocument<T>)

	open fun onCreate() {}

	open fun onDelete() {}

	abstract fun calculateInaccessMessage(player: Player): String?
}
