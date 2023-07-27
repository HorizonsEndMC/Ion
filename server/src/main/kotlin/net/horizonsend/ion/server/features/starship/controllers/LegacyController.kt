package net.horizonsend.ion.server.features.starship.controllers

import net.horizonsend.ion.server.features.starship.Starship
import org.bukkit.entity.Player

class LegacyController(serverPlayer: Player, starship: Starship) : PlayerController(serverPlayer, starship, "Legacy")
