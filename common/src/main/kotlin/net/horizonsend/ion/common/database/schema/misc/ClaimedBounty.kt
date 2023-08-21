package net.horizonsend.ion.common.database.schema.misc

import net.horizonsend.ion.common.database.DbObject
import net.horizonsend.ion.common.database.Oid
import net.horizonsend.ion.common.database.OidDbObjectCompanion
import net.horizonsend.ion.common.database.objId
import net.horizonsend.ion.common.database.trx
import org.litote.kmongo.Id
import org.litote.kmongo.ensureIndex
import java.util.Date

data class ClaimedBounty(
	override val _id: Id<ClaimedBounty>,
	val hunter: SLPlayerId,
	val target: SLPlayerId,
	val claimTime: Date
) : DbObject {
	companion object : OidDbObjectCompanion<ClaimedBounty>(
		ClaimedBounty::class,
		setup = {
			ensureIndex(ClaimedBounty::hunter)
			ensureIndex(ClaimedBounty::target)
		}
	) {
		fun claim(hunter: SLPlayerId, target: SLPlayerId) : Oid<ClaimedBounty> = trx { sess ->
			val id = objId<ClaimedBounty>()

			col.insertOne(
				sess,
				ClaimedBounty(
					id,
					hunter,
					target,
					Date(System.currentTimeMillis())
				)
			)

			return@trx id
		}
	}
}
