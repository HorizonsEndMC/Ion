package net.horizonsend.ion.server.database.schema.nations

import com.mongodb.client.MongoIterable
import com.mongodb.client.model.Filters
import com.mongodb.client.model.IndexOptions
import net.horizonsend.ion.server.database.DbObject
import net.horizonsend.ion.server.database.Oid
import net.horizonsend.ion.server.database.OidDbObjectCompanion
import net.horizonsend.ion.server.database.ensureUniqueIndexCaseInsensitive
import net.horizonsend.ion.server.database.objId
import net.horizonsend.ion.server.database.schema.misc.SLPlayer
import net.horizonsend.ion.server.database.schema.misc.SLPlayerId
import net.horizonsend.ion.server.database.schema.nations.Nation.Companion.exists
import net.horizonsend.ion.server.database.schema.starships.Blueprint
import net.horizonsend.ion.server.database.trx
import net.minecraft.world.phys.Vec3
import net.starlegacy.util.Vec3i
import net.starlegacy.util.Vec3iWithWorld
import org.bukkit.Color
import org.bukkit.Location
import org.litote.kmongo.*
import org.litote.kmongo.util.KMongoUtil.idFilterQuery

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
data class Nation(
	override val _id: Oid<Nation> = objId(),
	var name: String,
	var capital: Oid<Settlement>,
	var color: Int,
	override var balance: Int = 0,
	val invites: MutableSet<Oid<Settlement>> = mutableSetOf(),
	val aaguns: MutableSet<Vec3iWithWorld> = mutableSetOf()
) : DbObject, MoneyHolder {
	companion object : OidDbObjectCompanion<Nation>(Nation::class, setup = {
		ensureUniqueIndexCaseInsensitive(Nation::name, indexOptions = IndexOptions().textVersion(3))
		ensureUniqueIndex(Nation::capital)
		ensureIndex(Nation::invites)
	}) {
		private fun nameQuery(name: String) = Filters.regex("name", "^$name$", "i")

		fun findByName(name: String): Oid<Nation>? = findOneProp(nameQuery(name), Nation::_id)

		fun create(name: String, capitalId: Oid<Settlement>, color: Int): Oid<Nation> = trx { sess ->
			require(none(sess, nameQuery(name)))

			// require the settlement isn't already in a nation. will also fail if there's no such settlement
			require(Settlement.matches(sess, capitalId, Settlement::nation eq null))

			val id: Oid<Nation> =
				objId()

			// update the settlements members
			SLPlayer.col.updateMany(
				sess, SLPlayer::settlement eq capitalId, setValue(
					SLPlayer::nation, id
				)
			)

			// update the settlement
			Settlement.updateById(sess, capitalId, setValue(Settlement::nation, id))

			// create the actual nation
			col.insertOne(sess, Nation(id, name, capitalId, color))

			return@trx id
		}

		fun delete(id: Oid<Nation>): Unit = trx { sess ->
			require(exists(sess, id))

			// Update all the territories owned by the nation
			Territory.col.updateMany(sess, Territory::nation eq id, setValue(Territory::nation, null))

			// Update all the stations owned by the nation
			CapturableStation.col.updateMany(
				sess, CapturableStation::nation eq id, setValue(CapturableStation::nation, null)
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
				setValue(Settlement::nation, null)
			)

			// Delete all the nation roles associated with the nation
			NationRole.col.deleteMany(sess, NationRole::parent eq id)

			// Delete all the nation relations associated with the nation
			NationRelation.col.deleteMany(
				sess, or(NationRelation::nation eq id, NationRelation::other eq id)
			)

			// unset nation for all members
			SLPlayer.col.updateMany(
				sess, SLPlayer::nation eq id, setValue(SLPlayer::nation, null)
			)

			SpaceStation.col.updateMany(
				sess, SpaceStation::nation ne id, pull(SpaceStation::trustedNations, id)
			)

			SpaceStation.col.deleteMany(sess, SpaceStation::nation eq id)

			Blueprint.col.updateMany(sess, Blueprint::trustedNations contains id, pull(Blueprint::trustedNations, id))

			// Remove the nation itself
			col.deleteOne(sess, idFilterQuery(id))
		}

		fun deposit(nationId: Oid<Nation>, amount: Int) {
			updateById(nationId, inc(Nation::balance, amount))
		}

		fun withdraw(nationId: Oid<Nation>, amount: Int) {
			updateById(nationId, inc(Nation::balance, -amount))
		}

		fun getSettlements(nationId: Oid<Nation>): MongoIterable<Oid<Settlement>> {
			return Settlement.findProp(Settlement::nation eq nationId, Settlement::_id)
		}

		fun getPlayers(nationId: Oid<Nation>): MongoIterable<SLPlayerId> {
			return SLPlayer.findProp(SLPlayer::nation eq nationId, SLPlayer::_id)
		}

		fun isInvited(nationId: Oid<Nation>, settlementId: Oid<Settlement>): Boolean {
			return matches(nationId, Nation::invites contains settlementId)
		}

		fun addInvite(nationId: Oid<Nation>, settlementId: Oid<Settlement>) {
			updateById(nationId, addToSet(Nation::invites, settlementId))
		}

		fun removeInvite(nationId: Oid<Nation>, settlementId: Oid<Settlement>) {
			updateById(nationId, pull(Nation::invites, settlementId))
		}

		fun getMembers(nationId: Oid<Nation>): MongoIterable<SLPlayerId> = SLPlayer
			.findProp(SLPlayer::nation eq nationId, SLPlayer::_id)

		fun setName(nationId: Oid<Nation>, newName: String): Unit = trx { sess ->
			require(none(sess, and(Nation::_id ne nationId, nameQuery(newName)))) { "A different nation with that name already exists" }

			updateById(sess, nationId, setValue(Nation::name, newName))
		}

		fun setColor(nationId: Oid<Nation>, rgb: Int) {
			Color.fromRGB(rgb) // this will throw an exception if it's invalid

			updateById(nationId, setValue(Nation::color, rgb))
		}

		fun setCapital(nationId: Oid<Nation>, settlementId: Oid<Settlement>): Unit = trx { sess ->
			require(Settlement.matches(sess, settlementId, Settlement::nation eq nationId)) { "Settlement not in nation" }

			require(matches(sess, nationId, Nation::capital ne settlementId)) { "Settlement is already the capital" }

			updateById(sess, nationId, setValue(Nation::capital, settlementId))
		}

		fun addAAGun(nationId: Oid<Nation>, location: Location) = trx { sess ->
			updateById(sess, nationId, addToSet(Nation::aaguns, Vec3iWithWorld(location)))
		}

		fun removeAAGun(nationId: Oid<Nation>, location: Location) = trx { sess ->
			updateById(sess, nationId, pull(Nation::aaguns, Vec3iWithWorld(location)))
		}
	}

	data class Relation(val wish: NationRelation, val actual: NationRelation)
}
