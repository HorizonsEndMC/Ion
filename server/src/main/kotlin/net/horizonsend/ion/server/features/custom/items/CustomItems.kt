package net.horizonsend.ion.server.features.custom.items

import net.horizonsend.ion.common.utils.text.colors.HEColorScheme.Companion.HE_LIGHT_ORANGE
import net.horizonsend.ion.common.utils.text.colors.HEColorScheme.Companion.HE_MEDIUM_GRAY
import net.horizonsend.ion.common.utils.text.ofChildren
import net.horizonsend.ion.common.utils.text.text
import net.horizonsend.ion.server.IonServer
import net.horizonsend.ion.server.configuration.PVPBalancingConfiguration
import net.horizonsend.ion.server.configuration.PVPBalancingConfiguration.EnergyWeapons.Multishot
import net.horizonsend.ion.server.configuration.PVPBalancingConfiguration.EnergyWeapons.Singleshot
import net.horizonsend.ion.server.features.custom.blocks.CustomBlock
import net.horizonsend.ion.server.features.custom.blocks.CustomBlocks
import net.horizonsend.ion.server.features.custom.items.blasters.Blaster
import net.horizonsend.ion.server.features.custom.items.blasters.Magazine
import net.horizonsend.ion.server.features.custom.items.minerals.MineralItem
import net.horizonsend.ion.server.features.custom.items.minerals.Smeltable
import net.horizonsend.ion.server.features.custom.items.misc.PersonalTransporter
import net.horizonsend.ion.server.features.custom.items.misc.ProgressHolder
import net.horizonsend.ion.server.features.custom.items.mods.ItemModRegistry
import net.horizonsend.ion.server.features.custom.items.mods.ModificationItem
import net.horizonsend.ion.server.features.custom.items.powered.CratePlacer
import net.horizonsend.ion.server.features.custom.items.powered.PowerChainsaw
import net.horizonsend.ion.server.features.custom.items.powered.PowerDrill
import net.horizonsend.ion.server.features.custom.items.powered.PowerHoe
import net.horizonsend.ion.server.features.custom.items.throwables.ThrowableCustomItem
import net.horizonsend.ion.server.features.custom.items.throwables.ThrownCustomItem
import net.horizonsend.ion.server.features.custom.items.throwables.ThrownPumpkinGrenade
import net.horizonsend.ion.server.features.custom.items.throwables.thrown.ThrownDetonator
import net.horizonsend.ion.server.features.custom.items.throwables.thrown.ThrownSmokeGrenade
import net.horizonsend.ion.server.features.machine.PowerMachines
import net.horizonsend.ion.server.miscellaneous.registrations.NamespacedKeys.CUSTOM_ITEM
import net.horizonsend.ion.server.miscellaneous.utils.Tasks
import net.horizonsend.ion.server.miscellaneous.utils.text.itemName
import net.horizonsend.ion.server.miscellaneous.utils.updateMeta
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.format.NamedTextColor.BLUE
import net.kyori.adventure.text.format.NamedTextColor.DARK_GREEN
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
import org.bukkit.Material
import org.bukkit.Material.DIAMOND_HOE
import org.bukkit.Material.GOLDEN_HOE
import org.bukkit.Material.IRON_BLOCK
import org.bukkit.Material.IRON_HOE
import org.bukkit.Material.IRON_INGOT
import org.bukkit.Material.IRON_ORE
import org.bukkit.Material.RAW_IRON
import org.bukkit.Material.RAW_IRON_BLOCK
import org.bukkit.Material.WARPED_FUNGUS_ON_A_STICK
import org.bukkit.entity.Entity
import org.bukkit.entity.Item
import org.bukkit.entity.LivingEntity
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType.STRING
import java.util.function.Supplier
import kotlin.math.roundToInt

// budget minecraft registry lmao
@Suppress("unused")
object CustomItems {
	// If we want to be extra fancy we can replace this with some fastutils thing later .
	val ALL get() = customItems.values
	private val customItems: MutableMap<String, CustomItem> = mutableMapOf()

