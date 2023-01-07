package net.horizonsend.ion.server.starships.control

import net.horizonsend.ion.server.starships.Starship
import net.kyori.adventure.audience.Audience
import net.kyori.adventure.audience.ForwardingAudience
import net.minecraft.server.level.ServerPlayer

interface PlayerController : ForwardingAudience.Single {
	val name: String
	val starship: Starship

	val serverPlayer: ServerPlayer

	fun accelerationTick(): Triple<Int, Int, Int> = Triple(0, 0, 0)

	fun cleanup() {}

	override fun audience(): Audience = serverPlayer.bukkitEntity
}
