package net.horizonsend.ion.server.features.multiblock.crafting.ingredient

import net.horizonsend.ion.server.features.multiblock.Multiblock
import org.bukkit.block.Sign

interface Consumable {
	fun consume(multiblock: Multiblock, sign: Sign)
}
