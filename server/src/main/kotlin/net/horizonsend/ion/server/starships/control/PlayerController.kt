package net.horizonsend.ion.server.starships.control

import net.kyori.adventure.audience.Audience
import net.kyori.adventure.audience.ForwardingAudience
import net.minecraft.server.level.ServerPlayer

interface PlayerController : Controller, ForwardingAudience.Single {
	val serverPlayer: ServerPlayer

	override fun audience(): Audience = serverPlayer.bukkitEntity
}
