package net.horizonsend.ion.server.features.cache

import com.googlecode.cqengine.index.hash.HashIndex
import com.googlecode.cqengine.query.QueryFactory
import com.googlecode.cqengine.resultset.ResultSet
import com.mongodb.client.model.changestream.ChangeStreamDocument
import net.horizonsend.ion.common.database.Oid
import net.horizonsend.ion.common.database.boolean
import net.horizonsend.ion.common.database.cache.DbObjectCache
import net.horizonsend.ion.common.database.document
import net.horizonsend.ion.common.database.double
import net.horizonsend.ion.common.database.get
import net.horizonsend.ion.common.database.nullable
import net.horizonsend.ion.common.database.schema.economy.ChestShop
import net.horizonsend.ion.common.database.schema.misc.SLPlayerId
import net.horizonsend.ion.common.database.slPlayerId
import net.horizonsend.ion.common.database.string
import net.horizonsend.ion.common.utils.DBVec3i
import net.horizonsend.ion.server.features.economy.chestshops.ChestShops

object ChestShopCache : DbObjectCache<ChestShop, Oid<ChestShop>>(ChestShop.Companion) {
	override val idAttribute = attribute(ChestShop::_id)

	private val ownerAttribute = attribute(ChestShop::owner)
	private val locationAttribute = attribute(ChestShop::location)
	private val worldAttribute = attribute(ChestShop::world)

	override fun addExtraIndexes() {
		cache.addIndex(HashIndex.onAttribute(ownerAttribute))
		cache.addIndex(HashIndex.onAttribute(locationAttribute))
		cache.addIndex(HashIndex.onAttribute(worldAttribute))
	}

	override fun update(cached: ChestShop, change: ChangeStreamDocument<ChestShop>) {
		change[ChestShop::world]?.let { cached.world = it.string() }
		change[ChestShop::location]?.let { cached.location = it.document<DBVec3i>() }
		change[ChestShop::owner]?.let { cached.owner = it.slPlayerId() }
		change[ChestShop::soldItem]?.let { bson -> cached.soldItem = bson.nullable()?.string() }
		change[ChestShop::price]?.let { bson -> cached.price = bson.double() }
		change[ChestShop::selling]?.let { bson -> cached.selling = bson.boolean() }

		ChestShops.updateSign(cached)
	}

	override fun onInsert(cached: ChestShop) {
		ChestShops.updateSign(cached)
	}

	fun getByOwner(id: SLPlayerId): ResultSet<ChestShop> {
		return cache.retrieve(QueryFactory.equal(ownerAttribute, id))
	}

	fun getByLocation(world: String, location: DBVec3i): ChestShop? {
		return cache.retrieve(
            QueryFactory.and(
			QueryFactory.equal(locationAttribute, location),
			QueryFactory.equal(worldAttribute, world)
		)).firstOrNull()
	}
}
