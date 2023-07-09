package net.horizonsend.ion.server.features.cache.trade

import com.googlecode.cqengine.index.hash.HashIndex
import com.googlecode.cqengine.query.QueryFactory
import com.mongodb.client.model.changestream.ChangeStreamDocument
import net.horizonsend.ion.server.features.cache.DbObjectCache
import net.horizonsend.ion.common.database.Oid
import net.horizonsend.ion.common.database.document
import net.horizonsend.ion.common.database.get
import net.horizonsend.ion.common.database.int
import net.horizonsend.ion.common.database.mappedList
import net.horizonsend.ion.common.database.mappedSet
import net.horizonsend.ion.common.database.oid
import net.horizonsend.ion.common.database.schema.economy.CollectedItem
import net.horizonsend.ion.common.database.schema.economy.EcoStation
import net.horizonsend.ion.common.database.string
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
