package net.horizonsend.ion.server.features.starship.factory

import net.horizonsend.ion.common.database.schema.starships.Blueprint
import net.horizonsend.ion.server.features.client.display.ClientDisplayEntities.displayBlock
import net.horizonsend.ion.server.features.client.display.ClientDisplayEntities.sendEntityPacket
import net.horizonsend.ion.server.features.multiblock.type.shipfactory.ShipFactoryEntity
import net.horizonsend.ion.server.features.multiblock.type.shipfactory.ShipFactorySettings
import net.horizonsend.ion.server.miscellaneous.utils.Tasks
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.Vec3i
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.toVec3i
import net.horizonsend.ion.server.miscellaneous.utils.minecraft
import org.bukkit.block.data.BlockData
import org.bukkit.entity.Player

class NewShipFactoryTask(
	blueprint: Blueprint,
	settings: ShipFactorySettings,
	entity: ShipFactoryEntity,
	private val player: Player
) : ShipFactoryBlockProcessor(blueprint, settings, entity) {

	fun tickProgress() {
		if (isDisabled) return

		val first10 = blockQueue.take(entity.multiblock.blockPlacementsPerTick)
		for (entry in first10) {
			blockQueue.remove(entry)
			blockMap.remove(entry)?.let {
				debugSengBlock(it, toVec3i(entry), 0)
//				println("Removed $it")
			}
		}

		if (blockMap.isEmpty()) entity.disable()
	}

	fun onEnable() {
		loadBlockQueue()
	}

	private var isDisabled: Boolean = false

	fun onDisable() {
		println("Disabled task")
	}

	private fun debugSengBlock(data: BlockData, worldCoordinate: Vec3i, delay: Long) {
		Tasks.syncDelay(delay) {
			sendEntityPacket(
				player,
				displayBlock(player.world.minecraft, getRotatedBlockData(data), worldCoordinate.toCenterVector(), 0.75f, false),
				30 * 20L
			)
		}
	}
}
