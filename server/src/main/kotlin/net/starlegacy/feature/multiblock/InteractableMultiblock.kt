package net.starlegacy.feature.multiblock

import org.bukkit.block.Sign
import org.bukkit.entity.Player

interface InteractableMultiblock {
	fun onSignInteract(sign: Sign, player: Player)
}
