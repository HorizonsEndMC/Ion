package net.horizonsend.ion.server.database.schema.misc

import net.horizonsend.ion.server.database.DbObject
import net.horizonsend.ion.server.database.Oid
import net.horizonsend.ion.server.database.OidDbObjectCompanion
import net.horizonsend.ion.server.database.objId
import net.horizonsend.ion.server.database.trx
import org.litote.kmongo.SetTo
import org.litote.kmongo.deleteOneById
import org.litote.kmongo.ensureUniqueIndex
import org.litote.kmongo.eq
import org.litote.kmongo.findOne
import org.litote.kmongo.pull
import org.litote.kmongo.push
import org.litote.kmongo.set
import org.litote.kmongo.setValue
import org.litote.kmongo.updateOneById
import java.time.Instant
import java.util.Date

data class Shuttle(
	override val _id: Oid<Shuttle>,
	val name: String,
	val schematic: String,
	val destinations: List<Destination>,
	val currentPosition: Int,
	val lastMove: Date = Date.from(Instant.now())
) : DbObject {
	data class Destination(
		val name: String,
		val world: String,
		val x: Int,
		val y: Int,
		val z: Int
	)

	companion object : OidDbObjectCompanion<Shuttle>(Shuttle::class, setup = {
		ensureUniqueIndex(Shuttle::name)
	}) {
		operator fun get(name: String): Shuttle? = col.findOne(Shuttle::name eq name)

		fun create(name: String, schematic: String): Oid<Shuttle> = trx { sess ->
			val shuttle = Shuttle(objId(), name, schematic, listOf(), 0)
			col.insertOne(sess, shuttle)
			return@trx shuttle._id
		}

		fun addDestination(id: Oid<Shuttle>, destination: Destination): Unit = trx { sess ->
			col.updateOneById(sess, id, push(Shuttle::destinations, destination))
		}

		fun removeDestination(id: Oid<Shuttle>, destination: Destination): Unit = trx { sess ->
			col.updateOneById(sess, id, pull(Shuttle::destinations, destination))
		}

		fun moveLocation(id: Oid<Shuttle>, newLocation: Int): Unit = trx { sess ->
			col.updateOneById(
				sess, id,
				set(
					SetTo(Shuttle::currentPosition, newLocation),
					SetTo(Shuttle::lastMove, Date.from(Instant.now()))
				)
			)
		}

		fun setSchematic(id: Oid<Shuttle>, newSchematic: String): Unit = trx { sess ->
			col.updateOneById(sess, id, setValue(Shuttle::schematic, newSchematic))
		}

		fun delete(id: Oid<Shuttle>): Unit = trx { sess ->
			col.deleteOneById(sess, id)
		}
	}

	fun nextPosition(): Int = (currentPosition + 1) % destinations.size
}
