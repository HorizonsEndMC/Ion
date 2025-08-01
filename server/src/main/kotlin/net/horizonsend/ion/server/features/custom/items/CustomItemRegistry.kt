package net.horizonsend.ion.server.features.custom.items

import net.horizonsend.ion.common.utils.text.colors.HEColorScheme.Companion.HE_LIGHT_ORANGE
import net.horizonsend.ion.common.utils.text.colors.HEColorScheme.Companion.HE_MEDIUM_GRAY
import net.horizonsend.ion.common.utils.text.ofChildren
import net.horizonsend.ion.server.IonServerComponent
import net.horizonsend.ion.server.configuration.ConfigurationFiles
import net.horizonsend.ion.server.configuration.PVPBalancingConfiguration
import net.horizonsend.ion.server.configuration.PVPBalancingConfiguration.EnergyWeapons.Multishot
import net.horizonsend.ion.server.configuration.PVPBalancingConfiguration.EnergyWeapons.Singleshot
import net.horizonsend.ion.server.features.client.display.modular.display.PowerEntityDisplayModule.Companion.powerPrefix
import net.horizonsend.ion.server.features.custom.blocks.CustomBlock
import net.horizonsend.ion.server.features.custom.blocks.CustomBlocks
import net.horizonsend.ion.server.features.custom.items.CustomItemListeners.sortCustomItemListeners
import net.horizonsend.ion.server.features.custom.items.misc.MultiblockToken
import net.horizonsend.ion.server.features.custom.items.misc.MultimeterItem
import net.horizonsend.ion.server.features.custom.items.misc.PackagedMultiblock
import net.horizonsend.ion.server.features.custom.items.misc.Wrench
import net.horizonsend.ion.server.features.custom.items.type.CustomBlockItem
import net.horizonsend.ion.server.features.custom.items.type.GasCanister
import net.horizonsend.ion.server.features.custom.items.type.PersonalTransporter
import net.horizonsend.ion.server.features.custom.items.type.ProgressHolder
import net.horizonsend.ion.server.features.custom.items.type.armor.PowerArmorItem
import net.horizonsend.ion.server.features.custom.items.type.throwables.ThrowableCustomItem
import net.horizonsend.ion.server.features.custom.items.type.throwables.ThrownCustomItem
import net.horizonsend.ion.server.features.custom.items.type.throwables.ThrownPumpkinGrenade
import net.horizonsend.ion.server.features.custom.items.type.throwables.thrown.ThrownDetonator
import net.horizonsend.ion.server.features.custom.items.type.throwables.thrown.ThrownSmokeGrenade
import net.horizonsend.ion.server.features.custom.items.type.tool.Battery
import net.horizonsend.ion.server.features.custom.items.type.tool.CratePlacer
import net.horizonsend.ion.server.features.custom.items.type.tool.PowerChainsaw
import net.horizonsend.ion.server.features.custom.items.type.tool.PowerDrill
import net.horizonsend.ion.server.features.custom.items.type.tool.PowerHoe
import net.horizonsend.ion.server.features.custom.items.type.tool.mods.ItemModRegistry
import net.horizonsend.ion.server.features.custom.items.type.tool.mods.ModificationItem
import net.horizonsend.ion.server.features.custom.items.type.weapon.blaster.Blaster
import net.horizonsend.ion.server.features.custom.items.type.weapon.blaster.Magazine
import net.horizonsend.ion.server.features.custom.items.type.weapon.sword.EnergySword
import net.horizonsend.ion.server.features.custom.items.util.ItemFactory
import net.horizonsend.ion.server.features.custom.items.util.ItemFactory.Preset.stackableCustomItem
import net.horizonsend.ion.server.features.custom.items.util.ItemFactory.Preset.unStackableCustomItem
import net.horizonsend.ion.server.features.gas.Gasses
import net.horizonsend.ion.server.miscellaneous.registrations.persistence.NamespacedKeys.CUSTOM_ITEM
import net.horizonsend.ion.server.miscellaneous.utils.Tasks
import net.horizonsend.ion.server.miscellaneous.utils.text.itemName
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.NamedTextColor.BLACK
import net.kyori.adventure.text.format.NamedTextColor.BLUE
import net.kyori.adventure.text.format.NamedTextColor.DARK_GREEN
import net.kyori.adventure.text.format.NamedTextColor.DARK_PURPLE
import net.kyori.adventure.text.format.NamedTextColor.GOLD
import net.kyori.adventure.text.format.NamedTextColor.GRAY
import net.kyori.adventure.text.format.NamedTextColor.GREEN
import net.kyori.adventure.text.format.NamedTextColor.LIGHT_PURPLE
import net.kyori.adventure.text.format.NamedTextColor.RED
import net.kyori.adventure.text.format.NamedTextColor.YELLOW
import net.kyori.adventure.text.format.TextColor
import net.kyori.adventure.text.format.TextColor.fromHexString
import net.kyori.adventure.text.format.TextDecoration.BOLD
import net.kyori.adventure.text.format.TextDecoration.ITALIC
import org.bukkit.Material.DIAMOND_HOE
import org.bukkit.Material.GOLDEN_HOE
import org.bukkit.Material.IRON_HOE
import org.bukkit.Material.PUMPKIN
import org.bukkit.entity.Entity
import org.bukkit.entity.Item
import org.bukkit.entity.LivingEntity
import org.bukkit.inventory.EquipmentSlot
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType.STRING
import java.util.function.Supplier
import kotlin.math.roundToInt