	// Magazines Start
	val STANDARD_MAGAZINE = register(Magazine("STANDARD_MAGAZINE", WARPED_FUNGUS_ON_A_STICK, 1, text("Standard Magazine").decoration(ITALIC, false), IonServer.pvpBalancing.energyWeapons::standardMagazine))
	val SPECIAL_MAGAZINE = register(Magazine("SPECIAL_MAGAZINE", WARPED_FUNGUS_ON_A_STICK, 2, text("Special Magazine").decoration(ITALIC, false), IonServer.pvpBalancing.energyWeapons::specialMagazine))
	// Magazines End
	// Guns Start
	val PISTOL = register(Blaster(
		identifier = "PISTOL",
		material = DIAMOND_HOE,
		customModelData = 1,
		displayName = text("Blaster Pistol", RED, BOLD).decoration(ITALIC, false),
		magazineType = STANDARD_MAGAZINE,
		particleSize = 0.25f,
		soundRange = 50.0,
		soundFire = "horizonsend:blaster.pistol.shoot",
		soundWhizz = "horizonsend:blaster.whizz.standard",
		soundShell = "horizonsend:blaster.pistol.shell",
		soundReloadStart = "horizonsend:blaster.pistol.reload.start",
		soundReloadFinish = "horizonsend:blaster.pistol.reload.finish",
		explosiveShot = false,
		balancingSupplier = IonServer.pvpBalancing.energyWeapons::pistol
	))
	val RIFLE = register(Blaster(
		identifier = "RIFLE",
		material = IRON_HOE,
		customModelData = 1,
		displayName = text("Blaster Rifle", RED, BOLD).decoration(ITALIC, false),
		magazineType = STANDARD_MAGAZINE,
		particleSize = 0.25f,
		soundRange = 50.0,
		soundFire = "horizonsend:blaster.rifle.shoot",
		soundWhizz = "horizonsend:blaster.whizz.standard",
		soundShell = "horizonsend:blaster.rifle.shell",
		soundReloadStart = "horizonsend:blaster.rifle.reload.start",
		soundReloadFinish = "horizonsend:blaster.rifle.reload.finish",
		explosiveShot = false,
		balancingSupplier = IonServer.pvpBalancing.energyWeapons::rifle
	))
	val SUBMACHINE_BLASTER = register(object : Blaster<Singleshot>(
		identifier = "SUBMACHINE_BLASTER",
		material = IRON_HOE,
		customModelData = 2,
		displayName = text("Submachine Blaster", RED, BOLD).decoration(ITALIC, false),
		magazineType = STANDARD_MAGAZINE,
		particleSize = 0.25f,
		soundRange = 50.0,
		soundFire = "horizonsend:blaster.submachine_blaster.shoot",
		soundWhizz = "horizonsend:blaster.whizz.standard",
		soundShell = "horizonsend:blaster.submachine_blaster.shell",
		soundReloadStart = "horizonsend:blaster.submachine_blaster.reload.start",
		soundReloadFinish = "horizonsend:blaster.submachine_blaster.reload.finish",
		explosiveShot = false,
		balancingSupplier = IonServer.pvpBalancing.energyWeapons::submachineBlaster
	) {
		// Allows fire above 300 rpm
		override fun handleSecondaryInteract(
			livingEntity: LivingEntity,
			itemStack: ItemStack,
			event: PlayerInteractEvent?
		) {
			val repeatCount = if (balancing.timeBetweenShots >= 4) {
				1
			} else {
				(4.0 / balancing.timeBetweenShots).roundToInt()
			}
			val division = 4.0 / balancing.timeBetweenShots
			for (count in 0 until repeatCount) {
				val delay = (count * division).toLong()
				if (delay > 0) {
					Tasks.syncDelay(delay) { super.handleSecondaryInteract(livingEntity, itemStack, event) }
				} else {
					super.handleSecondaryInteract(livingEntity, itemStack, event)
				}
			}
		}
	})
	val SHOTGUN = register(object : Blaster<Multishot>(
		identifier = "SHOTGUN",
		material = GOLDEN_HOE,
		customModelData = 1,
		displayName = text("Blaster Shotgun", RED, BOLD).decoration(ITALIC, false),
		magazineType = SPECIAL_MAGAZINE,
		particleSize = 0.25f,
		soundRange = 50.0,
		soundFire = "horizonsend:blaster.shotgun.shoot",
		soundWhizz = "horizonsend:blaster.whizz.standard",
		soundShell = "horizonsend:blaster.shotgun.shell",
		soundReloadStart = "horizonsend:blaster.shotgun.reload.start",
		soundReloadFinish = "horizonsend:blaster.shotgun.reload.finish",
		explosiveShot = false,
		balancingSupplier = IonServer.pvpBalancing.energyWeapons::shotgun
	) {
		override fun fireProjectiles(livingEntity: LivingEntity) {
			for (i in 1..balancing.shotCount) super.fireProjectiles(livingEntity)
		}
	})
	val SNIPER = register(Blaster(
		identifier = "SNIPER",
		material = GOLDEN_HOE,
		customModelData = 2,
		displayName = text("Blaster Sniper", RED, BOLD).decoration(ITALIC, false),
		magazineType = SPECIAL_MAGAZINE,
		particleSize = 0.5f,
		soundRange = 100.0,
		soundFire = "horizonsend:blaster.sniper.shoot",
		soundWhizz = "horizonsend:blaster.whizz.sniper",
		soundShell = "horizonsend:blaster.sniper.shell",
		soundReloadStart = "horizonsend:blaster.sniper.reload.start",
		soundReloadFinish = "horizonsend:blaster.sniper.reload.finish",
		explosiveShot = false,
		balancingSupplier = IonServer.pvpBalancing.energyWeapons::sniper
	))
	val CANNON = register(Blaster(
		identifier = "CANNON",
		material = IRON_HOE,
		customModelData = 3,
		displayName = text("Blaster Cannon", RED, BOLD).decoration(ITALIC, false),
		magazineType = STANDARD_MAGAZINE,
		particleSize = 0.80f,
		soundRange = 50.0,
		soundFire = "horizonsend:blaster.cannon.shoot",
		soundWhizz = "horizonsend:blaster.whizz.standard",
		soundShell = "horizonsend:blaster.sniper.shell",
		soundReloadStart = "horizonsend:blaster.cannon.reload.start",
		soundReloadFinish = "horizonsend:blaster.cannon.reload.finish",
		explosiveShot = true,
		balancingSupplier = IonServer.pvpBalancing.energyWeapons::cannon
	))
	// Guns End
	// Gun Parts Start
	val GUN_BARREL = registerSimpleUnstackable("GUN_BARREL", 500, text("Gun Barrel"))
	val CIRCUITRY = registerSimpleUnstackable("CIRCUITRY", 501, text("Circuitry"))

