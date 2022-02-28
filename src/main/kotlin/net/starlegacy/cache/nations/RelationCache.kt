package net.starlegacy.cache.nations

import com.googlecode.cqengine.index.compound.CompoundIndex
import com.googlecode.cqengine.index.hash.HashIndex
import com.googlecode.cqengine.query.QueryFactory.and
import com.googlecode.cqengine.query.QueryFactory.equal
import com.mongodb.client.model.changestream.ChangeStreamDocument
import net.starlegacy.PLUGIN
import net.starlegacy.cache.Cache
import net.starlegacy.cache.DbObjectCache
import net.starlegacy.database.Oid
import net.starlegacy.database.enumValue
import net.starlegacy.database.get
import net.starlegacy.database.schema.nations.Nation
import net.starlegacy.database.schema.nations.NationRelation

object RelationCache : DbObjectCache<NationRelation, Oid<NationRelation>>(NationRelation.Companion), Cache {
	override val idAttribute = attribute(NationRelation::_id)
	private val nationAttr = attribute(NationRelation::nation)
	private val otherAttr = attribute(NationRelation::other)

	override fun addExtraIndexes() {
		try {
			cache.addIndex(HashIndex.onAttribute(nationAttr))
			cache.addIndex(HashIndex.onAttribute(otherAttr))
			cache.addIndex(
				CompoundIndex.onAttributes(
					nationAttr,
					otherAttr
				)
			)
		} catch (_: Exception) {} // Silently fail
	}

	override fun update(cached: NationRelation, change: ChangeStreamDocument<NationRelation>) {
		change[NationRelation::wish]?.let { cached.wish = it.enumValue() }
		change[NationRelation::actual]?.let { cached.actual = it.enumValue() }
	}

	operator fun get(nationId: Oid<Nation>, otherId: Oid<Nation>): NationRelation.Level = when (nationId) {
		otherId -> NationRelation.Level.NATION
		else -> cache.retrieve(and(equal(nationAttr, nationId), equal(otherAttr, otherId)))?.firstOrNull()
			?.actual ?: NationRelation.Level.NONE
	}
}
