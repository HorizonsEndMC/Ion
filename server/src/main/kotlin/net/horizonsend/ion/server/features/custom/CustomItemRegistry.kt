package net.horizonsend.ion.server.features.custom

import net.horizonsend.ion.server.IonServerComponent
import net.horizonsend.ion.server.configuration.ConfigurationFiles
import net.horizonsend.ion.server.configuration.PVPBalancingConfiguration.EnergyWeapons.Multishot
import net.horizonsend.ion.server.configuration.PVPBalancingConfiguration.EnergyWeapons.Singleshot
import net.horizonsend.ion.server.features.custom.NewCustomItemListeners.sortCustomItemListeners
import net.horizonsend.ion.server.features.custom.blocks.CustomBlock
import net.horizonsend.ion.server.features.custom.blocks.CustomBlocks
import net.horizonsend.ion.server.features.custom.items.CustomBlockItem
import net.horizonsend.ion.server.features.custom.items.blasters.Blaster
import net.horizonsend.ion.server.features.custom.items.blasters.Magazine
import net.horizonsend.ion.server.features.custom.items.components.CustomComponentType
import net.horizonsend.ion.server.features.custom.items.components.SmeltableComponent
import net.horizonsend.ion.server.features.custom.items.misc.ProgressHolder
import net.horizonsend.ion.server.features.custom.items.util.ItemFactory
import net.horizonsend.ion.server.features.custom.items.util.ItemFactory.Preset.stackableCustomItem
import net.horizonsend.ion.server.features.custom.items.util.ItemFactory.Preset.unStackableCustomItem
import net.horizonsend.ion.server.features.custom.items.util.withComponent
import net.horizonsend.ion.server.miscellaneous.registrations.NamespacedKeys.CUSTOM_ITEM
import net.horizonsend.ion.server.miscellaneous.utils.Tasks
import net.horizonsend.ion.server.miscellaneous.utils.map
import net.horizonsend.ion.server.miscellaneous.utils.text.itemName
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.format.NamedTextColor.RED
import net.kyori.adventure.text.format.NamedTextColor.YELLOW
import net.kyori.adventure.text.format.TextDecoration.BOLD
import net.kyori.adventure.text.format.TextDecoration.ITALIC
import org.bukkit.Material
import org.bukkit.Material.DIAMOND_HOE
import org.bukkit.Material.GOLDEN_HOE
import org.bukkit.Material.IRON_BLOCK
import org.bukkit.Material.IRON_HOE
import org.bukkit.Material.IRON_ORE
import org.bukkit.Material.RAW_IRON
import org.bukkit.Material.RAW_IRON_BLOCK
import org.bukkit.entity.LivingEntity
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType.STRING
import java.util.function.Supplier
import kotlin.math.roundToInt

object CustomItemRegistry : IonServerComponent() {
	private val customItems = mutableMapOf<String, NewCustomItem>()
	val ALL get() = customItems.values

	// Guns Start
	val STANDARD_MAGAZINE = register(Magazine(
		identifier = "STANDARD_MAGAZINE",
		displayName = text("Standard Magazine").decoration(ITALIC, false),
		itemFactory = unStackableCustomItem("weapon/blaster/standard_magazine"),
		balancingSupplier = ConfigurationFiles.pvpBalancing().energyWeapons::standardMagazine
	))
	val SPECIAL_MAGAZINE = register(Magazine(
		identifier = "SPECIAL_MAGAZINE",
		displayName = text("Special Magazine").decoration(ITALIC, false),
		itemFactory = unStackableCustomItem("weapon/blaster/special_magazine"),
		balancingSupplier = ConfigurationFiles.pvpBalancing().energyWeapons::specialMagazine
	))

