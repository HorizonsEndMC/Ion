package net.horizonsend.ion.server.features.starship.factory

import net.horizonsend.ion.common.database.schema.starships.Blueprint
import net.horizonsend.ion.server.features.multiblock.type.shipfactory.ShipFactoryEntity
import net.horizonsend.ion.server.features.multiblock.type.shipfactory.ShipFactorySettings
import net.horizonsend.ion.server.miscellaneous.utils.Tasks
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.getX
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.getY
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.getZ
import net.horizonsend.ion.server.miscellaneous.utils.getBlockDataSafe
import net.horizonsend.ion.server.miscellaneous.utils.minecraft
import net.horizonsend.ion.server.miscellaneous.utils.nms
import net.minecraft.core.BlockPos
import net.minecraft.network.protocol.game.ClientboundBlockUpdatePacket
import org.bukkit.block.data.BlockData
import org.bukkit.entity.Player

class PreviewTask(
	blueprint: Blueprint,
	settings: ShipFactorySettings,
	entity: ShipFactoryEntity,
	private val player: Player,
	private val durationTicks: Long,
) : ShipFactoryBlockProcessor(blueprint, settings, entity) {
	fun preview() {
		loadBlockQueue()
		sendBlocks()
		Tasks.asyncDelay(durationTicks, ::resetChunkView)
	}

	private fun sendBlocks() {
		for (entry in blockMap) {
			val key = entry.key

			sendFakeBlock(getX(key), getY(key), getZ(key), entry.value)
		}
	}

	fun resetChunkView() {
		for (key in blockMap.keys) {
			val worldBlockData = getBlockDataSafe(entity.world, getX(key), getY(key), getZ(key)) ?: continue

			sendFakeBlock(getX(key), getY(key), getZ(key), worldBlockData)
		}
	}

	private fun sendFakeBlock(x: Int, y: Int, z: Int, blockData: BlockData) {
		val nmsBlockPos = BlockPos(x, y, z)
		val packet = ClientboundBlockUpdatePacket(nmsBlockPos, blockData.nms.rotate(getNMSRotation()))

		player.minecraft.connection.send(packet)
	}
}
