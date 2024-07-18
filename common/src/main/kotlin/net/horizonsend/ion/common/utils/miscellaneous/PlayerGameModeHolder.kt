package net.horizonsend.ion.common.utils.miscellaneous

import kotlinx.serialization.Serializable
import net.horizonsend.ion.common.redis.kserializers.UUIDKSerializer
import java.util.UUID

@Serializable
data class PlayerGameModeHolder(
	val players: List<PlayerGameModeEntry> = listOf()
) {
	@Serializable
	data class PlayerGameModeEntry(
		@Serializable(with = UUIDKSerializer::class) val uuid: UUID,
		val gameMode: Int
	)
}
