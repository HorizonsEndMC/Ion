package net.horizonsend.ion.server.database.schema.nations.moonsieges

import net.horizonsend.ion.server.database.DbObject
import net.horizonsend.ion.server.database.Oid
import net.horizonsend.ion.server.database.OidDbObjectCompanion
import net.horizonsend.ion.server.database.schema.nations.Nation
import org.bukkit.Bukkit
import org.bukkit.World
import org.litote.kmongo.and
import org.litote.kmongo.ensureIndex
import org.litote.kmongo.findOneById
import org.litote.kmongo.setValue
import org.litote.kmongo.updateOneById
import java.util.Date

data class SiegeTerritory(
	override val _id: Oid<SiegeTerritory>,
	val name: String,
	val world: String,
	val nation: Oid<Nation>?,
	val previousOwner: Oid<Nation>?,
	var lastChangedHands: Date,
	var polygonData: ByteArray,
	var siegeActive: Boolean,
) : DbObject {
	companion object : OidDbObjectCompanion<SiegeTerritory>(
		SiegeTerritory::class,
		{
			ensureIndex(SiegeTerritory::name)
			ensureIndex(SiegeTerritory::nation)
		}
	) {
		fun changeOwner(territory: Oid<SiegeTerritory>, newOwner: Oid<Nation>) {
			val data = col.findOneById(territory)

			col.updateOneById(
				territory, and(
					setValue(SiegeTerritory::previousOwner, data?.nation),
					setValue(SiegeTerritory::nation, newOwner)
				)
			)
		}

		fun create() = TODO("")
	}

	fun bukkitWorld(): World = Bukkit.getWorld(world) ?: error("World $world not loaded!")
}
