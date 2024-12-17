package net.horizonsend.ion.server.miscellaneous.registrations.legacy

import com.google.common.collect.HashBasedTable
import com.google.common.collect.Table
import io.papermc.paper.datacomponent.DataComponentTypes
import io.papermc.paper.datacomponent.item.Unbreakable
import net.horizonsend.ion.common.utils.text.miniMessage
import net.horizonsend.ion.server.features.gear.ITEM_POWER_PREFIX
import net.horizonsend.ion.server.miscellaneous.utils.set
import net.horizonsend.ion.server.miscellaneous.utils.updateData
import net.horizonsend.ion.server.miscellaneous.utils.updateDisplayName
import net.horizonsend.ion.server.miscellaneous.utils.updateMeta
import org.bukkit.ChatColor
import org.bukkit.ChatColor.BLUE
import org.bukkit.ChatColor.DARK_AQUA
import org.bukkit.ChatColor.DARK_PURPLE
import org.bukkit.ChatColor.GOLD
import org.bukkit.ChatColor.GREEN
import org.bukkit.ChatColor.RED
import org.bukkit.ChatColor.YELLOW
import org.bukkit.Material
import org.bukkit.Material.SHIELD
import org.bukkit.inventory.ItemStack

open class CustomItem(
	val id: String,
	private val displayNameRaw: String,
	val material: Material,
	val model: Int,
	val unbreakable: Boolean,
	val useMiniMessage: Boolean = false
) {
	val displayName = "${ChatColor.RESET}$displayNameRaw"

	open fun itemStack(amount: Int): ItemStack {
		val base = ItemStack(material, amount)
		base.updateData(DataComponentTypes.UNBREAKABLE, Unbreakable.unbreakable(false))
		base.updateMeta { it.setCustomModelData(model) }

		if (useMiniMessage) base.updateDisplayName(miniMessage.deserialize(displayNameRaw)) else base.updateDisplayName(displayName)

		return base
	}

	fun singleItem() = itemStack(1)

	override fun equals(other: Any?): Boolean {
		return other === this
	}

	override fun hashCode(): Int {
		return id.hashCode()
	}
}

open class PoweredCustomItem(
	id: String,
	displayName: String,
	material: Material,
	model: Int,
	unbreakable: Boolean,
	val maxPower: Int
) : CustomItem(id, displayName, material, model, unbreakable) {
	override fun itemStack(amount: Int): ItemStack {
		val item = super.itemStack(amount)
		item.lore = (item.lore ?: mutableListOf()).apply {
			add("$ITEM_POWER_PREFIX$maxPower")
		}
		return item
	}
}

@Suppress("unused")
object CustomItems {
	private val idMap = mutableMapOf<String, CustomItem>()
	private val modelMap: Table<Material, Int, CustomItem> = HashBasedTable.create()

	private fun <T : CustomItem> register(item: T): T {
		idMap[item.id] = item
		modelMap[item.material, item.model] = item
		return item
	}

	operator fun get(id: String?): CustomItem? = idMap[id]

	operator fun get(item: ItemStack?): CustomItem? {
		val itemMeta = item?.itemMeta ?: return null
		if (!itemMeta.hasCustomModelData()) {
			return null
		}
		return modelMap[item.type, itemMeta.customModelData]
	}

	fun all(): Collection<CustomItem> = idMap.values

	//region Energy Swords
	private fun registerEnergySword(color: String, colorName: String, model: Int): EnergySwordItem = register(
		EnergySwordItem("energy_sword_$color", "$colorName$YELLOW Energy$DARK_AQUA Sword", SHIELD, model)
	)

	class EnergySwordItem(id: String, displayName: String, material: Material, model: Int, useMiniMessage: Boolean = false) :
		CustomItem(id, displayName, material, model, true, useMiniMessage)

	val ENERGY_SWORD_BLUE = registerEnergySword(color = "blue", colorName = "${BLUE}Blue", model = 1)
	val ENERGY_SWORD_RED = registerEnergySword(color = "red", colorName = "${RED}Red", model = 2)
	val ENERGY_SWORD_YELLOW = registerEnergySword(color = "yellow", colorName = "${YELLOW}Yellow", model = 3)
	val ENERGY_SWORD_GREEN = registerEnergySword(color = "green", colorName = "${GREEN}Green", model = 4)
	val ENERGY_SWORD_PURPLE = registerEnergySword(color = "purple", colorName = "${DARK_PURPLE}Purple", model = 5)
	val ENERGY_SWORD_ORANGE = registerEnergySword(color = "orange", colorName = "${GOLD}Orange", model = 6)
	val ENERGY_SWORD_PINK = register(EnergySwordItem("energy_sword_pink", "<#FFC0CB>Pink<yellow> Energy<dark_aqua> Sword", SHIELD, 7, useMiniMessage = true))
	val ENERGY_SWORD_BLACK = register(EnergySwordItem("energy_sword_black", "<black>Black<yellow> Energy<dark_aqua> Sword", SHIELD, 8, useMiniMessage = true))
	//endregion Energy Swords
}
