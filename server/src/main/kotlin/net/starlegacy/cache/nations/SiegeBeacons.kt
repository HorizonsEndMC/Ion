package net.starlegacy.cache.nations

import com.googlecode.cqengine.index.hash.HashIndex
import com.googlecode.cqengine.query.QueryFactory
import com.mongodb.client.model.changestream.ChangeStreamDocument
import net.starlegacy.cache.DbObjectCache
import net.horizonsend.ion.server.database.Oid
import net.horizonsend.ion.server.database.get
import net.horizonsend.ion.server.database.int
import net.horizonsend.ion.server.database.schema.nations.moonsieges.SiegeBeacon
import net.horizonsend.ion.server.database.string
import net.starlegacy.util.Vec3i

object SiegeBeacons : DbObjectCache<SiegeBeacon, Oid<SiegeBeacon>>(SiegeBeacon.Companion) {
	override val idAttribute = attribute(SiegeBeacon::_id)
	private val nameAttribute = attribute(SiegeBeacon::name)

	override fun addExtraIndexes() {
		cache.addIndex(HashIndex.onAttribute(nameAttribute))
	}

	override fun update(cached: SiegeBeacon, change: ChangeStreamDocument<SiegeBeacon>) {
		change[SiegeBeacon::name]?.let { cached.name = it.string() }
		change[SiegeBeacon::world]?.let { cached.world = it.string() }
		change[SiegeBeacon::x]?.let { cached.x = it.int() }
		change[SiegeBeacon::y]?.let { cached.y = it.int() }
		change[SiegeBeacon::z]?.let { cached.z = it.int() }

	}

	fun getByName(name: String): SiegeBeacon? {
		return cache.retrieve(QueryFactory.matchesRegex(nameAttribute, name)).firstOrNull()
	}

	fun containsBlock(world: String, point: Vec3i): Boolean {
		return !SiegeBeacons.getAll().asSequence()
			.filter { it.world == world }
			.filter { it.blocks.contains(point.toBlockPos().asLong()) }
			.any()
	}
}
