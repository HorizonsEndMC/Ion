package net.horizonsend.ion.server.features.world.data

import net.horizonsend.ion.server.features.multiblock.Multiblock
import org.bukkit.block.Sign

interface MultiblockSignDataFixer : DataFixer  {
	val multiblock: Multiblock

	fun fixSign(sign: Sign)
}
