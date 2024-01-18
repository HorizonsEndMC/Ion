package net.horizonsend.ion.common.redis.types

import net.horizonsend.ion.common.extensions.CommonPlayer
import net.horizonsend.ion.common.ServerType
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.Component.text
import net.luckperms.api.model.user.User
import java.util.UUID

data class CommonPlayerData(
	override val uniqueId: UUID,
	override val name: String
) : CommonPlayer {
	override fun getDisplayName(): Component = text(name)

	// Don't bother with luck perms stuff outside the servers
	override fun getUser(): User = throw NotImplementedError()
}

data class CommonPlayerDataContainer(
	val server: ServerType,
	val players: List<CommonPlayer>
)
