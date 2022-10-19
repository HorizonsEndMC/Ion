package net.starlegacy.cache.trade

import com.googlecode.cqengine.index.hash.HashIndex
import com.googlecode.cqengine.query.QueryFactory
import com.mongodb.client.model.changestream.ChangeStreamDocument
import net.starlegacy.cache.DbObjectCache
import net.starlegacy.database.Oid
import net.starlegacy.database.document
import net.starlegacy.database.get
import net.starlegacy.database.int
import net.starlegacy.database.mappedList
import net.starlegacy.database.mappedSet
import net.starlegacy.database.oid
import net.starlegacy.database.schema.economy.CollectedItem
import net.starlegacy.database.schema.economy.EcoStation
import net.starlegacy.database.string
import net.starlegacy.feature.economy.collectors.Collectors

object EcoStations : DbObjectCache<EcoStation, Oid<EcoStation>>(EcoStation.Companion) {
	override val idAttribute = attribute(EcoStation::_id)
	private val nameAttribute = attribute(EcoStation::name)

	override fun addExtraIndexes() {
		cache.addIndex(HashIndex.onAttribute(nameAttribute))
	}

	override fun update(cached: EcoStation, change: ChangeStreamDocument<EcoStation>) {
		change[EcoStation::name]?.let { cached.name = it.string() }
		change[EcoStation::world]?.let { cached.world = it.string() }
		change[EcoStation::x]?.let { cached.x = it.int() }
		change[EcoStation::z]?.let { cached.z = it.int() }
		change[EcoStation::collectors]?.let { bson ->
			cached.collectors = bson.mappedList {
				it.document<EcoStation.Collector>()
			}
			Collectors.synchronizeNPCsAsync()
		}
		change[EcoStation::collectedItems]?.let { bson ->
			cached.collectedItems = bson.mappedSet { it.oid<CollectedItem>() }
		}
	}

	fun getByName(name: String): EcoStation? {
		return cache.retrieve(QueryFactory.matchesRegex(nameAttribute, name)).firstOrNull()
	}
}
