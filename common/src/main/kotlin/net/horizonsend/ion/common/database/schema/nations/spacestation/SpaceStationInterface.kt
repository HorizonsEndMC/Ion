package net.horizonsend.ion.common.database.schema.nations.spacestation

import net.horizonsend.ion.common.database.DbObject
import net.horizonsend.ion.common.database.Oid
import net.horizonsend.ion.common.database.OidDbObjectCompanion
import net.horizonsend.ion.common.database.schema.misc.SLPlayerId
import net.horizonsend.ion.common.database.schema.nations.Nation
import net.horizonsend.ion.common.database.schema.nations.Settlement
import net.horizonsend.ion.common.database.trx
import org.litote.kmongo.Id
import org.litote.kmongo.addToSet
import org.litote.kmongo.and
import org.litote.kmongo.deleteOneById
import org.litote.kmongo.ensureIndex
import org.litote.kmongo.ensureUniqueIndex
import org.litote.kmongo.eq
import org.litote.kmongo.pull
import org.litote.kmongo.setValue
import org.litote.kmongo.updateOneById
import kotlin.reflect.KClass
import kotlin.reflect.KProperty

interface SpaceStationInterface<T> : DbObject {
	override val _id: Oid<*>

	var owner: Id<T>

	var name: String
	var world: String
	var x: Int
	var z: Int

	var radius: Int

	var trustedPlayers: Set<SLPlayerId>
	var trustedSettlements: Set<Oid<Settlement>>
	var trustedNations: Set<Oid<Nation>>

	var trustLevel: SpaceStationCompanion.TrustLevel
}

abstract class SpaceStationCompanion<Owner: DbObject, T: SpaceStationInterface<Owner>>(
    clazz: KClass<T>,
    private val ownerProperty: KProperty<Id<Owner>>,
    private val nameProperty: KProperty<String>,
    private val worldProperty: KProperty<String>,
    private val xProperty: KProperty<Int>,
    private val zProperty: KProperty<Int>,
    private val radiusProperty: KProperty<Int>,
    private val trustedPlayersProperty: KProperty<Set<SLPlayerId>>,
    private val trustedSettlementsProperty: KProperty<Set<Oid<Settlement>>>,
    private val trustedNationsProperty: KProperty<Set<Oid<Nation>>>,
    private val trustLevelProperty: KProperty<TrustLevel>,
) : OidDbObjectCompanion<T>(clazz, setup = {
	ensureIndex(ownerProperty)
	ensureUniqueIndex(nameProperty)
	ensureIndex(trustedPlayersProperty)
	ensureIndex(trustedSettlementsProperty)
	ensureIndex(trustedNationsProperty)
}) {
	protected abstract fun new(owner: Id<Owner>, name: String, world: String, x: Int, z: Int, radius: Int, trustLevel: TrustLevel): T

	fun create(owner: Id<Owner>, name: String, world: String, x: Int, z: Int, radius: Int, trustLevel: TrustLevel): Oid<T> = trx { session ->
		require(none(and(
					NationSpaceStation::name eq name,
					SettlementSpaceStation::name eq name,
					PlayerSpaceStation::name eq name
		)))

		val station: T = new(owner, name, world, x, z, radius, trustLevel)

		col.insertOne(session, station)

		@Suppress("UNCHECKED_CAST")
		return@trx station._id as Oid<T>
	}

	fun rename(id: Oid<T>, newName: String) = col.updateOneById(id, setValue(nameProperty, newName))

	fun setLocation(id: Oid<T>, newX: Int, newZ: Int, newWorld: String) = col.updateOneById(
		id, and(setValue(xProperty, newX), setValue(zProperty, newZ), setValue(worldProperty, newWorld))
	)

	fun setRadius(id: Oid<T>, newRadius: Int) = col.updateOneById(id, setValue(radiusProperty, newRadius))

	fun trustPlayer(id: Oid<T>, player: SLPlayerId) = col.updateOneById(id, addToSet(trustedPlayersProperty, player))
	fun trustSettlement(id: Oid<T>, settlement: Oid<Settlement>) = col.updateOneById(id, addToSet(trustedSettlementsProperty, settlement))
	fun trustNation(id: Oid<T>, nation: Oid<Nation>) = col.updateOneById(id, addToSet(trustedNationsProperty, nation))

	fun unTrustPlayer(id: Oid<T>, player: SLPlayerId) = col.updateOneById(id, pull(trustedPlayersProperty, player))
	fun unTrustSettlement(id: Oid<T>, settlement: Oid<Settlement>) = col.updateOneById(id, pull(trustedSettlementsProperty, settlement))
	fun unTrustNation(id: Oid<T>, nation: Oid<Nation>) = col.updateOneById(id, pull(trustedNationsProperty, nation))

	fun setTrustLevel(id: Oid<T>, trustLevel: TrustLevel) = col.updateOneById(id, setValue(trustLevelProperty, trustLevel))

	fun delete(id: Oid<T>) = trx { col.deleteOneById(it, id) }

	enum class TrustLevel { NONE, MANUAL, SETTLEMENT_MEMBER, NATION_MEMBER, ALLY }
}
