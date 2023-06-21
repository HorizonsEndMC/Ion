package net.horizonsend.ion.server.database.schema.nations.landsieges

import net.horizonsend.ion.server.database.DbObject
import net.horizonsend.ion.server.database.Oid
import net.horizonsend.ion.server.database.OidDbObjectCompanion
import net.horizonsend.ion.server.database.schema.nations.Nation
import net.horizonsend.ion.server.database.trx
import org.litote.kmongo.Id
import org.litote.kmongo.deleteOneById
import org.litote.kmongo.ensureIndex
import java.util.Date

data class MoonSiege(
	override val _id: Id<MoonSiege>,
	val siegingNation: Oid<Nation>,
	val siegeTerritory: Oid<SiegeTerritory>,

	val startTime: Date,
	val points: Int,
): DbObject{

	companion object : OidDbObjectCompanion<MoonSiege>(MoonSiege::class,
		{
			ensureIndex(MoonSiege::siegeTerritory)
			ensureIndex(MoonSiege::siegingNation)
		}
	) {
		fun create() {

		}

		fun delete(siegeId: Oid<MoonSiege>) = trx { session ->
			col.deleteOneById(session, siegeId)
		}
	}
}
