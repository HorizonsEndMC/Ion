package net.horizonsend.ion.server.features.custom.items

import net.horizonsend.ion.common.utils.text.colors.HEColorScheme.Companion.HE_LIGHT_ORANGE
import net.horizonsend.ion.common.utils.text.ofChildren
import net.horizonsend.ion.server.IonServer
import net.horizonsend.ion.server.configuration.PVPBalancingConfiguration
import net.horizonsend.ion.server.configuration.PVPBalancingConfiguration.EnergyWeapons.Multishot
import net.horizonsend.ion.server.configuration.PVPBalancingConfiguration.EnergyWeapons.Singleshot
import net.horizonsend.ion.server.features.custom.blocks.CustomBlocks
import net.horizonsend.ion.server.features.custom.items.blasters.Blaster
import net.horizonsend.ion.server.features.custom.items.blasters.Magazine
import net.horizonsend.ion.server.features.custom.items.minerals.MineralItem
import net.horizonsend.ion.server.features.custom.items.minerals.Smeltable
import net.horizonsend.ion.server.features.custom.items.misc.ProgressHolder
import net.horizonsend.ion.server.features.custom.items.misc.ShellItem
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
import net.horizonsend.ion.server.miscellaneous.registrations.NamespacedKeys.CUSTOM_ITEM
import net.horizonsend.ion.server.miscellaneous.utils.Tasks
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
import org.bukkit.block.Dispenser
import org.bukkit.entity.Entity
import org.bukkit.entity.Item
import org.bukkit.entity.LivingEntity
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType.STRING
import java.util.function.Supplier
import kotlin.math.roundToInt

// budget minecraft registry lmao
object CustomItems {
	// If we want to be extra fancy we can replace this with some fastutils thing later .
	val ALL get() = customItems.values
	private val customItems: MutableMap<String, CustomItem> = mutableMapOf()

	// Magazines Start

	val STANDARD_MAGAZINE = register(object : Magazine<PVPBalancingConfiguration.EnergyWeapons.AmmoStorage>(
		identifier = "STANDARD_MAGAZINE",
		material = WARPED_FUNGUS_ON_A_STICK,
		customModelData = 1,
		displayName = text("Standard Magazine").decoration(ITALIC, false),
		balancingSupplier = IonServer.pvpBalancing.energyWeapons::standardMagazine
	) {})

	val SPECIAL_MAGAZINE = register(object : Magazine<PVPBalancingConfiguration.EnergyWeapons.AmmoStorage>(
		identifier = "SPECIAL_MAGAZINE",
		material = WARPED_FUNGUS_ON_A_STICK,
		customModelData = 2,
		displayName = text("Special Magazine").decoration(ITALIC, false),
		balancingSupplier = IonServer.pvpBalancing.energyWeapons::specialMagazine
	) {})

	// Magazines End
	// Guns Start

	val PISTOL =
		register(
			object : Blaster<Singleshot>(
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
			) {}
		)

	val RIFLE =
		register(
			object : Blaster<Singleshot>(
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
			) {}
		)

	val SUBMACHINE_BLASTER =
		register(
			object : Blaster<Singleshot>(
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
			}
		)

	val SHOTGUN =
		register(
			object : Blaster<Multishot>(
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
			}
		)

	val SNIPER =
		register(
			object : Blaster<Singleshot>(
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
			) {}
		)

	val CANNON =
		register(
			object : Blaster<Singleshot>(
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
			) {}
		)

	// Guns End
	// Gun Parts Start

	val GUN_BARREL = register("GUN_BARREL", 500, text("Gun Barrel"))
	val CIRCUITRY = register("CIRCUITRY", 501, text("Circuitry"))

	val PISTOL_RECEIVER = register("PISTOL_RECEIVER", 502, text("Pistol Receiver"))
	val RIFLE_RECEIVER = register("RIFLE_RECEIVER", 503, text("Rifle Receiver"))
	val SMB_RECEIVER = register("SMB_RECEIVER", 504, text("SMB Receiver"))
	val SNIPER_RECEIVER = register("SNIPER_RECEIVER", 505, text("Sniper Receiver"))
	val SHOTGUN_RECEIVER = register("SHOTGUN_RECEIVER", 506, text("Shotgun Receiver"))
	val CANNON_RECEIVER = register("CANNON_RECEIVER", 507, text("Cannon Receiver"))

	// Gun Parts End

	// Minerals start

