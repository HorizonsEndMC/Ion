package net.horizonsend.ion.server.database.schema.nations.moonsieges

import net.horizonsend.ion.server.database.DbObject
import net.horizonsend.ion.server.database.Oid
import net.horizonsend.ion.server.database.OidDbObjectCompanion
import net.horizonsend.ion.server.database.objId
import net.horizonsend.ion.server.database.schema.nations.AbstractTerritoryCompanion
import net.horizonsend.ion.server.database.schema.nations.Nation
import net.horizonsend.ion.server.database.schema.nations.TerritoryInterface
import net.horizonsend.ion.server.database.trx
import org.bukkit.Bukkit
import org.bukkit.World
import org.litote.kmongo.and
import org.litote.kmongo.ensureIndex
import org.litote.kmongo.findOneById
import org.litote.kmongo.setValue
import org.litote.kmongo.updateOneById
import java.time.Instant
import java.util.Date

data class SiegeTerritory(
	override val _id: Oid<SiegeTerritory>,
	val name: String,
	override val world: String,
	val nation: Oid<Nation>?,
	val previousOwner: Oid<Nation>?,
	var lastChangedHands: Date,
	override var polygonData: ByteArray,
	var siegeActive: Boolean,
) : TerritoryInterface {
	companion object : AbstractTerritoryCompanion<SiegeTerritory>(
		SiegeTerritory::class,
		SiegeTerritory::name,
		SiegeTerritory::world,
		SiegeTerritory::polygonData,
		{
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

		override fun new(id: Oid<SiegeTerritory>, name: String, world: String, polygonData: ByteArray): SiegeTerritory =
			SiegeTerritory(
				_id = id,
				name = name,
				world = world,
				nation = null,
				previousOwner = null,
				lastChangedHands = Date.from(Instant.now()),
				polygonData = polygonData,
				siegeActive = false
			)
	}

	fun bukkitWorld(): World = Bukkit.getWorld(world) ?: error("World $world not loaded!")
}