	val PISTOL_RECEIVER = registerSimpleUnstackable("PISTOL_RECEIVER", 502, text("Pistol Receiver"))
	val RIFLE_RECEIVER = registerSimpleUnstackable("RIFLE_RECEIVER", 503, text("Rifle Receiver"))
	val SMB_RECEIVER = registerSimpleUnstackable("SMB_RECEIVER", 504, text("SMB Receiver"))
	val SNIPER_RECEIVER = registerSimpleUnstackable("SNIPER_RECEIVER", 505, text("Sniper Receiver"))
	val SHOTGUN_RECEIVER = registerSimpleUnstackable("SHOTGUN_RECEIVER", 506, text("Shotgun Receiver"))
	val CANNON_RECEIVER = registerSimpleUnstackable("CANNON_RECEIVER", 507, text("Cannon Receiver"))
	// Gun Parts End
	// Minerals start
	val ALUMINUM_INGOT = registerSimpleStackable(identifier = "ALUMINUM_INGOT", customModelData = 1, displayName = text("Aluminum Ingot").decoration(ITALIC, false))
	val RAW_ALUMINUM : MineralItem = register(object : MineralItem(
		identifier = "RAW_ALUMINUM",
		material = RAW_IRON,
		customModelData = 1,
		displayName = text("Raw Aluminum").decoration(ITALIC, false)
	), Smeltable {
		override val smeltingResult: Supplier<ItemStack> = Supplier { ALUMINUM_INGOT.constructItemStack() }
	})
	val ALUMINUM_ORE : CustomBlockItem = register(object : CustomBlockItem(
		identifier = "ALUMINUM_ORE",
		material = IRON_ORE,
		customModelData = 1,
		displayName = text("Aluminum Ore").decoration(ITALIC, false),
		customBlockSupplier = { CustomBlocks.ALUMINUM_ORE }
	), Smeltable {
		override val smeltingResult: Supplier<ItemStack> = Supplier { ALUMINUM_INGOT.constructItemStack() }
	})
	val ALUMINUM_BLOCK = registerCustomBlockItem(identifier = "ALUMINUM_BLOCK", baseBlock = IRON_BLOCK, customModelData = 1, displayName = text("Aluminum Block")) { CustomBlocks.ALUMINUM_BLOCK }
	val RAW_ALUMINUM_BLOCK = registerCustomBlockItem(identifier = "RAW_ALUMINUM_BLOCK", baseBlock = RAW_IRON_BLOCK, customModelData = 1, displayName = text("Raw Aluminum Block").decoration(ITALIC, false)) { CustomBlocks.RAW_ALUMINUM_BLOCK }
	val CHETHERITE = registerSimpleStackable(identifier = "CHETHERITE", customModelData = 2, displayName = text("Chetherite"))
	val CHETHERITE_ORE : CustomBlockItem = register(object : CustomBlockItem(
		identifier = "CHETHERITE_ORE",
		material = IRON_ORE,
		customModelData = 2,
		displayName = text("Chetherite Ore").decoration(ITALIC, false),
		customBlockSupplier = { CustomBlocks.CHETHERITE_ORE }
	), Smeltable {
		override val smeltingResult: Supplier<ItemStack> = Supplier { CHETHERITE.constructItemStack() }
	})
	val CHETHERITE_BLOCK = registerCustomBlockItem(identifier = "CHETHERITE_BLOCK", baseBlock = IRON_BLOCK, customModelData = 2, displayName = text("Chetherite Block").decoration(ITALIC, false)) { CustomBlocks.CHETHERITE_BLOCK }
	val TITANIUM_INGOT = registerSimpleStackable(identifier = "TITANIUM_INGOT", customModelData = 3, displayName = text("Titanium Ingot"))
	val RAW_TITANIUM : MineralItem = register(object : MineralItem(
		identifier = "RAW_TITANIUM",
		material = RAW_IRON,
		customModelData = 3,
		displayName = text("Raw Titanium").decoration(ITALIC, false)
	), Smeltable {
		override val smeltingResult: Supplier<ItemStack> = Supplier { TITANIUM_INGOT.constructItemStack() }
	})
	val TITANIUM_ORE : CustomBlockItem = register(object : CustomBlockItem(
		identifier = "TITANIUM_ORE",
		material = IRON_ORE,
		customModelData = 3,
		displayName = text("Titanium Ore").decoration(ITALIC, false),
		customBlockSupplier = { CustomBlocks.TITANIUM_ORE }
	), Smeltable {
		override val smeltingResult: Supplier<ItemStack> = Supplier { TITANIUM_INGOT.constructItemStack() }
	})
	val TITANIUM_BLOCK = registerCustomBlockItem(identifier = "TITANIUM_BLOCK", baseBlock = IRON_BLOCK, customModelData = 3, displayName = text("Titanium Block")) { CustomBlocks.TITANIUM_BLOCK }
	val RAW_TITANIUM_BLOCK = registerCustomBlockItem(identifier = "RAW_TITANIUM_BLOCK", baseBlock = RAW_IRON_BLOCK, customModelData = 3, displayName = text("Raw Titanium Block").decoration(ITALIC, false)) { CustomBlocks.RAW_TITANIUM_BLOCK }
	val URANIUM = registerSimpleStackable(identifier = "URANIUM", customModelData = 4, displayName = text("Uranium").decoration(ITALIC, false))
	val RAW_URANIUM : MineralItem = register(object : MineralItem(
		identifier = "RAW_URANIUM",
		material = RAW_IRON,
		customModelData = 4,
		displayName = text("Raw Uranium").decoration(ITALIC, false)
	), Smeltable {
		override val smeltingResult: Supplier<ItemStack> = Supplier { URANIUM.constructItemStack() }
	})
	val URANIUM_ORE : CustomBlockItem = register(object : CustomBlockItem(
		identifier = "URANIUM_ORE",
		material = IRON_ORE,
		customModelData = 4,
		displayName = text("Uranium Ore").decoration(ITALIC, false),
		customBlockSupplier = { CustomBlocks.URANIUM_ORE }
	), Smeltable {
		override val smeltingResult: Supplier<ItemStack> = Supplier { URANIUM.constructItemStack() }
	})
	val URANIUM_BLOCK = registerCustomBlockItem(identifier = "URANIUM_BLOCK", baseBlock = IRON_BLOCK, customModelData = 4, displayName = text("Uranium Block").decoration(ITALIC, false)) { CustomBlocks.URANIUM_BLOCK }
	val RAW_URANIUM_BLOCK = registerCustomBlockItem(identifier = "RAW_URANIUM_BLOCK", baseBlock = RAW_IRON_BLOCK, customModelData = 4, displayName = text("Raw Uranium Block").decoration(ITALIC, false)) { CustomBlocks.RAW_URANIUM_BLOCK }
	// Minerals end
	// Industry start
	val NETHERITE_CASING = registerCustomBlockItem(identifier = "NETHERITE_CASING", baseBlock = IRON_BLOCK, customModelData = 1400, displayName = text("Netherite Casing").decoration(ITALIC, false)) { CustomBlocks.NETHERITE_CASING }
	val ENRICHED_URANIUM = registerSimpleStackable(identifier = "ENRICHED_URANIUM", customModelData = 1000, displayName = text("Enriched Uranium"))
	val ENRICHED_URANIUM_BLOCK = registerCustomBlockItem(identifier = "ENRICHED_URANIUM_BLOCK", baseBlock = IRON_BLOCK, customModelData = 1000, displayName = text("Enriched Uranium Block")) { CustomBlocks.ENRICHED_URANIUM_BLOCK }
	val URANIUM_CORE = registerSimpleUnstackable(identifier = "URANIUM_CORE", customModelData = 2000, displayName = text("Uranium Core"))
	val URANIUM_ROD = registerSimpleUnstackable(identifier = "URANIUM_ROD", customModelData = 2001, displayName = text("Uranium Rod"))
	val FUEL_ROD_CORE = registerSimpleUnstackable(identifier = "FUEL_ROD_CORE", customModelData = 2002, displayName = text("Fuel Rod Core"))
	val FUEL_CELL = registerSimpleUnstackable(identifier = "FUEL_CELL", customModelData = 2003, displayName = text("Fuel Cell"))
	val FUEL_CONTROL = registerSimpleUnstackable(identifier = "FUEL_CONTROL", customModelData = 2004, displayName = text("Fuel Control").decoration(BOLD, true))
	// Reactive line
	val REACTIVE_COMPONENT = registerSimpleUnstackable(identifier = "REACTIVE_COMPONENT", customModelData = 2005, displayName = text("Reactive Component"))
	val REACTIVE_HOUSING = registerSimpleUnstackable(identifier = "REACTIVE_HOUSING", customModelData = 2006, displayName = text("Reactive Housing"))
	val REACTIVE_PLATING = registerSimpleUnstackable(identifier = "REACTIVE_PLATING", customModelData = 2007, displayName = text("Reactive Plating"))
	val REACTIVE_CHASSIS = registerSimpleUnstackable(identifier = "REACTIVE_CHASSIS", customModelData = 2008, displayName = text("Reactive Chassis"))
	val REACTIVE_MEMBRANE = registerSimpleUnstackable(identifier = "REACTIVE_MEMBRANE", customModelData = 2009, displayName = text("Reactive Membrane"))
	val REACTIVE_ASSEMBLY = registerSimpleUnstackable(identifier = "REACTIVE_ASSEMBLY", customModelData = 2010, displayName = text("Reactive Assembly"))
	val FABRICATED_ASSEMBLY = registerSimpleUnstackable(identifier = "FABRICATED_ASSEMBLY", customModelData = 2011, displayName = text("Fabricated Assembly"))
	// Circuitry line
	val CIRCUIT_BOARD = registerSimpleUnstackable(identifier = "CIRCUIT_BOARD", customModelData = 2012, displayName = text("Circuit Board"))
	val MOTHERBOARD = registerSimpleUnstackable(identifier = "MOTHERBOARD", customModelData = 2013, displayName = text("Motherboard"))
	val REACTOR_CONTROL = registerSimpleUnstackable(identifier = "REACTOR_CONTROL", customModelData = 2014, displayName = text("Reactor Control").decoration(BOLD, true))
	// Superconductor line
	val SUPERCONDUCTOR = registerSimpleUnstackable(identifier = "SUPERCONDUCTOR", customModelData = 2015, displayName = text("Superconductor"))
	val SUPERCONDUCTOR_BLOCK = registerCustomBlockItem(identifier = "SUPERCONDUCTOR_BLOCK", baseBlock = IRON_BLOCK, customModelData = 1002, displayName = text("Superconductor Block")) { CustomBlocks.SUPERCONDUCTOR_BLOCK }
	val SUPERCONDUCTOR_CORE = registerSimpleUnstackable(identifier = "SUPERCONDUCTOR_CORE", customModelData = 2016, displayName = text("Superconductor Core", BOLD))
	// Steel line
	val STEEL_INGOT = registerSimpleStackable(identifier = "STEEL_INGOT", customModelData = 1001, displayName = text("Steel Ingot"))
	val STEEL_BLOCK = registerCustomBlockItem(identifier = "STEEL_BLOCK", baseBlock = IRON_BLOCK, customModelData = 1001, displayName = text("Steel Block")) { CustomBlocks.STEEL_BLOCK }
	val STEEL_PLATE = registerSimpleUnstackable(identifier = "STEEL_PLATE", customModelData = 2017, displayName = text("Steel Plate"))
	val STEEL_CHASSIS = registerSimpleUnstackable(identifier = "STEEL_CHASSIS", customModelData = 2018, displayName = text("Steel Chassis"))
	val STEEL_MODULE = registerSimpleUnstackable(identifier = "STEEL_MODULE", customModelData = 2019, displayName = text("Steel Module"))
	val STEEL_ASSEMBLY = registerSimpleUnstackable(identifier = "STEEL_ASSEMBLY", customModelData = 2020, displayName = text("Steel Assembly"))
	val REINFORCED_FRAME = registerSimpleUnstackable(identifier = "REINFORCED_FRAME", customModelData = 2021, displayName = text("Reinforced Frame"))
	val REACTOR_FRAME = registerSimpleUnstackable(identifier = "REACTOR_FRAME", customModelData = 2022, displayName = text("Reactor Frame").decoration(BOLD, true))
	val PROGRESS_HOLDER = register(ProgressHolder)
	// Industry End
	// Starship Components Start
	val BATTLECRUISER_REACTOR_CORE = registerCustomBlockItem(identifier = "BATTLECRUISER_REACTOR_CORE", baseBlock = IRON_BLOCK, customModelData = 2000, displayName = text("Battlecruiser Reactor Core", BOLD)) { CustomBlocks.BATTLECRUISER_REACTOR_CORE }
	val BARGE_REACTOR_CORE = registerCustomBlockItem(identifier = "BARGE_REACTOR_CORE", baseBlock = IRON_BLOCK, customModelData = 2002, displayName = text("Barge Reactor Core", BOLD)) { CustomBlocks.BARGE_REACTOR_CORE }
	val CRUISER_REACTOR_CORE = registerCustomBlockItem(identifier = "CRUISER_REACTOR_CORE", baseBlock = IRON_BLOCK, customModelData = 2001, displayName = text("Cruiser Reactor Core", BOLD)) { CustomBlocks.CRUISER_REACTOR_CORE }
	// Starship Components End
	// Ship Ammunition Start
	val UNLOADED_SHELL = registerSimpleUnstackable(identifier = "UNLOADED_SHELL", customModelData = 702, displayName = text("Unloaded Shell"))
	val LOADED_SHELL = registerSimpleStackable(identifier = "LOADED_SHELL", customModelData = 2001, displayName = text("Loaded Shell"))
	val UNCHARGED_SHELL = registerSimpleUnstackable(identifier = "UNCHARGED_SHELL", customModelData = 703, displayName = text("Uncharged Shell"))
	val CHARGED_SHELL = registerSimpleStackable(identifier = "CHARGED_SHELL", customModelData = 2002, displayName = text("Charged Shell"))
	val ARSENAL_MISSILE = registerSimpleStackable(identifier = "ARSENAL_MISSILE", customModelData = 2000, displayName = text("Arsenal Missile"))
	val UNLOADED_ARSENAL_MISSILE = registerSimpleUnstackable(identifier = "UNLOADED_ARSENAL_MISSILE", customModelData = 701, displayName = text("Unloaded Arsenal Missile"))
	val ACTIVATED_ARSENAL_MISSILE = registerSimpleUnstackable(identifier = "ACTIVATED_ARSENAL_MISSILE", customModelData = 700, displayName = text("Activated Arsenal Missile", RED))
	// Ship Ammunition End
	// Gas Canisters Start
	val GAS_CANISTER_EMPTY = registerSimpleUnstackable("GAS_CANISTER_EMPTY", 1000, text("Empty Gas Canister"))
	val GAS_CANISTER_HYDROGEN = register(GasCanister("GAS_CANISTER_HYDROGEN", 1001, canisterName(text("Hydrogen", RED)),"HYDROGEN"))
	val GAS_CANISTER_NITROGEN = register(GasCanister("GAS_CANISTER_NITROGEN", 1002, canisterName(text("Nitrogen", RED)), "NITROGEN"))
	val GAS_CANISTER_METHANE = register(GasCanister("GAS_CANISTER_METHANE", 1003, canisterName(text("Methane", RED)), "METHANE"))
	val GAS_CANISTER_OXYGEN = register( GasCanister("GAS_CANISTER_OXYGEN", 1010, canisterName(text("Oxygen", YELLOW)), "OXYGEN"))
	val GAS_CANISTER_CHLORINE = register(GasCanister("GAS_CANISTER_CHLORINE", 1011, canisterName(text("Chlorine", YELLOW)), "CHLORINE"))
	val GAS_CANISTER_FLUORINE = register(GasCanister("GAS_CANISTER_FLUORINE", 1012, canisterName(text("Fluorine", YELLOW)), "FLUORINE"))
	val GAS_CANISTER_HELIUM = register(GasCanister("GAS_CANISTER_HELIUM", 1020, canisterName(text("Helium", BLUE)), "HELIUM"))
	val GAS_CANISTER_CARBON_DIOXIDE = register(GasCanister("GAS_CANISTER_CARBON_DIOXIDE", 1021, canisterName(text("Carbon Dioxide", BLUE)), "CARBON_DIOXIDE"))

