package net.horizonsend.ion.server.database.schema.space

import com.mongodb.client.FindIterable
import com.mongodb.client.result.DeleteResult
import com.mongodb.client.result.UpdateResult
import net.horizonsend.ion.server.database.DbObject
import net.horizonsend.ion.server.database.Oid
import net.horizonsend.ion.server.database.OidDbObjectCompanion
import net.horizonsend.ion.server.database.objId
import org.litote.kmongo.deleteOneById
import org.litote.kmongo.ensureUniqueIndex
import org.litote.kmongo.eq
import org.litote.kmongo.setValue
import org.litote.kmongo.updateOneById

data class Moon(
	override val _id: Oid<Moon> = objId(),
	val orbitingPlanet: Oid<Planet>,

	val name: String,
	val planetWorld: String,

	val orbitDistance: Int,
	val orbitSpeed: Double,
	val orbitProgress: Double,

	val size: Double,

	val seed: Long,
	val crustNoise: Double = 0.05,
	val crustMaterials: List<String> = listOf(),
): DbObject {
	companion object : OidDbObjectCompanion<Moon>(Moon::class, setup = {
		ensureUniqueIndex(Moon::name)
		ensureUniqueIndex(Moon::planetWorld)
	}) {
		fun getOrbiting(parentId: Oid<Planet>): FindIterable<Moon> = Moon.col.find(
			Moon::orbitingPlanet eq parentId
		)

		fun create(
			name: String,
			parent: Oid<Planet>,
			planetWorld: String,
			orbitDistance: Int,
			size: Double,
			orbitSpeed: Double,
			orbitProgress: Double,
			seed: Long
		) {
			col.insertOne(
				Moon(
					_id = objId(),
					orbitingPlanet = parent,
					name = name,
					planetWorld = planetWorld,
					orbitDistance = orbitDistance,
					size = size,
					orbitSpeed = orbitSpeed,
					orbitProgress = orbitProgress,
					seed = seed
				)
			)
		}

		fun setParent(id: Oid<Moon>, parent: Oid<Planet>): UpdateResult =
			Moon.col.updateOneById(id, setValue(Moon::orbitingPlanet, parent))

		fun setOrbitDistance(id: Oid<Moon>, orbitDistance: Int): UpdateResult =
			Moon.col.updateOneById(id, setValue(Moon::orbitDistance, orbitDistance))

		fun setOrbitProgress(id: Oid<Moon>, orbitProgress: Double): UpdateResult =
			Moon.col.updateOneById(id, setValue(Moon::orbitProgress, orbitProgress))

		fun setSeed(id: Oid<Moon>, seed: Long): UpdateResult =
			Moon.col.updateOneById(id, setValue(Moon::seed, seed))
		fun setCrustNoise(id: Oid<Moon>, crustNoise: Double): UpdateResult =
			Moon.col.updateOneById(id, setValue(Moon::crustNoise, crustNoise))

		fun setCrustMaterials(id: Oid<Moon>, crustMaterials: List<String>): UpdateResult =
			Moon.col.updateOneById(id, setValue(Moon::crustMaterials, crustMaterials))

		fun delete(id: Oid<Moon>): DeleteResult {
			return Moon.col.deleteOneById(id)
		}
	}
}
