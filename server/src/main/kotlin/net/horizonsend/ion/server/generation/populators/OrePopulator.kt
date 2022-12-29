package net.horizonsend.ion.server.generation.populators

import java.util.Random
import net.horizonsend.ion.common.loadConfiguration
import net.horizonsend.ion.server.IonServer
import net.horizonsend.ion.server.generation.configuration.AsteroidConfiguration
import net.horizonsend.ion.server.generation.configuration.Ore
import org.bukkit.Bukkit.createBlockData
import org.bukkit.Material
import org.bukkit.block.data.BlockData
import org.bukkit.generator.BlockPopulator
import org.bukkit.generator.LimitedRegion
import org.bukkit.generator.WorldInfo

open class OrePopulator : BlockPopulator() {
    // default asteroid configuration values
    private val configuration: AsteroidConfiguration =
        loadConfiguration(IonServer.Ion.dataFolder.resolve("asteroids"), "asteroid_configuration.conf")

    companion object {
        val asteroidBlocks: MutableSet<Material> = mutableSetOf()
        val oreMap: MutableMap<String, BlockData> = mutableMapOf()

        fun getSphereBlocks(radius: Int, origin: Triple<Int, Int, Int>): List<Triple<Int, Int, Int>> {
            if (radius == 1) return listOf(origin) // bypass the rest of this if it's useless

			val (originX, originY, originZ) = origin

            val circleBlocks = mutableListOf<Triple<Int, Int, Int>>()
            val upperBoundSquared = radius * radius

            for (x in originX - radius..originX + radius) {
                for (y in originY - radius..originY + radius) {
                    for (z in originZ - radius..originZ + radius) {
                        val distance =
                            ((originX - x) * (originX - x) + (originX - z) * (originX - z) + (originY - y) * (originY - y)).toDouble()

                        if (distance < upperBoundSquared) {
                            circleBlocks.add(Triple(x, y, z))
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
			val originX = random.nextInt(worldX, worldX + 16)
			val originY = random.nextInt(worldInfo.minHeight + 10, worldInfo.maxHeight - 10)
			val originZ = random.nextInt(worldZ, worldZ + 16)

            if (!asteroidBlocks.contains(limitedRegion.getType(originX, originY, originZ))) { continue }
			// Quickly move on if it's not in an asteroid

            val ore = weightedOres[random.nextInt(0, weightedOres.size - 1)]

            val blobSize = random.nextInt(0, ore.maxBlobSize).coerceAtLeast(1)

            val oreBlocks = getSphereBlocks(blobSize, origin = Triple(originX, originY, originZ))

			for (block in oreBlocks) {
				val (x, y ,z) = block

                if (!limitedRegion.isInRegion(x, y, z)) continue

                if (!asteroidBlocks.contains(limitedRegion.getType(x, y, z))
                ) continue

                oreMap[ore.material]?.let { limitedRegion.setBlockData(x, y, z, it) }
			}

            storeOreBlob(ore, originX, originY, originZ)
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

    private fun storeOreBlob(ore: Ore, x: Int, y: Int, z: Int) {
        val list = listOf<PlacedOre>()
    }

    data class PlacedOre(
        val material: BlockData,
        val blobSize: Int,
		val centerX: Int,
		val centerY: Int,
		val centerZ: Int
    )
}