	private fun canisterName(gasName: Component): Component = text()
		.decoration(ITALIC, false)
		.append(gasName)
		.append(text(" Gas Canister", GRAY))
		.build()

	// Gas Canisters End

	// Throwables start
	private fun registerThrowable(identifier: String, customModelData: Int, displayName: Component, balancing: Supplier<PVPBalancingConfiguration.Throwables.ThrowableBalancing>, thrown: (Item, Int, Entity?) -> ThrownCustomItem) =
		register(object : ThrowableCustomItem(identifier = identifier, customModelData = customModelData, displayName = displayName, balancingSupplier = balancing) {
			override fun constructThrownRunnable(item: Item, maxTicks: Int, damageSource: Entity?): ThrownCustomItem = thrown.invoke(item, maxTicks, damageSource)
		})

	val DETONATOR = registerThrowable("DETONATOR", 1101, ofChildren(text("Thermal ", RED), text("Detonator", GRAY)).itemName, IonServer.pvpBalancing.throwables::detonator) { item, maxTicks, source -> ThrownDetonator(item, maxTicks, source, IonServer.pvpBalancing.throwables::detonator) }
	val SMOKE_GRENADE = registerThrowable("SMOKE_GRENADE", 1102, ofChildren(text("Smoke ", DARK_GREEN), text("Grenade", GRAY)).itemName, IonServer.pvpBalancing.throwables::smokeGrenade) { item, maxTicks, source -> ThrownSmokeGrenade(item, maxTicks, source) }

