package net.horizonsend.ion.common.database.collections

import net.horizonsend.ion.common.database.Collection
import net.horizonsend.ion.common.database.Document
import net.horizonsend.ion.common.database.enums.Achievement
import org.litote.kmongo.eq
import org.litote.kmongo.findOne
import java.util.UUID

class PlayerData private constructor(
	override val _id: UUID,
	var minecraftUsername: String? = null,
	var discordId: Long? = null,
	var achievements: MutableList<Achievement> = mutableListOf()
) : Document<UUID>() {
	companion object : Collection<PlayerData, UUID>(PlayerData::class) {
		override fun construct(id: UUID): PlayerData = PlayerData(id)

		operator fun get(minecraftUsername: String): PlayerData? {
			return collection.findOne(PlayerData::minecraftUsername eq minecraftUsername)
		}

		operator fun get(discordId: Long): PlayerData? {
			return collection.findOne(PlayerData::discordId eq discordId)
		}
	}

	override fun update() = update(this)
}