package net.starlegacy.cache

import co.aikar.timings.Timing
import com.googlecode.cqengine.ConcurrentIndexedCollection
import com.googlecode.cqengine.attribute.support.FunctionalSimpleAttribute
import com.googlecode.cqengine.attribute.support.SimpleFunction
import com.googlecode.cqengine.index.unique.UniqueIndex
import com.googlecode.cqengine.query.QueryFactory
import com.mongodb.client.model.changestream.ChangeStreamDocument
import net.starlegacy.database.DbObject
import net.starlegacy.database.OidDbObjectCompanion
import net.starlegacy.database.oid
import net.starlegacy.util.Tasks
import net.starlegacy.util.timing
import org.litote.kmongo.Id
import kotlin.reflect.KProperty1

@Suppress("UNCHECKED_CAST")
abstract class DbObjectCache<T : DbObject, ID : Id<T>>(private val companion: OidDbObjectCompanion<T>) : Cache {
    protected lateinit var cache: ConcurrentIndexedCollection<T>

    protected abstract val idAttribute: FunctionalSimpleAttribute<T, ID>

    //    private val mutex = Any()
    //    private fun synced(block: () -> Unit): Unit = synchronized(mutex, block)
    private fun synced(timing: Timing, block: () -> Unit): Unit = Tasks.syncTimed(timing, block)

    private val insertTiming = timing("${javaClass.simpleName} Insert")
    private val updateTiming = timing("${javaClass.simpleName} Update")
    private val deleteTiming = timing("${javaClass.simpleName} Delete")

    override fun load() {
        cache = ConcurrentIndexedCollection()

        cache.addAll(companion.all())
        cache.addIndex(UniqueIndex.onAttribute(idAttribute))
        addExtraIndexes()

        companion.watchInserts { change ->
            val fullDocument = change.fullDocument ?: return@watchInserts
            synced(insertTiming) {
                cache.add(fullDocument)
                onInsert(fullDocument)
            }
        }

        companion.watchUpdates { change ->
            synced(updateTiming) {
                val cached = this[change.oid as ID]
                update(cached, change)
            }
        }

        companion.watchDeletes {
            synced(deleteTiming) {
                val cached = this[it.oid as ID]
                cache.remove(cached)
                onDelete(cached)
            }
        }
    }

    protected open fun addExtraIndexes() {}

    protected open fun onInsert(cached: T) {}

    protected abstract fun update(cached: T, change: ChangeStreamDocument<T>)

    protected open fun onDelete(cached: T) {}

    fun getAll(): List<T> = cache.toList()

    operator fun get(id: ID): T = cache
        .retrieve(QueryFactory.equal(idAttribute, id))
        .firstOrNull() ?: error("$id not cached!")

    // https://github.com/npgall/cqengine/blob/master/documentation/OtherJVMLanguages.md
    protected inline fun <reified O, reified A> attribute(accessor: KProperty1<O, A>): FunctionalSimpleAttribute<O, A> {
        return FunctionalSimpleAttribute(
            O::class.java,
            A::class.java,
            accessor.javaClass.simpleName,
            SimpleFunction { accessor.get(it) })
    }
}
