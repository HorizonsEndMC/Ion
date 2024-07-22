package net.horizonsend.ion.server.features.custom.items.powered

import net.horizonsend.ion.common.utils.text.ofChildren
import net.horizonsend.ion.server.features.custom.items.CustomItem
import net.horizonsend.ion.server.features.custom.items.objects.CustomModeledItem
import net.horizonsend.ion.server.features.custom.items.objects.LoreCustomItem
import net.horizonsend.ion.server.features.custom.items.objects.ModdedCustomItem
import net.horizonsend.ion.server.miscellaneous.registrations.NamespacedKeys.CUSTOM_ITEM
import net.horizonsend.ion.server.miscellaneous.utils.enumSetOf
import net.horizonsend.ion.server.miscellaneous.utils.updateMeta
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.format.NamedTextColor.GOLD
import net.kyori.adventure.text.format.NamedTextColor.GRAY
import net.kyori.adventure.text.format.TextDecoration
import org.bukkit.FluidCollisionMode
import org.bukkit.Material
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemFlag
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType.STRING
import java.util.EnumSet

object PowerHoe : CustomItem("POWER_HOE"), ModdedPowerItem, CustomModeledItem {
	val displayName: Component = ofChildren(text("Power ", GOLD), text("Drill", GRAY)).decoration(TextDecoration.ITALIC, false)
	override val basePowerCapacity: Int = 50_000
	override val basePowerUsage: Int = 10

	override val material: Material = Material.DIAMOND_HOE
	override val customModelData: Int = 1
	override val displayDurability: Boolean = true

	override fun constructItemStack(): ItemStack {
		val base = getModeledItem()

		setPower(base, getPowerCapacity(base))

		return base.updateMeta {
			it.displayName(displayName)
			it.persistentDataContainer.set(CUSTOM_ITEM, STRING, identifier)
			it.addItemFlags(ItemFlag.HIDE_ATTRIBUTES)
		}
	}

	override fun getLoreManagers(): List<LoreCustomItem.CustomItemLoreManager> {
		return listOf(
			PoweredItem.PowerLoreManager,
			ModdedCustomItem.ModLoreManager,
		)
	}

	/**
	 * Block types that may erroneously trigger the open menu, instead of the intended use of the blocks
	 **/
	private val ignoreMenuOpen: EnumSet<Material> = enumSetOf(
		Material.WHEAT,
		Material.POTATOES,
		Material.CARROTS,
		Material.BEETROOTS,
		Material.NETHER_WART,
		Material.DIRT,
	)

	override fun handleSecondaryInteract(livingEntity: LivingEntity, itemStack: ItemStack) {
		if (livingEntity !is Player) return

		val type = livingEntity.getTargetBlockExact(4, FluidCollisionMode.NEVER)?.type

		if (!ignoreMenuOpen.contains(type)) return
		if (livingEntity is Player && livingEntity.isSneaking) openMenu(livingEntity, itemStack)
	}
}