	val PUMPKIN_GRENADE = register(object : ThrowableCustomItem("PUMPKIN_GRENADE", 0, ofChildren(text("Pumpkin ", GOLD), text("Grenade", GREEN)).itemName, IonServer.pvpBalancing.throwables::detonator) {
		override fun constructItemStack(): ItemStack = super.constructItemStack().apply { type = Material.PUMPKIN }.updateMeta { it.lore(mutableListOf(text("Spooky", LIGHT_PURPLE))) }
		override fun constructThrownRunnable(item: Item, maxTicks: Int, damageSource: Entity?): ThrownCustomItem = ThrownPumpkinGrenade(item, maxTicks, damageSource, IonServer.pvpBalancing.throwables::detonator)
	})
	// Throwables end
	// Planets start
	val AERACH = registerSimpleUnstackable("AERACH", 5000, text("Aerach"))
	val ARET = registerSimpleUnstackable("ARET", 5001, text("Aret"))
	val CHANDRA = registerSimpleUnstackable("CHANDRA", 5002, text("Chandra"))
	val CHIMGARA = registerSimpleUnstackable("CHIMGARA", 5003, text("Chimgara"))
	val DAMKOTH = registerSimpleUnstackable("DAMKOTH", 5004, text("Damkoth"))
	val DISTERRA = registerSimpleUnstackable("DISTERRA", 5005, text("Disterra"))
	val EDEN = registerSimpleUnstackable("EDEN", 5006, text("Eden"))
	val GAHARA = registerSimpleUnstackable("GAHARA", 5007, text("Gahara"))
	val HERDOLI = registerSimpleUnstackable("HERDOLI", 5008, text("Herdoli"))
	val ILIUS = registerSimpleUnstackable("ILIUS", 5009, text("Ilius"))
	val ISIK = registerSimpleUnstackable("ISIK", 5010, text("Isik"))
	val KOVFEFE = registerSimpleUnstackable("KOVFEFE", 5011, text("Kovfefe"))
	val KRIO = registerSimpleUnstackable("KRIO", 5012, text("Krio"))
	val LIODA = registerSimpleUnstackable("LIODA", 5013, text("Lioda"))
	val LUXITERNA = registerSimpleUnstackable("LUXITERNA", 5014, text("Luxiterna"))
	val QATRA = registerSimpleUnstackable("QATRA", 5015, text("Qatra"))
	val RUBACIEA = registerSimpleUnstackable("RUBACIEA", 5016, text("Rubaciea"))
	val TURMS = registerSimpleUnstackable("TURMS", 5017, text("Turms"))
	val VASK = registerSimpleUnstackable("VASK", 5018, text("Vask"))
	// Stars
	val ASTERI = registerSimpleUnstackable("ASTERI", 5100, text("Asteri"))
	val HORIZON = registerSimpleUnstackable("HORIZON", 5101, text("Horizon"))
	val ILIOS = registerSimpleUnstackable("ILIOS", 5102, text("Ilios"))
	val REGULUS = registerSimpleUnstackable("REGULUS", 5103, text("Regulus"))
	val SIRIUS = registerSimpleUnstackable("SIRIUS", 5104, text("Sirius"))
	// UI
	val PLANET_SELECTOR = registerSimpleUnstackable("PLANET_SELECTOR", 5900, text("PLANET_SELECTOR"))
	// Planets end
	// Tools begin
	val PERSONAL_TRANSPORTER = register(PersonalTransporter)

