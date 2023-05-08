package net.starlegacy.database.schema.starships

import com.sk89q.worldedit.extent.clipboard.Clipboard
import java.util.Base64
import net.horizonsend.ion.common.database.Nation
import net.starlegacy.cache.nations.PlayerCache
import net.starlegacy.database.DbObject
import net.starlegacy.database.Oid
import net.starlegacy.database.OidDbObjectCompanion
import net.starlegacy.database.objId
import net.starlegacy.database.schema.misc.SLPlayerId
import net.starlegacy.database.slPlayerId
import net.starlegacy.database.trx
import net.starlegacy.feature.starship.StarshipSchematic
import net.starlegacy.feature.starship.StarshipType
import net.starlegacy.util.Vec3i
import org.bukkit.entity.Player
import org.litote.kmongo.and
import org.litote.kmongo.deleteOneById
import org.litote.kmongo.ensureIndex
import org.litote.kmongo.ensureUniqueIndex
import org.litote.kmongo.eq
import org.litote.kmongo.findOne

data class Blueprint(
	override val _id: Oid<Blueprint>,
	var owner: SLPlayerId,
	var name: String,
	var type: StarshipType,
	var pilotLoc: Vec3i,
	var size: Int,
	var blockData: String, // base64 representation of the schematic
	var trustedPlayers: MutableSet<SLPlayerId> = mutableSetOf(),
	var trustedNations: MutableSet<Oid<Nation>> = mutableSetOf()
) : DbObject {
	companion object : OidDbObjectCompanion<Blueprint>(Blueprint::class, setup = {
		ensureIndex(Blueprint::owner)
		ensureIndex(Blueprint::name)
		ensureUniqueIndex(Blueprint::owner, Blueprint::name)
	}) {
		fun createData(schematic: Clipboard): String {
			return Base64.getEncoder().encodeToString(StarshipSchematic.serializeSchematic(schematic))
		}

		fun parseData(data: String): Clipboard {
			return StarshipSchematic.deserializeSchematic(Base64.getDecoder().decode(data))
		}

		fun get(owner: SLPlayerId, name: String): Blueprint? {
			return col.findOne(and(Blueprint::owner eq owner, Blueprint::name eq name))
		}

		fun create(owner: SLPlayerId, name: String, type: StarshipType, pilotLoc: Vec3i, size: Int, data: String) {
			col.insertOne(Blueprint(objId(), owner, name, type, pilotLoc, size, data))
		}

		fun delete(id: Oid<Blueprint>) = trx { sess ->
			col.deleteOneById(sess, id)
		}
	}

	fun loadClipboard(): Clipboard {
		return parseData(blockData)
	}

	override fun hashCode(): Int {
		return _id.hashCode()
	}

	fun canAccess(player: Player): Boolean {
		val slPlayerId = player.slPlayerId
		return slPlayerId == owner || trustedPlayers.contains(slPlayerId) || trustedNations.contains(PlayerCache[player].nationOid)
	}
}
