package net.horizonsend.ion.server.features.custom.items.mods.drops

import net.horizonsend.ion.common.utils.text.BOLD
import net.horizonsend.ion.common.utils.text.colors.HEColorScheme.Companion.HE_LIGHT_BLUE
import net.horizonsend.ion.common.utils.text.ofChildren
import net.horizonsend.ion.server.features.custom.blocks.CustomBlock
import net.horizonsend.ion.server.features.custom.items.CustomItems
import net.horizonsend.ion.server.features.custom.items.mods.ItemModification
import net.horizonsend.ion.server.features.custom.items.mods.ModificationItem
import net.horizonsend.ion.server.features.custom.items.mods.tool.PowerUsageIncrease
import net.horizonsend.ion.server.features.custom.items.objects.ModdedCustomItem
import net.horizonsend.ion.server.features.custom.items.powered.PowerChainsaw
import net.horizonsend.ion.server.features.custom.items.powered.PowerDrill
import net.horizonsend.ion.server.miscellaneous.utils.updateMeta
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.format.TextDecoration
import org.bukkit.Material
import org.bukkit.block.Block
import org.bukkit.enchantments.Enchantment
import org.bukkit.inventory.ItemStack
import java.util.function.Supplier
import kotlin.reflect.KClass

object SilkTouchSource : ItemModification, DropSource, PowerUsageIncrease {
	override val crouchingDisables: Boolean = false
	override val displayName: Component = ofChildren(text("Silk Touch ", HE_LIGHT_BLUE, BOLD).decoration(TextDecoration.ITALIC, false))
	override val identifier: String = "SILK_TOUCH"
	override val applicableTo: Array<KClass<out ModdedCustomItem>> = arrayOf(PowerDrill::class, PowerChainsaw::class)
	override val incompatibleWithMods: Array<KClass<out ItemModification>> = arrayOf(FortuneModifier::class, SilkTouchSource::class)
	override val shouldDropXP: Boolean = false
	override val usageMultiplier: Double = 1.25
	override val modItem: Supplier<ModificationItem?> = Supplier { CustomItems.SILK_TOUCH_MOD }

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
