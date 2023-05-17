package net.starlegacy.feature.misc

import com.google.common.collect.HashBasedTable
import com.google.common.collect.Table
import net.starlegacy.util.set
import net.horizonsend.ion.server.miscellaneous.updateMeta
import org.bukkit.ChatColor
import org.bukkit.ChatColor.AQUA
import org.bukkit.ChatColor.BLUE
import org.bukkit.ChatColor.DARK_AQUA
import org.bukkit.ChatColor.DARK_GRAY
import org.bukkit.ChatColor.DARK_PURPLE
import org.bukkit.ChatColor.GOLD
import org.bukkit.ChatColor.GRAY
import org.bukkit.ChatColor.GREEN
import org.bukkit.ChatColor.RED
import org.bukkit.ChatColor.WHITE
import org.bukkit.ChatColor.YELLOW
import org.bukkit.Material
import org.bukkit.Material.DIAMOND_AXE
import org.bukkit.Material.DIAMOND_PICKAXE
import org.bukkit.Material.FLINT_AND_STEEL
import org.bukkit.Material.IRON_BLOCK
import org.bukkit.Material.IRON_INGOT
import org.bukkit.Material.IRON_ORE
import org.bukkit.Material.LEATHER_BOOTS
import org.bukkit.Material.LEATHER_CHESTPLATE
import org.bukkit.Material.LEATHER_HELMET
import org.bukkit.Material.LEATHER_LEGGINGS
import org.bukkit.Material.SHEARS
import org.bukkit.Material.SHIELD
import org.bukkit.Material.SNOWBALL
import org.bukkit.Material.WARPED_FUNGUS_ON_A_STICK
import org.bukkit.inventory.ItemStack
import java.util.Locale

