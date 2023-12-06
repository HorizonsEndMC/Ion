package net.horizonsend.ion.proxy.wrappers

import net.horizonsend.ion.common.extensions.CommonPlayer
import net.horizonsend.ion.common.utils.luckPerms
import net.horizonsend.ion.proxy.PLUGIN
import net.kyori.adventure.audience.Audience
import net.kyori.adventure.audience.ForwardingAudience
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer.legacyAmpersand
import net.luckperms.api.model.user.User
import net.md_5.bungee.api.connection.ProxiedPlayer
import java.util.UUID

class WrappedPlayer(private val inner: ProxiedPlayer) : CommonPlayer, ForwardingAudience.Single{
	private val audience = PLUGIN.adventure.player(inner)
	override fun audience(): Audience = audience

	override val name: String get() = inner.name
	override val uniqueId: UUID get() = inner.uniqueId

	override fun getDisplayName(): Component = legacyAmpersand().deserialize(inner.displayName)
	override fun getUser(): User = luckPerms.getPlayerAdapter(ProxiedPlayer::class.java).getUser(inner)
}
