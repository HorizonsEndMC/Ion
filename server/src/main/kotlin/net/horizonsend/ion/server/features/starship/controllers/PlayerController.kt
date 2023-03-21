package net.horizonsend.ion.server.features.starship.controllers

import net.horizonsend.ion.server.features.starship.Starship
import net.kyori.adventure.audience.Audience
import net.minecraft.server.level.ServerPlayer

abstract class PlayerController(val serverPlayer: ServerPlayer, starship: Starship, name: String) : Controller(starship, name) {
	override fun audience(): Audience = serverPlayer.bukkitEntity
}
