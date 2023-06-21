package net.horizonsend.ion.server.database.schema.nations.landsieges

import net.horizonsend.ion.server.database.DbObject
import net.horizonsend.ion.server.database.Oid
import net.horizonsend.ion.server.database.OidDbObjectCompanion
import net.horizonsend.ion.server.database.ensureUniqueIndexCaseInsensitive
import net.horizonsend.ion.server.database.schema.nations.Nation
import org.litote.kmongo.Id
import org.litote.kmongo.ensureIndex

data class ForwardOperatingBase(
	override val _id: Id<ForwardOperatingBase>,
	val name: String,
	val world: String,
	val nation: Oid<Nation>?,
	val polygonData: ByteArray,
) : DbObject {
	companion object : OidDbObjectCompanion<ForwardOperatingBase>(
		ForwardOperatingBase::class,
		{
			ensureUniqueIndexCaseInsensitive(ForwardOperatingBase::name)
			ensureIndex(ForwardOperatingBase::nation)
		}
	) {

	}
}
