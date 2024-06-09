package net.horizonsend.ion.server.features.custom.items.mods.tool.drill

import net.horizonsend.ion.common.utils.text.colors.HEColorScheme
import net.horizonsend.ion.common.utils.text.ofChildren
import net.horizonsend.ion.server.features.custom.items.mods.ItemModification
import net.horizonsend.ion.server.features.custom.items.mods.tool.BlockListModifier
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.Component.text
import org.bukkit.block.Block
import org.bukkit.block.BlockFace
import kotlin.reflect.KClass

class AOEDrillMod(
	val radius: Int,
	override val identifier: String = "DRILL_AOE_$radius",
) : DrillModification(), BlockListModifier {
	override val displayName: Component = ofChildren(text("Drill AOE Level ", HEColorScheme.HE_LIGHT_ORANGE), text(radius, HEColorScheme.HE_LIGHT_GRAY))
	override val incompatibleWithMods: Array<KClass<out ItemModification>> = arrayOf(
		AOEDrillMod::class,
		VeinMinerMod::class
	)

	override val priority: Int = 1

	override fun modifyBlockList(interactedSide: BlockFace, origin: Block, list: MutableList<Block>) {
		list.clear()

		val neighborList = mutableListOf<Block>()

		for (leftRight in -radius..radius) {
			val leftRightBlock = origin.getRelative(interactedSide.oppositeFace.left(), leftRight)

			for (upDown in -radius..radius) {
				val upDownBlock = leftRightBlock.getRelative(interactedSide.oppositeFace.up(), upDown)

				neighborList.add(upDownBlock)
			}
		}

		list.addAll(neighborList)
	}

	private fun BlockFace.left(): BlockFace = when (this) {
		BlockFace.NORTH -> BlockFace.WEST
		BlockFace.WEST -> BlockFace.SOUTH
		BlockFace.SOUTH -> BlockFace.EAST
		BlockFace.EAST -> BlockFace.NORTH
		BlockFace.UP -> BlockFace.WEST
		BlockFace.DOWN -> BlockFace.EAST
		else -> error("Unsupported direction $this")
	}

	private fun BlockFace.up(): BlockFace = when (this) {
		BlockFace.NORTH -> BlockFace.UP
		BlockFace.WEST -> BlockFace.UP
		BlockFace.SOUTH -> BlockFace.UP
		BlockFace.EAST -> BlockFace.UP
		BlockFace.UP -> BlockFace.NORTH
		BlockFace.DOWN -> BlockFace.SOUTH
		else -> error("Unsupported direction $this")
	}
}
