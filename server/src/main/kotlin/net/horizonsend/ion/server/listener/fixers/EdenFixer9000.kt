package net.horizonsend.ion.server.listener.fixers

import net.horizonsend.ion.server.IonServer
import net.horizonsend.ion.server.listener.SLEventListener
import net.horizonsend.ion.server.miscellaneous.registrations.NamespacedKeys
import net.minecraft.core.BlockPos
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.world.ChunkLoadEvent
import org.bukkit.persistence.PersistentDataType

class EdenFixer9000 : SLEventListener() {
	private val airBlockData = Material.AIR.createBlockData()
	var runningTotal = 0

	@EventHandler(priority = EventPriority.MONITOR)
	fun onChunkLoad(event: ChunkLoadEvent) {
		if (!event.chunk.world.name.lowercase().contains("eden")) return

		val chunkOreVersion = event.chunk.persistentDataContainer.get(NamespacedKeys.EDEN_FIX, PersistentDataType.BYTE)
		if (chunkOreVersion == 2.toByte()) return

		val evilBlocks = setOf(Material.SCULK_SENSOR, Material.SCULK_CATALYST, Material.SCULK_SHRIEKER)

		val chunkSnapshot = event.chunk.getChunkSnapshot(true, false, false)
		Bukkit.getScheduler().runTaskAsynchronously(
			IonServer,
			Runnable {
				val removedBlocks = mutableListOf<BlockPos>() // Everything

				for (x in 0..15) for (z in 0..15) {
					val minBlockY = event.chunk.world.minHeight
					val maxBlockY = chunkSnapshot.getHighestBlockYAt(x, z)

					for (y in minBlockY..maxBlockY) {
						val blockData = chunkSnapshot.getBlockData(x, y, z)
						if (!evilBlocks.contains(blockData.material)) continue

						removedBlocks += BlockPos(x, y, z); runningTotal++
					}
				}

				Bukkit.getScheduler().runTask(
					IonServer,
					Runnable {
						removedBlocks.forEach { position ->
							event.chunk.getBlock(position.x, position.y, position.z).setBlockData(airBlockData, false)
						}

						IonServer.slF4JLogger.info(
							"Removed ${removedBlocks.size} evil blocks form chunk (${event.chunk.x},${event.chunk.z}) on Eden for a total of $runningTotal this session."
						)

						event.chunk.persistentDataContainer.set(
							NamespacedKeys.EDEN_FIX,
							PersistentDataType.BYTE,
							2
						)
					}
				)
			}
		)
	}
}
