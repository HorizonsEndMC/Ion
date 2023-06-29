package net.horizonsend.ion.server.features.multiblock

import org.bukkit.block.Sign
import org.bukkit.entity.Player

interface InteractableMultiblock {
	fun onSignInteract(sign: Sign, player: Player)
}