	val ALUMINUM_INGOT = register(object : MineralItem(
		identifier = "ALUMINUM_INGOT",
		material = IRON_INGOT,
		customModelData = 1,
		displayName = text("Aluminum Ingot").decoration(ITALIC, false)
	) {})

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

	val ALUMINUM_BLOCK = register(object : CustomBlockItem(
		identifier = "ALUMINUM_BLOCK",
		material = IRON_BLOCK,
		customModelData = 1,
		displayName = text("Aluminum Block").decoration(ITALIC, false),
		customBlockSupplier = { CustomBlocks.ALUMINUM_BLOCK }
	) {})

	val RAW_ALUMINUM_BLOCK = register(object : CustomBlockItem(
		identifier = "RAW_ALUMINUM_BLOCK",
		material = RAW_IRON_BLOCK,
		customModelData = 1,
		displayName = text("Raw Aluminum Block").decoration(ITALIC, false),
		customBlockSupplier = { CustomBlocks.RAW_ALUMINUM_BLOCK }
	) {})

	val CHETHERITE = register(object : MineralItem(
		identifier = "CHETHERITE",
		material = IRON_INGOT,
		customModelData = 2,
		displayName = text("Chetherite").decoration(ITALIC, false)
	) {})

	val CHETHERITE_ORE : CustomBlockItem = register(object : CustomBlockItem(
		identifier = "CHETHERITE_ORE",
		material = IRON_ORE,
		customModelData = 2,
		displayName = text("Chetherite Ore").decoration(ITALIC, false),
		customBlockSupplier = { CustomBlocks.CHETHERITE_ORE }
	), Smeltable {
		override val smeltingResult: Supplier<ItemStack> = Supplier { CHETHERITE.constructItemStack() }
	})

	val CHETHERITE_BLOCK = register(object : CustomBlockItem(
		identifier = "CHETHERITE_BLOCK",
		material = IRON_BLOCK,
		customModelData = 2,
		displayName = text("Chetherite Block").decoration(ITALIC, false),
		customBlockSupplier = { CustomBlocks.CHETHERITE_BLOCK }
	) {})

	val TITANIUM_INGOT = register(object : MineralItem(
		identifier = "TITANIUM_INGOT",
		material = IRON_INGOT,
		customModelData = 3,
		displayName = text("Titanium Ingot").decoration(ITALIC, false)
	) {})

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

	val TITANIUM_BLOCK = register(object : CustomBlockItem(
		identifier = "TITANIUM_BLOCK",
		material = IRON_BLOCK,
		customModelData = 3,
		displayName = text("Titanium Block").decoration(ITALIC, false),
		customBlockSupplier = { CustomBlocks.TITANIUM_BLOCK }
	) {})

	val RAW_TITANIUM_BLOCK = register(object : CustomBlockItem(
		identifier = "RAW_TITANIUM_BLOCK",
		material = RAW_IRON_BLOCK,
		customModelData = 3,
		displayName = text("Raw Titanium Block").decoration(ITALIC, false),
		customBlockSupplier = { CustomBlocks.RAW_TITANIUM_BLOCK }
	) {})

	val URANIUM = register(object : MineralItem(
		identifier = "URANIUM",
		material = IRON_INGOT,
		customModelData = 4,
		displayName = text("Uranium").decoration(ITALIC, false),
	) {})

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

	val URANIUM_BLOCK = register(object : CustomBlockItem(
		identifier = "URANIUM_BLOCK",
		material = IRON_BLOCK,
		customModelData = 4,
		displayName = text("Uranium Block").decoration(ITALIC, false),
		customBlockSupplier = { CustomBlocks.URANIUM_BLOCK }
	) {})

	val RAW_URANIUM_BLOCK = register(object : CustomBlockItem(
		identifier = "RAW_URANIUM_BLOCK",
		material = RAW_IRON_BLOCK,
		customModelData = 4,
		displayName = text("Raw Uranium Block").decoration(ITALIC, false),
		customBlockSupplier = { CustomBlocks.RAW_URANIUM_BLOCK }
	) {})

	// Minerals end

	// Industry start

	val NETHERITE_CASING = register(object : CustomBlockItem(
		identifier = "NETHERITE_CASING",
		material = IRON_BLOCK,
		customModelData = 1400,
		displayName = text("Netherite Casing").decoration(ITALIC, false),
		customBlockSupplier = { CustomBlocks.NETHERITE_CASING }
	) {})

