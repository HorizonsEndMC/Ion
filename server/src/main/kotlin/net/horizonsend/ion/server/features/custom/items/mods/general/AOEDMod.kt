package net.horizonsend.ion.server.features.custom.items.mods.general

import net.horizonsend.ion.common.utils.text.colors.HEColorScheme
import net.horizonsend.ion.common.utils.text.ofChildren
import net.horizonsend.ion.server.features.custom.items.CustomItem
import net.horizonsend.ion.server.features.custom.items.mods.ItemModification
import net.horizonsend.ion.server.features.custom.items.mods.ModificationItem
import net.horizonsend.ion.server.features.custom.items.mods.tool.BlockListModifier
import net.horizonsend.ion.server.features.custom.items.mods.tool.drill.VeinMinerMod
import net.horizonsend.ion.server.features.custom.items.powered.PowerDrill
import net.horizonsend.ion.server.features.custom.items.powered.PowerHoe
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextDecoration
import org.bukkit.block.Block
import org.bukkit.block.BlockFace
import java.util.function.Supplier
import kotlin.reflect.KClass

class AOEDMod(
	val radius: Int,
	override val applicableTo: Array<KClass<out CustomItem>> = arrayOf(PowerDrill::class, PowerHoe::class),
	override val modItem: Supplier<ModificationItem?>,
) : ItemModification, BlockListModifier {
	override val identifier: String = "AOE_$radius"

	override val crouchingDisables: Boolean = true

	override val displayName: Component = ofChildren(
		text(1 + (2 * radius), HEColorScheme.HE_LIGHT_ORANGE).decoration(TextDecoration.ITALIC, false),
		text("×", HEColorScheme.HE_MEDIUM_GRAY).decoration(TextDecoration.ITALIC, false),
		text(1 + (2 * radius), HEColorScheme.HE_LIGHT_ORANGE).decoration(TextDecoration.ITALIC, false),
		text(" Range", NamedTextColor.GRAY).decoration(TextDecoration.ITALIC, false)
	)

	override val incompatibleWithMods: Array<KClass<out ItemModification>> = arrayOf(
		AOEDMod::class,
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
