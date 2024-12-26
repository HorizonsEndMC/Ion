package net.horizonsend.ion.common.database.schema.space

import com.mongodb.client.result.DeleteResult
import com.mongodb.client.result.UpdateResult
import net.horizonsend.ion.common.database.DbObject
import net.horizonsend.ion.common.database.Oid
import net.horizonsend.ion.common.database.OidDbObjectCompanion
import net.horizonsend.ion.common.database.objId
import net.horizonsend.ion.common.database.trx
import org.litote.kmongo.deleteOneById
import org.litote.kmongo.ensureUniqueIndex
import org.litote.kmongo.setValue
import org.litote.kmongo.updateOneById

class RoguePlanet(
	override val _id: Oid<RoguePlanet> = objId(),
	val name: String,
	val x: Int,
	val y: Int,
	val z: Int,
	val spaceWorld: String,
	val planetWorld: String,
	val size: Double,
	val seed: Long,
	val crustNoise: Double = 0.05,
	val crustMaterials: List<String> = listOf(),
	val cloudDensity: Double = 0.1,
	val cloudDensityNoise: Double = 0.1,
	val cloudThreshold: Double = 0.1,
	val cloudNoise: Double = 0.1,
	val cloudMaterials: List<String> = listOf(),
	val description: String = "",
) : DbObject, ParentPlanet {
	companion object : OidDbObjectCompanion<RoguePlanet>(RoguePlanet::class, setup = {
		ensureUniqueIndex(RoguePlanet::name)
		ensureUniqueIndex(RoguePlanet::planetWorld)
	}) {
		fun create(
			name: String,
			x: Int,
			y: Int,
			z: Int,
			spaceWorld: String,
			planetWorld: String,
			size: Double,
			seed: Long
		) {
			trx {
				RoguePlanet.col.insertOne(RoguePlanet(objId(), name, x, y, z, planetWorld, spaceWorld, size, seed))
			}
		}

		fun setX(id: Oid<RoguePlanet>, x: Int): UpdateResult =
			RoguePlanet.col.updateOneById(id, setValue(RoguePlanet::x, x))

		fun setY(id: Oid<RoguePlanet>, y: Int): UpdateResult =
			RoguePlanet.col.updateOneById(id, setValue(RoguePlanet::y, y))

		fun setZ(id: Oid<RoguePlanet>, z: Int): UpdateResult =
			RoguePlanet.col.updateOneById(id, setValue(RoguePlanet::z, z))

		fun setSeed(id: Oid<RoguePlanet>, seed: Long): UpdateResult =
			RoguePlanet.col.updateOneById(id, setValue(RoguePlanet::seed, seed))

		fun setCloudMaterials(id: Oid<RoguePlanet>, cloudMaterials: List<String>): UpdateResult =
			RoguePlanet.col.updateOneById(id, setValue(RoguePlanet::cloudMaterials, cloudMaterials))

		fun setCloudDensity(id: Oid<RoguePlanet>, cloudDensity: Double): UpdateResult =
			RoguePlanet.col.updateOneById(id, setValue(RoguePlanet::cloudDensity, cloudDensity))

		fun setCloudDensityNoise(id: Oid<RoguePlanet>, cloudDensityNoise: Double): UpdateResult =
			RoguePlanet.col.updateOneById(id, setValue(RoguePlanet::cloudDensityNoise, cloudDensityNoise))

		fun setCloudThreshold(id: Oid<RoguePlanet>, cloudThreshold: Double): UpdateResult =
			RoguePlanet.col.updateOneById(id, setValue(RoguePlanet::cloudThreshold, cloudThreshold))

		fun setCloudNoise(id: Oid<RoguePlanet>, cloudNoise: Double): UpdateResult =
			RoguePlanet.col.updateOneById(id, setValue(RoguePlanet::cloudNoise, cloudNoise))

		fun setCrustNoise(id: Oid<RoguePlanet>, crustNoise: Double): UpdateResult =
			RoguePlanet.col.updateOneById(id, setValue(RoguePlanet::crustNoise, crustNoise))

		fun setCrustMaterials(id: Oid<RoguePlanet>, crustMaterials: List<String>): UpdateResult =
			RoguePlanet.col.updateOneById(id, setValue(RoguePlanet::crustMaterials, crustMaterials))

		fun setDescription(id: Oid<RoguePlanet>, description: String): UpdateResult =
			RoguePlanet.col.updateOneById(id, setValue(RoguePlanet::description, description))

		fun delete(id: Oid<RoguePlanet>): DeleteResult {
			return RoguePlanet.col.deleteOneById(id)
		}
	}
}