	// Uranium line
	val ENRICHED_URANIUM = register(object : MineralItem(
		identifier = "ENRICHED_URANIUM",
		material = IRON_INGOT,
		customModelData = 1000,
		displayName = text("Enriched Uranium").decoration(ITALIC, false)
	) {})

	val ENRICHED_URANIUM_BLOCK = register(object : CustomBlockItem(
		identifier = "ENRICHED_URANIUM_BLOCK",
		material = IRON_BLOCK,
		customModelData = 1000,
		displayName = text("Enriched Uranium Block").decoration(ITALIC, false),
		customBlockSupplier = { CustomBlocks.ENRICHED_URANIUM_BLOCK }
	) {})

	val URANIUM_CORE = register(
		identifier = "URANIUM_CORE",
		customModelData = 2000,
		displayName = text("Uranium Core").decoration(ITALIC, false)
	)

	val URANIUM_ROD = register(
		identifier = "URANIUM_ROD",
		customModelData = 2001,
		displayName = text("Uranium Rod").decoration(ITALIC, false)
	)

	val FUEL_ROD_CORE = register(
		identifier = "FUEL_ROD_CORE",
		customModelData = 2002,
		displayName = text("Fuel Rod Core").decoration(ITALIC, false)
	)

	val FUEL_CELL = register(
		identifier = "FUEL_CELL",
		customModelData = 2003,
		displayName = text("Fuel Cell").decoration(ITALIC, false)
	)

	val FUEL_CONTROL = register(
		identifier = "FUEL_CONTROL",
		customModelData = 2004,
		displayName = text("Fuel Control").decoration(ITALIC, false).decoration(BOLD, true)
	)

	// Reactive line
	val REACTIVE_COMPONENT = register(
		identifier = "REACTIVE_COMPONENT",
		customModelData = 2005,
		displayName = text("Reactive Component").decoration(ITALIC, false)
	)

	val REACTIVE_HOUSING = register(
		identifier = "REACTIVE_HOUSING",
		customModelData = 2006,
		displayName = text("Reactive Housing").decoration(ITALIC, false)
	)

	val REACTIVE_PLATING = register(
		identifier = "REACTIVE_PLATING",
		customModelData = 2007,
		displayName = text("Reactive Plating").decoration(ITALIC, false)
	)

	val REACTIVE_CHASSIS = register(
		identifier = "REACTIVE_CHASSIS",
		customModelData = 2008,
		displayName = text("Reactive Chassis").decoration(ITALIC, false)
	)

	val REACTIVE_MEMBRANE = register(
		identifier = "REACTIVE_MEMBRANE",
		customModelData = 2009,
		displayName = text("Reactive Membrane").decoration(ITALIC, false)
	)

	val REACTIVE_ASSEMBLY = register(
		identifier = "REACTIVE_ASSEMBLY",
		customModelData = 2010,
		displayName = text("Reactive Assembly").decoration(ITALIC, false)
	)

	val FABRICATED_ASSEMBLY = register(
		identifier = "FABRICATED_ASSEMBLY",
		customModelData = 2011,
		displayName = text("Fabricated Assembly").decoration(ITALIC, false)
	)

	// Circuitry line
	val CIRCUIT_BOARD = register(
		identifier = "CIRCUIT_BOARD",
		customModelData = 2012,
		displayName = text("Circuit Board").decoration(ITALIC, false)
	)

	val MOTHERBOARD = register(
		identifier = "MOTHERBOARD",
		customModelData = 2013,
		displayName = text("Motherboard").decoration(ITALIC, false)
	)

	val REACTOR_CONTROL = register(
		identifier = "REACTOR_CONTROL",
		customModelData = 2014,
		displayName = text("Reactor Control").decoration(ITALIC, false).decoration(BOLD, true)
	)

	// Superconductor line
	val SUPERCONDUCTOR = register(
		identifier = "SUPERCONDUCTOR",
		customModelData = 2015,
		displayName = text("Superconductor").decoration(ITALIC, false)
	)

	val SUPERCONDUCTOR_BLOCK = register(object : CustomBlockItem(
		identifier = "SUPERCONDUCTOR_BLOCK",
		material = IRON_BLOCK,
		customModelData = 1002,
		displayName = text("Superconductor Block").decoration(ITALIC, false),
		customBlockSupplier = { CustomBlocks.SUPERCONDUCTOR_BLOCK }
	) {})

