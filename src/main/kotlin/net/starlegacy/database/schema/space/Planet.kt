package net.starlegacy.database.schema.space

import com.mongodb.client.FindIterable
import com.mongodb.client.result.DeleteResult
import com.mongodb.client.result.UpdateResult
import net.starlegacy.database.DbObject
import net.starlegacy.database.Oid
import net.starlegacy.database.OidDbObjectCompanion
import net.starlegacy.database.objId
import org.litote.kmongo.deleteOneById
import org.litote.kmongo.ensureUniqueIndex
import org.litote.kmongo.eq
import org.litote.kmongo.setValue
import org.litote.kmongo.updateOneById

/**
 * @property name The name of the planet
 * @property sun The star the planet orbits
 * @property planetWorld The name of the world the planet represents
 * @property size The scale of the planet model
 * @property orbitDistance The distance from the star the planet orbits
 * @property orbitSpeed The speed at which the planet orbits, in degrees (can be negative)
 * @property orbitProgress The degrees in orbit the planet is currently in
 * @property seed The seed to use for randomization of the planet model
 * @property crustNoise The scale to use for randomizing the crust
 * @property crustMaterials List of string materials to use for the crust
 * @property cloudDensity The % of the atmosphere of the planet to allow clouds in
 * @property cloudDensityNoise The scale to use for randomizing the above percent
 * @property cloudThreshold The % of the cloud area of the atmosphere to actually spawn clouds
 * @property cloudNoise The scale to use for randomizing the above percent
 * @property cloudMaterials The material palette to use for the clouds
 */
data class Planet(
	override val _id: Oid<Planet> = objId(),
	val name: String,
	val sun: Oid<Star>,
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
	val cloudMaterials: List<String> = listOf()
) : DbObject {
	companion object : OidDbObjectCompanion<Planet>(Planet::class, setup = {
		ensureUniqueIndex(Planet::name)
		ensureUniqueIndex(Planet::planetWorld)
	}) {
		fun getOrbiting(sunId: Oid<Star>): FindIterable<Planet> = col.find(
			Planet::sun eq sunId
		)

		fun create(
			name: String, sun: Oid<Star>, planetWorld: String, size: Double,
			orbitDistance: Int, orbitSpeed: Double, orbitProgress: Double, seed: Long
		) {
			col.insertOne(
				Planet(objId(), name, sun, planetWorld, size, orbitDistance, orbitSpeed, orbitProgress, seed)
			)
		}

		fun setSun(id: Oid<Planet>, sun: Oid<Star>): UpdateResult =
			col.updateOneById(id, setValue(Planet::sun, sun))

		fun setOrbitDistance(id: Oid<Planet>, orbitDistance: Int): UpdateResult =
			col.updateOneById(id, setValue(Planet::orbitDistance, orbitDistance))

		fun setOrbitProgress(id: Oid<Planet>, orbitProgress: Double): UpdateResult =
			col.updateOneById(id, setValue(Planet::orbitProgress, orbitProgress))

		fun setSeed(id: Oid<Planet>, seed: Long): UpdateResult =
			col.updateOneById(id, setValue(Planet::seed, seed))

		fun setCloudMaterials(id: Oid<Planet>, cloudMaterials: List<String>): UpdateResult =
			col.updateOneById(id, setValue(Planet::cloudMaterials, cloudMaterials))

		fun setCloudDensity(id: Oid<Planet>, cloudDensity: Double): UpdateResult =
			col.updateOneById(id, setValue(Planet::cloudDensity, cloudDensity))

		fun setCloudDensityNoise(id: Oid<Planet>, cloudDensityNoise: Double): UpdateResult =
			col.updateOneById(id, setValue(Planet::cloudDensityNoise, cloudDensityNoise))

		fun setCloudThreshold(id: Oid<Planet>, cloudThreshold: Double): UpdateResult =
			col.updateOneById(id, setValue(Planet::cloudThreshold, cloudThreshold))

		fun setCloudNoise(id: Oid<Planet>, cloudNoise: Double): UpdateResult =
			col.updateOneById(id, setValue(Planet::cloudNoise, cloudNoise))

		fun setCrustNoise(id: Oid<Planet>, crustNoise: Double): UpdateResult =
			col.updateOneById(id, setValue(Planet::crustNoise, crustNoise))

		fun setCrustMaterials(id: Oid<Planet>, crustMaterials: List<String>): UpdateResult =
			col.updateOneById(id, setValue(Planet::crustMaterials, crustMaterials))

		fun delete(id: Oid<Planet>): DeleteResult {
			return col.deleteOneById(id)
		}
	}
}
