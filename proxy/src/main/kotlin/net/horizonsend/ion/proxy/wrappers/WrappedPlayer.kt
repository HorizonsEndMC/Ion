package net.horizonsend.ion.proxy.wrappers

import com.velocitypowered.api.proxy.Player
import net.horizonsend.ion.common.database.slPlayerId
import net.horizonsend.ion.common.extensions.CommonPlayer
import net.horizonsend.ion.common.utils.luckPerms
import net.kyori.adventure.audience.Audience
import net.kyori.adventure.audience.ForwardingAudience
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer.legacyAmpersand
import net.luckperms.api.model.user.User
import java.util.UUID

class WrappedPlayer(private val inner: Player) : CommonPlayer, ForwardingAudience.Single{
	override fun audience(): Audience = inner

	override val name: String get() = inner.username
	override val uniqueId: UUID get() = inner.uniqueId
	val slPlayerId get() = uniqueId.slPlayerId

	override fun getDisplayName(): Component = legacyAmpersand().deserialize(inner.username)
	override fun getUser(): User = luckPerms.getPlayerAdapter(Player::class.java).getUser(inner)
}
