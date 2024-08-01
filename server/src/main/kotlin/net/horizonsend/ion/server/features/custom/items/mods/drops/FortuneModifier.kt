package net.horizonsend.ion.server.features.custom.items.mods.drops

import net.horizonsend.ion.common.utils.text.BOLD
import net.horizonsend.ion.server.features.custom.blocks.CustomBlock
import net.horizonsend.ion.server.features.custom.items.mods.ItemModification
import net.horizonsend.ion.server.features.custom.items.mods.ModificationItem
import net.horizonsend.ion.server.features.custom.items.mods.tool.PowerUsageIncrease
import net.horizonsend.ion.server.features.custom.items.objects.ModdedCustomItem
import net.horizonsend.ion.server.features.custom.items.powered.PowerChainsaw
import net.horizonsend.ion.server.features.custom.items.powered.PowerDrill
import net.horizonsend.ion.server.features.custom.items.powered.PowerHoe
import net.horizonsend.ion.server.miscellaneous.utils.updateMeta
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.format.TextColor
import net.kyori.adventure.text.format.TextDecoration
import org.bukkit.Material
import org.bukkit.block.Block
import org.bukkit.enchantments.Enchantment
import org.bukkit.inventory.ItemStack
import java.util.function.Supplier
import kotlin.reflect.KClass

class FortuneModifier(
	private val level: Int,
	color: String,
	override val modItem: Supplier<ModificationItem?>
) : ItemModification, DropSource, PowerUsageIncrease {
	override val crouchingDisables: Boolean = false
	override val identifier: String = "FORTUNE_$level"
	override val applicableTo: Array<KClass<out ModdedCustomItem>> = arrayOf(PowerDrill::class, PowerChainsaw::class, PowerHoe::class)
	override val incompatibleWithMods: Array<KClass<out ItemModification>> = arrayOf(FortuneModifier::class, SilkTouchSource::class)
	override val shouldDropXP: Boolean = true
	override val usageMultiplier: Double = 0.25 + level

	override val displayName: Component = text("Fortune $level", TextColor.fromHexString(color)!!, BOLD).decoration(TextDecoration.ITALIC, false)

	override fun getDrop(block: Block): Collection<ItemStack> {
		return block.getDrops(fortunePick)
	}

	override fun getDrop(block: CustomBlock): Collection<ItemStack> {
		return block.drops.getDrops(fortunePick, false)
	}

	private val fortunePick = ItemStack(Material.DIAMOND_PICKAXE).updateMeta {
		it.addEnchant(Enchantment.LOOT_BONUS_BLOCKS, level, true)
	}

	override val usedTool: ItemStack = fortunePick
}
