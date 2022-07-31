package net.horizonsend.ion.common.database

import java.util.UUID
import org.jetbrains.exposed.dao.UUIDEntity
import org.jetbrains.exposed.dao.UUIDEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.UUIDTable
import org.jetbrains.exposed.sql.Column

object PlayerDataTable : UUIDTable(columnName = "minecraftUUID") {
	val minecraftUsername: Column<String> =
		varchar("minecraftUsername", 16)
			.uniqueIndex()

	val discordUUID: Column<Long?> =
		long("discordUUID")
			.nullable()
			.uniqueIndex()

	val achievements: Column<String> =
		varchar("achievements", Achievement.values().sumOf { it.name.length + 1 } - 1)
			.default("")
}

class PlayerData(minecraftUUID: EntityID<UUID>) : UUIDEntity(minecraftUUID) {
	companion object : UUIDEntityClass<PlayerData>(PlayerDataTable) {
		fun getOrCreate(minecraftUUID: UUID, minecraftUsername: String) =
			findById(minecraftUUID) ?: new(minecraftUUID) { this.mcUsername = minecraftUsername }
	}

	var mcUsername by PlayerDataTable.minecraftUsername
	var discordUUID by PlayerDataTable.discordUUID

	private var _achievements by PlayerDataTable.achievements

	val achievements: List<Achievement>
		get() = _achievements
			.split(",")
			.map {
				Achievement.valueOf(it)
			}

	fun addAchievement(achievement: Achievement) {
		_achievements = achievements
			.toMutableList()
			.apply {
				add(achievement)
			}
			.joinToString(",", "", "")
	}

	fun removeAchievement(achievement: Achievement) {
		_achievements = achievements
			.toMutableList()
			.apply {
				remove(achievement)
			}
			.joinToString(",", "", "")
	}
}