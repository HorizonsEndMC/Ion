package net.starlegacy.database.schema.starships

import net.starlegacy.database.DbObject
import net.starlegacy.database.Oid
import net.starlegacy.database.OidDbObjectCompanion
import org.bukkit.Bukkit
import org.bukkit.World
import org.bukkit.block.BlockFace
import org.litote.kmongo.deleteOneById
import org.litote.kmongo.ensureIndex
import org.litote.kmongo.ensureUniqueIndex
import org.litote.kmongo.eq
import java.io.Serializable

data class SubCraftData(
	override val _id: Oid<SubCraftData>,
	val parent: Oid<PlayerStarshipData>,
	var serverName: String?,
	var levelName: String,
	var blockKey: Long,
	var facing: BlockFace,
	var name: String?
) : DbObject, Serializable {
	companion object : OidDbObjectCompanion<SubCraftData>(SubCraftData::class, setup = {
		ensureIndex(SubCraftData::serverName)
		ensureIndex(SubCraftData::levelName)
		ensureUniqueIndex(SubCraftData::levelName, SubCraftData::blockKey)
	}) {
		fun add(data: SubCraftData) {
			SubCraftData.col.insertOne(data)
		}

		fun remove(dataId: Oid<SubCraftData>) {
			SubCraftData.col.deleteOneById(dataId)
		}

		fun findByKey(blockKey: Long) = SubCraftData.find(SubCraftData::blockKey eq blockKey)
	}
	fun bukkitWorld(): World = requireNotNull(Bukkit.getWorld(levelName)) {
		"World $levelName is not loaded, but tried getting it for computer $_id"
	}
}
