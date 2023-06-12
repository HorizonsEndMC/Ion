package net.starlegacy.database.schema.nations

import com.mongodb.client.MongoIterable
import com.mongodb.client.model.Filters
import kotlin.reflect.KClass
import kotlin.reflect.KProperty
import kotlin.reflect.KProperty1
import net.starlegacy.database.schema.nations.Nation
import net.starlegacy.database.DbObject
import net.starlegacy.database.Oid
import net.starlegacy.database.OidDbObjectCompanion
import net.starlegacy.database.objId
import net.starlegacy.database.projected
import net.starlegacy.database.schema.misc.SLPlayer
import net.starlegacy.database.schema.misc.SLPlayerId
import net.starlegacy.database.trx
import net.starlegacy.util.SLTextStyle
import net.starlegacy.util.isAlphanumeric
import org.bson.Document
import org.bson.conversions.Bson
import org.litote.kmongo.addToSet
import org.litote.kmongo.and
import org.litote.kmongo.contains
import org.litote.kmongo.deleteOneById
import org.litote.kmongo.descending
import org.litote.kmongo.ensureIndex
import org.litote.kmongo.ensureUniqueIndex
import org.litote.kmongo.eq
import org.litote.kmongo.projection
import org.litote.kmongo.util.KMongoUtil.idFilterQuery
import org.litote.kmongo.withDocumentClass

/**
 * @property parent The parent entity of this role
 * @property name The name of the role
 * @property color The color of the role
 * @property weight The weight level of the role
 * @property permissions The permissions the role gives
 * @property members Players who have this role
 */
sealed class Role<Parent : DbObject, Permission : Enum<Permission>> : DbObject {
	abstract override val _id: Oid<*>
	abstract val parent: Oid<Parent>
	abstract var name: String
	abstract var color: SLTextStyle
	abstract var weight: Int
	abstract val permissions: MutableSet<Permission>
	abstract val members: MutableSet<SLPlayerId>

	val coloredName get() = "$color$name"
}

abstract class RoleCompanion<Parent: DbObject, Permission : Enum<Permission>, T : Role<Parent, Permission>>(
	clazz: KClass<T>,
	val parentProperty: KProperty<Oid<Parent>>,
	val nameProperty: KProperty<String>,
	val colorProperty: KProperty<SLTextStyle>,
	val permissionsProperty: KProperty<Set<Permission>>,
	val membersProperty: KProperty<MutableSet<SLPlayerId>>,
	val weightProperty: KProperty<Int>,
	private val memberParentProperty: KProperty1<SLPlayer, Oid<Parent>?>
) : OidDbObjectCompanion<T>(clazz, setup = {
	ensureIndex(parentProperty)
	ensureIndex(nameProperty)
	ensureUniqueIndex(parentProperty, nameProperty)
	ensureIndex(membersProperty)
}) {
	fun nameQuery(name: String): Bson = Filters.regex("name", "^$name$", "i")

	protected abstract fun new(parent: Oid<Parent>, name: String, color: SLTextStyle, weight: Int): T

	fun create(parent: Oid<Parent>, name: String, color: SLTextStyle, weight: Int): Oid<T> {
		// require is alphanumeric and none for the same parent w/ same name (ignoring case)
		require(name.isAlphanumeric())

		require(none(and(parentProperty eq parent, nameQuery(name))))

		val role: T = new(parent, name, color, weight)

		col.insertOne(role)

		@Suppress("UNCHECKED_CAST")
		return role._id as Oid<T>
	}

	fun addMember(roleId: Oid<T>, playerId: SLPlayerId): Boolean = trx { sess ->
		// ensure they're in the same parent group
		val parent: Oid<Parent> = col.projection(sess, parentProperty, idFilterQuery(roleId)).first()!!

		require(SLPlayer.matches(sess, playerId, memberParentProperty eq parent)) {
			val playerName: String? = SLPlayer.findPropById(playerId, SLPlayer::lastKnownName)
			val playerNation: Oid<Nation>? = SLPlayer.findPropById(playerId, SLPlayer::nation)
			return@require "$playerName is in $playerNation, they can't be added to role $roleId which is in $parent"
		}

		return@trx col.updateOne(sess, idFilterQuery(roleId), addToSet(membersProperty, playerId)).modifiedCount == 1L
	}

	fun hasPermission(playerId: SLPlayerId, permission: Permission): Boolean {
		return SLPlayer.isSettlementLeader(playerId) ||
			!none(and(membersProperty contains playerId, permissionsProperty contains permission))
	}

	@Suppress("UNCHECKED_CAST")
	fun getRoles(playerId: SLPlayerId): MongoIterable<Oid<T>> = col.withDocumentClass<Document>()
		.find(membersProperty contains playerId)
		.sort(descending(weightProperty))
		.projected(DbObject::_id).map { it[DbObject::_id] as Oid<T> }

	fun getHighestRole(playerId: SLPlayerId): T? = col
		.find(membersProperty contains playerId)
		.sort(descending(weightProperty))
		.firstOrNull()

	fun getTag(playerId: SLPlayerId): String? = col.withDocumentClass<Document>()
		.find(membersProperty contains playerId)
		.sort(descending(weightProperty))
		.projection(colorProperty, nameProperty)
		.firstOrNull()
		?.projected(colorProperty, nameProperty)
		?.let { "${it[colorProperty]}${it[nameProperty]}" }

	fun delete(id: Oid<T>) {
		col.deleteOneById(id)
	}
}

