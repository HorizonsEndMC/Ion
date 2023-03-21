package net.horizonsend.ion.server.features.starship.controllers

import net.horizonsend.ion.server.features.starship.Starship
import net.minecraft.server.level.ServerPlayer

class LegacyController(serverPlayer: ServerPlayer, starship: Starship) : PlayerController(serverPlayer, starship, "Legacy")
