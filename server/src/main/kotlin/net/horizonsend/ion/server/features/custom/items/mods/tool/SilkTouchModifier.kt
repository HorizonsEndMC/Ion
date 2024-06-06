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

object SilkTouchModifier : ItemModification, DropModifier {
	override val identifier: String = "SILK_TOUCH"
	override val applicableTo: Array<KClass<out CustomItem>> = arrayOf(PowerDrill::class)
	override val incompatibleWithMods: Array<KClass<out ItemModification>> = arrayOf(FortuneModifier::class, SilkTouchModifier::class)
	override val shouldDropXP: Boolean = false

	override fun getDrop(block: Block): Collection<ItemStack> {
		return block.getDrops(silkPick)
	}

	private val silkPick = ItemStack(Material.DIAMOND_PICKAXE).updateMeta {
		it.addEnchant(Enchantment.SILK_TOUCH, 1, true)
	}

	override val usedTool: ItemStack = silkPick
}
