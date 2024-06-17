package net.horizonsend.ion.server.features.multiblock.type.crafting.recipe

import org.bukkit.block.Furnace
import org.bukkit.block.Sign
import org.bukkit.event.inventory.FurnaceBurnEvent

interface FurnaceEventHandler {
	fun handleFurnaceEvent(event: FurnaceBurnEvent, furnace: Furnace, sign: Sign)
}
