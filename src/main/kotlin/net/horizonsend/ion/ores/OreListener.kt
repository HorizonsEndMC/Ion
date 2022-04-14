package net.horizonsend.ion.ores

import kotlin.random.Random
import net.horizonsend.ion.Ion
import net.horizonsend.ion.ores.OrePlacementConfig.valueOf
import org.bukkit.Bukkit
import org.bukkit.NamespacedKey
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.world.ChunkLoadEvent
import org.bukkit.persistence.PersistentDataType.INTEGER

internal class OreListener(private val ion: Ion): Listener {
	private val oreCheckNamespace = NamespacedKey(ion, "oreCheck")

	@EventHandler
	fun onChunkLoad(event: ChunkLoadEvent) {
		if (event.chunk.persistentDataContainer.get(oreCheckNamespace, INTEGER) != null) return

		Bukkit.getScheduler().runTaskAsynchronously(ion, Runnable {
			val chunkSnapshot = event.chunk.getChunkSnapshot(true, false, false)
			val placementConfig = try { valueOf(event.world.name) } catch (exception: IllegalArgumentException) { return@Runnable }
			val random = Random(event.chunk.chunkKey)

			val placedOres = mutableMapOf<Triple<Int, Int, Int>, Ore>()

			for (x in 0 .. 15) {
				for (z in 0 .. 15) {
					for (y in event.world.minHeight .. chunkSnapshot.getHighestBlockYAt(x, z)) {
						if (!placementConfig.groundMaterial.contains(chunkSnapshot.getBlockType(x, y, z))) continue

						placementConfig.options.forEach { (ore, chance) ->
							if (random.nextDouble(0.0, 100.0) > chance) return@forEach
							placedOres[Triple(x, y, z)] = ore
						}
					}
				}
			}

			Bukkit.getScheduler().runTask(ion, Runnable {
				placedOres.forEach { (position, ore) ->
					event.chunk.getBlock(position.first, position.second, position.third).setBlockData(ore.blockData, false)
				}

				event.chunk.persistentDataContainer.set(oreCheckNamespace, INTEGER, 1)
			})

			if (placedOres.isEmpty()) return@Runnable

			ion.dataFolder.resolve("ores/1/${event.world.name}")
				.apply { mkdirs() }
				.resolve("${chunkSnapshot.x}_${chunkSnapshot.z}.ores.csv")
				.writeText(placedOres.map {
					"${it.key.first},${it.key.second},${it.key.third},${chunkSnapshot.getBlockType(it.key.first, it.key.second, it.key.third)},${it.value}"
				}.joinToString("\n","",""))
		})
	}
}