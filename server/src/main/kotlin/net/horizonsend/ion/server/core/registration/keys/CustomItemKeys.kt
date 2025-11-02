package net.horizonsend.ion.server.core.registration.keys

import net.horizonsend.ion.server.configuration.PVPBalancingConfiguration.EnergyWeapons.Multishot
import net.horizonsend.ion.server.configuration.PVPBalancingConfiguration.EnergyWeapons.Singleshot
import net.horizonsend.ion.server.features.custom.items.CustomItem
import net.horizonsend.ion.server.features.custom.items.misc.MultiblockToken
import net.horizonsend.ion.server.features.custom.items.misc.PackagedMultiblock
import net.horizonsend.ion.server.features.custom.items.misc.Wrench
import net.horizonsend.ion.server.features.custom.items.type.CustomBlockItem
import net.horizonsend.ion.server.features.custom.items.type.GasCanister
import net.horizonsend.ion.server.features.custom.items.type.PersonalTransporter
import net.horizonsend.ion.server.features.custom.items.type.armor.PowerArmorItem
import net.horizonsend.ion.server.features.custom.items.type.throwables.ThrowableCustomItem
import net.horizonsend.ion.server.features.custom.items.type.tool.Battery
import net.horizonsend.ion.server.features.custom.items.type.tool.CratePlacer
import net.horizonsend.ion.server.features.custom.items.type.tool.HandheldTank
import net.horizonsend.ion.server.features.custom.items.type.tool.PowerChainsaw
import net.horizonsend.ion.server.features.custom.items.type.tool.PowerDrill
import net.horizonsend.ion.server.features.custom.items.type.tool.PowerHoe
import net.horizonsend.ion.server.features.custom.items.type.tool.mods.ModificationItem
import net.horizonsend.ion.server.features.custom.items.type.weapon.blaster.Blaster
import net.horizonsend.ion.server.features.custom.items.type.weapon.blaster.Magazine
import net.horizonsend.ion.server.features.custom.items.type.weapon.sword.EnergySword

object CustomItemKeys : KeyRegistry<CustomItem>(RegistryKeys.CUSTOM_ITEMS, CustomItem::class) {
	val DETONATOR = registerTypedKey<ThrowableCustomItem>("DETONATOR")
	val SMOKE_GRENADE = registerTypedKey<ThrowableCustomItem>("SMOKE_GRENADE")
	val PUMPKIN_GRENADE = registerTypedKey<ThrowableCustomItem>("PUMPKIN_GRENADE")

	val STANDARD_MAGAZINE = registerTypedKey<Magazine>("STANDARD_MAGAZINE")
	val SPECIAL_MAGAZINE = registerTypedKey<Magazine>("SPECIAL_MAGAZINE")

	val BLASTER_PISTOL = registerTypedKey<Blaster<Singleshot>>("BLASTER_PISTOL")
	val BLASTER_RIFLE = registerTypedKey<Blaster<Singleshot>>("BLASTER_RIFLE")
	val SUBMACHINE_BLASTER = registerTypedKey<Blaster<Singleshot>>("SUBMACHINE_BLASTER")
	val BLASTER_SHOTGUN = registerTypedKey<Blaster<Multishot>>("BLASTER_SHOTGUN")
	val BLASTER_SNIPER = registerTypedKey<Blaster<Singleshot>>("BLASTER_SNIPER")
	val BLASTER_CANNON = registerTypedKey<Blaster<Singleshot>>("BLASTER_CANNON")

	val GUN_BARREL = registerKey("GUN_BARREL")
	val CIRCUITRY = registerKey("CIRCUITRY")

	val PISTOL_RECEIVER = registerKey("PISTOL_RECEIVER")
	val RIFLE_RECEIVER = registerKey("RIFLE_RECEIVER")
	val SMB_RECEIVER = registerKey("SMB_RECEIVER")
	val SNIPER_RECEIVER = registerKey("SNIPER_RECEIVER")
	val SHOTGUN_RECEIVER = registerKey("SHOTGUN_RECEIVER")
	val CANNON_RECEIVER = registerKey("CANNON_RECEIVER")

