package net.horizonsend.ion.server.features.cache

import com.google.common.collect.HashBasedTable
import net.horizonsend.ion.common.database.Oid
import net.horizonsend.ion.common.database.boolean
import net.horizonsend.ion.common.database.cache.ManualCache
import net.horizonsend.ion.common.database.document
import net.horizonsend.ion.common.database.double
import net.horizonsend.ion.common.database.get
import net.horizonsend.ion.common.database.nullable
import net.horizonsend.ion.common.database.oid
import net.horizonsend.ion.common.database.schema.economy.ChestShop
import net.horizonsend.ion.common.database.slPlayerId
import net.horizonsend.ion.common.database.string
import net.horizonsend.ion.common.utils.DBVec3i
import net.horizonsend.ion.common.utils.set
import net.horizonsend.ion.server.features.economy.chestshops.ChestShops
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.Vec3i
import net.kyori.adventure.key.Key
import java.util.concurrent.ConcurrentHashMap

object ChestShopCache : ManualCache() {
	private val byLocation = HashBasedTable.create</* World Key */ Key, Vec3i, ChestShop>()
	private val locationMap = ConcurrentHashMap<Oid<ChestShop>, Pair<Key, Vec3i>>()
	private val byId = ConcurrentHashMap<Oid<ChestShop>, ChestShop>()

	override fun load() {
		byId.clear()
		byLocation.clear()

		for (shop in ChestShop.all()) {
			cache(shop)
		}

		ChestShop.watchInserts { change ->
			val id: Oid<ChestShop> = change.oid

			synced {
				cache(change.fullDocument!!)
			}

			val cached = this[id]
			ChestShops.updateSign(cached)
		}

		ChestShop.watchDeletes { change ->
			val id: Oid<ChestShop> = change.oid

			synced {
				byId.remove(id)
				val (worldKey, location) = locationMap.remove(id) ?: return@synced
				byLocation.remove(worldKey, location)
			}
		}

		ChestShop.watchUpdates { change ->
			synced {
				val id: Oid<ChestShop> = change.oid
				val cached = this[id]

				change[ChestShop::world]?.let { cached.world = it.string() }
				change[ChestShop::location]?.let { cached.location = it.document<DBVec3i>() }
				change[ChestShop::owner]?.let { cached.owner = it.slPlayerId() }
				change[ChestShop::soldItem]?.let { bson -> cached.soldItem = bson.nullable()?.string() }
				change[ChestShop::price]?.let { bson -> cached.price = bson.double() }
				change[ChestShop::selling]?.let { bson -> cached.selling = bson.boolean() }

				ChestShops.updateSign(cached)
			}
		}
	}

	fun cache(shop: ChestShop) {
		byId[shop._id] = shop

		val worldKey = Key.key(shop.world)
		val vec3i = Vec3i(shop.location)

		byLocation[worldKey, vec3i] = shop
		locationMap[shop._id] = worldKey to vec3i
	}

	operator fun get(id: Oid<ChestShop>): ChestShop = byId[id] ?: error("Chest shop $id is not cached!")

	fun getByLocation(world: Key, location: DBVec3i): ChestShop? {
		return byLocation[world, location]
	}
}
