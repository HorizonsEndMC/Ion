package net.horizonsend.ion.server.listener.fixers

import net.horizonsend.ion.server.IonServer
import net.horizonsend.ion.server.miscellaneous.registrations.NamespacedKeys
import net.horizonsend.ion.server.listener.SLEventListener
import org.bukkit.Bukkit
import org.bukkit.block.Biome
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.world.ChunkLoadEvent
import org.bukkit.persistence.PersistentDataType

class BiomeFixer9001 : SLEventListener() {
	val needsFixing = listOf("Rubaciea", "Aret")

	@EventHandler(priority = EventPriority.MONITOR)
	fun onChunkLoad(event: ChunkLoadEvent) {
		if (!needsFixing.contains(event.world.name)) return

		val chunkOreVersion = event.chunk.persistentDataContainer.get(NamespacedKeys.BIOME_FIX, PersistentDataType.BYTE)
		if (chunkOreVersion == 2.toByte()) return

		val chunkSnapshot = event.chunk.getChunkSnapshot(true, true, false)
		Bukkit.getScheduler().runTaskAsynchronously(
			IonServer,
			Runnable {
				val startY = 256
				val sampleY = startY - 1

				// Only need columns
				val biomePoints = mutableMapOf<Pair<Int, Int>, Biome>()

				for (x in 0..15) for (z in 0..15) {
					val biome = chunkSnapshot.getBiome(x, sampleY, z)

					biomePoints[x to z] = biome
				}

				// Sync
				Bukkit.getScheduler().runTask(
					IonServer,
					Runnable {
						for ((column, biome) in biomePoints) {
							val (x, z) = column

							val absoluteX = event.chunk.x.shl(4) + x
							val absoluteZ = event.chunk.z.shl(4) + z

							for (y in startY until event.world.maxHeight) {
								event.world.setBiome(absoluteX, y, absoluteZ, biome)
							}
						}

						event.chunk.persistentDataContainer.set(
							NamespacedKeys.BIOME_FIX,
							PersistentDataType.BYTE,
							2
						)
					}
				)
			}
		)
	}
}