	val SUPERCONDUCTOR_CORE = register(
		identifier = "SUPERCONDUCTOR_CORE",
		customModelData = 2016,
		displayName = text("Superconductor Core").decoration(ITALIC, false).decoration(BOLD, true)
	)

	// Steel line
	val STEEL_INGOT = register(object : MineralItem(
		identifier = "STEEL_INGOT",
		material = IRON_INGOT,
		customModelData = 1001,
		displayName = text("Steel Ingot").decoration(ITALIC, false)
	) {})

	val STEEL_BLOCK = register(object : CustomBlockItem(
		identifier = "STEEL_BLOCK",
		material = IRON_BLOCK,
		customModelData = 1001,
		displayName = text("Steel Block").decoration(ITALIC, false),
		customBlockSupplier = { CustomBlocks.STEEL_BLOCK }
	) {})

	val STEEL_PLATE = register(
		identifier = "STEEL_PLATE",
		customModelData = 2017,
		displayName = text("Steel Plate").decoration(ITALIC, false)
	)

	val STEEL_CHASSIS = register(
		identifier = "STEEL_CHASSIS",
		customModelData = 2018,
		displayName = text("Steel Chassis").decoration(ITALIC, false)
	)

	val STEEL_MODULE = register(
		identifier = "STEEL_MODULE",
		customModelData = 2019,
		displayName = text("Steel Module").decoration(ITALIC, false)
	)

	val STEEL_ASSEMBLY = register(
		identifier = "STEEL_ASSEMBLY",
		customModelData = 2020,
		displayName = text("Steel Assembly").decoration(ITALIC, false)
	)

	val REINFORCED_FRAME = register(
		identifier = "REINFORCED_FRAME",
		customModelData = 2021,
		displayName = text("Reinforced Frame").decoration(ITALIC, false)
	)

	val REACTOR_FRAME = register(
		identifier = "REACTOR_FRAME",
		customModelData = 2022,
		displayName = text("Reactor Frame").decoration(ITALIC, false).decoration(BOLD, true)
	)

	// Industry End

	// Starship Components Start

	val BATTLECRUISER_REACTOR_CORE = register(object : CustomBlockItem(
		identifier = "BATTLECRUISER_REACTOR_CORE",
		material = IRON_BLOCK,
		customModelData = 2000,
		displayName = text("Battlecruiser Reactor Core").decoration(ITALIC, false).decoration(BOLD, true),
		customBlockSupplier = { CustomBlocks.BATTLECRUISER_REACTOR_CORE }
	) {})

	val BARGE_REACTOR_CORE = register(object : CustomBlockItem(
		identifier = "BARGE_REACTOR_CORE",
		material = IRON_BLOCK,
		customModelData = 2002,
		displayName = text("Barge Reactor Core").decoration(ITALIC, false).decoration(BOLD, true),
		customBlockSupplier = { CustomBlocks.BARGE_REACTOR_CORE }
	) {})

	val CRUISER_REACTOR_CORE = register(object : CustomBlockItem(
		identifier = "CRUISER_REACTOR_CORE",
		material = IRON_BLOCK,
		customModelData = 2001,
		displayName = text("Cruiser Reactor Core").decoration(ITALIC, false).decoration(BOLD, true),
		customBlockSupplier = { CustomBlocks.CRUISER_REACTOR_CORE }
	) {})

	// Starship Components End

	// Ship Ammunition Start

	val UNLOADED_SHELL = register(object : ShellItem(
		identifier = "UNLOADED_SHELL",
		material = WARPED_FUNGUS_ON_A_STICK,
		customModelData = 702,
		displayName = text("Unloaded Shell").decoration(ITALIC, false)
	) {})

	val LOADED_SHELL = register(object : ShellItem(
		identifier = "LOADED_SHELL",
		material = IRON_INGOT,
		customModelData = 2001,
		displayName = text("Loaded Shell").decoration(ITALIC, false)
	) {})

	val UNCHARGED_SHELL = register(object : ShellItem(
		identifier = "UNCHARGED_SHELL",
		material = WARPED_FUNGUS_ON_A_STICK,
		customModelData = 703,
		displayName = text("Uncharged Shell").decoration(ITALIC, false)
	) {})

	val CHARGED_SHELL = register(object : ShellItem(
		identifier = "CHARGED_SHELL",
		material = IRON_INGOT,
		customModelData = 2002,
		displayName = text("Charged Shell").decoration(ITALIC, false)
	) {})

