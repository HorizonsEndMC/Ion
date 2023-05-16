package net.starlegacy.feature.multiblock

import org.bukkit.block.Sign
import org.bukkit.entity.Player
import org.bukkit.event.player.PlayerInteractEvent

interface InteractableMultiblock {
	fun onSignInteract(sign: Sign, player: Player, event: PlayerInteractEvent)
}
