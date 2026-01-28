package net.horizonsend.ion.server.features.custom.items.type.tool.mods.drops

import io.papermc.paper.datacomponent.DataComponentTypes
import io.papermc.paper.datacomponent.item.ItemEnchantments
import io.papermc.paper.datacomponent.item.TooltipDisplay
import net.horizonsend.ion.common.utils.text.BOLD
import net.horizonsend.ion.server.core.registration.IonRegistryKey
import net.horizonsend.ion.server.features.custom.blocks.CustomBlock
import net.horizonsend.ion.server.features.custom.items.CustomItem
import net.horizonsend.ion.server.features.custom.items.attribute.AdditionalPowerConsumption
import net.horizonsend.ion.server.features.custom.items.attribute.CustomItemAttribute
import net.horizonsend.ion.server.features.custom.items.type.tool.PowerChainsaw
import net.horizonsend.ion.server.features.custom.items.type.tool.PowerDrill
import net.horizonsend.ion.server.features.custom.items.type.tool.PowerHoe
import net.horizonsend.ion.server.features.custom.items.type.tool.mods.ApplicationPredicate
import net.horizonsend.ion.server.features.custom.items.type.tool.mods.ItemModification
import net.horizonsend.ion.server.miscellaneous.utils.updateData
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.format.TextColor
import net.kyori.adventure.text.format.TextDecoration
import org.bukkit.Material
import org.bukkit.block.Block
import org.bukkit.enchantments.Enchantment
import org.bukkit.inventory.ItemStack
import kotlin.reflect.KClass

class FortuneModifier(
	override val key: IonRegistryKey<ItemModification, out ItemModification>,
	val level: Int,
	color: String,
	override val modItem: IonRegistryKey<CustomItem, out CustomItem>?,
) : ItemModification, DropSource {
	override val crouchingDisables: Boolean = false
	override val applicationPredicates: Array<ApplicationPredicate> = arrayOf(
		ApplicationPredicate.ClassPredicate(PowerDrill::class),
		ApplicationPredicate.ClassPredicate(PowerHoe::class),
		ApplicationPredicate.ClassPredicate(PowerChainsaw::class)
	)
	override val incompatibleWithMods: Array<KClass<out ItemModification>> = arrayOf(FortuneModifier::class, SilkTouchSource::class)
	override val shouldDropXP: Boolean = true

	override val displayName: Component = text("Fortune $level", TextColor.fromHexString(color)!!, BOLD).decoration(TextDecoration.ITALIC, false)

	override fun getDrop(block: Block): Collection<ItemStack> {
		return block.getDrops(fortunePick)
	}

	override fun getDrop(block: CustomBlock): Collection<ItemStack> {
		return block.drops.getDrops(fortunePick, false)
	}

	private val fortunePick = ItemStack(Material.DIAMOND_PICKAXE)
		.updateData(DataComponentTypes.ENCHANTMENTS, ItemEnchantments.itemEnchantments(mutableMapOf(Enchantment.FORTUNE to level)))
		.updateData(DataComponentTypes.TOOLTIP_DISPLAY, TooltipDisplay.tooltipDisplay().addHiddenComponents(DataComponentTypes.ENCHANTMENTS).build())

	override val usedTool: ItemStack = fortunePick

	override fun getAttributes(): List<CustomItemAttribute> = listOf(AdditionalPowerConsumption(0.25 + level))
}
