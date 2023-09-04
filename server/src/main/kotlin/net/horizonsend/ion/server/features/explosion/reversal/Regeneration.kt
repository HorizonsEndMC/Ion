package net.horizonsend.ion.server.features.explosion.reversal

import net.horizonsend.ion.server.IonServer
import net.horizonsend.ion.server.features.explosion.reversal.ExplosionReversal.setTileEntity
import org.bukkit.Bukkit
import org.bukkit.World

object Regeneration {
	private val settings get() = IonServer.configuration.explosionRegenConfig

	fun pulse() {
		regenerateBlocks()
	}

	fun regenerateBlocks(instant: Boolean = false): Int {
		println("regenning")
		val millisecondDelay: Long = settings.regenDelay.times( 60L * 1000L).toLong()
		val maxNanos: Long = settings.placementIntensity.times( 1000000L).toLong()

		val start = System.nanoTime()
		var regenerated = 0

		for (world in Bukkit.getWorlds()) {
			val blocks = ExplosionReversal.worldData?.getBlocks(world) ?: return 0
			val iterator = blocks.iterator()

			println(blocks)

			while (iterator.hasNext()) {
				val data = iterator.next()
				println(data)

				if (!instant) {
					if (System.nanoTime() - start > maxNanos) { // i.e. taking too long
						return regenerated;
					}

					if (System.currentTimeMillis() - data.explodedTime < millisecondDelay) {
						continue
					}
				}

				iterator.remove()
				regenerateBlock(world, data)
				regenerated++
			}
		}

		return regenerated
	}

	private fun regenerateBlock(world: World, data: ExplodedBlockData) {
		val block = world.getBlockAt(data.x, data.y, data.z)
		val blockData = data.blockData

		block.setBlockData(blockData, false)

		val tileData = data.tileData

		if (tileData != null) {
			setTileEntity(block, tileData)
		}
	}
}