	private fun formatToolName(tierName: String, tierColor: TextColor, toolName: String) = ofChildren(
		text("$tierName ", tierColor),
		text("Power ", GOLD),
		text(toolName, GRAY)
	).itemName

	val POWER_DRILL_BASIC = register(PowerDrill(identifier = "POWER_DRILL_BASIC", displayName = formatToolName("Basic", HE_LIGHT_ORANGE, "Drill"), modLimit = 2, basePowerCapacity = 50_000, customModelData = 1))
	val POWER_DRILL_ENHANCED = register(PowerDrill(identifier = "POWER_DRILL_ENHANCED", displayName = formatToolName("Enhanced", fromHexString("#00FFA1")!!, "Drill"), modLimit = 4, basePowerCapacity = 75_000, customModelData = 4))
	val POWER_DRILL_ADVANCED = register(PowerDrill(identifier = "POWER_DRILL_ADVANCED", displayName = formatToolName("Advanced", fromHexString("#B12BC9")!!, "Drill"), modLimit = 6, basePowerCapacity = 100_000, customModelData = 7))
	val POWER_CHAINSAW_BASIC = register(PowerChainsaw(identifier = "POWER_CHAINSAW_BASIC", displayName = formatToolName("Basic", HE_LIGHT_ORANGE, "Chainsaw"), modLimit = 2, basePowerCapacity = 50_000, customModelData = 2, initialBlocksBroken = 50))
	val POWER_CHAINSAW_ENHANCED = register(PowerChainsaw(identifier = "POWER_CHAINSAW_ENHANCED", displayName = formatToolName("Enhanced", fromHexString("#00FFA1")!!, "Chainsaw"), modLimit = 4, basePowerCapacity = 75_000, customModelData = 5, initialBlocksBroken = 100))
	val POWER_CHAINSAW_ADVANCED = register(PowerChainsaw(identifier = "POWER_CHAINSAW_ADVANCED", displayName = formatToolName("Advanced", fromHexString("#B12BC9")!!, "Chainsaw"), modLimit = 6, basePowerCapacity = 100_000, customModelData = 8, initialBlocksBroken = 150))
	val POWER_HOE_BASIC = register(PowerHoe(identifier = "POWER_HOE_BASIC", displayName = formatToolName("Basic", HE_LIGHT_ORANGE, "Hoe"), modLimit = 2, basePowerCapacity = 50_000, customModelData = 3))
	val POWER_HOE_ENHANCED = register(PowerHoe(identifier = "POWER_HOE_ENHANCED", displayName = formatToolName("Enhanced", fromHexString("#00FFA1")!!, "Hoe"), modLimit = 4, basePowerCapacity = 75_000, customModelData = 6))
	val POWER_HOE_ADVANCED = register(PowerHoe(identifier = "POWER_HOE_ADVANCED", displayName = formatToolName("Advanced", fromHexString("#B12BC9")!!, "Hoe"), modLimit = 6, basePowerCapacity = 100_000, customModelData = 9))

