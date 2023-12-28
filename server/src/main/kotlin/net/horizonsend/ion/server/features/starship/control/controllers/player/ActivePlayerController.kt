package net.horizonsend.ion.server.features.starship.control.controllers.player

import net.horizonsend.ion.server.features.starship.active.ActiveStarship
import net.horizonsend.ion.server.features.starship.control.movement.PlayerStarshipControl
import org.bukkit.entity.Player

class ActivePlayerController(player: Player, starship: ActiveStarship) : PlayerController(player, starship, "Player") {
	override fun isSneakFlying(): Boolean = player.isSneaking && PlayerStarshipControl.isHoldingController(player)
	override val selectedDirectControlSpeed: Int get() = player.inventory.heldItemSlot
}
