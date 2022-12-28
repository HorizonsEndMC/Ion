package net.horizonsend.ion.server.generation.populators

import net.horizonsend.ion.server.generation.configuration.AsteroidConfiguration
import net.horizonsend.ion.server.generation.configuration.Ore
import net.minecraft.core.BlockPos
import org.bukkit.Bukkit.createBlockData
import org.bukkit.Material
import org.bukkit.block.data.BlockData
import org.bukkit.generator.BlockPopulator
import org.bukkit.generator.LimitedRegion
import org.bukkit.generator.WorldInfo
import java.util.Random
import net.horizonsend.ion.common.loadConfiguration
import net.horizonsend.ion.server.IonServer

open class OrePopulator : BlockPopulator() {
    // default asteroid configuration values
    private val configuration: AsteroidConfiguration =
        loadConfiguration(IonServer.Ion.dataFolder.resolve("asteroids"), "asteroid_configuration.conf")

    companion object {
        val asteroidBlocks: MutableSet<Material> = mutableSetOf()
        val oreMap: MutableMap<String, BlockData> = mutableMapOf()

        fun getSphereBlocks(radius: Int, origin: BlockPos): List<BlockPos> {
            if (radius == 1) return listOf(origin) // bypass the rest of this if it's useless

            val circleBlocks = mutableListOf<BlockPos>()
            val upperBoundSquared = radius * radius

            for (x in origin.x - radius..origin.x + radius) {
                for (y in origin.y - radius..origin.y + radius) {
                    for (z in origin.z - radius..origin.z + radius) {
                        val distance =
                            ((origin.x - x) * (origin.x - x) + (origin.z - z) * (origin.z - z) + (origin.y - y) * (origin.y - y)).toDouble()

                        if (distance < upperBoundSquared) {
                            circleBlocks.add(BlockPos(x, y, z))
                        }
                    }
                }
            }

            return circleBlocks
        }
    }

    private val weightedOres = oreWeights()

    init {
        configuration.blockPalettes.forEach { asteroidBlocks.addAll((it.materials.keys)) }

        configuration.ores.forEach { oreMap[it.material] = createBlockData(it.material) }
    }

    override fun populate(
		worldInfo: WorldInfo,
		random: Random,
		chunkX: Int,
		chunkZ: Int,
		limitedRegion: LimitedRegion,
	) {
        val worldX = chunkX * 16
        val worldZ = chunkZ * 16

        if (weightedOres.isEmpty()) return

        for (count in configuration.orePlacementsPerChunk downTo 0) {
            val origin = BlockPos(
                random.nextInt(worldX, worldX + 16),
                random.nextInt(worldInfo.minHeight + 10, worldInfo.maxHeight - 10),
                random.nextInt(worldZ, worldZ + 16)
            )

            if (!asteroidBlocks.contains(limitedRegion.getType(origin.x,
                    origin.y,
                    origin.z))
            ) {
                continue
            } // Quickly move on if it's not in an asteroid

            val ore = weightedOres[random.nextInt(0, weightedOres.size - 1)]

            val blobSize = random.nextInt(0, ore.maxBlobSize).coerceAtLeast(1)

            val oreBlocks = getSphereBlocks(blobSize, origin = origin)

			for (block in oreBlocks) {
                if (!limitedRegion.isInRegion(block.x, block.y, block.z)) continue

                if (!asteroidBlocks.contains(limitedRegion.getType(block.x,
                        block.y,
                        block.z))
                ) continue

                oreMap[ore.material]?.let { limitedRegion.setBlockData(block.x, block.y, block.z, it) }
			}

            storeOreBlob(ore, origin)
        }
    }

	private fun oreWeights(): List<Ore> {
        val weightedList = mutableListOf<Ore>()

        for (ore in configuration.ores) {
            for (occurrence in ore.rolls downTo 0) {
                weightedList.add(ore)
            }
        }

        return weightedList
    }

    private fun storeOreBlob(ore: Ore, origin: BlockPos) {
        val list = listOf<PlacedOre>()
    }

    data class PlacedOre(
        val material: BlockData,
        val blobSize: Int,
        val location: BlockPos
    )
}