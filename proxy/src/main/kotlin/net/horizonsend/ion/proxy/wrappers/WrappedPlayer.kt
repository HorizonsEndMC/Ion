package net.horizonsend.ion.proxy.wrappers

import net.horizonsend.ion.proxy.PLUGIN
import net.kyori.adventure.audience.Audience
import net.kyori.adventure.audience.ForwardingAudience
import net.md_5.bungee.api.connection.ProxiedPlayer
import java.util.UUID

class WrappedPlayer(private val inner: ProxiedPlayer) : ForwardingAudience.Single {
	private val audience = PLUGIN.adventure.player(inner)
	override fun audience(): Audience = audience

	val name: String get() = inner.name
	val uniqueId: UUID get() = inner.uniqueId
}
