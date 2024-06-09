package net.horizonsend.ion.server.features.custom.items.mods.tool.drops

import net.horizonsend.ion.common.utils.text.colors.HEColorScheme.Companion.HE_LIGHT_BLUE
import net.horizonsend.ion.common.utils.text.colors.HEColorScheme.Companion.HE_LIGHT_GRAY
import net.horizonsend.ion.common.utils.text.ofChildren
import net.horizonsend.ion.server.features.custom.blocks.CustomBlock
import net.horizonsend.ion.server.features.custom.items.CustomItem
import net.horizonsend.ion.server.features.custom.items.mods.ItemModification
import net.horizonsend.ion.server.features.custom.items.mods.tool.PowerUsageIncrease
import net.horizonsend.ion.server.features.custom.items.powered.PowerDrill
import net.horizonsend.ion.server.miscellaneous.utils.updateMeta
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.Component.text
import org.bukkit.Material
import org.bukkit.block.Block
import org.bukkit.enchantments.Enchantment
import org.bukkit.inventory.ItemStack
import kotlin.reflect.KClass

object SilkTouchModifier : ItemModification, DropModifier, PowerUsageIncrease {
	override val displayName: Component = ofChildren(text("Silk Touch ", HE_LIGHT_BLUE), text("Modifier", HE_LIGHT_GRAY))
	override val identifier: String = "SILK_TOUCH"
	override val applicableTo: Array<KClass<out CustomItem>> = arrayOf(PowerDrill::class)
	override val incompatibleWithMods: Array<KClass<out ItemModification>> = arrayOf(FortuneModifier::class, SilkTouchModifier::class)
	override val shouldDropXP: Boolean = false
	override val usageMultiplier: Double = 2.0

	override fun getDrop(block: Block): Collection<ItemStack> {
		return block.getDrops(silkPick)
	}

	override fun getDrop(block: CustomBlock): Collection<ItemStack> {
		return block.drops.getDrops(silkPick, true)
	}

	private val silkPick = ItemStack(Material.DIAMOND_PICKAXE).updateMeta {
		it.addEnchant(Enchantment.SILK_TOUCH, 1, true)
	}

	override val usedTool: ItemStack = silkPick
}