	val ALUMINUM_INGOT = registerKey("ALUMINUM_INGOT")
	val RAW_ALUMINUM = registerKey("RAW_ALUMINUM")
	val ALUMINUM_ORE = registerTypedKey<CustomBlockItem>("ALUMINUM_ORE")
	val ALUMINUM_BLOCK = registerTypedKey<CustomBlockItem>("ALUMINUM_BLOCK")
	val RAW_ALUMINUM_BLOCK = registerTypedKey<CustomBlockItem>("RAW_ALUMINUM_BLOCK")

	val CHETHERITE = registerKey("CHETHERITE")
	val CHETHERITE_ORE = registerTypedKey<CustomBlockItem>("CHETHERITE_ORE")
	val CHETHERITE_BLOCK = registerTypedKey<CustomBlockItem>("CHETHERITE_BLOCK")

	val TITANIUM_INGOT = registerKey("TITANIUM_INGOT")
	val RAW_TITANIUM = registerKey("RAW_TITANIUM")
	val TITANIUM_ORE = registerTypedKey<CustomBlockItem>("TITANIUM_ORE")
	val TITANIUM_BLOCK = registerTypedKey<CustomBlockItem>("TITANIUM_BLOCK")
	val RAW_TITANIUM_BLOCK = registerTypedKey<CustomBlockItem>("RAW_TITANIUM_BLOCK")

	val URANIUM = registerKey("URANIUM")
	val RAW_URANIUM = registerKey("RAW_URANIUM")
	val URANIUM_ORE = registerTypedKey<CustomBlockItem>("URANIUM_ORE")
	val URANIUM_BLOCK = registerTypedKey<CustomBlockItem>("URANIUM_BLOCK")
	val RAW_URANIUM_BLOCK = registerTypedKey<CustomBlockItem>("RAW_URANIUM_BLOCK")

	val NETHERITE_CASING = registerTypedKey<CustomBlockItem>("NETHERITE_CASING")
	val ENRICHED_URANIUM = registerKey("ENRICHED_URANIUM")
	val ENRICHED_URANIUM_BLOCK = registerTypedKey<CustomBlockItem>("ENRICHED_URANIUM_BLOCK")
	val URANIUM_CORE = registerKey("URANIUM_CORE")
	val URANIUM_ROD = registerKey("URANIUM_ROD")
	val FUEL_ROD_CORE = registerKey("FUEL_ROD_CORE")
	val FUEL_CELL = registerKey("FUEL_CELL")
	val FUEL_CONTROL = registerKey("FUEL_CONTROL")

	val REACTIVE_COMPONENT = registerKey("REACTIVE_COMPONENT")
	val REACTIVE_HOUSING = registerKey("REACTIVE_HOUSING")
	val REACTIVE_PLATING = registerKey("REACTIVE_PLATING")
	val REACTIVE_CHASSIS = registerKey("REACTIVE_CHASSIS")
	val REACTIVE_MEMBRANE = registerKey("REACTIVE_MEMBRANE")
	val REACTIVE_ASSEMBLY = registerKey("REACTIVE_ASSEMBLY")
	val FABRICATED_ASSEMBLY = registerKey("FABRICATED_ASSEMBLY")

	val CIRCUIT_BOARD = registerKey("CIRCUIT_BOARD")
	val MOTHERBOARD = registerKey("MOTHERBOARD")
	val REACTOR_CONTROL = registerKey("REACTOR_CONTROL")

	val SUPERCONDUCTOR = registerKey("SUPERCONDUCTOR")
	val SUPERCONDUCTOR_BLOCK = registerKey("SUPERCONDUCTOR_BLOCK")
	val SUPERCONDUCTOR_CORE = registerKey("SUPERCONDUCTOR_CORE")

	val COPPER_WIRE = registerKey("COPPER_WIRE")

	val STEEL_INGOT = registerKey("STEEL_INGOT")
	val STEEL_BLOCK = registerTypedKey<CustomBlockItem>("STEEL_BLOCK")
	val STEEL_PLATE = registerKey("STEEL_PLATE")
	val STEEL_CHASSIS = registerKey("STEEL_CHASSIS")
	val STEEL_MODULE = registerKey("STEEL_MODULE")
	val STEEL_ASSEMBLY = registerKey("STEEL_ASSEMBLY")
	val REINFORCED_FRAME = registerKey("REINFORCED_FRAME")
	val REACTOR_FRAME = registerKey("REACTOR_FRAME")

