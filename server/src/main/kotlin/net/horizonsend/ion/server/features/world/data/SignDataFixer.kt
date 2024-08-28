package net.horizonsend.ion.server.features.world.data

import org.bukkit.block.Sign

interface SignDataFixer : DataFixer  {
	fun fixSign(sign: Sign)
}
