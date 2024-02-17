package net.horizonsend.ion.common.database.cache

import com.googlecode.cqengine.index.compound.CompoundIndex
import com.googlecode.cqengine.index.hash.HashIndex
import com.googlecode.cqengine.query.QueryFactory.and
import com.googlecode.cqengine.query.QueryFactory.equal
import com.mongodb.client.model.changestream.ChangeStreamDocument
import net.horizonsend.ion.common.database.Oid
import net.horizonsend.ion.common.database.schema.misc.Bookmark
import net.horizonsend.ion.common.database.schema.misc.SLPlayerId

object BookmarkCache: DbObjectCache<Bookmark, Oid<Bookmark>>(Bookmark) {
    override val idAttribute = attribute(Bookmark::_id)
    private val nameAttribute = attribute(Bookmark::name)
    private val xAttribute = attribute(Bookmark::x)
    private val yAttribute = attribute(Bookmark::y)
    private val zAttribute = attribute(Bookmark::z)
    private val serverNameAttribute  = attribute(Bookmark::serverName)
    private val worldNameAttribute = attribute(Bookmark::worldName)
    private val ownerAttribute = attribute(Bookmark::owner)

    override fun addExtraIndexes() {
        cache.addIndex(HashIndex.onAttribute(ownerAttribute))
        cache.addIndex(
            CompoundIndex.onAttributes(
                ownerAttribute,
                nameAttribute
            )
        )
    }

    // Bookmarks are not expected to be updated, only created and deleted
    override fun update(cached: Bookmark, change: ChangeStreamDocument<Bookmark>) {}

    operator fun get(name: String,  owner: SLPlayerId): Bookmark? = cache.retrieve(
        and(equal(ownerAttribute, owner), equal(nameAttribute, name)))?.firstOrNull()
}