	val BLASTER_PISTOL = register(Blaster(
		identifier = "BLASTER_PISTOL",
		displayName = text("Blaster Pistol", RED, BOLD).itemName,
		itemFactory = ItemFactory.builder().setMaterial(DIAMOND_HOE).setCustomModel("weapon/blaster/pistol").build(),
		balancingSupplier = ConfigurationFiles.pvpBalancing().energyWeapons::pistol
	))
	val BLASTER_RIFLE = register(Blaster(
		identifier = "BLASTER_RIFLE",
		displayName = text("Blaster Rifle", RED, BOLD).itemName,
		itemFactory = ItemFactory.builder().setMaterial(IRON_HOE).setCustomModel("weapon/blaster/rifle").build(),
		balancingSupplier = ConfigurationFiles.pvpBalancing().energyWeapons::rifle
	))
	val SUBMACHINE_BLASTER = register(object : Blaster<Singleshot>(
		identifier = "SUBMACHINE_BLASTER",
		itemFactory = ItemFactory.builder().setMaterial(IRON_HOE).setCustomModel("weapon/blaster/submachine_blaster").build(),
		displayName = text("Submachine Blaster", RED, BOLD).decoration(ITALIC, false),
		balancingSupplier = ConfigurationFiles.pvpBalancing().energyWeapons::submachineBlaster
	) {
		// Allows fire above 300 rpm
		override fun fire(shooter: LivingEntity, blasterItem: ItemStack) {
			val repeatCount = if (balancing.timeBetweenShots >= 4) 1 else (4.0 / balancing.timeBetweenShots).roundToInt()
			val division = 4.0 / balancing.timeBetweenShots

			for (count in 0 until repeatCount) {
				val delay = (count * division).toLong()
				if (delay > 0) Tasks.syncDelay(delay) { super.fire(shooter, blasterItem) } else super.fire(shooter, blasterItem)
			}
		}
	})
	val BLASTER_SHOTGUN = register(object : Blaster<Multishot>(
		identifier = "BLASTER_SHOTGUN",
		displayName = text("Blaster Shotgun", RED, BOLD).decoration(ITALIC, false),
		itemFactory = ItemFactory.builder().setMaterial(GOLDEN_HOE).setCustomModel("weapon/blaster/shotgun").build(),
		balancingSupplier = ConfigurationFiles.pvpBalancing().energyWeapons::shotgun
	) {
		override fun fireProjectiles(livingEntity: LivingEntity) {
			for (i in 1..balancing.shotCount) super.fireProjectiles(livingEntity)
		}
	})
	val BLASTER_SNIPER = register(Blaster(
		identifier = "BLASTER_SNIPER",
		displayName = text("Blaster Sniper", RED, BOLD).decoration(ITALIC, false),
		itemFactory = ItemFactory.builder().setMaterial(GOLDEN_HOE).setCustomModel("weapon/blaster/sniper").build(),
		balancingSupplier = ConfigurationFiles.pvpBalancing().energyWeapons::sniper
	))
	val BLASTER_CANNON = register(Blaster(
		identifier = "BLASTER_CANNON",
		displayName = text("Blaster Cannon", RED, BOLD).decoration(ITALIC, false),
		itemFactory = ItemFactory.builder().setMaterial(IRON_HOE).setCustomModel("weapon/blaster/cannon").build(),
		balancingSupplier = ConfigurationFiles.pvpBalancing().energyWeapons::cannon
	))

	val GUN_BARREL = register("GUN_BARREL", text("Gun Barrel"), unStackableCustomItem("industry/gun_barrel"))
	val CIRCUITRY = register("CIRCUITRY", text("Circuitry"), unStackableCustomItem("industry/circuitry"))

	val PISTOL_RECEIVER = register("PISTOL_RECEIVER", text("Pistol Receiver"), unStackableCustomItem("industry/pistol_receiver"))
	val RIFLE_RECEIVER = register("RIFLE_RECEIVER", text("Rifle Receiver"), unStackableCustomItem("industry/rifle_receiver"))
	val SMB_RECEIVER = register("SMB_RECEIVER", text("SMB Receiver"), unStackableCustomItem("industry/smb_receiver"))
	val SNIPER_RECEIVER = register("SNIPER_RECEIVER", text("Sniper Receiver"), unStackableCustomItem("industry/sniper_receiver"))
	val SHOTGUN_RECEIVER = register("SHOTGUN_RECEIVER", text("Shotgun Receiver"), unStackableCustomItem("industry/shotgun_receiver"))
	val CANNON_RECEIVER = register("CANNON_RECEIVER", text("Cannon Receiver"), unStackableCustomItem("industry/cannon_receiver"))

