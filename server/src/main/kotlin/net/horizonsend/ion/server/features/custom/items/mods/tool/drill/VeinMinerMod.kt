package net.horizonsend.ion.server.features.custom.items.mods.tool.drill

import net.horizonsend.ion.common.utils.text.colors.HEColorScheme.Companion.HE_LIGHT_ORANGE
import net.horizonsend.ion.common.utils.text.ofChildren
import net.horizonsend.ion.server.features.custom.items.attribute.CustomItemAttribute
import net.horizonsend.ion.server.features.custom.items.mods.ItemModification
import net.horizonsend.ion.server.features.custom.items.mods.ModificationItem
import net.horizonsend.ion.server.features.custom.items.mods.general.AOEDMod
import net.horizonsend.ion.server.features.custom.items.mods.tool.BlockListModifier
import net.horizonsend.ion.server.miscellaneous.utils.ADJACENT_BLOCK_FACES
import net.horizonsend.ion.server.miscellaneous.utils.getBlockDataSafe
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.format.NamedTextColor.GRAY
import net.kyori.adventure.text.format.TextDecoration
import net.minecraft.core.BlockPos
import org.bukkit.block.Block
import org.bukkit.block.BlockFace
import java.util.ArrayDeque
import java.util.concurrent.TimeUnit
import java.util.function.Supplier
import kotlin.reflect.KClass

class VeinMinerMod(
	val depth: Int,
	override val identifier: String = "DRILL_VEIN_$depth",
	override val modItem: Supplier<ModificationItem?>
) : DrillModification(), BlockListModifier {
	override val displayName: Component = ofChildren(text("Vein ", HE_LIGHT_ORANGE), text("Mining", GRAY)).decoration(TextDecoration.ITALIC, false)

	override val incompatibleWithMods: Array<KClass<out ItemModification>> = arrayOf(
		AOEDMod::class,
		VeinMinerMod::class
	)

	override val priority: Int = 1

	override fun modifyBlockList(interactedSide: BlockFace, origin: Block, list: MutableList<Block>) {
		val originType = origin.blockData

		list.clear()

		// blocks that are pending checking
		val queue = ArrayDeque<Long>()

		// blocks that were already checked and should not be detected twice
		val visited = mutableMapOf<Long, Block>()

		// Jumpstart the queue by adding the origin block
		val originKey = BlockPos.asLong(origin.x, origin.y, origin.z)
		visited[originKey] = origin
		queue.push(originKey)

		val start = System.nanoTime()

		while (!queue.isEmpty()) {
			// Took too long; assume that the object is too large
			if (System.nanoTime() - start > TimeUnit.SECONDS.toNanos(1)) break

			if (visited.count() > depth) break

			val key = queue.removeFirst()
			val x = BlockPos.getX(key)
			val y = BlockPos.getY(key)
			val z = BlockPos.getZ(key)

			// Do not allow checking blocks larger than render distance
			val block = origin.world.getBlockAt(x, y, z)
			val blockData = getBlockDataSafe(origin.world, x, y, z) ?: break

			if (blockData != originType) continue

			visited[key] = block

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

				if (visited.containsKey(key1)) continue

				queue.addLast(key1)
			}
		}

		list.addAll(visited.values)
	}

	override fun getAttributes(): List<CustomItemAttribute> = listOf()
}
