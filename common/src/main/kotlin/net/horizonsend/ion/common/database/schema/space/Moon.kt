package net.horizonsend.ion.common.database.schema.space

import com.mongodb.client.FindIterable
import com.mongodb.client.result.DeleteResult
import com.mongodb.client.result.UpdateResult
import net.horizonsend.ion.common.database.DbObject
import net.horizonsend.ion.common.database.Oid
import net.horizonsend.ion.common.database.OidDbObjectCompanion
import net.horizonsend.ion.common.database.objId
import org.litote.kmongo.deleteOneById
import org.litote.kmongo.ensureUniqueIndex
import org.litote.kmongo.eq
import org.litote.kmongo.setValue
import org.litote.kmongo.updateOneById

class Moon(
	override val _id: Oid<Moon> = objId(),
	val name: String,
	val parent: Oid<out ParentPlanet>,
	val planetWorld: String,
	val size: Double,
	val orbitDistance: Int,
	val orbitSpeed: Double,
	val orbitProgress: Double,
	val seed: Long,
	val crustNoise: Double = 0.05,
	val crustMaterials: List<String> = listOf(),
	val cloudDensity: Double = 0.1,
	val cloudDensityNoise: Double = 0.1,
	val cloudThreshold: Double = 0.1,
	val cloudNoise: Double = 0.1,
	val cloudMaterials: List<String> = listOf(),
	val description: String = "",
) : DbObject , ParentPlanet {
	companion object : OidDbObjectCompanion<Moon>(Moon::class, setup = {
		ensureUniqueIndex(Moon::name)
		ensureUniqueIndex(Moon::planetWorld)
	}) {
		fun getOrbiting(planet: Oid<out ParentPlanet>): FindIterable<Moon> = Moon.col.find(
			Moon::parent eq planet
		)

		fun create(
			name: String,
			parent: Oid<out ParentPlanet>,
			planetWorld: String,
			size: Double,
			orbitDistance: Int,
			orbitSpeed: Double,
			orbitProgress: Double,
			seed: Long
		) {
			Moon.col.insertOne(
				Moon(objId(), name, parent, planetWorld, size, orbitDistance, orbitSpeed, orbitProgress, seed)
			)
		}

		fun setParent(id: Oid<Moon>, parent: Oid<out ParentPlanet>): UpdateResult =
			Moon.col.updateOneById(id, setValue(Moon::parent, parent))

		fun setOrbitDistance(id: Oid<Moon>, orbitDistance: Int): UpdateResult =
			Moon.col.updateOneById(id, setValue(Moon::orbitDistance, orbitDistance))

		fun setOrbitProgress(id: Oid<Moon>, orbitProgress: Double): UpdateResult =
			Moon.col.updateOneById(id, setValue(Moon::orbitProgress, orbitProgress))

		fun setOrbitSpeed(id: Oid<Moon>, orbitProgress: Double): UpdateResult =
			Moon.col.updateOneById(id, setValue(Moon::orbitSpeed, orbitProgress))

		fun setSeed(id: Oid<Moon>, seed: Long): UpdateResult =
			Moon.col.updateOneById(id, setValue(Moon::seed, seed))

		fun setCloudMaterials(id: Oid<Moon>, cloudMaterials: List<String>): UpdateResult =
			Moon.col.updateOneById(id, setValue(Moon::cloudMaterials, cloudMaterials))

		fun setCloudDensity(id: Oid<Moon>, cloudDensity: Double): UpdateResult =
			Moon.col.updateOneById(id, setValue(Moon::cloudDensity, cloudDensity))

		fun setCloudDensityNoise(id: Oid<Moon>, cloudDensityNoise: Double): UpdateResult =
			Moon.col.updateOneById(id, setValue(Moon::cloudDensityNoise, cloudDensityNoise))

		fun setCloudThreshold(id: Oid<Moon>, cloudThreshold: Double): UpdateResult =
			Moon.col.updateOneById(id, setValue(Moon::cloudThreshold, cloudThreshold))

		fun setCloudNoise(id: Oid<Moon>, cloudNoise: Double): UpdateResult =
			Moon.col.updateOneById(id, setValue(Moon::cloudNoise, cloudNoise))

		fun setCrustNoise(id: Oid<Moon>, crustNoise: Double): UpdateResult =
			Moon.col.updateOneById(id, setValue(Moon::crustNoise, crustNoise))

		fun setCrustMaterials(id: Oid<Moon>, crustMaterials: List<String>): UpdateResult =
			Moon.col.updateOneById(id, setValue(Moon::crustMaterials, crustMaterials))

		fun setDescription(id: Oid<Moon>, description: String): UpdateResult =
			Moon.col.updateOneById(id, setValue(Moon::description, description))

		fun delete(id: Oid<Moon>): DeleteResult {
			return Moon.col.deleteOneById(id)
		}
	}
}