	// Minerals start
	private fun registerRawOre(identifier: String, name: String, smeltingResult: Supplier<NewCustomItem>) = register(identifier, text("Raw ${name.replaceFirstChar { it.uppercase() }}"), stackableCustomItem(RAW_IRON, model = "mineral/raw_$name")).withComponent(CustomComponentType.SMELTABLE, SmeltableComponent(smeltingResult.map { it.constructItemStack() }))
	private fun registerOreIngot(identifier: String, name: String) = register(identifier, text("${name.replaceFirstChar { it.uppercase() }} Ingot"), stackableCustomItem(RAW_IRON, model = "mineral/$name"))
	private fun registerOreBlock(identifier: String, name: String, block: Supplier<CustomBlock>, smeltingResult: Supplier<NewCustomItem>) = customBlockItem(identifier, IRON_ORE, "mineral/${name}_ore", text("${name.replaceFirstChar { it.uppercase() }} Ore"), block).withComponent(CustomComponentType.SMELTABLE, SmeltableComponent(smeltingResult.map { it.constructItemStack() }))
	private fun registerIngotBlock(identifier: String, name: String, block: Supplier<CustomBlock>) = customBlockItem(identifier, IRON_BLOCK, "mineral/${name}_block", text("${name.replaceFirstChar { it.uppercase() }} Block"), block)
	private fun registerRawBlock(identifier: String, name: String, block: Supplier<CustomBlock>) = customBlockItem(identifier, RAW_IRON_BLOCK, "mineral/raw_${name}_block", text("Raw ${name.replaceFirstChar { it.uppercase() }} Block"), block)

	val ALUMINUM_INGOT = registerOreIngot("ALUMINUM_INGOT", "aluminum")
	val RAW_ALUMINUM = registerRawOre("RAW_ALUMINUM", "aluminum", smeltingResult = ::ALUMINUM_INGOT)
	val ALUMINUM_ORE = registerOreBlock("ALUMINUM_ORE", "aluminum", block = CustomBlocks::ALUMINUM_ORE, smeltingResult = ::ALUMINUM_INGOT)
	val ALUMINUM_BLOCK = registerIngotBlock("ALUMINUM_BLOCK", "aluminum", block = CustomBlocks::ALUMINUM_BLOCK)
	val RAW_ALUMINUM_BLOCK = registerRawBlock("RAW_ALUMINUM_BLOCK", "aluminum", block = CustomBlocks::RAW_ALUMINUM_BLOCK)

	val CHETHERITE = registerOreIngot("CHETHERITE", "chetherite")
	val CHETHERITE_ORE = registerOreBlock("CHETHERITE_ORE", "chetherite", block = CustomBlocks::CHETHERITE_ORE, smeltingResult = ::CHETHERITE)
	val CHETHERITE_BLOCK = registerIngotBlock("CHETHERITE_BLOCK", "chetherite", block = CustomBlocks::CHETHERITE_BLOCK)

	val TITANIUM_INGOT = registerOreIngot("TITANIUM_INGOT", "titanium")
	val RAW_TITANIUM = registerRawOre("RAW_TITANIUM", "titanium", smeltingResult = ::TITANIUM_INGOT)
	val TITANIUM_ORE = registerOreBlock("TITANIUM_ORE", "titanium", block = CustomBlocks::TITANIUM_ORE, smeltingResult = ::TITANIUM_INGOT)
	val TITANIUM_BLOCK = registerIngotBlock("TITANIUM_BLOCK", "titanium", block = CustomBlocks::TITANIUM_BLOCK)
	val RAW_TITANIUM_BLOCK = registerRawBlock("RAW_TITANIUM_BLOCK", "titanium", block = CustomBlocks::RAW_TITANIUM_BLOCK)

