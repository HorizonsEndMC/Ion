package net.horizonsend.ion.server.features.custom.items.mods.tool

import net.horizonsend.ion.server.features.custom.items.CustomItem
import net.horizonsend.ion.server.features.custom.items.mods.ItemModification
import net.horizonsend.ion.server.features.custom.items.powered.PowerDrill
import net.horizonsend.ion.server.miscellaneous.utils.updateMeta
import org.bukkit.Material
import org.bukkit.block.Block
import org.bukkit.enchantments.Enchantment
import org.bukkit.inventory.ItemStack
import kotlin.reflect.KClass

class FortuneModifier(private val level: Int) : ItemModification, DropModifier {
	override val identifier: String = "FORTUNE_$level"
	override val applicableTo: Array<KClass<out CustomItem>> = arrayOf(PowerDrill::class)
	override val incompatibleWithMods: Array<KClass<out ItemModification>> = arrayOf(FortuneModifier::class, SilkTouchModifier::class)
	override val shouldDropXP: Boolean = true

	override fun getDrop(block: Block): Collection<ItemStack> {
		return block.getDrops(fortunePick)
	}

	private val fortunePick = ItemStack(Material.DIAMOND_PICKAXE).updateMeta {
		it.addEnchant(Enchantment.LOOT_BONUS_BLOCKS, level, true)
	}

	override val usedTool: ItemStack = fortunePick
}
