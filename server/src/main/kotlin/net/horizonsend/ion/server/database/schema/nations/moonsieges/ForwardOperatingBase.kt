package net.horizonsend.ion.server.database.schema.nations.moonsieges

import com.mongodb.client.FindIterable
import net.horizonsend.ion.server.database.DbObject
import net.horizonsend.ion.server.database.Oid
import net.horizonsend.ion.server.database.OidDbObjectCompanion
import net.horizonsend.ion.server.database.ensureUniqueIndexCaseInsensitive
import net.horizonsend.ion.server.database.schema.nations.Nation
import org.bukkit.World
import org.litote.kmongo.deleteOneById
import org.litote.kmongo.ensureIndex
import org.litote.kmongo.eq

data class ForwardOperatingBase(
	override val _id: Oid<ForwardOperatingBase>,
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
		fun get(world: World): FindIterable<ForwardOperatingBase> = col.find(
			ForwardOperatingBase::world eq world.name
		)

		fun create() = TODO("")

		fun delete(id: Oid<ForwardOperatingBase>) = col.deleteOneById(id)
	}
}
