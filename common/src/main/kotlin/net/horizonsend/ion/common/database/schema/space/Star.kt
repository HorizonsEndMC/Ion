package net.horizonsend.ion.common.database.schema.space

import net.horizonsend.ion.common.database.DbObject
import net.horizonsend.ion.common.database.Oid
import net.horizonsend.ion.common.database.OidDbObjectCompanion
import net.horizonsend.ion.common.database.objId
import org.litote.kmongo.ensureUniqueIndex
import org.litote.kmongo.eq
import org.litote.kmongo.pullByFilter
import org.litote.kmongo.push
import org.litote.kmongo.set
import org.litote.kmongo.setTo
import org.litote.kmongo.setValue

data class Star(
    override val _id: Oid<Star> = objId(),
    var name: String,
    var spaceWorld: String,
    var x: Int,
    var y: Int,
    var z: Int,
    var size: Double,
	val seed: Long,

	@Deprecated("fallback value")
	var material: String? = null,

	val crustLayers: List<CrustLayer> = listOf(
		CrustLayer(0, 1.0, listOf(material ?: "GLOWSTONE")
	)),
) : DbObject {
	companion object : OidDbObjectCompanion<Star>(Star::class, setup = {
		ensureUniqueIndex(Star::name)
	}) {
		fun create(name: String, spaceWorld: String, x: Int, y: Int, z: Int, size: Double, seed: Long): Oid<Star> {
			val id = objId<Star>()

			col.insertOne(Star(id, name, spaceWorld, x, y, z, size, seed))
			return id
		}

		fun setPos(id: Oid<Star>, spaceWorld: String, x: Int, y: Int, z: Int) {
			updateById(id, set(Star::spaceWorld setTo spaceWorld, Star::x setTo x, Star::y setTo y, Star::z setTo z))
		}

		fun removeCrustLayer(id: Oid<Star>, layerIndex: Int) {
			updateById(id, pullByFilter(Star::crustLayers, CrustLayer::index eq layerIndex))
		}

		fun clearCrustLayers(id: Oid<Star>) {
			updateById(id, setValue(Star::crustLayers, listOf(CrustLayer(0, 1.0, listOf("GLOWSTONE")))))
		}

		fun setCrustLayer(id: Oid<Star>, layer: CrustLayer) {
			removeCrustLayer(id, layer.index)

			updateById(id, push(Star::crustLayers, layer))
		}

		fun setSize(id: Oid<Star>, size: Double) {
			updateById(id, setValue(Star::size, size))
		}

		data class CrustLayer(
			val index: Int,
			val crustNoise: Double = 0.05,
			val materials: List<String>,
		)
	}
}
