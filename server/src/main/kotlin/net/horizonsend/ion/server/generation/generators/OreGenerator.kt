package net.horizonsend.ion.server.generation.generators

import net.horizonsend.ion.server.IonServer
import net.horizonsend.ion.server.NamespacedKeys
import net.horizonsend.ion.server.ServerConfiguration
import net.horizonsend.ion.server.generation.PlacedOre
import net.horizonsend.ion.server.generation.PlacedOres
import net.horizonsend.ion.server.generation.PlacedOresDataType
import net.minecraft.util.RandomSource
import net.minecraft.world.level.chunk.LevelChunkSection
import net.starlegacy.util.nms
import org.bukkit.Bukkit.createBlockData
import org.bukkit.Chunk
import org.bukkit.Material
import org.bukkit.World
import org.bukkit.block.data.BlockData

object OreGenerator {
	val weightedOres = oreWeights()
	val asteroidBlocks: MutableSet<Material> = mutableSetOf()
	private val oreMap: MutableMap<String, BlockData> = mutableMapOf()

	init {
		IonServer.Ion.configuration.blockPalettes.forEach { asteroidBlocks.addAll((it.materials.keys)) }

		IonServer.Ion.configuration.ores.forEach { oreMap[it.material] = createBlockData(it.material) }
	}

	fun generateOres(world: World, chunk: Chunk) {
		val worldX = chunk.x * 16
		val worldZ = chunk.z * 16

		if (weightedOres.isEmpty()) return

		val random: RandomSource = RandomSource.create(world.seed)

		val ores = mutableListOf<PlacedOre>()

		for (count in (IonServer.Ion.configuration.oreRatio * 10000).toInt() downTo 0) {
			val originX = random.nextInt(worldX, worldX + 16)
			val originY = random.nextInt(world.minHeight + 10, world.maxHeight - 10)
			val originZ = random.nextInt(worldZ, worldZ + 16)

			if (!asteroidBlocks.contains(world.getBlockAt(originX, originY, originZ).type)) {
				continue
			}
			// Quickly move on if it's not in an asteroid

			val ore = weightedOres[random.nextInt(0, weightedOres.size - 1)]

			val blobSize = random.nextInt(ore.maxBlobSize).coerceAtLeast(1)

			generateOre(world, PlacedOre(ore.material, blobSize, originX, originY, originZ))

			ores += PlacedOre(ore.material, blobSize, originX, originY, originZ)
		}

		chunk.persistentDataContainer.set(NamespacedKeys.ASTEROIDS_ORES, PlacedOresDataType(), PlacedOres(ores))
	}

	fun generateOre(world: World, ore: PlacedOre) {
		val oreBlocks = getSphereBlocks(ore.blobSize, Triple(ore.x, ore.y, ore.z))
		val mappedOre = oreMap[ore.material]?.nms

		var nmsChunk = world.getChunkAt(ore.x.shr(4), ore.z.shr(4)).nms
		var section: LevelChunkSection

		for (block in oreBlocks) {
			val (x, y, z) = block
			val chunkX = x.shr(4)
			val chunkZ = z.shr(4)

			if (nmsChunk.locX != chunkX || nmsChunk.locZ != chunkZ) {
				nmsChunk = world.getChunkAt(chunkX, chunkZ).nms
			}

			// shouldn't go negative with this scheme
			section = try {
				nmsChunk.sections[
					(y + world.minHeight)
						.coerceAtLeast(0)
						.coerceAtMost(world.maxHeight - 1)
						.shr(4)
				]
			} catch (e: java.lang.Exception) {
				e.printStackTrace(); continue
			}

			if (!asteroidBlocks.contains<Material>(world.getBlockAt(x, y, z).type)) continue

			mappedOre?.let {
				section.setBlockState(
					x - chunkX.shl(4),
					y - section.bottomBlockY(),
					z - chunkZ.shl(4),
					it
				)
			}
			nmsChunk.playerChunk?.broadcastChanges(nmsChunk)
		}
	}

	private fun getSphereBlocks(radius: Int, origin: Triple<Int, Int, Int>): List<Triple<Int, Int, Int>> {
		if (radius == 1) return listOf(origin) // bypass the rest of this if it's useless

		val (originX, originY, originZ) = origin

		val circleBlocks = mutableListOf<Triple<Int, Int, Int>>()
		val upperBoundSquared = radius * radius

		for (x in originX - radius..originX + radius) {
			for (z in originZ - radius..originZ + radius) {
				for (y in originY - radius..originY + radius) {
					val distance =
						((x - originX) * (x - originX) + (y - originY) * (y - originY) + (z - originZ) * (z - originZ))

					if (distance < upperBoundSquared) {
						circleBlocks.add(Triple(x, y, z))
					}
				}
			}
		}

		return circleBlocks
	}

	private fun oreWeights(): List<ServerConfiguration.Ore> {
		val weightedList = mutableListOf<ServerConfiguration.Ore>()

		for (ore in IonServer.Ion.configuration.ores) {
			for (occurrence in ore.rolls downTo 0) {
				weightedList.add(ore)
			}
		}

		return weightedList
	}
}
