package net.horizonsend.ion.server.features.custom.items.type.tool.mods.tool.hoe

import net.horizonsend.ion.common.utils.text.ITALIC
import net.horizonsend.ion.server.core.registration.IonRegistryKey
import net.horizonsend.ion.server.core.registration.keys.CustomItemKeys
import net.horizonsend.ion.server.core.registration.keys.ItemModKeys
import net.horizonsend.ion.server.features.custom.items.CustomItem
import net.horizonsend.ion.server.features.custom.items.attribute.CustomItemAttribute
import net.horizonsend.ion.server.features.custom.items.type.tool.PowerHoe
import net.horizonsend.ion.server.features.custom.items.type.tool.mods.ApplicationPredicate
import net.horizonsend.ion.server.features.custom.items.type.tool.mods.ItemModification
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.format.NamedTextColor.DARK_GREEN
import org.bukkit.Material
import org.bukkit.block.Block
import org.bukkit.block.BlockFace
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import kotlin.reflect.KClass

object FertilizerDispenser : ItemModification {
	override val key = ItemModKeys.FERTILIZER_DISPENSER
	override val displayName: Component = text("Fertilizer Sprayer", DARK_GREEN).decoration(ITALIC, false)

	override val applicationPredicates: Array<ApplicationPredicate> = arrayOf(ApplicationPredicate.ClassPredicate(PowerHoe::class))
	override val incompatibleWithMods: Array<KClass<out ItemModification>> = arrayOf()

	override val modItem: IonRegistryKey<CustomItem, out CustomItem> = CustomItemKeys.TOOL_MODIFICATION_FERTILIZER_DISPENSER

	override val crouchingDisables: Boolean = true

	fun fertilizeCrop(player: Player, block: Block): Boolean {
		if (!player.inventory.contains(Material.BONE_MEAL)) return false

		val result = block.applyBoneMeal(BlockFace.DOWN)

		if (result) {
			player.inventory.removeItemAnySlot(ItemStack(Material.BONE_MEAL))
		}

		return result
	}

	override fun getAttributes(): List<CustomItemAttribute> = listOf()
}