	val ACTIVATED_ARSENAL_MISSILE = register(
		identifier = "ACTIVATED_ARSENAL_MISSILE",
		customModelData = 700,
		displayName = text("Activated Arsenal Missile", RED).decoration(BOLD, false),
	)

	val ARSENAL_MISSILE = register(object : ShellItem(
		identifier = "ARSENAL_MISSILE",
		material = IRON_INGOT,
		customModelData = 2000,
		displayName = text("Arsenal Missile").decoration(ITALIC, false),
	) {})

	val UNLOADED_ARSENAL_MISSILE = register(object : ShellItem(
		identifier = "UNLOADED_ARSENAL_MISSILE",
		material = WARPED_FUNGUS_ON_A_STICK,
		customModelData = 701,
		displayName = text("Unloaded Arsenal Missile").decoration(ITALIC, false),
	) {})
	// Ship Ammunition End

	// Gas Canisters Start
	val GAS_CANISTER_EMPTY = register("GAS_CANISTER_EMPTY", 1000, text("Empty Gas Canister"))

	// Fuels
	val GAS_CANISTER_HYDROGEN = register(
		object : GasCanister(
			identifier = "GAS_CANISTER_HYDROGEN",
			customModelData = 1001,
			gasIdentifier = "HYDROGEN",
			displayName = canisterName(text("Hydrogen", RED))
		) {}
	)
	val GAS_CANISTER_NITROGEN = register(
		object : GasCanister(
			identifier = "GAS_CANISTER_NITROGEN",
			customModelData = 1002,
			gasIdentifier = "NITROGEN",
			displayName = canisterName(text("Nitrogen", RED))
		) {}
	)
	val GAS_CANISTER_METHANE = register(
		object : GasCanister(
			identifier = "GAS_CANISTER_METHANE",
			customModelData = 1003,
			gasIdentifier = "METHANE",
			displayName = canisterName(text("Methane", RED))
		) {}
	)

	// Oxidizers
	val GAS_CANISTER_OXYGEN = register(
		object : GasCanister(
			identifier = "GAS_CANISTER_OXYGEN",
			customModelData = 1010,
			gasIdentifier = "OXYGEN",
			displayName = canisterName(text("Oxygen", YELLOW))
		) {}
	)
	val GAS_CANISTER_CHLORINE = register(
		object : GasCanister(
			identifier = "GAS_CANISTER_CHLORINE",
			customModelData = 1011,
			gasIdentifier = "CHLORINE",
			displayName = canisterName(text("Chlorine", YELLOW))
		) {}
	)
	val GAS_CANISTER_FLUORINE = register(
		object : GasCanister(
			identifier = "GAS_CANISTER_FLUORINE",
			customModelData = 1012,
			gasIdentifier = "FLUORINE",
			displayName = canisterName(text("Fluorine", YELLOW))
		) {}
	)

	// Other
	val GAS_CANISTER_HELIUM = register(
		object : GasCanister(
			identifier = "GAS_CANISTER_HELIUM",
			customModelData = 1020,
			gasIdentifier = "HELIUM",
			displayName = canisterName(text("Helium", BLUE))
		) {}
	)
	val GAS_CANISTER_CARBON_DIOXIDE = register(
		object : GasCanister(
			identifier = "GAS_CANISTER_CARBON_DIOXIDE",
			customModelData = 1021,
			gasIdentifier = "CARBON_DIOXIDE",
			displayName = canisterName(text("Carbon Dioxide", BLUE))
		) {}
	)

	fun canisterName(gasName: Component): Component = text()
		.append(gasName)
		.append(text(" Gas Canister", GRAY))
		.build()
		.decoration(ITALIC, false)

	// Gas Canisters End

	// Throwables start

	val DETONATOR = register(
		object : ThrowableCustomItem(
			identifier = "DETONATOR",
			customModelData = 1101,
			text().append(text("Thermal ", RED), text("Detonator", GRAY)).decoration(ITALIC, false).build(),
			IonServer.pvpBalancing.throwables::detonator
		) {
			override fun constructThrownRunnable(item: Item, maxTicks: Int, damageSource: Entity?): ThrownCustomItem {
				return ThrownDetonator(item, maxTicks, damageSource, IonServer.pvpBalancing.throwables::detonator)
			}
		}
	)

