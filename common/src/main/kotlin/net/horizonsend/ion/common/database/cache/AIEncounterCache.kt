package net.horizonsend.ion.common.database.cache

import com.googlecode.cqengine.query.QueryFactory.equal
import com.mongodb.client.model.changestream.ChangeStreamDocument
import net.horizonsend.ion.common.database.Oid
import net.horizonsend.ion.common.database.get
import net.horizonsend.ion.common.database.long
import net.horizonsend.ion.common.database.schema.misc.AIEncounterData
import net.horizonsend.ion.common.database.string
import java.time.Duration

object AIEncounterCache : DbObjectCache<AIEncounterData, Oid<AIEncounterData>>(AIEncounterData)  {
	override val idAttribute = AIEncounterCache.attribute(AIEncounterData::_id)
	private val nameAttribute = AIEncounterCache.attribute(AIEncounterData::name)

	override fun update(cached: AIEncounterData, change: ChangeStreamDocument<AIEncounterData>) {}

	operator fun get(name: String): AIEncounterData? = AIEncounterCache.cache.retrieve(
		equal(nameAttribute, name)
	)?.firstOrNull()
}
