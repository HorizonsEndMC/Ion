package net.horizonsend.ion.server.features.explosion.reversal

import org.bukkit.block.data.BlockData

class ExplodedBlockData(
	val x: Int,
	val y: Int,
	val z: Int, // the time to start the regenerate timer, NOT necessarily the time that it exploded
	val explodedTime: Long,
	val blockData: BlockData,
	val tileData: ByteArray?
)
