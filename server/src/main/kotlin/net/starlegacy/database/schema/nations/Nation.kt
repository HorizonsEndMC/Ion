package net.starlegacy.database.schema.nations

import com.mongodb.client.model.IndexOptions
import net.starlegacy.database.DbObject
import net.starlegacy.database.Oid
import net.starlegacy.database.OidDbObjectCompanion
import net.starlegacy.database.ensureUniqueIndexCaseInsensitive
import net.starlegacy.database.schema.misc.SLPlayer
import net.starlegacy.database.schema.starships.Blueprint
import net.starlegacy.database.trx
import org.bson.types.ObjectId
import org.jetbrains.exposed.sql.transactions.transaction
import org.litote.kmongo.contains
import org.litote.kmongo.ensureIndex
import org.litote.kmongo.ensureUniqueIndex
import org.litote.kmongo.eq
import org.litote.kmongo.ne
import org.litote.kmongo.or
import org.litote.kmongo.pull

/**
 * Referenced on:
 * - Territory (for territory owner)
 * - NationRole (for parent)
 * - CapturableStation (for owner)
 * - NationRelation (for both the nation in question, and the other nation)
 * - Settlement (for the nation it's in)
 * - SLPlayer (for the nation it's currently in)
 * - CapturableStationSiege (for who sieged it)
 * - SettlementZone (trusted nations)
 * - SpaceStation (owning nation)
 * - SpaceStation (trusted nations)
 * - Blueprint (trusted nations)
 *
 * @property name The name of the nation (user-adjustable)
 * @property capital The capital of the settlement. Also determines the leader.
 * @property color The color of the nation (for map and blasters etc)
 * @property balance The amount of money the nation has
 * @property invites The settlements the nation has invited
 */

@Deprecated("")
data class Nation(
	@Deprecated("")
	override val _id: Oid<net.horizonsend.ion.common.database.Nation> = Oid(ObjectId()),

	@Deprecated("")
	var name: String,

	@Deprecated("")
	var capital: Oid<Settlement>,

	@Deprecated("")
	var color: Int,

	@Deprecated("")
	override var balance: Int = 0,

	@Deprecated("")
	val invites: MutableSet<Oid<Settlement>> = mutableSetOf()
) : DbObject, MoneyHolder {
	@Deprecated("")
	companion object : OidDbObjectCompanion<Nation>(Nation::class, setup = {
		ensureUniqueIndexCaseInsensitive(Nation::name, indexOptions = IndexOptions().textVersion(3))
		ensureUniqueIndex(Nation::capital)
		ensureIndex(Nation::invites)
	})
}

fun net.horizonsend.ion.common.database.Nation.deleteNation(): Unit = transaction {
	trx { sess ->
		val id = this@deleteNation.objectId

		// Update all the territories owned by the nation
		Territory.col.updateMany(sess, Territory::nation eq id, org.litote.kmongo.setValue(Territory::nation, null))

		// Update all the stations owned by the nation
		CapturableStation.col.updateMany(
			sess, CapturableStation::nation eq id, org.litote.kmongo.setValue(CapturableStation::nation, null)
		)

		CapturableStationSiege.col.deleteMany(sess, CapturableStationSiege::nation eq id)

		// remove from zones it's trusted to
		SettlementZone.col.updateMany(
			sess,
			SettlementZone::trustedNations ne null,
			pull(SettlementZone::trustedNations, id)
		)

		// unset the nation of all member settlements
		Settlement.col.updateMany(
			sess, Settlement::nation eq id,
			org.litote.kmongo.setValue(Settlement::nation, null)
		)

		// Delete all the nation roles associated with the nation
		NationRole.col.deleteMany(sess, NationRole::parent eq id)

		// Delete all the nation relations associated with the nation
		NationRelation.col.deleteMany(
			sess, or(NationRelation::nation eq id, NationRelation::other eq id)
		)

		// unset nation for all members
		SLPlayer.col.updateMany(
			sess, SLPlayer::nation eq id, org.litote.kmongo.setValue(SLPlayer::nation, null)
		)

		SpaceStation.col.updateMany(
			sess, SpaceStation::nation ne id, pull(SpaceStation::trustedNations, id)
		)

		SpaceStation.col.deleteMany(sess, SpaceStation::nation eq id)

		Blueprint.col.updateMany(sess, Blueprint::trustedNations contains id, pull(Blueprint::trustedNations, id))

		// Remove the nation itself
		this@deleteNation.delete()
	}
}
