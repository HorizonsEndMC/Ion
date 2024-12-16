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
import org.bukkit.ChatColor.GRAY
import org.bukkit.ChatColor.GREEN
import org.bukkit.ChatColor.RED
import org.bukkit.ChatColor.YELLOW
import org.bukkit.Material
import org.bukkit.Material.FLINT_AND_STEEL
import org.bukkit.Material.LEATHER_BOOTS
import org.bukkit.Material.LEATHER_CHESTPLATE
import org.bukkit.Material.LEATHER_HELMET
import org.bukkit.Material.LEATHER_LEGGINGS
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

class CustomBlockItem(id: String, displayName: String, material: Material, model: Int, val customBlockId: String) :
	CustomItem(id, displayName, material, model, false) {

	val customBlock: CustomBlock
		get() = CustomBlocks[customBlockId] ?: error("Custom block $customBlockId not found for custom item $id")
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

	private fun makeItem(
		id: String,
		name: String,
		mat: Material,
		model: Int,
		unbreakable: Boolean = false
	): CustomItem = register(CustomItem(id, name, mat, model, unbreakable))

	private fun makePoweredItem(
		id: String,
		displayName: String,
		material: Material,
		model: Int,
		maxPower: Int,
		unbreakable: Boolean = false
	): PoweredCustomItem = register(PoweredCustomItem(id, displayName, material, model, unbreakable, maxPower))

	private fun makeBlockItem(
		id: String,
		displayName: String,
		material: Material,
		model: Int,
		blockId: String
	): CustomBlockItem = register(CustomBlockItem(id, displayName, material, model, blockId))

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

	//region Power Armor
	private fun registerPowerArmor(piece: String, pieceName: String, material: Material): PowerArmorItem = register(
		PowerArmorItem("power_armor_$piece", "${GOLD}Power$GRAY $pieceName", material, 1, 50000)
	)

	class PowerArmorItem(
		id: String,
		displayName: String,
		material: Material,
		model: Int,
		maxPower: Int
	) : PoweredCustomItem(id, displayName, material, model, true, maxPower)

	val POWER_ARMOR_HELMET = registerPowerArmor("helmet", "Helmet", LEATHER_HELMET)
	val POWER_ARMOR_CHESTPLATE = registerPowerArmor("chestplate", "Chestplate", LEATHER_CHESTPLATE)
	val POWER_ARMOR_LEGGINGS = registerPowerArmor("leggings", "Leggings", LEATHER_LEGGINGS)
	val POWER_ARMOR_BOOTS = registerPowerArmor("boots", "Boots", LEATHER_BOOTS)
	//endregion Power Armor

	//region Power Modules
	private fun registerModule(type: String, typeName: String, model: Int): PowerModuleItem =
		register(PowerModuleItem("power_module_$type", "$GRAY$typeName$GOLD Module", FLINT_AND_STEEL, model))

	class PowerModuleItem(
		id: String,
		displayName: String,
		material: Material,
		model: Int
	) : CustomItem(id, displayName, material, model, true)

	val POWER_MODULE_SHOCK_ABSORBING = registerModule("shock_absorbing", "Shock Absorbing", 1)
	val POWER_MODULE_SPEED_BOOSTING = registerModule("speed_boosting", "Speed Boosting", 2)
	val POWER_MODULE_ROCKET_BOOSTING = registerModule("rocket_boosting", "Rocket Boosting", 3)
	val POWER_MODULE_NIGHT_VISION = registerModule("night_vision", "Night Vision", 4)
	val POWER_MODULE_ENVIRONMENT = registerModule("environment", "Environment", 5)
	val POWER_MODULE_PRESSURE_FIELD = registerModule("pressure_field", "Pressure Field", 6)
	//endregion Power Modules
}
