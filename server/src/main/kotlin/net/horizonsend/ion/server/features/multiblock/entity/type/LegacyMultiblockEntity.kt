package net.horizonsend.ion.server.features.multiblock.entity.type

import org.bukkit.block.Sign

interface LegacyMultiblockEntity {
	fun loadFromSign(sign: Sign) {}
}