object CustomItemRegistry : IonServerComponent() {
	private val customItems = mutableMapOf<String, CustomItem>()
	val ALL get() = customItems.values

	// Throwables start
	private fun registerThrowable(
		identifier: String,
		customModel: String,
		displayName: Component,
		balancing: Supplier<PVPBalancingConfiguration.Throwables.ThrowableBalancing>,
		thrown: (Item, Int, Entity?) -> ThrownCustomItem
	) = register(object : ThrowableCustomItem(identifier = identifier, customModel = customModel, displayName = displayName, balancingSupplier = balancing) {
		override fun constructThrownRunnable(item: Item, maxTicks: Int, damageSource: Entity?): ThrownCustomItem = thrown.invoke(item, maxTicks, damageSource)
	})

	val DETONATOR = registerThrowable(
		"DETONATOR",
		"throwables/detonator",
		ofChildren(text("Thermal ", RED), text("Detonator", GRAY)),
		ConfigurationFiles.pvpBalancing().throwables::detonator
	) { item, maxTicks, source -> ThrownDetonator(item, maxTicks, source, ConfigurationFiles.pvpBalancing().throwables::detonator) }
	val SMOKE_GRENADE = registerThrowable(
		"SMOKE_GRENADE",
		"throwables/detonator",
		ofChildren(text("Smoke ", DARK_GREEN), text("Grenade", GRAY)),
		ConfigurationFiles.pvpBalancing().throwables::smokeGrenade
	) { item, maxTicks, source -> ThrownSmokeGrenade(item, maxTicks, source) }

	val PUMPKIN_GRENADE = register(object : ThrowableCustomItem(
		"PUMPKIN_GRENADE",
		"",
		ofChildren(text("Pumpkin ", GOLD), text("Grenade", GREEN)),
		ConfigurationFiles.pvpBalancing().throwables::detonator
	) {
		override val baseItemFactory: ItemFactory = ItemFactory.builder(ItemFactory.builder().setMaterial(PUMPKIN).build())
			.setNameSupplier { displayName.itemName }
			.addPDCEntry(CUSTOM_ITEM, STRING, identifier)
			.addModifier { base -> customComponents.getAll().forEach { it.decorateBase(base, this) } }
			.addModifier { base -> decorateItemStack(base) }
			.setLoreSupplier { base -> assembleLore(base) }
			.build()

		override fun assembleLore(itemStack: ItemStack): List<Component> {
			return mutableListOf(text("Spooky", LIGHT_PURPLE))
		}
		override fun constructThrownRunnable(item: Item, maxTicks: Int, damageSource: Entity?): ThrownCustomItem = ThrownPumpkinGrenade(item, maxTicks, damageSource, ConfigurationFiles.pvpBalancing().throwables::detonator)
	})
	// Throwables end

	// Guns Start
	val STANDARD_MAGAZINE = register(
		Magazine(
		identifier = "STANDARD_MAGAZINE",
		displayName = text("Standard Magazine").decoration(ITALIC, false),
		itemFactory = unStackableCustomItem("weapon/blaster/standard_magazine"),
		balancingSupplier = ConfigurationFiles.pvpBalancing().energyWeapons::standardMagazine
	)
	)
	val SPECIAL_MAGAZINE = register(
		Magazine(
		identifier = "SPECIAL_MAGAZINE",
		displayName = text("Special Magazine").decoration(ITALIC, false),
		itemFactory = unStackableCustomItem("weapon/blaster/special_magazine"),
		balancingSupplier = ConfigurationFiles.pvpBalancing().energyWeapons::specialMagazine
	)
	)

