package net.starlegacy.database.schema.nations

import net.starlegacy.database.schema.misc.SLPlayer
import net.starlegacy.database.schema.starships.Blueprint
import net.starlegacy.database.trx
import org.jetbrains.exposed.sql.transactions.transaction
import org.litote.kmongo.contains
import org.litote.kmongo.eq
import org.litote.kmongo.ne
import org.litote.kmongo.or
import org.litote.kmongo.pull

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
