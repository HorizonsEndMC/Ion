package net.horizonsend.ion.server.features.multiblock

import org.bukkit.block.Furnace
import org.bukkit.block.Sign
import org.bukkit.event.inventory.FurnaceBurnEvent

interface FurnaceMultiblock {
	fun onFurnaceTick(event: FurnaceBurnEvent, furnace: Furnace, sign: Sign)
}
