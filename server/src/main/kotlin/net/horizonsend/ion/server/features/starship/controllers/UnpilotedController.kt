package net.horizonsend.ion.server.features.starship.controllers

import net.horizonsend.ion.server.features.starship.Starship
import org.bukkit.entity.Player

/** Represents a previously piloted starship controller. **/
class UnpilotedController(player: Player, starship: Starship) : PlayerController(player, starship, "Unpiloted")

