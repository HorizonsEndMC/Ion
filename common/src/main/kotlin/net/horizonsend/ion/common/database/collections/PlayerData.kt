package net.horizonsend.ion.common.database.collections

import com.mongodb.client.model.Collation
import com.mongodb.client.model.IndexOptions
import net.horizonsend.ion.common.database.Collection
import net.horizonsend.ion.common.database.Document
import net.horizonsend.ion.common.database.enums.Achievement
import org.bson.codecs.pojo.annotations.BsonId
import org.litote.kmongo.ensureIndex
import org.litote.kmongo.eq
import org.litote.kmongo.findOne
import java.util.UUID

internal class PlayerData private constructor(
	@BsonId val uuid: UUID,
	var snowflake: Long? = null,
	var username: String? = null,
	var achievements: MutableList<Achievement> = mutableListOf(),
	var voteTimes: MutableMap<String, Long> = mutableMapOf(),
	var acceptedBounty: UUID? = null,
	var bounty: Int = 0,
	var particle: String? = null,
	var color: Int? = null
) : Document() {
	companion object : Collection<PlayerData>(PlayerData::class) {
		init {
			collection.ensureIndex(PlayerData::uuid)
			collection.ensureIndex(PlayerData::snowflake)
			collection.ensureIndex(
				PlayerData::username,
				indexOptions = IndexOptions().collation(Collation.builder().locale("en").caseLevel(false).build())
			)
		}

		override fun construct(id: UUID): PlayerData = PlayerData(id)

		operator fun get(minecraftUsername: String): PlayerData? {
			return collection.findOne(PlayerData::username eq minecraftUsername)
		}

		operator fun get(discordId: Long): PlayerData? {
			return collection.findOne(PlayerData::snowflake eq discordId)
		}
	}

	override fun update() = update(this)
}