	val CRATE_PLACER = register(CratePlacer)

	val RANGE_1: ModificationItem = register(ModificationItem("TOOL_MODIFICATION_RANGE_1", 7000, text("Range Addon +1").itemName, text("Expands the working area by 1 block", GRAY).itemName) { ItemModRegistry.AOE_1 })
	val RANGE_2: ModificationItem = register(ModificationItem("TOOL_MODIFICATION_RANGE_2", 7001, text("Range Addon +2").itemName, text("Expands the working area by 2 blocks", GRAY).itemName) { ItemModRegistry.AOE_2 })
	val VEIN_MINER_25: ModificationItem = register(ModificationItem("TOOL_MODIFICATION_VEIN_MINER_25", 7002, text("Vein Miner").itemName, text("Allows a drill to mine veins of connected blocks, up to 25.", GRAY).itemName) { ItemModRegistry.VEIN_MINER_25 })
	val SILK_TOUCH_MOD: ModificationItem = register(ModificationItem("TOOL_MODIFICATION_SILK_TOUCH_MOD", 7003, text("Silk Touch Modifier").itemName, text("Applies silk touch to drops", GRAY).itemName, text("Incurs a power usage penalty", RED).itemName) { ItemModRegistry.SILK_TOUCH })
	val AUTO_SMELT: ModificationItem = register(ModificationItem("TOOL_MODIFICATION_AUTO_SMELT", 7004, text("Auto Smelt Modifier").itemName, text("Sears the drops before they hit the ground", GRAY).itemName, text("Incurs a power usage penalty", RED).itemName) { ItemModRegistry.AUTO_SMELT })
	val FORTUNE_1: ModificationItem = register(ModificationItem("TOOL_MODIFICATION_FORTUNE_1", 7005, text("Fortune 1 Modifier").itemName, text("Applies fortune 1 touch to drops", GRAY).itemName, text("Incurs a power usage penalty", RED).itemName) { ItemModRegistry.FORTUNE_1 })
	val FORTUNE_2: ModificationItem = register(ModificationItem("TOOL_MODIFICATION_FORTUNE_2", 7006, text("Fortune 2 Modifier").itemName, text("Applies fortune 2 touch to drops", GRAY).itemName, text("Incurs a power usage penalty", RED).itemName) { ItemModRegistry.FORTUNE_2 })
	val FORTUNE_3: ModificationItem = register(ModificationItem("TOOL_MODIFICATION_FORTUNE_3", 7007, text("Fortune 3 Modifier").itemName, text("Applies fortune 3 touch to drops", GRAY).itemName, text("Incurs a power usage penalty", RED).itemName) { ItemModRegistry.FORTUNE_3 })
	val POWER_CAPACITY_25: ModificationItem = register(ModificationItem("TOOL_MODIFICATION_POWER_CAPACITY_25", 7008, text("Small Auxiliary battery").itemName, ofChildren(text("Increases power storage by ", HE_MEDIUM_GRAY), PowerMachines.prefixComponent, text(25000, GREEN)).itemName) { ItemModRegistry.POWER_CAPACITY_25 })
	val POWER_CAPACITY_50: ModificationItem = register(ModificationItem("TOOL_MODIFICATION_POWER_CAPACITY_50", 7009, text("Medium Auxiliary battery").itemName, ofChildren(text("Increases power storage by ", HE_MEDIUM_GRAY), PowerMachines.prefixComponent, text(50000, GREEN)).itemName) { ItemModRegistry.POWER_CAPACITY_50 })
	val AUTO_REPLANT: ModificationItem = register(ModificationItem("TOOL_MODIFICATION_AUTO_REPLANT", 7010, text("Auto Replant Modifier").itemName, text("Automatically plants back harvested crops and cut trees", GRAY).itemName,) { ItemModRegistry.AUTO_REPLANT })
	val AUTO_COMPOST: ModificationItem = register(ModificationItem("TOOL_MODIFICATION_AUTO_COMPOST", 7011, text("Auto Compost Modifier").itemName, text("Sends applicable drops through a composter, turning them into bonemeal.", GRAY).itemName,) { ItemModRegistry.AUTO_COMPOST })
	val RANGE_3: ModificationItem = register(ModificationItem("TOOL_MODIFICATION_RANGE_3", 7012, text("Range Addon +3").itemName, text("Expands the working area by 3 blocks", GRAY).itemName) { ItemModRegistry.AOE_3 })
	val EXTENDED_BAR: ModificationItem = register(ModificationItem("TOOL_MODIFICATION_EXTENDED_BAR", 7013, text("Extended Chainsaw Bar").itemName, text("Allows a chainsaw to cut down larger trees", GRAY).itemName) { ItemModRegistry.EXTENDED_BAR })
	val FERTILIZER_DISPENSER: ModificationItem = register(ModificationItem("TOOL_MODIFICATION_FERTILIZER_DISPENSER", 7014, text("Fertilizer Sprayer").itemName, text("Applies bonemeal to crops in the effected area, if available in the user's inventory", GRAY).itemName) { ItemModRegistry.FERTILIZER_DISPENSER })
	// Tools end

