package net.horizonsend.ion.server.features.custom.items.type.tool.mods.drops

import io.papermc.paper.datacomponent.DataComponentTypes
import io.papermc.paper.datacomponent.item.ItemEnchantments
import net.horizonsend.ion.common.utils.text.BOLD
import net.horizonsend.ion.common.utils.text.colors.HEColorScheme.Companion.HE_LIGHT_BLUE
import net.horizonsend.ion.common.utils.text.ofChildren
import net.horizonsend.ion.server.features.custom.blocks.CustomBlock
import net.horizonsend.ion.server.features.custom.items.CustomItemRegistry
import net.horizonsend.ion.server.features.custom.items.attribute.AdditionalPowerConsumption
import net.horizonsend.ion.server.features.custom.items.attribute.CustomItemAttribute
import net.horizonsend.ion.server.features.custom.items.type.tool.PowerChainsaw
import net.horizonsend.ion.server.features.custom.items.type.tool.PowerDrill
import net.horizonsend.ion.server.features.custom.items.type.tool.mods.ApplicationPredicate
import net.horizonsend.ion.server.features.custom.items.type.tool.mods.ItemModification
import net.horizonsend.ion.server.features.custom.items.type.tool.mods.ModificationItem
import net.horizonsend.ion.server.miscellaneous.utils.updateData
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.format.TextDecoration
import org.bukkit.Material
import org.bukkit.block.Block
import org.bukkit.enchantments.Enchantment
import org.bukkit.inventory.ItemStack
import java.util.function.Supplier
import kotlin.reflect.KClass

object SilkTouchSource : ItemModification, DropSource {
	override val crouchingDisables: Boolean = false
	override val displayName: Component = ofChildren(text("Silk Touch ", HE_LIGHT_BLUE, BOLD).decoration(TextDecoration.ITALIC, false))
	override val identifier: String = "SILK_TOUCH"

	override val applicationPredicates: Array<ApplicationPredicate> = arrayOf(
		ApplicationPredicate.ClassPredicate(PowerDrill::class),
		ApplicationPredicate.ClassPredicate(PowerChainsaw::class)
	)

	override val incompatibleWithMods: Array<KClass<out ItemModification>> = arrayOf(FortuneModifier::class, SilkTouchSource::class)
	override val shouldDropXP: Boolean = false
	override val modItem: Supplier<ModificationItem?> = Supplier { CustomItemRegistry.SILK_TOUCH_MOD }

	override fun getDrop(block: Block): Collection<ItemStack> {
		return block.getDrops(silkPick)
	}

	override fun getDrop(block: CustomBlock): Collection<ItemStack> {
		return block.drops.getDrops(silkPick, true)
	}

	private val silkPick = ItemStack(Material.DIAMOND_PICKAXE)
		.updateData(DataComponentTypes.ENCHANTMENTS, ItemEnchantments.itemEnchantments(mutableMapOf(Enchantment.SILK_TOUCH to 1), true))

	override val usedTool: ItemStack = silkPick

	override fun getAttributes(): List<CustomItemAttribute> = listOf(AdditionalPowerConsumption(1.25))
}