	val URANIUM = registerOreIngot(identifier = "URANIUM", name = "uranium")
	val RAW_URANIUM = registerRawOre(identifier = "RAW_URANIUM", name = "uranium", smeltingResult = CustomItemRegistry::URANIUM)
	val URANIUM_ORE = registerOreBlock(identifier = "URANIUM_ORE", name = "uranium", block = CustomBlocks::URANIUM_ORE, smeltingResult = ::URANIUM)
	val URANIUM_BLOCK = registerIngotBlock(identifier = "URANIUM_BLOCK", name = "uranium", block = CustomBlocks::URANIUM_BLOCK)
	val RAW_URANIUM_BLOCK = registerRawBlock(identifier = "RAW_URANIUM_BLOCK", name = "uranium", block = CustomBlocks::RAW_URANIUM_BLOCK)
	// Minerals end

	// Industry start
	val NETHERITE_CASING = customBlockItem(identifier = "NETHERITE_CASING", model = "industry/netherite_casing", displayName = text("Netherite Casing"), customBlock = CustomBlocks::NETHERITE_CASING)
	val ENRICHED_URANIUM = stackable(identifier = "ENRICHED_URANIUM", text("Enriched Uranium"), "industry/enriched_uranium")
	val ENRICHED_URANIUM_BLOCK = customBlockItem(identifier = "ENRICHED_URANIUM_BLOCK", model = "industry/enriched_uranium_block", displayName = text("Enriched Uranium Block"), customBlock = CustomBlocks::ENRICHED_URANIUM_BLOCK)
	val URANIUM_CORE = unStackable(identifier = "URANIUM_CORE", model = "industry/uranium_core", displayName = text("Uranium Core"))
	val URANIUM_ROD = unStackable(identifier = "URANIUM_ROD", model = "industry/uranium_rod", displayName = text("Uranium Rod"))
	val FUEL_ROD_CORE = unStackable(identifier = "FUEL_ROD_CORE", model = "industry/fuel_rod_core", displayName = text("Fuel Rod Core"))
	val FUEL_CELL = unStackable(identifier = "FUEL_CELL", model = "industry/fuel_cell", displayName = text("Fuel Cell"))
	val FUEL_CONTROL = unStackable(identifier = "FUEL_CONTROL", model = "industry/fuel_control", displayName = text("Fuel Control"))

	val REACTIVE_COMPONENT = unStackable(identifier = "REACTIVE_COMPONENT", model = "industry/reactive_component", displayName = text("Reactive Component"))
	val REACTIVE_HOUSING = unStackable(identifier = "REACTIVE_HOUSING", model = "industry/reactive_housing", displayName = text("Reactive Housing"))
	val REACTIVE_PLATING = unStackable(identifier = "REACTIVE_PLATING", model = "industry/reactive_plating", displayName = text("Reactive Plating"))
	val REACTIVE_CHASSIS = unStackable(identifier = "REACTIVE_CHASSIS", model = "industry/reactive_chassis", displayName = text("Reactive Chassis"))
	val REACTIVE_MEMBRANE = unStackable(identifier = "REACTIVE_MEMBRANE", model = "industry/reactive_membrane", displayName = text("Reactive Membrane"))
	val REACTIVE_ASSEMBLY = unStackable(identifier = "REACTIVE_ASSEMBLY", model = "industry/reactive_assembly", displayName = text("Reactive Assembly"))
	val FABRICATED_ASSEMBLY = unStackable(identifier = "FABRICATED_ASSEMBLY", model = "industry/fabricated_assembly", displayName = text("Fabricated Assembly"))

	val CIRCUIT_BOARD = unStackable(identifier = "CIRCUIT_BOARD", model = "industry/circuit_board", displayName = text("Circuit Board"))
	val MOTHERBOARD = unStackable(identifier = "MOTHERBOARD", model = "industry/motherboard", displayName = text("Motherboard"))
	val REACTOR_CONTROL = unStackable(identifier = "REACTOR_CONTROL", model = "industry/reactor_control", displayName = text("Reactor Control", YELLOW))

	val SUPERCONDUCTOR = unStackable(identifier = "SUPERCONDUCTOR", model = "industry/superconductor", displayName = text("Superconductor"))
	val SUPERCONDUCTOR_BLOCK = customBlockItem(identifier = "SUPERCONDUCTOR_BLOCK", model = "industry/superconductor_block", displayName = text("Superconductor Block"), customBlock = CustomBlocks::SUPERCONDUCTOR_BLOCK)
	val SUPERCONDUCTOR_CORE = unStackable(identifier = "SUPERCONDUCTOR_CORE", model = "industry/superconductor_core", displayName = text("Superconductor Core", YELLOW))