open class CustomItem(
	val id: String,
	displayName: String,
	val material: Material,
	val model: Int,
	val unbreakable: Boolean
) {
	val displayName = "${ChatColor.RESET}$displayName"

	open fun itemStack(amount: Int): ItemStack = ItemStack(material, amount)
		.updateMeta {
			it.setDisplayName(displayName)
			it.isUnbreakable = unbreakable
			it.setCustomModelData(model)
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

	//region Misc
	val DETONATOR: CustomItem = makeItem(
		id = "detonator",
		name = "${RED}Thermal$GRAY Detonator",
		mat = SHEARS,
		model = 1
	)
	//endregion Misc

	//region Gas Canisters
	private fun registerGas(id: String, name: String, model: Int) = register(
		GasItem(id, displayName = "$name$GRAY Gas Canister", material = SNOWBALL, model = model)
	)

	class GasItem(id: String, displayName: String, material: Material, model: Int) :
		CustomItem(id, displayName, material, model, false)

	val GAS_CANISTER_EMPTY = registerGas(id = "empty_canister", name = "${WHITE}Empty", model = 1)
	val GAS_CANISTER_HELIUM = registerGas(id = "gas_helium", name = "${YELLOW}Helium", model = 2)
	val GAS_CANISTER_OXYGEN = registerGas(id = "gas_oxygen", name = "${AQUA}Oxygen", model = 3)
	val GAS_CANISTER_HYDROGEN = registerGas(id = "gas_hydrogen", name = "${GREEN}Hydrogen", model = 4)
	val GAS_CANISTER_NITROGEN = registerGas(id = "gas_nitrogen", name = "${DARK_PURPLE}Nitrogen", model = 5)
	val GAS_CANISTER_CARBON_DIOXIDE = registerGas(id = "gas_carbon_dioxide", name = "${RED}Carbon Dioxide", model = 6)
	//endregion Gas Canisters

	//region Batteries
	enum class BatteryType(val itemId: String, val maxPower: Int) {
		SMALL("battery_a", 500), MEDIUM("battery_m", 1000), LARGE("battery_g", 2000);

		fun getItem(): CustomItem = CustomItems[itemId] ?: error("No custom item for battery type $name!")
	}

	class BatteryItem(type: BatteryType, typeName: String, model: Int) : PoweredCustomItem(
		type.itemId, "${BLUE}Size$DARK_GRAY-$typeName$BLUE Battery", SNOWBALL, model, false, type.maxPower
	)

	private fun registerBattery(type: BatteryType, typeName: String, model: Int): PoweredCustomItem =
		register(BatteryItem(type, typeName, model))

	val BATTERY_SMALL = registerBattery(type = BatteryType.SMALL, typeName = "${RED}A", model = 7)
	val BATTERY_MEDIUM = registerBattery(type = BatteryType.MEDIUM, typeName = "${GREEN}M", model = 8)
	val BATTERY_LARGE = registerBattery(type = BatteryType.LARGE, typeName = "${GOLD}G", model = 9)
	//endregion

	//region Energy Swords
	private fun registerEnergySword(color: String, colorName: String, model: Int): EnergySwordItem = register(
		EnergySwordItem("energy_sword_$color", "$colorName$YELLOW Energy$DARK_AQUA Sword", SHIELD, model)
	)

	class EnergySwordItem(id: String, displayName: String, material: Material, model: Int) :
		CustomItem(id, displayName, material, model, true)

	val ENERGY_SWORD_BLUE = registerEnergySword(color = "blue", colorName = "${BLUE}Blue", model = 1)
	val ENERGY_SWORD_RED = registerEnergySword(color = "red", colorName = "${RED}Red", model = 2)
	val ENERGY_SWORD_YELLOW = registerEnergySword(color = "yellow", colorName = "${YELLOW}Yellow", model = 3)
	val ENERGY_SWORD_GREEN = registerEnergySword(color = "green", colorName = "${GREEN}Green", model = 4)
	val ENERGY_SWORD_PURPLE = registerEnergySword(color = "purple", colorName = "${DARK_PURPLE}Purple", model = 5)
	val ENERGY_SWORD_ORANGE = registerEnergySword(color = "orange", colorName = "${GOLD}Orange", model = 6)
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

	//region Power Tools
	private fun registerPowerTool(type: String, name: String, mat: Material, model: Int, maxPower: Int) =
		makePoweredItem("power_tool_$type", "${GOLD}Power$GRAY $name", mat, model, maxPower)

	val POWER_TOOL_DRILL = registerPowerTool("drill", "Drill", DIAMOND_PICKAXE, 1, 50000)

	init {
		idMap["power_tool_pickaxe"] = POWER_TOOL_DRILL
	}

	val POWER_TOOL_CHAINSAW = registerPowerTool("chainsaw", "Chainsaw", DIAMOND_AXE, 1, 100000)
	//endregion Power Tools

	//region Minerals
	class MineralCustomItem(
		id: String,
		displayName: String,
		material: Material,
		model: Int,
		val ore: CustomBlockItem,
		val fullBlock: CustomBlockItem
	) : CustomItem(id, displayName, material, model, false)

	private fun registerMineral(type: String, typeName: String, model: Int): MineralCustomItem {
		return register(
			MineralCustomItem(
				id = type,
				displayName = typeName,
				material = IRON_INGOT,
				model = model,
				ore = makeBlockItem(
					id = "${type}_ore",
					displayName = "$typeName Ore",
					material = IRON_ORE,
					model = model,
					blockId = "${type}_ore"
				),
				fullBlock = makeBlockItem(
					id = "${type}_block",
					displayName = "$typeName Block",
					material = IRON_BLOCK,
					model = model,
					blockId = "${type}_block"
				)
			)
		)
	}

	val MINERAL_ALUMINUM = registerMineral(type = "aluminum", typeName = "Aluminum", model = 1)
	val MINERAL_CHETHERITE = registerMineral(type = "chetherite", typeName = "Chetherite", model = 2)
	val MINERAL_TITANIUM = registerMineral(type = "titanium", typeName = "Titanium", model = 3)
	val MINERAL_URANIUM = registerMineral(type = "uranium", typeName = "Uranium", model = 4)
	//endregion Minerals

	//region Planet Icons
	private fun registerPlanetIcon(name: String, model: Int): CustomItem = makeItem(
		id = "planet_icon_${name.lowercase(Locale.getDefault()).replace(" ", "")}",
		name = name,
		mat = WARPED_FUNGUS_ON_A_STICK,
		model = model
	)

	val PLANET_ICON_AERACH = registerPlanetIcon("Aerach", 201)
	val PLANET_ICON_ARET = registerPlanetIcon("Aret", 202)
	val PLANET_ICON_CHANDRA = registerPlanetIcon("Chandra", 203)
	val PLANET_ICON_CHIMGARA = registerPlanetIcon("Chimgara", 204)
	val PLANET_ICON_DAMKOTH = registerPlanetIcon("Damkoth", 205)
	val PLANET_ICON_GAHARA = registerPlanetIcon("Gahara", 206)
	val PLANET_ICON_HERDOLI = registerPlanetIcon("Herdoli", 207)
	val PLANET_ICON_ILIUS = registerPlanetIcon("Ilius", 208)
	val PLANET_ICON_ISIK = registerPlanetIcon("Isik", 209)
	val PLANET_ICON_KOVFEFE = registerPlanetIcon("Kovfefe", 210)
	val PLANET_ICON_KRIO = registerPlanetIcon("Krio", 211)
	val PLANET_ICON_LIODA = registerPlanetIcon("Lioda", 212)
	val PLANET_ICON_LUXITERNA = registerPlanetIcon("Luxiterna", 213)
	val PLANET_ICON_QATRA = registerPlanetIcon("Qatra", 214)
	val PLANET_ICON_RUBACIEA = registerPlanetIcon("Rubaciea", 215)
	val PLANET_ICON_TURMS = registerPlanetIcon("Turms", 216)
	val PLANET_ICON_VASK = registerPlanetIcon("Vask", 217)
	//endregion

	//region Rockets
	val ROCKET_BASE = makeItem("rocket_base", "Rocket Base", Material.STICK, 1)

	val ROCKET_WARHEAD_ORIOMIUM = makeItem("rocket_warhead_oriomium", "Oriomium Warhead", Material.STICK, 2)

	val ROCKET_ORIOMIUM = makeItem("rocket_oriomium", "Oriomium Rocket", Material.STICK, 3)
}