	val BLASTER_PISTOL = register(
		Blaster(
		identifier = "BLASTER_PISTOL",
		displayName = text("Blaster Pistol", RED, BOLD),
		itemFactory = ItemFactory.builder().setMaterial(DIAMOND_HOE).setCustomModel("weapon/blaster/pistol").build(),
		balancingSupplier = ConfigurationFiles.pvpBalancing().energyWeapons::pistol
	)
	)
	val BLASTER_RIFLE = register(
		Blaster(
		identifier = "BLASTER_RIFLE",
		displayName = text("Blaster Rifle", RED, BOLD),
		itemFactory = ItemFactory.builder().setMaterial(IRON_HOE).setCustomModel("weapon/blaster/rifle").build(),
		balancingSupplier = ConfigurationFiles.pvpBalancing().energyWeapons::rifle
	)
	)
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
			repeat(balancing.shotCount) { super.fireProjectiles(livingEntity) }
		}
	})
	val BLASTER_SNIPER = register(
		Blaster(
		identifier = "BLASTER_SNIPER",
		displayName = text("Blaster Sniper", RED, BOLD).decoration(ITALIC, false),
		itemFactory = ItemFactory.builder().setMaterial(GOLDEN_HOE).setCustomModel("weapon/blaster/sniper").build(),
		balancingSupplier = ConfigurationFiles.pvpBalancing().energyWeapons::sniper
	)
	)
	val BLASTER_CANNON = register(
		Blaster(
		identifier = "BLASTER_CANNON",
		displayName = text("Blaster Cannon", RED, BOLD).decoration(ITALIC, false),
		itemFactory = ItemFactory.builder().setMaterial(IRON_HOE).setCustomModel("weapon/blaster/cannon").build(),
		balancingSupplier = ConfigurationFiles.pvpBalancing().energyWeapons::cannon
	)
	)

	val GUN_BARREL = register("GUN_BARREL", text("Gun Barrel"), unStackableCustomItem("industry/gun_barrel"))
	val CIRCUITRY = register("CIRCUITRY", text("Circuitry"), unStackableCustomItem("industry/circuitry"))

	val PISTOL_RECEIVER = register("PISTOL_RECEIVER", text("Pistol Receiver"), unStackableCustomItem("industry/pistol_receiver"))
	val RIFLE_RECEIVER = register("RIFLE_RECEIVER", text("Rifle Receiver"), unStackableCustomItem("industry/rifle_receiver"))
	val SMB_RECEIVER = register("SMB_RECEIVER", text("SMB Receiver"), unStackableCustomItem("industry/smb_receiver"))
	val SNIPER_RECEIVER = register("SNIPER_RECEIVER", text("Sniper Receiver"), unStackableCustomItem("industry/sniper_receiver"))
	val SHOTGUN_RECEIVER = register("SHOTGUN_RECEIVER", text("Shotgun Receiver"), unStackableCustomItem("industry/shotgun_receiver"))
	val CANNON_RECEIVER = register("CANNON_RECEIVER", text("Cannon Receiver"), unStackableCustomItem("industry/cannon_receiver"))

	// Minerals start
	private fun registerRawOre(identifier: String, name: String) = register(identifier, text("Raw ${name.replaceFirstChar { it.uppercase() }}"), stackableCustomItem(model = "mineral/raw_$name"))
	private fun registerOreIngot(identifier: String, name: String, useSuffix: Boolean) = register(identifier, text("${name.replaceFirstChar { it.uppercase() }}${if (useSuffix) " Ingot" else ""}"), stackableCustomItem(model = "mineral/$name"))
	private fun registerOreBlock(identifier: String, name: String, block: Supplier<CustomBlock>) = customBlockItem(identifier, "mineral/${name}_ore", text("${name.replaceFirstChar { it.uppercase() }} Ore"), block)
	private fun registerIngotBlock(identifier: String, name: String, block: Supplier<CustomBlock>) = customBlockItem(identifier, "mineral/${name}_block", text("${name.replaceFirstChar { it.uppercase() }} Block"), block)
	private fun registerRawBlock(identifier: String, name: String, block: Supplier<CustomBlock>) = customBlockItem(identifier, "mineral/raw_${name}_block", text("Raw ${name.replaceFirstChar { it.uppercase() }} Block"), block)

	val ALUMINUM_INGOT = registerOreIngot("ALUMINUM_INGOT", "aluminum", true)
	val RAW_ALUMINUM = registerRawOre("RAW_ALUMINUM", "aluminum")
	val ALUMINUM_ORE: CustomBlockItem = registerOreBlock("ALUMINUM_ORE", "aluminum", block = CustomBlocks::ALUMINUM_ORE)
	val ALUMINUM_BLOCK: CustomBlockItem = registerIngotBlock("ALUMINUM_BLOCK", "aluminum", block = CustomBlocks::ALUMINUM_BLOCK)
	val RAW_ALUMINUM_BLOCK: CustomBlockItem = registerRawBlock("RAW_ALUMINUM_BLOCK", "aluminum", block = CustomBlocks::RAW_ALUMINUM_BLOCK)

	val CHETHERITE = registerOreIngot("CHETHERITE", "chetherite", false)
	val CHETHERITE_ORE: CustomBlockItem = registerOreBlock("CHETHERITE_ORE", "chetherite", block = CustomBlocks::CHETHERITE_ORE)
	val CHETHERITE_BLOCK: CustomBlockItem = registerIngotBlock("CHETHERITE_BLOCK", "chetherite", block = CustomBlocks::CHETHERITE_BLOCK)

	val TITANIUM_INGOT = registerOreIngot("TITANIUM_INGOT", "titanium", true)
	val RAW_TITANIUM = registerRawOre("RAW_TITANIUM", "titanium")
	val TITANIUM_ORE: CustomBlockItem = registerOreBlock("TITANIUM_ORE", "titanium", block = CustomBlocks::TITANIUM_ORE)
	val TITANIUM_BLOCK: CustomBlockItem = registerIngotBlock("TITANIUM_BLOCK", "titanium", block = CustomBlocks::TITANIUM_BLOCK)
	val RAW_TITANIUM_BLOCK: CustomBlockItem = registerRawBlock("RAW_TITANIUM_BLOCK", "titanium", block = CustomBlocks::RAW_TITANIUM_BLOCK)

	val URANIUM = registerOreIngot(identifier = "URANIUM", name = "uranium", false)
	val RAW_URANIUM = registerRawOre(identifier = "RAW_URANIUM", name = "uranium")
	val URANIUM_ORE: CustomBlockItem = registerOreBlock(identifier = "URANIUM_ORE", name = "uranium", block = CustomBlocks::URANIUM_ORE)
	val URANIUM_BLOCK: CustomBlockItem = registerIngotBlock(identifier = "URANIUM_BLOCK", name = "uranium", block = CustomBlocks::URANIUM_BLOCK)
	val RAW_URANIUM_BLOCK: CustomBlockItem = registerRawBlock(identifier = "RAW_URANIUM_BLOCK", name = "uranium", block = CustomBlocks::RAW_URANIUM_BLOCK)
	// Minerals end

	// Industry start
	val NETHERITE_CASING: CustomBlockItem = customBlockItem(identifier = "NETHERITE_CASING", model = "industry/netherite_casing", displayName = text("Netherite Casing"), customBlock = CustomBlocks::NETHERITE_CASING)
	val ENRICHED_URANIUM = stackable(identifier = "ENRICHED_URANIUM", text("Enriched Uranium"), "industry/enriched_uranium")
	val ENRICHED_URANIUM_BLOCK: CustomBlockItem = customBlockItem(identifier = "ENRICHED_URANIUM_BLOCK", model = "industry/enriched_uranium_block", displayName = text("Enriched Uranium Block"), customBlock = CustomBlocks::ENRICHED_URANIUM_BLOCK)
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
	val SUPERCONDUCTOR_BLOCK: CustomBlockItem = customBlockItem(identifier = "SUPERCONDUCTOR_BLOCK", model = "industry/superconductor_block", displayName = text("Superconductor Block"), customBlock = CustomBlocks::SUPERCONDUCTOR_BLOCK)
	val SUPERCONDUCTOR_CORE = unStackable(identifier = "SUPERCONDUCTOR_CORE", model = "industry/superconductor_core", displayName = text("Superconductor Core", YELLOW))

	val STEEL_INGOT = stackable(identifier = "STEEL_INGOT", text("Steel Ingot"), "industry/steel_ingot")
	val STEEL_BLOCK: CustomBlockItem = customBlockItem(identifier = "STEEL_BLOCK", model = "industry/steel_block", displayName = text("Steel Block"), customBlock = CustomBlocks::STEEL_BLOCK)
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

	// Starship Components Start
	val BATTLECRUISER_REACTOR_CORE: CustomBlockItem = customBlockItem(identifier = "BATTLECRUISER_REACTOR_CORE", model = "starship/battlecruiser_reactor_core", displayName = text("Battlecruiser Reactor Core", NamedTextColor.WHITE, BOLD), customBlock = CustomBlocks::BATTLECRUISER_REACTOR_CORE)
	val BARGE_REACTOR_CORE: CustomBlockItem = customBlockItem(identifier = "BARGE_REACTOR_CORE", model = "starship/barge_reactor_core", displayName = text("Barge Reactor Core", NamedTextColor.WHITE, BOLD), customBlock = CustomBlocks::BARGE_REACTOR_CORE)
	val CRUISER_REACTOR_CORE: CustomBlockItem = customBlockItem(identifier = "CRUISER_REACTOR_CORE", model = "starship/cruiser_reactor_core", displayName = text("Cruiser Reactor Core", NamedTextColor.WHITE, BOLD), customBlock = CustomBlocks::CRUISER_REACTOR_CORE)
	// Starship Components End

	// Gas canisters start
	private fun canisterName(gasName: Component): Component = ofChildren(gasName, text(" Gas Canister", GRAY))

	val GAS_CANISTER_EMPTY = unStackable("GAS_CANISTER_EMPTY", model = "gas/gas_canister_empty", displayName = text("Empty Gas Canister"))
	val GAS_CANISTER_HYDROGEN = register(GasCanister("GAS_CANISTER_HYDROGEN", "gas/gas_canister_hydrogen", canisterName(text("Hydrogen", RED)), Gasses::HYDROGEN))
	val GAS_CANISTER_NITROGEN = register(GasCanister("GAS_CANISTER_NITROGEN", "gas/gas_canister_nitrogen", canisterName(text("Nitrogen", RED)), Gasses::NITROGEN))
	val GAS_CANISTER_METHANE = register(GasCanister("GAS_CANISTER_METHANE", "gas/gas_canister_methane", canisterName(text("Methane", RED)), Gasses::METHANE))
	val GAS_CANISTER_OXYGEN = register(GasCanister("GAS_CANISTER_OXYGEN", "gas/gas_canister_oxygen", canisterName(text("Oxygen", YELLOW)), Gasses::OXYGEN))
	val GAS_CANISTER_CHLORINE = register(GasCanister("GAS_CANISTER_CHLORINE", "gas/gas_canister_chlorine", canisterName(text("Chlorine", YELLOW)), Gasses::CHLORINE))
	val GAS_CANISTER_FLUORINE = register(GasCanister("GAS_CANISTER_FLUORINE", "gas/gas_canister_fluorine", canisterName(text("Fluorine", YELLOW)), Gasses::FLUORINE))
	val GAS_CANISTER_HELIUM = register(GasCanister("GAS_CANISTER_HELIUM", "gas/gas_canister_helium", canisterName(text("Helium", BLUE)), Gasses::HELIUM))
	val GAS_CANISTER_CARBON_DIOXIDE = register(GasCanister("GAS_CANISTER_CARBON_DIOXIDE", "gas/gas_canister_carbon_dioxide", canisterName(text("Carbon Dioxide", BLUE)), Gasses::CARBON_DIOXIDE))
	// Gas canisters end

	// Tools start
	val BATTERY_A = register(Battery('A', RED, 1000))
	val BATTERY_M = register(Battery('M', GREEN, 2500))
	val BATTERY_G = register(Battery('G', GOLD, 7500))

	val CRATE_PLACER = register(CratePlacer)

	val MULTIMETER = register(MultimeterItem)

	val MULTIBLOCK_TOKEN = register(MultiblockToken)
	val PACKAGED_MULTIBLOCK = register(PackagedMultiblock)
	val MULTIBLOCK_WORKBENCH = register(CustomBlockItem(
		identifier = "MULTIBLOCK_WORKBENCH",
		displayName = text("Multiblock Workbench"),
		customModel = "misc/multiblock_workbench"
	) { CustomBlocks.MULTIBLOCK_WORKBENCH })

	val WRENCH = register(Wrench)

	val ADVANCED_ITEM_EXTRACTOR = customBlockItem("ADVANCED_ITEM_EXTRACTOR", "misc/advanced_item_extractor", text("Advanced Item Extractor"), CustomBlocks::ADVANCED_ITEM_EXTRACTOR)
	val ITEM_FILTER = customBlockItem("ITEM_FILTER", "misc/item_filter", text("Item Filter"), CustomBlocks::ITEM_FILTER)

	private fun formatToolName(tierName: String, tierColor: TextColor, toolName: String) = ofChildren(
		text("$tierName ", tierColor),
		text("Power ", GOLD),
		text(toolName, GRAY)
	)

	val POWER_DRILL_BASIC = register(PowerDrill(
		identifier = "POWER_DRILL_BASIC",
		displayName = formatToolName("Basic", HE_LIGHT_ORANGE, "Drill"),
		modLimit = 2,
		basePowerCapacity = 50_000,
		model = "tool/power_drill_basic"
	))
	val POWER_DRILL_ENHANCED = register(
		PowerDrill(
		identifier = "POWER_DRILL_ENHANCED",
		displayName = formatToolName("Enhanced", fromHexString("#00FFA1")!!, "Drill"),
		modLimit = 4,
		basePowerCapacity = 75_000,
		model = "tool/power_drill_enhanced"
	))
	val POWER_DRILL_ADVANCED = register(
		PowerDrill(identifier = "POWER_DRILL_ADVANCED",
		displayName = formatToolName("Advanced", fromHexString("#B12BC9")!!, "Drill"),
		modLimit = 6,
		basePowerCapacity = 100_000,
		model = "tool/power_drill_advanced"
	))

	val POWER_CHAINSAW_BASIC = register(PowerChainsaw(
		identifier = "POWER_CHAINSAW_BASIC",
		displayName = formatToolName("Basic", HE_LIGHT_ORANGE, "Chainsaw"),
		modLimit = 2,
		basePowerCapacity = 50_000,
		model = "tool/power_chainsaw_basic",
		initialBlocksBroken = 50
	))
	val POWER_CHAINSAW_ENHANCED = register(PowerChainsaw(
		identifier = "POWER_CHAINSAW_ENHANCED",
		displayName = formatToolName("Enhanced", fromHexString("#00FFA1")!!, "Chainsaw"),
		modLimit = 4,
		basePowerCapacity = 75_000,
		model = "tool/power_chainsaw_enhanced",
		initialBlocksBroken = 100
	))
	val POWER_CHAINSAW_ADVANCED = register(PowerChainsaw(
		identifier = "POWER_CHAINSAW_ADVANCED",
		displayName = formatToolName("Advanced", fromHexString("#B12BC9")!!, "Chainsaw"),
		modLimit = 6,
		basePowerCapacity = 100_000,
		model = "tool/power_chainsaw_advanced",
		initialBlocksBroken = 150
	))

	val POWER_HOE_BASIC = register(PowerHoe(
		identifier = "POWER_HOE_BASIC",
		displayName = formatToolName("Basic", HE_LIGHT_ORANGE, "Hoe"),
		modLimit = 2,
		basePowerCapacity = 50_000,
		model = "tool/power_hoe_basic"
	))
	val POWER_HOE_ENHANCED = register(
		PowerHoe(
		identifier = "POWER_HOE_ENHANCED",
		displayName = formatToolName("Enhanced", fromHexString("#00FFA1")!!, "Hoe"),
		modLimit = 4,
		basePowerCapacity = 75_000,
		model = "tool/power_hoe_enhanced"
	))
	val POWER_HOE_ADVANCED = register(
		PowerHoe(
		identifier = "POWER_HOE_ADVANCED",
		displayName = formatToolName("Advanced", fromHexString("#B12BC9")!!, "Hoe"),
		modLimit = 6,
		basePowerCapacity = 100_000,
		model = "tool/power_hoe_advanced"
	))

	val POWER_ARMOR_HELMET = register(PowerArmorItem(
		"POWER_ARMOR_HELMET",
		ofChildren(text("Power ", GOLD), text("Helmet", GRAY)),
		"power_armor/power_armor_helmet",
		EquipmentSlot.HEAD
	))
	val POWER_ARMOR_CHESTPLATE = register(PowerArmorItem(
		"POWER_ARMOR_CHESTPLATE",
		ofChildren(text("Power ", GOLD), text("Chestplate", GRAY)),
		"power_armor/power_armor_chestplate",
		EquipmentSlot.CHEST
	))
	val POWER_ARMOR_LEGGINGS = register(PowerArmorItem(
		"POWER_ARMOR_LEGGINGS",
		ofChildren(text("Power ", GOLD), text("Leggings", GRAY)),
		"power_armor/power_armor_leggings",
		EquipmentSlot.LEGS
	))
	val POWER_ARMOR_BOOTS = register(PowerArmorItem(
		"POWER_ARMOR_BOOTS",
		ofChildren(text("Power ", GOLD), text("Boots", GRAY)),
		"power_armor/power_armor_boots",
		EquipmentSlot.FEET
	))

	val ENERGY_SWORD_BLUE = register(EnergySword("BLUE", BLUE))
	val ENERGY_SWORD_RED = register(EnergySword("RED", RED))
	val ENERGY_SWORD_YELLOW = register(EnergySword("YELLOW", YELLOW))
	val ENERGY_SWORD_GREEN = register(EnergySword("GREEN", GREEN))
	val ENERGY_SWORD_PURPLE = register(EnergySword("PURPLE", DARK_PURPLE))
	val ENERGY_SWORD_ORANGE = register(EnergySword("ORANGE", fromHexString("#FF5F15")!!))
	val ENERGY_SWORD_PINK = register(EnergySword("PINK", fromHexString("#FFC0CB")!!))
	val ENERGY_SWORD_BLACK = register(EnergySword("BLACK", BLACK))

	val ARMOR_MODIFICATION_ENVIRONMENT: ModificationItem = register(ModificationItem(
		"ARMOR_MODIFICATION_ENVRORNMENT",
		"power_armor/module/environment",
		ofChildren(text("Enviornment", GRAY), text(" Module", GOLD)),
		text("Allows the user to survive inhospitable planetary enviornments.")
	) { ItemModRegistry.ENVIRONMENT })
	val ARMOR_MODIFICATION_NIGHT_VISION: ModificationItem = register(ModificationItem(
		"ARMOR_MODIFICATION_NIGHT_VISION",
		"power_armor/module/night_vision",
		ofChildren(text("Night Vision", GRAY), text(" Module", GOLD)),
		text("Allows the user to see in dark enviornments. ")
	) { ItemModRegistry.NIGHT_VISION })
	val ARMOR_MODIFICATION_PRESSURE_FIELD: ModificationItem = register(ModificationItem(
		"ARMOR_MODIFICATION_PRESSURE_FIELD",
		"power_armor/module/pressure_field",
		ofChildren(text("Pressure Field", GRAY), text(" Module", GOLD)),
		text("Allows the user to breathe in space.")
	) { ItemModRegistry.PRESSURE_FIELD })
	val ARMOR_MODIFICATION_ROCKET_BOOSTING: ModificationItem = register(ModificationItem(
		"ARMOR_MODIFICATION_ROCKET_BOOSTING",
		"power_armor/module/rocket_boosting",
		ofChildren(text("Rocket Boosting", GRAY), text(" Module", GOLD)),
		text("Allows propelled flight.")
	) { ItemModRegistry.ROCKET_BOOSTING })
	val ARMOR_MODIFICATION_SHOCK_ABSORBING: ModificationItem = register(ModificationItem(
		"ARMOR_MODIFICATION_SHOCK_ABSORBING",
		"power_armor/module/shock_absorbing",
		ofChildren(text("Shock Absorbing", GRAY), text(" Module", GOLD)),
		text("Reduces knockback.")
	) { ItemModRegistry.SHOCK_ABSORBING })
	val ARMOR_MODIFICATION_SPEED_BOOSTING: ModificationItem = register(ModificationItem(
		"ARMOR_MODIFICATION_SPEED_BOOSTING",
		"power_armor/module/speed_boosting",
		ofChildren(text("Speed Boosting", GRAY), text(" Module", GOLD)),
		text("Boosts the user's running speed.")
	) { ItemModRegistry.SPEED_BOOSTING })

	val RANGE_1: ModificationItem = register(ModificationItem(
		"TOOL_MODIFICATION_RANGE_1",
		"tool/modification/drill_aoe_1",
		text("Range Addon +1"),
		text("Expands the working area by 1 block", GRAY)
	) { ItemModRegistry.AOE_1 })
	val RANGE_2: ModificationItem = register(ModificationItem(
		"TOOL_MODIFICATION_RANGE_2",
		"tool/modification/drill_aoe_2",
		text("Range Addon +2"),
		text("Expands the working area by 2 blocks", GRAY)
	) { ItemModRegistry.AOE_2 })
	val VEIN_MINER_25: ModificationItem = register(ModificationItem(
		"TOOL_MODIFICATION_VEIN_MINER_25",
		"tool/modification/drill_vein_miner_25",
		text("Vein Miner"),
		text("Allows a drill to mine veins of connected blocks, up to 25.", GRAY)
	) { ItemModRegistry.VEIN_MINER_25 })
	val SILK_TOUCH_MOD: ModificationItem = register(ModificationItem(
		"TOOL_MODIFICATION_SILK_TOUCH_MOD",
		"tool/modification/silk_touch",
		text("Silk Touch Modifier"),
		text("Applies silk touch to drops", GRAY),
		text("Incurs a power usage penalty", RED)
	) { ItemModRegistry.SILK_TOUCH })
	val AUTO_SMELT: ModificationItem = register(ModificationItem(
		"TOOL_MODIFICATION_AUTO_SMELT",
		"tool/modification/auto_smelt",
		text("Auto Smelt Modifier"),
		text("Sears the drops before they hit the ground", GRAY),
		text("Incurs a power usage penalty", RED)
	) { ItemModRegistry.AUTO_SMELT })
	val FORTUNE_1: ModificationItem = register(ModificationItem(
		"TOOL_MODIFICATION_FORTUNE_1",
		"tool/modification/fortune_1",
		text("Fortune 1 Modifier"),
		text("Applies fortune 1 touch to drops", GRAY),
		text("Incurs a power usage penalty", RED)
	) { ItemModRegistry.FORTUNE_1 })
	val FORTUNE_2: ModificationItem = register(ModificationItem(
		"TOOL_MODIFICATION_FORTUNE_2",
		"tool/modification/fortune_2",
		text("Fortune 2 Modifier"),
		text("Applies fortune 2 touch to drops", GRAY),
		text("Incurs a power usage penalty", RED)
	) { ItemModRegistry.FORTUNE_2 })
	val FORTUNE_3: ModificationItem = register(ModificationItem(
		"TOOL_MODIFICATION_FORTUNE_3",
		"tool/modification/fortune_3",
		text("Fortune 3 Modifier"),
		text("Applies fortune 3 touch to drops", GRAY),
		text("Incurs a power usage penalty", RED)
	) { ItemModRegistry.FORTUNE_3 })
	val POWER_CAPACITY_25: ModificationItem = register(ModificationItem(
		"TOOL_MODIFICATION_POWER_CAPACITY_25",
		"tool/modification/power_capacity_25",
		text("Small Auxiliary battery"),
		ofChildren(text("Increases power storage by ", HE_MEDIUM_GRAY), powerPrefix, text(25000, GREEN))
	) { ItemModRegistry.POWER_CAPACITY_25 })
	val POWER_CAPACITY_50: ModificationItem = register(ModificationItem(
		"TOOL_MODIFICATION_POWER_CAPACITY_50",
		"tool/modification/power_capacity_50",
		text("Medium Auxiliary battery"),
		ofChildren(text("Increases power storage by ", HE_MEDIUM_GRAY), powerPrefix, text(50000, GREEN))
	) { ItemModRegistry.POWER_CAPACITY_50 })
	val AUTO_REPLANT: ModificationItem = register(ModificationItem(
		"TOOL_MODIFICATION_AUTO_REPLANT",
		"tool/modification/auto_replant",
		text("Auto Replant Modifier"),
		text("Automatically plants back harvested crops and cut trees", GRAY)
	) { ItemModRegistry.AUTO_REPLANT })
	val AUTO_COMPOST: ModificationItem = register(ModificationItem(
		"TOOL_MODIFICATION_AUTO_COMPOST",
		"tool/modification/auto_compost",
		text("Auto Compost Modifier"),
		text("Sends applicable drops through a composter, turning them into bonemeal.", GRAY)
	) { ItemModRegistry.AUTO_COMPOST })
	val COLLECTOR: ModificationItem = register(ModificationItem(
		"TOOL_MODIFICATION_COLLECTOR",
		"tool/modification/collector",
		text("Collector Modifier"),
		text("Sends dropped items directly to your inventory if there is room.", GRAY)
	) { ItemModRegistry.COLLECTOR })
	val RANGE_3: ModificationItem = register(ModificationItem(
		"TOOL_MODIFICATION_RANGE_3",
		"tool/modification/drill_aoe_3",
		text("Range Addon +3"),
		text("Expands the working area by 3 blocks", GRAY)
	) { ItemModRegistry.AOE_3 })
	val EXTENDED_BAR: ModificationItem = register(ModificationItem(
		"TOOL_MODIFICATION_EXTENDED_BAR",
		"tool/modification/extended_bar",
		text("Extended Chainsaw Bar"),
		text("Allows a chainsaw to cut down larger trees", GRAY)
	) { ItemModRegistry.EXTENDED_BAR })
	val FERTILIZER_DISPENSER: ModificationItem = register(ModificationItem(
		"TOOL_MODIFICATION_FERTILIZER_DISPENSER",
		"tool/modification/fertilizer_dispenser",
		text("Fertilizer Sprayer"),
		text("Applies bonemeal to crops in the effected area, if available in the user's inventory", GRAY)
	) { ItemModRegistry.FERTILIZER_DISPENSER })

	val PERSONAL_TRANSPORTER = register(PersonalTransporter)
	// Tools end

	// Planets start
	val AERACH = unStackable(identifier = "AERACH", displayName = text("Aerach"), model = "planet/aerach")
	val ARET = unStackable(identifier = "ARET", displayName = text("Aret"), model = "planet/aret")
	val CHANDRA = unStackable(identifier = "CHANDRA", displayName = text("Chandra"), model = "planet/chandra")
	val CHIMGARA = unStackable(identifier = "CHIMGARA", displayName = text("Chimgara"), model = "planet/chimgara")
	val DAMKOTH = unStackable(identifier = "DAMKOTH", displayName = text("Damkoth"), model = "planet/damkoth")
	val DISTERRA = unStackable(identifier = "DISTERRA", displayName = text("Disterra"), model = "planet/disterra")
	val EDEN = unStackable(identifier = "EDEN", displayName = text("Eden"), model = "planet/eden")
	val GAHARA = unStackable(identifier = "GAHARA", displayName = text("Gahara"), model = "planet/gahara")
	val HERDOLI = unStackable(identifier = "HERDOLI", displayName = text("Herdoli"), model = "planet/herdoli")
	val ILIUS = unStackable(identifier = "ILIUS", displayName = text("Ilius"), model = "planet/ilius")
	val ISIK = unStackable(identifier = "ISIK", displayName = text("Isik"), model = "planet/isik")
	val KOVFEFE = unStackable(identifier = "KOVFEFE", displayName = text("Kovfefe"), model = "planet/kovfefe")
	val KRIO = unStackable(identifier = "KRIO", displayName = text("Krio"), model = "planet/krio")
	val LIODA = unStackable(identifier = "LIODA", displayName = text("Lioda"), model = "planet/lioda")
	val LUXITERNA = unStackable(identifier = "LUXITERNA", displayName = text("Luxiterna"), model = "planet/luxiterna")
	val QATRA = unStackable(identifier = "QATRA", displayName = text("Qatra"), model = "planet/qatra")
	val RUBACIEA = unStackable(identifier = "RUBACIEA", displayName = text("Rubaciea"), model = "planet/rubaciea")
	val TURMS = unStackable(identifier = "TURMS", displayName = text("Turms"), model = "planet/turms")
	val VASK = unStackable(identifier = "VASK", displayName = text("Vask"), model = "planet/vask")
	val ASTERI = unStackable(identifier = "ASTERI", displayName = text("Asteri"), model = "planet/asteri")
	val HORIZON = unStackable(identifier = "HORIZON", displayName = text("Horizon"), model = "planet/horizon")
	val ILIOS = unStackable(identifier = "ILIOS", displayName = text("Ilios"), model = "planet/ilios")
	val REGULUS = unStackable(identifier = "REGULUS", displayName = text("Regulus"), model = "planet/regulus")
	val SIRIUS = unStackable(identifier = "SIRIUS", displayName = text("Sirius"), model = "planet/sirius")
	val PLANET_SELECTOR = unStackable(identifier = "PLANET_SELECTOR", displayName = text("PLANET_SELECTOR"), model = "planet/planet_selector")


	init {
		sortCustomItemListeners()
	}

	private fun <T : CustomItem> register(item: T): T {
		customItems[item.identifier] = item
		return item
	}

	private fun register(identifier: String, displayName: Component, factory: ItemFactory): CustomItem {
		return register(CustomItem(identifier, displayName, factory))
	}

	private fun stackable(identifier: String, displayName: Component, model: String): CustomItem {
		return register(CustomItem(identifier, displayName, stackableCustomItem(model = model)))
	}

	private fun unStackable(identifier: String, displayName: Component, model: String): CustomItem {
		return register(CustomItem(identifier, displayName, unStackableCustomItem(model = model)))
	}

	private fun customBlockItem(identifier: String, model: String, displayName: Component, customBlock: Supplier<CustomBlock>) =
		register(CustomBlockItem(identifier, model, displayName, customBlock))

	val ItemStack.customItem: CustomItem? get() {
		return customItems[persistentDataContainer.get(CUSTOM_ITEM, STRING) ?: return null]
	}

	val identifiers = customItems.keys

	fun getByIdentifier(identifier: String): CustomItem? = customItems[identifier.uppercase()]
}
