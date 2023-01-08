package net.horizonsend.ion.server.generation.generators

import net.horizonsend.ion.common.loadConfiguration
import net.horizonsend.ion.server.IonServer
import net.horizonsend.ion.server.generation.PlacedOre
import net.horizonsend.ion.server.generation.configuration.AsteroidConfiguration
import net.horizonsend.ion.server.generation.configuration.Ore
import net.minecraft.util.RandomSource
import org.bukkit.Chunk
import org.bukkit.Material
import org.bukkit.World
import org.bukkit.block.data.BlockData

object OreGenerator {
	// default asteroid configuration values
	private val configuration: AsteroidConfiguration =
		loadConfiguration(IonServer.Ion.dataFolder.resolve("asteroids"), "asteroid_configuration.conf")

	private val weightedOres = oreWeights()
	private val asteroidBlocks: MutableSet<Material> = mutableSetOf()
	private val oreMap: MutableMap<String, BlockData> = mutableMapOf()

	fun generateOres(world: World, chunk: Chunk): List<PlacedOre> {
		val worldX = chunk.x * 16
		val worldZ = chunk.z * 16

		if (weightedOres.isEmpty()) return listOf()

		val random: RandomSource = RandomSource.create(world.seed)

		val ores = mutableListOf<PlacedOre>()

		for (count in configuration.orePlacementsPerChunk downTo 0) {
			val originX = random.nextInt(worldX, worldX + 16)
			val originY = random.nextInt(world.minHeight + 10, world.maxHeight - 10)
			val originZ = random.nextInt(worldZ, worldZ + 16)

			if (!asteroidBlocks.contains(world.getBlockAt(originX, originY, originZ).type)) {
				continue
			}
			// Quickly move on if it's not in an asteroid

			val ore = weightedOres[random.nextInt(0, weightedOres.size - 1)]

			val blobSize = random.nextInt(0, ore.maxBlobSize).coerceAtLeast(1)

			val oreBlocks = getSphereBlocks(blobSize, origin = Triple(originX, originY, originZ))

			for (block in oreBlocks) {
				val (x, y, z) = block

				if (!asteroidBlocks.contains<Material>(world.getBlockAt(x, y, z).type)
				) {
					continue
				}

				oreMap[ore.material]?.let { world.setBlockData(x, y, z, it) }
			}

			ores += oreMap[ore.material]?.let { PlacedOre(it, ore.maxBlobSize, originX, originY, originZ) } ?: continue
		}

		return ores
	}

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

	private fun oreWeights(): List<Ore> {
		val weightedList = mutableListOf<Ore>()

		for (ore in configuration.ores) {
			for (occurrence in ore.rolls downTo 0) {
				weightedList.add(ore)
			}
		}

		return weightedList
	}
}
