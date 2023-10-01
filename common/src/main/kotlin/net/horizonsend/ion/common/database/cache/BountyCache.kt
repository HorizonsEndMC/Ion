package net.horizonsend.ion.common.database.cache

import com.googlecode.cqengine.index.compound.CompoundIndex
import com.googlecode.cqengine.index.hash.HashIndex
import com.googlecode.cqengine.query.QueryFactory
import com.mongodb.client.model.changestream.ChangeStreamDocument
import net.horizonsend.ion.common.database.Oid
import net.horizonsend.ion.common.database.boolean
import net.horizonsend.ion.common.database.get
import net.horizonsend.ion.common.database.schema.misc.ClaimedBounty
import net.horizonsend.ion.common.database.schema.misc.SLPlayerId

object BountyCache: DbObjectCache<ClaimedBounty, Oid<ClaimedBounty>>(ClaimedBounty.Companion), Cache {
	override val idAttribute = attribute(ClaimedBounty::_id)
	private val hunterAttr = attribute(ClaimedBounty::hunter)
	private val targetAttr = attribute(ClaimedBounty::target)
	private val dateAttr = attribute(ClaimedBounty::claimTime)

	override fun addExtraIndexes() {
		cache.addIndex(HashIndex.onAttribute(hunterAttr))
//		cache.addIndex(HashIndex.onAttribute(otherAttr))
		cache.addIndex(
			CompoundIndex.onAttributes(
				hunterAttr,
				targetAttr
			)
		)
		cache.addIndex(HashIndex.onAttribute(dateAttr))
	}

	// Not updated, just added
	override fun update(cached: ClaimedBounty, change: ChangeStreamDocument<ClaimedBounty>) {
		change[ClaimedBounty::completed]?.let { cached.completed = it.boolean() }
	}

	operator fun get(hunter: SLPlayerId, target: SLPlayerId): ClaimedBounty? = cache.retrieve(
		QueryFactory.and(
			QueryFactory.equal(hunterAttr, hunter),
			QueryFactory.equal(targetAttr, target)
		))?.firstOrNull()
}
