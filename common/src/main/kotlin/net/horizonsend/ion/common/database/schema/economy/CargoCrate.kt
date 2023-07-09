package net.horizonsend.ion.common.database.schema.economy

import net.horizonsend.ion.common.database.DbObject
import net.horizonsend.ion.common.database.Oid
import net.horizonsend.ion.common.database.OidDbObjectCompanion
import net.horizonsend.ion.common.database.objId
import net.horizonsend.ion.common.database.trx
import org.litote.kmongo.deleteOneById
import org.litote.kmongo.ensureUniqueIndex
import org.litote.kmongo.eq

/**
 * Referenced by cargo crate shipments
 */
data class CargoCrate(
    override val _id: Oid<CargoCrate> = objId(),
    var name: String,
    var color: Color,
    val values: Map<String, Double> // String = Importing Planet Name // Double = Export Amount
) : DbObject {
	companion object : OidDbObjectCompanion<CargoCrate>(CargoCrate::class, {
		ensureUniqueIndex(CargoCrate::name)
	}) {
		fun delete(crateId: Oid<CargoCrate>): Unit = trx { sess ->
			CargoCrateShipment.col.deleteMany(sess, CargoCrateShipment::crate eq crateId)
			col.deleteOneById(sess, crateId)
		}

		fun create(name: String, color: Color, values: Map<String, Double>): Oid<CargoCrate> {
			val id = objId<CargoCrate>()
			col.insertOne(CargoCrate(id, name, color, values))
			return id
		}
	}

	enum class Color(val chatColor: String, val shulkerMaterial: String) {
		WHITE("WHITE", "WHITE_SHULKER_BOX"),
		ORANGE("GOLD", "ORANGE_SHULKER_BOX"),
		MAGENTA("RED", "MAGENTA_SHULKER_BOX"),
		YELLOW("YELLOW", "YELLOW_SHULKER_BOX"),
		GRAY("DARK_GRAY", "GRAY_SHULKER_BOX"),
		LIGHT_GRAY("GRAY", "LIGHT_GRAY_SHULKER_BOX"),
		CYAN("DARK_AQUA", "CYAN_SHULKER_BOX"),
		BLUE("BLUE", "BLUE_SHULKER_BOX"),
		BROWN("GOLD", "BROWN_SHULKER_BOX"),
		GREEN("DARK_GREEN", "GREEN_SHULKER_BOX"),
		BLACK("BLACK", "BLACK_SHULKER_BOX")
	}

	fun getValue(planet: String): Double = values.getOrDefault(planet, 0.0)
}
