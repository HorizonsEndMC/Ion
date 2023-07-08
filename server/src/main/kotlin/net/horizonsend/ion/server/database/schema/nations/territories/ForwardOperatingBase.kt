package net.horizonsend.ion.server.database.schema.nations.territories

import com.mongodb.client.FindIterable
import net.horizonsend.ion.server.database.Oid
import net.horizonsend.ion.server.database.schema.nations.Nation
import net.horizonsend.ion.server.database.trx
import org.bukkit.World
import org.litote.kmongo.and
import org.litote.kmongo.deleteOneById
import org.litote.kmongo.ensureIndex
import org.litote.kmongo.eq

data class ForwardOperatingBase(
	override val _id: Oid<ForwardOperatingBase>,
	val name: String,
	override val world: String,
	val nation: Oid<Nation>?,
	override val polygonData: ByteArray,
) : TerritoryInterface {
	companion object : AbstractTerritoryCompanion<ForwardOperatingBase>(
		ForwardOperatingBase::class,
		ForwardOperatingBase::name,
		ForwardOperatingBase::world,
		ForwardOperatingBase::polygonData,
		ForwardOperatingBase::nation,
		{
			ensureIndex(ForwardOperatingBase::nation)
		}
	) {
		fun get(world: World): FindIterable<ForwardOperatingBase> = col.find(
			ForwardOperatingBase::world eq world.name
		)

		override val unclaimedQuery = and(SiegeTerritory.nationProperty eq null)

		override fun new(
			id: Oid<ForwardOperatingBase>,
			name: String,
			world: String,
			polygonData: ByteArray
		): ForwardOperatingBase = ForwardOperatingBase(
			_id = id,
			name = name,
			world = world,
			nation = null,
			polygonData = polygonData
		)

		fun delete(id: Oid<ForwardOperatingBase>) = trx { session -> col.deleteOneById(session, id) }
	}
}
