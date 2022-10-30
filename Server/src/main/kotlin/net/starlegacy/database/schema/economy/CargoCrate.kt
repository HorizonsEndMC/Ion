package net.starlegacy.database.schema.economy

import net.starlegacy.database.DbObject
import net.starlegacy.database.Oid
import net.starlegacy.database.OidDbObjectCompanion
import net.starlegacy.database.objId
import net.starlegacy.database.trx
import net.starlegacy.util.SLTextStyle
import org.bukkit.Material
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

	enum class Color(val chatColor: SLTextStyle, val shulkerMaterial: Material) {
		WHITE(SLTextStyle.WHITE, Material.WHITE_SHULKER_BOX),
		ORANGE(SLTextStyle.GOLD, Material.ORANGE_SHULKER_BOX),
		MAGENTA(SLTextStyle.RED, Material.MAGENTA_SHULKER_BOX),
		YELLOW(SLTextStyle.YELLOW, Material.YELLOW_SHULKER_BOX),
		GRAY(SLTextStyle.DARK_GRAY, Material.GRAY_SHULKER_BOX),
		LIGHT_GRAY(SLTextStyle.GRAY, Material.LIGHT_GRAY_SHULKER_BOX),
		CYAN(SLTextStyle.DARK_AQUA, Material.CYAN_SHULKER_BOX),
		BLUE(SLTextStyle.BLUE, Material.BLUE_SHULKER_BOX),
		BROWN(SLTextStyle.GOLD, Material.BROWN_SHULKER_BOX),
		GREEN(SLTextStyle.DARK_GREEN, Material.GREEN_SHULKER_BOX),
		BLACK(SLTextStyle.BLACK, Material.BLACK_SHULKER_BOX)
	}

	fun getValue(planet: String): Double = values.getOrDefault(planet, 0.0)
}