/**
 * Referenced by:
 * - Settlement (for child roles)
 * - NationPlayer (for roles they have)
 */
data class SettlementRole(
	override val _id: Oid<SettlementRole>,
	override val parent: Oid<Settlement>,
	override var name: String,
	override var color: SLTextStyle,
	override var weight: Int,
	override var permissions: MutableSet<Permission> = mutableSetOf(),
	override var members: MutableSet<SLPlayerId> = mutableSetOf()
) : Role<Settlement, SettlementRole.Permission>() {
	companion object : RoleCompanion<Settlement, SettlementRole.Permission, SettlementRole>(
		SettlementRole::class,
		SettlementRole::parent,
		SettlementRole::name,
		SettlementRole::color,
		SettlementRole::permissions,
		SettlementRole::members,
		SettlementRole::weight,
		SLPlayer::settlement
	) {
		override fun new(parent: Oid<Settlement>, name: String, color: SLTextStyle, weight: Int): SettlementRole {
			return SettlementRole(objId(), parent, name, color, weight)
		}
	}

	enum class Permission {
		BUILD,
		MONEY_DEPOSIT,
		MONEY_WITHDRAW,
		MANAGE_ROLES,
		INVITE,
		KICK,
		MANAGE_ZONES
	}
}

/**
 * Referenced by:
 * - Nation (for child roles)
 * - NationPlayer (for roles they have)
 */
data class NationRole(
	override val _id: Oid<NationRole>,
	override val parent: Oid<Nation>,
	override var name: String,
	override var color: SLTextStyle,
	override var weight: Int,
	override var permissions: MutableSet<Permission> = mutableSetOf(),
	override var members: MutableSet<SLPlayerId> = mutableSetOf()
) : Role<Nation, NationRole.Permission>() {
	companion object : RoleCompanion<Nation, NationRole.Permission, NationRole>(
		NationRole::class,
		NationRole::parent,
		NationRole::name,
		NationRole::color,
		NationRole::permissions,
		NationRole::members,
		NationRole::weight,
		SLPlayer::nation
	) {
		override fun new(parent: Oid<Nation>, name: String, color: SLTextStyle, weight: Int): NationRole {
			return NationRole(objId(), parent, name, color, weight)
		}
	}

	enum class Permission {
		CLAIM_CREATE,
		CLAIM_DELETE,
		MONEY_DEPOSIT,
		MONEY_WITHDRAW,
		MANAGE_ROLES,
		SETTLEMENT_INVITE,
		SETTLEMENT_KICK,
		MANAGE_RELATIONS,
		CREATE_STATION,
		MANAGE_STATION,
		DELETE_STATION
	}
}