	val STEEL_INGOT = stackable(identifier = "STEEL_INGOT", text("Steel Ingot"), "industry/steel_ingot")
	val STEEL_BLOCK = unStackable(identifier = "STEEL_BLOCK", model = "industry/steel_block", displayName = text("Steel Block"))
	val STEEL_PLATE = unStackable(identifier = "STEEL_PLATE", model = "industry/steel_plate", displayName = text("Steel Plate"))
	val STEEL_CHASSIS = unStackable(identifier = "STEEL_CHASSIS", model = "industry/steel_chassis", displayName = text("Steel Chassis"))
	val STEEL_MODULE = unStackable(identifier = "STEEL_MODULE", model = "industry/steel_module", displayName = text("Steel Module"))
	val STEEL_ASSEMBLY = unStackable(identifier = "STEEL_ASSEMBLY", model = "industry/steel_assembly", displayName = text("Steel Assembly"))
	val REINFORCED_FRAME = unStackable(identifier = "REINFORCED_FRAME", model = "industry/reinforced_frame", displayName = text("Reinforced Frame"))
	val REACTOR_FRAME = unStackable(identifier = "REACTOR_FRAME", model = "industry/reactor_frame", displayName = text("Reactor Frame", YELLOW))

	val UNLOADED_SHELL = unStackable(identifier = "UNLOADED_SHELL", model= "industry/unloaded_shell", displayName = text("Unloaded Shell"))
	val LOADED_SHELL = stackable(identifier = "LOADED_SHELL", model = "industry/loaded_shell", displayName = text("Loaded Shell"))
	val UNCHARGED_SHELL = unStackable(identifier = "UNCHARGED_SHELL", model= "industry/uncharged_shell", displayName = text("Uncharged Shell"))
	val CHARGED_SHELL = stackable(identifier = "CHARGED_SHELL", model = "industry/charged_shell", displayName = text("Charged Shell"))

	val ARSENAL_MISSILE = stackable(identifier = "ARSENAL_MISSILE", model = "projectile/arsenal_missile", displayName = text("Arsenal Missile"))
	val UNLOADED_ARSENAL_MISSILE = unStackable(identifier = "UNLOADED_ARSENAL_MISSILE", model= "projectile/unloaded_arsenal_missile", displayName = text("Unloaded Arsenal Missile"))
	val ACTIVATED_ARSENAL_MISSILE = unStackable(identifier = "ACTIVATED_ARSENAL_MISSILE", model= "projectile/activated_arsenal_missile", displayName = text("Activated Arsenal Missile", RED))

	val PROGRESS_HOLDER = register(ProgressHolder)

	init {
		sortCustomItemListeners()
	}

	private fun <T : NewCustomItem> register(item: T): T {
		customItems[item.identifier] = item
		return item
	}

	private fun register(identifier: String, displayName: Component, factory: ItemFactory): NewCustomItem {
		return register(NewCustomItem(identifier, displayName, factory))
	}

	private fun stackable(identifier: String, displayName: Component, model: String): NewCustomItem {
		return register(NewCustomItem(identifier, displayName, stackableCustomItem(model = model)))
	}

	private fun unStackable(identifier: String, displayName: Component, model: String): NewCustomItem {
		return register(NewCustomItem(identifier, displayName, unStackableCustomItem(model = model)))
	}

	private fun customBlockItem(identifier: String, material: Material = IRON_BLOCK, model: String, displayName: Component, customBlock: Supplier<CustomBlock>) =
		register(CustomBlockItem(identifier, material, model, displayName, customBlock))

	val ItemStack.newCustomItem: NewCustomItem? get() {
		return customItems[persistentDataContainer.get(CUSTOM_ITEM, STRING) ?: return null]
	}

	val identifiers = customItems.keys

	fun getByIdentifier(identifier: String): NewCustomItem? = customItems[identifier]
}