	val SMOKE_GRENADE = register(
		object : ThrowableCustomItem(
			identifier = "SMOKE_GRENADE",
			customModelData = 1102,
			ofChildren(text("Smoke ", DARK_GREEN), text("Grenade", GRAY)).decoration(ITALIC, false),
			IonServer.pvpBalancing.throwables::smokeGrenade
		) {
			override fun constructThrownRunnable(item: Item, maxTicks: Int, damageSource: Entity?): ThrownCustomItem {
				return ThrownSmokeGrenade(item, maxTicks, damageSource)
			}
		}
	)

	val PUMPKIN_GRENADE = register(
		object : ThrowableCustomItem(
			identifier = "PUMPKIN_GRENADE",
			customModelData = 0,
			text().append(text("Pumpkin ", GOLD), text("Grenade", GREEN)).decoration(ITALIC, false).build(),
			IonServer.pvpBalancing.throwables::detonator
		) {
			override fun constructItemStack(): ItemStack {
				return super.constructItemStack().apply {
					type = Material.PUMPKIN
				}.updateMeta { it.lore(mutableListOf(text("Spooky", LIGHT_PURPLE))) }
			}

			override fun constructThrownRunnable(item: Item, maxTicks: Int, damageSource: Entity?): ThrownCustomItem {
				return ThrownPumpkinGrenade(item, maxTicks, damageSource, IonServer.pvpBalancing.throwables::detonator)
			}
		}
	)
//	val INCENDIARY_GRENADE = register(
//		object : ThrowableCustomItem(
//			identifier = "INCENDIARY_GRENADE",
//			customModelData = 1102,
//			text().append(text("Incendiary ", RED), text("Grenade", GOLD)).decoration(ITALIC, false).build(),
//			IonServer.pvpBalancing.throwables::detonator
//		) {
//			override fun constructThrownRunnable(item: Item, maxTicks: Int, damageSource: Entity?): ThrownCustomItem {
//				return ThrownIncendiaryGrenade(item, maxTicks, damageSource, IonServer.pvpBalancing.throwables::detonator)
//			}
//		}
//	)

	// Throwables end

	// Ammunition start

	// Ammunition end

	val PROGRESS_HOLDER = register(ProgressHolder)

	// Planets start
	val AERACH = register("AERACH", 5000, text("Aerach"))
	val ARET = register("ARET", 5001, text("Aret"))
	val CHANDRA = register("CHANDRA", 5002, text("Chandra"))
	val CHIMGARA = register("CHIMGARA", 5003, text("Chimgara"))
	val DAMKOTH = register("DAMKOTH", 5004, text("Damkoth"))
	val DISTERRA = register("DISTERRA", 5005, text("Disterra"))
	val EDEN = register("EDEN", 5006, text("Eden"))
	val GAHARA = register("GAHARA", 5007, text("Gahara"))
	val HERDOLI = register("HERDOLI", 5008, text("Herdoli"))
	val ILIUS = register("ILIUS", 5009, text("Ilius"))
	val ISIK = register("ISIK", 5010, text("Isik"))
	val KOVFEFE = register("KOVFEFE", 5011, text("Kovfefe"))
	val KRIO = register("KRIO", 5012, text("Krio"))
	val LIODA = register("LIODA", 5013, text("Lioda"))
	val LUXITERNA = register("LUXITERNA", 5014, text("Luxiterna"))
	val QATRA = register("QATRA", 5015, text("Qatra"))
	val RUBACIEA = register("RUBACIEA", 5016, text("Rubaciea"))
	val TURMS = register("TURMS", 5017, text("Turms"))
	val VASK = register("VASK", 5018, text("Vask"))

	val ASTERI = register("ASTERI", 5100, text("Asteri"))
	val HORIZON = register("HORIZON", 5101, text("Horizon"))
	val ILIOS = register("ILIOS", 5102, text("Ilios"))
	val REGULUS = register("REGULUS", 5103, text("Regulus"))
	val SIRIUS = register("SIRIUS", 5104, text("Sirius"))

	val PLANET_SELECTOR = register("PLANET_SELECTOR", 5900, text("PLANET_SELECTOR"))
	// Planets end

	// Tools begin
	private fun formatToolName(tierName: String, tierColor: TextColor, toolName: String) = ofChildren(
		text("$tierName ", tierColor),
		text("$toolName ", GOLD),
		text("Drill", GRAY)
	).decoration(ITALIC, false)

	val POWER_DRILL_BASIC = register(PowerDrill(
		identifier = "POWER_DRILL_BASIC",
		displayName = formatToolName("Basic", HE_LIGHT_ORANGE, "Drill"),
		modLimit = 2,
		basePowerCapacity = 50_000,
		customModelData = 1
	))

