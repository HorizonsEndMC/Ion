package net.horizonsend.ion.common.database.schema.starships

import com.mongodb.client.MongoCollection
import net.horizonsend.ion.common.database.DbObject
import net.horizonsend.ion.common.database.Oid
import net.horizonsend.ion.common.database.OidDbObjectCompanion
import net.horizonsend.ion.common.database.StarshipTypeDB
import org.litote.kmongo.deleteOneById
import org.litote.kmongo.ensureIndex
import org.litote.kmongo.ensureUniqueIndex
import org.litote.kmongo.setValue
import org.litote.kmongo.updateOneById
import kotlin.reflect.KClass
import kotlin.reflect.KProperty

interface StarshipData : DbObject {
	override val _id: Oid<out StarshipData>

	var starshipType: StarshipTypeDB
	var serverName: String?
	var levelName: String
	var blockKey: Long

	var name: String?

	var containedChunks: Set<Long>?

	var lastUsed: Long
	var isLockEnabled: Boolean

	/** assumes that it's also deactivated */
	fun isLockActive(): Boolean {
		return isLockEnabled && System.currentTimeMillis() - lastUsed >= PlayerStarshipData.LOCK_TIME_MS
	}

	fun companion(): StarshipDataCompanion<StarshipData>
}

abstract class StarshipDataCompanion<T: StarshipData>(
	clazz: KClass<T>,
	private val serverNameProperty: KProperty<String?>,
	private val levelNameProperty: KProperty<String>,
	private val blockKeyProperty: KProperty<Long>,
	private val coveredChunksProperty: KProperty<Set<Long>?>,
	private val typeProperty: KProperty<StarshipTypeDB>,
	private val lockEnabledProperty: KProperty<Boolean>,
	private val nameProperty: KProperty<String?>,
	private val setup: MongoCollection<T>.() -> Unit = {}
) : OidDbObjectCompanion<T>(clazz, setup = {
	ensureIndex(serverNameProperty)
	ensureIndex(levelNameProperty)
	ensureUniqueIndex(levelNameProperty, blockKeyProperty)
	setup(this)
}) {
	fun add(data: T) {
		col.insertOne(data)
	}

	fun remove(dataId: Oid<out T>) {
		col.deleteOneById(dataId)
	}

	fun updateChunks(id: Oid<out T>, chunks: Set<Long>?) {
		col.updateOneById(id, setValue(coveredChunksProperty, chunks))
	}

	fun setType(id: Oid<out T>, type: StarshipTypeDB) {
		col.updateOneById(id, setValue(typeProperty, type))
	}

	fun setLockEnabled(id: Oid<out T>, enabled: Boolean) {
		col.updateOneById(id, setValue(lockEnabledProperty, enabled))
	}

	fun setName(id: Oid<out T>, name: String?) {
		col.updateOneById(id, setValue(nameProperty, name))
	}
}
