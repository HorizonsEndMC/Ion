package net.horizonsend.ion.server.features.custom.items.mods.tool.drill

import net.horizonsend.ion.common.utils.text.colors.HEColorScheme
import net.horizonsend.ion.common.utils.text.ofChildren
import net.horizonsend.ion.server.features.custom.items.mods.ItemModification
import net.horizonsend.ion.server.features.custom.items.mods.tool.BlockListModifier
import net.horizonsend.ion.server.miscellaneous.utils.ADJACENT_BLOCK_FACES
import net.horizonsend.ion.server.miscellaneous.utils.getBlockIfLoaded
import net.horizonsend.ion.server.miscellaneous.utils.getTypeSafe
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.Component.text
import net.minecraft.core.BlockPos
import org.bukkit.Material
import org.bukkit.block.Block
import org.bukkit.block.BlockFace
import java.util.ArrayDeque
import java.util.concurrent.TimeUnit
import kotlin.reflect.KClass

class VeinMinerMod(
	val depth: Int,
	override val identifier: String = "DRILL_VEIN_$depth",
) : DrillModification(), BlockListModifier {
	override val displayName: Component = ofChildren(text("Vein Miner ", HEColorScheme.HE_LIGHT_ORANGE), Component.text("Modification", HEColorScheme.HE_LIGHT_GRAY))
	override val incompatibleWithMods: Array<KClass<out ItemModification>> = arrayOf(
		AOEDrillMod::class,
		VeinMinerMod::class
	)

	override val priority: Int = 1

	override fun modifyBlockList(interactedSide: BlockFace, origin: Block, list: MutableList<Block>) {
//		val originOre = Ore[origin.blockData] ?: return
		val originType = origin.getTypeSafe() ?: return

		list.clear()

		val neighborList = mutableListOf<Block>()

		// blocks that are pending checking
		val queue = ArrayDeque<Long>()

		// blocks that were already checked and should not be detected twice
		val visited = mutableSetOf<Long>()

		// Jumpstart the queue by adding the origin block
		val originKey = BlockPos.asLong(origin.x, origin.y, origin.z)
		visited.add(originKey)
		queue.push(originKey)

		val start = System.nanoTime()

		while (!queue.isEmpty()) {
			// Took too long; assume that the object is too large
			if (System.nanoTime() - start > TimeUnit.SECONDS.toNanos(30)) break

			if (neighborList.count() > depth) break

			val key = queue.pop()
			val x = BlockPos.getX(key)
			val y = BlockPos.getY(key)
			val z = BlockPos.getZ(key)

			// Do not allow checking blocks larger than render distance.
			// The type being null usually means the chunk is unloaded.
			val block = getBlockIfLoaded(origin.world, x, y, z) ?: break
			val material = block.getTypeSafe() ?: break

			if (material == Material.AIR || material == Material.VOID_AIR || material == Material.CAVE_AIR) continue

			if (material != originType) continue

			val data = block.blockData
//			if (Ore[data] != originOre) continue

			// Add the location to the list of blocks that'll be set on the starships
			neighborList.add(block)

			// Detect adjacent blocks
			for (offset in ADJACENT_BLOCK_FACES.shuffled()) {
				val adjacentX = offset.modX + x
				val adjacentY = offset.modY + y
				val adjacentZ = offset.modZ + z

				// Ensure it's a valid Y-level before adding it to the queue
				if (adjacentY < 0 || adjacentY > origin.world.maxHeight) {
					continue
				}

				val key1 = BlockPos.asLong(adjacentX, adjacentY, adjacentZ)
				// Ignore active starships (prevents self-detection)
				if (visited.add(key1)) {
					queue.push(key1)
				}
			}
		}

		list.addAll(neighborList)
	}
}
