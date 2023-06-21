package net.horizonsend.ion.server.database.schema.nations.moonsieges

import net.horizonsend.ion.server.database.DbObject
import net.horizonsend.ion.server.database.Oid
import net.horizonsend.ion.server.database.OidDbObjectCompanion
import net.horizonsend.ion.server.database.ensureUniqueIndexCaseInsensitive
import net.horizonsend.ion.server.database.objId
import net.horizonsend.ion.server.database.trx
import net.minecraft.world.phys.Vec3
import net.starlegacy.util.Vec3i
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.World
import org.litote.kmongo.Id
import org.litote.kmongo.ensureIndex

data class SiegeBeacon(
	override val _id: Id<SiegeBeacon>,
	val name: String,
	val siegeTerritory: Oid<SiegeTerritory>,
	val world: String,
	val x: Int,
	val y: Int,
	val z: Int,
	val status: Boolean,
) : DbObject {
	companion object : OidDbObjectCompanion<SiegeBeacon>(
		SiegeBeacon::class,
		{
			ensureUniqueIndexCaseInsensitive(SiegeBeacon::name)
			ensureIndex(SiegeBeacon::world)
			ensureIndex(SiegeBeacon::siegeTerritory)
		}
	) {
		fun create(name: String, siegeTerritory: Oid<SiegeTerritory>, world: String, signLoc: Vec3i): Oid<SiegeBeacon> = trx {
			val id = objId<SiegeBeacon>()

			val (x, y, z) = signLoc

			col.insertOne(
				SiegeBeacon(
					_id = id,
					name = name,
					siegeTerritory = siegeTerritory,
					world = world,
					x = x,
					y = y,
					z = z,
					status = false,
				)
			)

			return@trx id
		}
	}

	fun bukkitWorld(): World = Bukkit.getWorld(world) ?: error("World $world not loaded!")

	fun vec3i(): Vec3i = Vec3i(x, y, z)

	fun location(): Location = Location(bukkitWorld(), x.toDouble(), y.toDouble(), z.toDouble())
}