	val UNLOADED_SHELL = registerKey("UNLOADED_SHELL")
	val LOADED_SHELL = registerKey("LOADED_SHELL")
	val UNCHARGED_SHELL = registerKey("UNCHARGED_SHELL")
	val CHARGED_SHELL = registerKey("CHARGED_SHELL")

	val ARSENAL_MISSILE = registerKey("ARSENAL_MISSILE")
	val UNLOADED_ARSENAL_MISSILE = registerKey("UNLOADED_ARSENAL_MISSILE")
	val ACTIVATED_ARSENAL_MISSILE = registerKey("ACTIVATED_ARSENAL_MISSILE")

	val PROGRESS_HOLDER = registerKey("PROGRESS_HOLDER")

	val BATTLECRUISER_REACTOR_CORE = registerTypedKey<CustomBlockItem>("BATTLECRUISER_REACTOR_CORE")
	val BARGE_REACTOR_CORE = registerTypedKey<CustomBlockItem>("BARGE_REACTOR_CORE")
	val CRUISER_REACTOR_CORE = registerTypedKey<CustomBlockItem>("CRUISER_REACTOR_CORE")

	val GAS_CANISTER_EMPTY = registerKey("GAS_CANISTER_EMPTY")
	val GAS_CANISTER_HYDROGEN = registerTypedKey<GasCanister>("GAS_CANISTER_HYDROGEN")
	val GAS_CANISTER_NITROGEN = registerTypedKey<GasCanister>("GAS_CANISTER_NITROGEN")
	val GAS_CANISTER_METHANE = registerTypedKey<GasCanister>("GAS_CANISTER_METHANE")
	val GAS_CANISTER_OXYGEN = registerTypedKey<GasCanister>("GAS_CANISTER_OXYGEN")
	val GAS_CANISTER_CHLORINE = registerTypedKey<GasCanister>("GAS_CANISTER_CHLORINE")
	val GAS_CANISTER_FLUORINE = registerTypedKey<GasCanister>("GAS_CANISTER_FLUORINE")
	val GAS_CANISTER_HELIUM = registerTypedKey<GasCanister>("GAS_CANISTER_HELIUM")
	val GAS_CANISTER_CARBON_DIOXIDE = registerTypedKey<GasCanister>("GAS_CANISTER_CARBON_DIOXIDE")

	val BATTERY_A = registerTypedKey<Battery>("BATTERY_A")
	val BATTERY_M = registerTypedKey<Battery>("BATTERY_M")
	val BATTERY_G = registerTypedKey<Battery>("BATTERY_G")

	val CRATE_PLACER = registerTypedKey<CratePlacer>("CRATE_PLACER")

	val MULTIMETER = registerKey("MULTIMETER")

	val MULTIBLOCK_TOKEN = registerTypedKey<MultiblockToken>("MULTIBLOCK_TOKEN")
	val PACKAGED_MULTIBLOCK = registerTypedKey<PackagedMultiblock>("PACKAGED_MULTIBLOCK")
	val MULTIBLOCK_WORKBENCH = registerTypedKey<CustomBlockItem>("MULTIBLOCK_WORKBENCH")
	val WRENCH = registerTypedKey<Wrench>("WRENCH")

	val ADVANCED_ITEM_EXTRACTOR = registerTypedKey<CustomBlockItem>("ADVANCED_ITEM_EXTRACTOR")
	val ITEM_FILTER = registerTypedKey<CustomBlockItem>("ITEM_FILTER")

	val POWER_DRILL_BASIC = registerTypedKey<PowerDrill>("POWER_DRILL_BASIC")
	val POWER_DRILL_ENHANCED = registerTypedKey<PowerDrill>("POWER_DRILL_ENHANCED")
	val POWER_DRILL_ADVANCED = registerTypedKey<PowerDrill>("POWER_DRILL_ADVANCED")

	val POWER_CHAINSAW_BASIC = registerTypedKey<PowerChainsaw>("POWER_CHAINSAW_BASIC")
	val POWER_CHAINSAW_ENHANCED = registerTypedKey<PowerChainsaw>("POWER_CHAINSAW_ENHANCED")
	val POWER_CHAINSAW_ADVANCED = registerTypedKey<PowerChainsaw>("POWER_CHAINSAW_ADVANCED")

	val POWER_HOE_BASIC = registerTypedKey<PowerHoe>("POWER_HOE_BASIC")
	val POWER_HOE_ENHANCED = registerTypedKey<PowerHoe>("POWER_HOE_ENHANCED")
	val POWER_HOE_ADVANCED = registerTypedKey<PowerHoe>("POWER_HOE_ADVANCED")