	val POWER_DRILL_ENHANCED = register(PowerDrill(
		identifier = "POWER_DRILL_ENHANCED",
		displayName = formatToolName("Enhanced", fromHexString("#00FFA1")!!, "Drill"),
		modLimit = 4,
		basePowerCapacity = 75_000,
		customModelData = 4
	))

	val POWER_DRILL_ADVANCED = register(PowerDrill(
		identifier = "POWER_DRILL_ADVANCED",
		displayName = formatToolName("Advanced", fromHexString("#B12BC9")!!, "Drill"),
		modLimit = 6,
		basePowerCapacity = 100_000,
		customModelData = 7
	))

	val POWER_CHAINSAW_BASIC = register(PowerChainsaw(
		identifier = "POWER_CHAINSAW_BASIC",
		displayName = formatToolName("Basic", HE_LIGHT_ORANGE, "Chainsaw"),
		modLimit = 2,
		basePowerCapacity = 50_000,
		customModelData = 2
	))

	val POWER_CHAINSAW_ENHANCED = register(PowerChainsaw(
		identifier = "POWER_CHAINSAW_ENHANCED",
		displayName = formatToolName("Enhanced", fromHexString("#00FFA1")!!, "Chainsaw"),
		modLimit = 4,
		basePowerCapacity = 75_000,
		customModelData = 5

	))

	val POWER_CHAINSAW_ADVANCED = register(PowerChainsaw(
		identifier = "POWER_CHAINSAW_ADVANCED",
		displayName = formatToolName("Advanced", fromHexString("#B12BC9")!!, "Chainsaw"),
		modLimit = 6,
		basePowerCapacity = 100_000,
		customModelData = 8
	))

	val POWER_HOE_BASIC = register(PowerHoe(
		identifier = "POWER_HOE_BASIC",
		displayName = formatToolName("Basic", HE_LIGHT_ORANGE, "Hoe"),
		modLimit = 2,
		basePowerCapacity = 50_000,
		customModelData = 3
	))

	val POWER_HOE_ENHANCED = register(PowerHoe(
		identifier = "POWER_HOE_ENHANCED",
		displayName = formatToolName("Enhanced", fromHexString("#00FFA1")!!, "Hoe"),
		modLimit = 4,
		basePowerCapacity = 75_000,
		customModelData = 6
	))

	val POWER_HOE_ADVANCED = register(PowerHoe(
		identifier = "POWER_HOE_ADVANCED",
		displayName = formatToolName("Advanced", fromHexString("#B12BC9")!!, "Hoe"),
		modLimit = 6,
		basePowerCapacity = 100_000,
		customModelData = 9
	))

	val CRATE_PLACER = register(CratePlacer)

	val DRILL_AOE_1: ModificationItem = register(ModificationItem(
		"TOOL_MODIFICATION_DRILL_AOE_1",
		7000,
		text("Drill AOE 1").decoration(ITALIC, false)
	) { ItemModRegistry.AOE_1 })

	val DRILL_AOE_2: ModificationItem = register(ModificationItem(
		"TOOL_MODIFICATION_DRILL_AOE_2",
		7001,
		text("Drill AOE 2").decoration(ITALIC, false)
	) { ItemModRegistry.AOE_2 })

	val VEIN_MINER_25: ModificationItem = register(ModificationItem(
		"TOOL_MODIFICATION_VEIN_MINER_25",
		7002,
		text("Vein Miner").decoration(ITALIC, false)
	) { ItemModRegistry.VEIN_MINER_25 })

	val SILK_TOUCH_MOD: ModificationItem = register(ModificationItem(
		"TOOL_MODIFICATION_SILK_TOUCH_MOD",
		7003,
		text("Silk Touch Modifier").decoration(ITALIC, false)
	) { ItemModRegistry.SILK_TOUCH })

	val AUTO_SMELT: ModificationItem = register(ModificationItem(
		"TOOL_MODIFICATION_AUTO_SMELT",
		7004,
		text("Auto Smelt Modifier").decoration(ITALIC, false)
	) { ItemModRegistry.AUTO_SMELT })

	val FORTUNE_1: ModificationItem = register(ModificationItem(
		"TOOL_MODIFICATION_FORTUNE_1",
		7005,
		text("Fortune 1").decoration(ITALIC, false)
	) { ItemModRegistry.FORTUNE_1 })

