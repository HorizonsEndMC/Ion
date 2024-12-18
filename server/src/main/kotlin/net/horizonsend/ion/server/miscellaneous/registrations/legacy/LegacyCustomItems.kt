package net.horizonsend.ion.server.miscellaneous.registrations.legacy

import com.google.common.collect.HashBasedTable
import com.google.common.collect.Table
import io.papermc.paper.datacomponent.DataComponentTypes
import io.papermc.paper.datacomponent.item.Unbreakable
import net.horizonsend.ion.common.utils.text.miniMessage
import net.horizonsend.ion.server.features.gear.ITEM_POWER_PREFIX
import net.horizonsend.ion.server.miscellaneous.utils.updateData
import net.horizonsend.ion.server.miscellaneous.utils.updateDisplayName
import net.horizonsend.ion.server.miscellaneous.utils.updateMeta
import org.bukkit.ChatColor
import org.bukkit.Material
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

	operator fun get(id: String?): CustomItem? = idMap[id]

	operator fun get(item: ItemStack?): CustomItem? {
		val itemMeta = item?.itemMeta ?: return null
		if (!itemMeta.hasCustomModelData()) {
			return null
		}
		return modelMap[item.type, itemMeta.customModelData]
	}

	fun all(): Collection<CustomItem> = idMap.values
}
