package net.horizonsend.ion.server.features.starship.controllers

import net.horizonsend.ion.server.features.starship.Starship
import net.kyori.adventure.audience.Audience
import net.minecraft.server.level.ServerPlayer

class LegacyController(starship: Starship, val serverPlayer: ServerPlayer) : Controller("Legacy", starship) {
	override fun audience(): Audience = serverPlayer.bukkitEntity
}
