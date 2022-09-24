package net.horizonsend.ion.common.database.sql

import net.horizonsend.ion.common.database.enums.Achievement
import java.util.UUID
import org.jetbrains.exposed.dao.UUIDEntity
import org.jetbrains.exposed.dao.UUIDEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.UUIDTable
import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.lowerCase

@Deprecated("")
object PlayerDataTable : UUIDTable(columnName = "minecraftUUID") {
	@Deprecated("")
	val minecraftUsername: Column<String> =
		varchar("minecraftUsername", 16)
			.uniqueIndex()

	@Deprecated("")
	val discordUUID: Column<Long?> =
		long("discordUUID")
			.nullable()
			.uniqueIndex()

	@Deprecated("")
	val achievements: Column<String> =
		varchar("achievements", Achievement.values().sumOf { it.name.length + 1 } - 1)
			.default("")
}

@Deprecated("")
class PlayerData(minecraftUUID: EntityID<UUID>) : UUIDEntity(minecraftUUID) {
	@Deprecated("")
	companion object : UUIDEntityClass<PlayerData>(PlayerDataTable) {
		@Deprecated("")
		fun getOrCreate(minecraftUUID: UUID, minecraftUsername: String) =
			findById(minecraftUUID) ?: new(minecraftUUID) { this.mcUsername = minecraftUsername }

		@Deprecated("")
		fun getByUsername(minecraftUsername: String) =
			find { PlayerDataTable.minecraftUsername.lowerCase() eq minecraftUsername.lowercase() }.firstOrNull()
	}

	@Deprecated("")
	var mcUsername by PlayerDataTable.minecraftUsername

	@Deprecated("")
	var discordUUID by PlayerDataTable.discordUUID

	private var _achievements by PlayerDataTable.achievements

	@Deprecated("")
	val achievements: List<Achievement>
		get() = _achievements
			.split(",")
			.mapNotNull {
				try {
					Achievement.valueOf(it)
				} catch (_: IllegalArgumentException) {
					null
				}
			}

	@Deprecated("")
	fun addAchievement(achievement: Achievement) {
		_achievements = achievements
			.toMutableList()
			.apply {
				add(achievement)
			}
			.joinToString(",", "", "")
	}

	@Deprecated("")
	fun removeAchievement(achievement: Achievement) {
		_achievements = achievements
			.toMutableList()
			.apply {
				remove(achievement)
			}
			.joinToString(",", "", "")
	}
}