	val FORTUNE_2: ModificationItem = register(ModificationItem(
		"TOOL_MODIFICATION_FORTUNE_2",
		7006,
		text("Fortune 2").decoration(ITALIC, false)
	) { ItemModRegistry.FORTUNE_2 })

	val FORTUNE_3: ModificationItem = register(ModificationItem(
		"TOOL_MODIFICATION_FORTUNE_3",
		7007,
		text("Fortune 3").decoration(ITALIC, false)
	) { ItemModRegistry.FORTUNE_3 })

	val POWER_CAPACITY_25: ModificationItem = register(ModificationItem(
		"TOOL_MODIFICATION_POWER_CAPACITY_25",
		7008,
		text("Power Capacity 25000").decoration(ITALIC, false)
	) { ItemModRegistry.POWER_CAPACITY_25 })

	val POWER_CAPACITY_50: ModificationItem = register(ModificationItem(
		"TOOL_MODIFICATION_POWER_CAPACITY_50",
		7009,
		text("Power Capacity 50000").decoration(ITALIC, false)
	) { ItemModRegistry.POWER_CAPACITY_50 })

	val AUTO_REPLANT: ModificationItem = register(ModificationItem(
		"TOOL_MODIFICATION_AUTO_REPLANT",
		7010,
		text("Auto Replant Modifier").decoration(ITALIC, false)
	) { ItemModRegistry.AUTO_REPLANT })

	val AUTO_COMPOST: ModificationItem = register(ModificationItem(
		"TOOL_MODIFICATION_AUTO_COMPOST",
		7011,
		text("Auto Compost Modifier").decoration(ITALIC, false)
	) { ItemModRegistry.AUTO_COMPOST })

	val DRILL_AOE_3: ModificationItem = register(ModificationItem(
		"TOOL_MODIFICATION_DRILL_AOE_3",
		7012,
		text("Drill AOE 3").decoration(ITALIC, false)
	) { ItemModRegistry.AOE_3 })

	val EXTENDED_BAR: ModificationItem = register(ModificationItem(
		"TOOL_MODIFICATION_EXTENDED_BAR",
		7013,
		text("Extended Bar Modifier").decoration(ITALIC, false)
	) { ItemModRegistry.EXTENDED_BAR })

	val FERTILIZER_DISPENSER: ModificationItem = register(ModificationItem(
		"TOOL_MODIFICATION_FERTILIZER_DISPENSER",
		7014,
		text("Fertilizer Dispenser Bar Modifier").decoration(ITALIC, false)
	) { ItemModRegistry.FERTILIZER_DISPENSER })

	// Tools end

	// This is just a convenient alias for items that don't do anything or are placeholders.
	private fun register(identifier: String, customModelData: Int, displayName: Component): CustomItem {
		return register(object :
			CustomItem(identifier) {
			override fun constructItemStack(): ItemStack {
				return ItemStack(WARPED_FUNGUS_ON_A_STICK).updateMeta {
					it.setCustomModelData(customModelData)
					it.displayName(displayName.decoration(ITALIC, false))
					it.persistentDataContainer.set(CUSTOM_ITEM, STRING, identifier)
				}
			}
		})
	}

	private fun <T : CustomItem> register(customItem: T): T {
		customItems[customItem.identifier] = customItem
		return customItem
	}

	val ItemStack.customItem: CustomItem?
		get() {
			// Who tf annotated itemMeta with "UndefinedNullability"
			// if ya cant promise it's not null, then mark it nullable
			// ^ he did not know how nullability works in java
			return customItems[itemMeta?.persistentDataContainer?.get(CUSTOM_ITEM, STRING) ?: return null]
		}

	val identifiers = customItems.keys

	fun getByIdentifier(identifier: String): CustomItem? = customItems[identifier]
}

abstract class CustomItem(val identifier: String) {
	/** Left Click **/
	open fun handlePrimaryInteract(livingEntity: LivingEntity, itemStack: ItemStack, event: PlayerInteractEvent) {}

	/** Right Click **/
	open fun handleSecondaryInteract(livingEntity: LivingEntity, itemStack: ItemStack, event: PlayerInteractEvent?) {}

	/** Swap Hands **/
	open fun handleSwapHands(livingEntity: LivingEntity, itemStack: ItemStack) {}

	/** Dispensed from a dispense **/
	open fun handleDispense(dispenser: Dispenser, slot: Int) {}

	abstract fun constructItemStack(): ItemStack
}