	val POWER_ARMOR_HELMET = registerTypedKey<PowerArmorItem>("POWER_ARMOR_HELMET")
	val POWER_ARMOR_CHESTPLATE = registerTypedKey<PowerArmorItem>("POWER_ARMOR_CHESTPLATE")
	val POWER_ARMOR_LEGGINGS = registerTypedKey<PowerArmorItem>("POWER_ARMOR_LEGGINGS")
	val POWER_ARMOR_BOOTS = registerTypedKey<PowerArmorItem>("POWER_ARMOR_BOOTS")

	val ENERGY_SWORD_BLUE = registerTypedKey<EnergySword>("ENERGY_SWORD_BLUE")
	val ENERGY_SWORD_RED = registerTypedKey<EnergySword>("ENERGY_SWORD_RED")
	val ENERGY_SWORD_YELLOW = registerTypedKey<EnergySword>("ENERGY_SWORD_YELLOW")
	val ENERGY_SWORD_GREEN = registerTypedKey<EnergySword>("ENERGY_SWORD_GREEN")
	val ENERGY_SWORD_PURPLE = registerTypedKey<EnergySword>("ENERGY_SWORD_PURPLE")
	val ENERGY_SWORD_ORANGE = registerTypedKey<EnergySword>("ENERGY_SWORD_ORANGE")
	val ENERGY_SWORD_PINK = registerTypedKey<EnergySword>("ENERGY_SWORD_PINK")
	val ENERGY_SWORD_BLACK = registerTypedKey<EnergySword>("ENERGY_SWORD_BLACK")

	val ARMOR_MODIFICATION_ENVIRONMENT = registerTypedKey<ModificationItem>("ARMOR_MODIFICATION_ENVIRONMENT")
	val ARMOR_MODIFICATION_NIGHT_VISION = registerTypedKey<ModificationItem>("ARMOR_MODIFICATION_NIGHT_VISION")
	val ARMOR_MODIFICATION_PRESSURE_FIELD = registerTypedKey<ModificationItem>("ARMOR_MODIFICATION_PRESSURE_FIELD")
	val ARMOR_MODIFICATION_ROCKET_BOOSTING = registerTypedKey<ModificationItem>("ARMOR_MODIFICATION_ROCKET_BOOSTING")
	val ARMOR_MODIFICATION_SHOCK_ABSORBING = registerTypedKey<ModificationItem>("ARMOR_MODIFICATION_SHOCK_ABSORBING")
	val ARMOR_MODIFICATION_SPEED_BOOSTING = registerTypedKey<ModificationItem>("ARMOR_MODIFICATION_SPEED_BOOSTING")

	val TOOL_MODIFICATION_RANGE_1 = registerTypedKey<ModificationItem>("TOOL_MODIFICATION_RANGE_1")
	val TOOL_MODIFICATION_RANGE_2 = registerTypedKey<ModificationItem>("TOOL_MODIFICATION_RANGE_2")
	val TOOL_MODIFICATION_VEIN_MINER_25 = registerTypedKey<ModificationItem>("TOOL_MODIFICATION_VEIN_MINER_25")
	val TOOL_MODIFICATION_SILK_TOUCH_MOD = registerTypedKey<ModificationItem>("TOOL_MODIFICATION_SILK_TOUCH_MOD")
	val TOOL_MODIFICATION_AUTO_SMELT = registerTypedKey<ModificationItem>("TOOL_MODIFICATION_AUTO_SMELT")
	val TOOL_MODIFICATION_FORTUNE_1 = registerTypedKey<ModificationItem>("TOOL_MODIFICATION_FORTUNE_1")
	val TOOL_MODIFICATION_FORTUNE_2 = registerTypedKey<ModificationItem>("TOOL_MODIFICATION_FORTUNE_2")
	val TOOL_MODIFICATION_FORTUNE_3 = registerTypedKey<ModificationItem>("TOOL_MODIFICATION_FORTUNE_3")
	val TOOL_MODIFICATION_POWER_CAPACITY_25 = registerTypedKey<ModificationItem>("TOOL_MODIFICATION_POWER_CAPACITY_25")
	val TOOL_MODIFICATION_POWER_CAPACITY_50 = registerTypedKey<ModificationItem>("TOOL_MODIFICATION_POWER_CAPACITY_50")
	val TOOL_MODIFICATION_AUTO_REPLANT = registerTypedKey<ModificationItem>("TOOL_MODIFICATION_AUTO_REPLANT")
	val TOOL_MODIFICATION_AUTO_COMPOST = registerTypedKey<ModificationItem>("TOOL_MODIFICATION_AUTO_COMPOST")
	val TOOL_MODIFICATION_RANGE_3 = registerTypedKey<ModificationItem>("TOOL_MODIFICATION_RANGE_3")
	val TOOL_MODIFICATION_EXTENDED_BAR = registerTypedKey<ModificationItem>("TOOL_MODIFICATION_EXTENDED_BAR")
	val TOOL_MODIFICATION_FERTILIZER_DISPENSER = registerTypedKey<ModificationItem>("TOOL_MODIFICATION_FERTILIZER_DISPENSER")
	val TOOL_MODIFICATION_COLLECTOR = registerTypedKey<ModificationItem>("TOOL_MODIFICATION_FERTILIZER_DISPENSER")