	// This is just a convenient alias for items that don't do anything or are placeholders.
	private fun registerSimpleUnstackable(identifier: String, customModelData: Int, displayName: Component): CustomItem = register(object : CustomItem(identifier) {
		override fun constructItemStack(): ItemStack {
			val formattedDisplayName = text()
				.decoration(ITALIC, false)
				.append(displayName)
				.build()

			return ItemStack(WARPED_FUNGUS_ON_A_STICK).updateMeta {
				it.setCustomModelData(customModelData)
				it.displayName(formattedDisplayName)
				it.persistentDataContainer.set(CUSTOM_ITEM, STRING, identifier)
			}
		}
	})

	private fun registerSimpleStackable(identifier: String, baseItem: Material = IRON_INGOT, customModelData: Int, displayName: Component): CustomItem = register(object : CustomItem(identifier) {
		override fun constructItemStack(): ItemStack {
			val formattedDisplayName = text()
				.decoration(ITALIC, false)
				.append(displayName)
				.build()

			return ItemStack(baseItem).updateMeta {
				it.setCustomModelData(customModelData)
				it.displayName(formattedDisplayName)
				it.persistentDataContainer.set(CUSTOM_ITEM, STRING, identifier)
			}
		}
	})

	private fun registerCustomBlockItem(identifier: String, baseBlock: Material, customModelData: Int, displayName: Component, customBlock: Supplier<CustomBlock>): CustomItem {
		val formattedDisplayName = text()
			.decoration(ITALIC, false)
			.append(displayName)
			.build()

		return register(CustomBlockItem(identifier, baseBlock, customModelData, formattedDisplayName, customBlock))
	}

	private fun <T : CustomItem> register(customItem: T): T {
		customItems[customItem.identifier] = customItem
		return customItem
	}

	val ItemStack.customItem: CustomItem?
		get() {
			return customItems[itemMeta?.persistentDataContainer?.get(CUSTOM_ITEM, STRING) ?: return null]
		}

	val identifiers = customItems.keys

	fun getByIdentifier(identifier: String): CustomItem? = customItems[identifier]
}