	val PERSONAL_TRANSPORTER = registerTypedKey<PersonalTransporter>("PERSONAL_TRANSPORTER")

	val AERACH = registerKey("AERACH")
	val ARET = registerKey("ARET")
	val CHANDRA = registerKey("CHANDRA")
	val CHIMGARA = registerKey("CHIMGARA")
	val DAMKOTH = registerKey("DAMKOTH")
	val DISTERRA = registerKey("DISTERRA")
	val EDEN = registerKey("EDEN")
	val GAHARA = registerKey("GAHARA")
	val HERDOLI = registerKey("HERDOLI")
	val ILIUS = registerKey("ILIUS")
	val ISIK = registerKey("ISIK")
	val KOVFEFE = registerKey("KOVFEFE")
	val KRIO = registerKey("KRIO")
	val LIODA = registerKey("LIODA")
	val LUXITERNA = registerKey("LUXITERNA")
	val QATRA = registerKey("QATRA")
	val RUBACIEA = registerKey("RUBACIEA")
	val TURMS = registerKey("TURMS")
	val VASK = registerKey("VASK")
	val ASTERI = registerKey("ASTERI")
	val HORIZON = registerKey("HORIZON")
	val ILIOS = registerKey("ILIOS")
	val REGULUS = registerKey("REGULUS")
	val SIRIUS = registerKey("SIRIUS")
	val PLANET_SELECTOR = registerKey("PLANET_SELECTOR")

	val DEBUG_LINE_RED = registerKey("DEBUG_LINE_RED")
	val DEBUG_LINE_GREEN = registerKey("DEBUG_LINE_GREEN")
	val DEBUG_LINE_BLUE = registerKey("DEBUG_LINE_BLUE")
	val DEBUG_LINE = registerKey("DEBUG_LINE")

	val FLUID_PORT = registerTypedKey<CustomBlockItem>("FLUID_PORT")
	val FLUID_VALVE = registerTypedKey<CustomBlockItem>("FLUID_VALVE")
	val FLUID_PIPE = registerTypedKey<CustomBlockItem>("FLUID_PIPE")
	val FLUID_PIPE_JUNCTION = registerTypedKey<CustomBlockItem>("FLUID_PIPE_JUNCTION")
	val REINFORCED_FLUID_PIPE = registerTypedKey<CustomBlockItem>("REINFORCED_FLUID_PIPE")
	val REINFORCED_FLUID_PIPE_JUNCTION = registerTypedKey<CustomBlockItem>("REINFORCED_FLUID_PIPE_JUNCTION")
	val TEMPERATURE_GAUGE = registerTypedKey<CustomBlockItem>("TEMPERATURE_GAUGE")

	val GRID_ENERGY_PORT = registerTypedKey<CustomBlockItem>("GRID_ENERGY_PORT")

	val COPPER_COIL = registerTypedKey<CustomBlockItem>("COPPER_COIL")
	val ROTATION_SHAFT = registerTypedKey<CustomBlockItem>("ROTATION_SHAFT")
	val REDSTONE_CONTROL_PORT = registerTypedKey<CustomBlockItem>("REDSTONE_CONTROL_PORT")

	val HANDHELD_TANK = registerTypedKey<HandheldTank>("HANDHELD_TANK")
}
