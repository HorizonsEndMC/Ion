package net.horizonsend.ion.server.core.registration.registries

import net.horizonsend.ion.common.utils.text.colors.HEColorScheme
import net.horizonsend.ion.common.utils.text.ofChildren
import net.horizonsend.ion.server.configuration.ConfigurationFiles
import net.horizonsend.ion.server.configuration.PVPBalancingConfiguration
import net.horizonsend.ion.server.core.registration.IonRegistryKey
import net.horizonsend.ion.server.core.registration.keys.AtmosphericGasKeys
import net.horizonsend.ion.server.core.registration.keys.CustomBlockKeys
import net.horizonsend.ion.server.core.registration.keys.CustomItemKeys
import net.horizonsend.ion.server.core.registration.keys.ItemModKeys
import net.horizonsend.ion.server.core.registration.keys.KeyRegistry
import net.horizonsend.ion.server.core.registration.keys.RegistryKeys
import net.horizonsend.ion.server.features.client.display.modular.display.PowerEntityDisplayModule
import net.horizonsend.ion.server.features.custom.blocks.CustomBlock
import net.horizonsend.ion.server.features.custom.items.CustomItem
import net.horizonsend.ion.server.features.custom.items.CustomItemListeners
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
import net.horizonsend.ion.server.features.custom.items.type.tool.mods.ModificationItem
import net.horizonsend.ion.server.features.custom.items.type.weapon.blaster.Blaster
import net.horizonsend.ion.server.features.custom.items.type.weapon.blaster.Magazine
import net.horizonsend.ion.server.features.custom.items.type.weapon.sword.EnergySword
import net.horizonsend.ion.server.features.custom.items.util.ItemFactory
import net.horizonsend.ion.server.miscellaneous.registrations.persistence.NamespacedKeys
import net.horizonsend.ion.server.miscellaneous.utils.Tasks
import net.horizonsend.ion.server.miscellaneous.utils.text.itemName
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextColor
import net.kyori.adventure.text.format.TextDecoration
import org.bukkit.Material
import org.bukkit.entity.Entity
import org.bukkit.entity.Item
import org.bukkit.entity.LivingEntity
import org.bukkit.inventory.EquipmentSlot
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType
import java.util.function.Supplier
import kotlin.math.roundToInt

class CustomItemRegistry : Registry<CustomItem>(RegistryKeys.CUSTOM_ITEMS) {
	override fun getKeySet(): KeyRegistry<CustomItem> = CustomItemKeys
	override fun boostrap() {
		registerThrowables()
		registerGuns()
		registerGunParts()
		registerMinerals()
		registerIndustry()
		registerShipCores()
		registerGasCanisters()
		registerTools()
		registerModificationItems()
		registerEnergySwords()
		registerPlanetIcons()

		unStackable(CustomItemKeys.DEBUG_LINE, displayName = Component.text("DEBUG_LINE"), model = "debug/debug_line")
		unStackable(CustomItemKeys.DEBUG_LINE_GREEN, displayName = Component.text("DEBUG_LINE_GREEN"), model = "debug/debug_line_green")
		unStackable(CustomItemKeys.DEBUG_LINE_RED, displayName = Component.text("DEBUG_LINE_RED"), model = "debug/debug_line_red")
		unStackable(CustomItemKeys.DEBUG_LINE_BLUE, displayName = Component.text("DEBUG_LINE_BLUE"), model = "debug/debug_line_blue")

        CustomItemListeners.sortCustomItemListeners()
	}

	private fun registerThrowables() {
		fun registerThrowable(
            key: IonRegistryKey<CustomItem, out CustomItem>,
            customModel: String,
            displayName: Component,
            balancing: Supplier<PVPBalancingConfiguration.Throwables.ThrowableBalancing>,
            thrown: (Item, Int, Entity?) -> ThrownCustomItem
		) = register(key, object : ThrowableCustomItem(key = key, customModel = customModel, displayName = displayName, balancingSupplier = balancing) {
			override fun constructThrownRunnable(item: Item, maxTicks: Int, damageSource: Entity?): ThrownCustomItem = thrown.invoke(item, maxTicks, damageSource)
		})

		registerThrowable(
			CustomItemKeys.DETONATOR,
			"throwables/detonator",
            ofChildren(Component.text("Thermal ", NamedTextColor.RED), Component.text("Detonator", NamedTextColor.GRAY)),
			ConfigurationFiles.pvpBalancing().throwables::detonator
		) { item, maxTicks, source -> ThrownDetonator(item, maxTicks, source, ConfigurationFiles.pvpBalancing().throwables::detonator) }

		registerThrowable(
			CustomItemKeys.SMOKE_GRENADE,
			"throwables/detonator",
            ofChildren(Component.text("Smoke ", NamedTextColor.DARK_GREEN), Component.text("Grenade", NamedTextColor.GRAY)),
			ConfigurationFiles.pvpBalancing().throwables::smokeGrenade
		) { item, maxTicks, source -> ThrownSmokeGrenade(item, maxTicks, source) }

		register(CustomItemKeys.PUMPKIN_GRENADE, object : ThrowableCustomItem(
			CustomItemKeys.PUMPKIN_GRENADE,
			"",
            ofChildren(Component.text("Pumpkin ", NamedTextColor.GOLD), Component.text("Grenade", NamedTextColor.GREEN)),
			ConfigurationFiles.pvpBalancing().throwables::detonator
		) {
			override val baseItemFactory: ItemFactory = ItemFactory.Preset.builder(ItemFactory.Preset.builder().setMaterial(Material.PUMPKIN).build())
				.setNameSupplier { displayName.itemName }
				.addPDCEntry(NamespacedKeys.CUSTOM_ITEM, PersistentDataType.STRING, key.key)
				.addModifier { base -> customComponents.getAll().forEach { it.decorateBase(base, this) } }
				.addModifier { base -> decorateItemStack(base) }
				.setLoreSupplier { base -> assembleLore(base) }
				.build()

			override fun assembleLore(itemStack: ItemStack): List<Component> {
				return mutableListOf(Component.text("Spooky", NamedTextColor.LIGHT_PURPLE))
			}
			override fun constructThrownRunnable(item: Item, maxTicks: Int, damageSource: Entity?): ThrownCustomItem =
                ThrownPumpkinGrenade(item, maxTicks, damageSource, ConfigurationFiles.pvpBalancing().throwables::detonator)
		})
	}

	private fun registerGuns() {
		register(
            CustomItemKeys.STANDARD_MAGAZINE, Magazine(
                key = CustomItemKeys.STANDARD_MAGAZINE,
                displayName = Component.text("Standard Magazine").decoration(TextDecoration.ITALIC, false),
                itemFactory = ItemFactory.Preset.unStackableCustomItem("weapon/blaster/standard_magazine"),
                balancingSupplier = ConfigurationFiles.pvpBalancing().energyWeapons::standardMagazine
            )
        )

		register(
            CustomItemKeys.SPECIAL_MAGAZINE, Magazine(
                key = CustomItemKeys.SPECIAL_MAGAZINE,
                displayName = Component.text("Special Magazine").decoration(TextDecoration.ITALIC, false),
                itemFactory = ItemFactory.Preset.unStackableCustomItem("weapon/blaster/special_magazine"),
                balancingSupplier = ConfigurationFiles.pvpBalancing().energyWeapons::specialMagazine
            )
        )

		register(
            CustomItemKeys.BLASTER_PISTOL, Blaster(
                key = CustomItemKeys.BLASTER_PISTOL,
                displayName = Component.text("Blaster Pistol", NamedTextColor.RED, TextDecoration.BOLD),
                itemFactory = ItemFactory.Preset.builder().setMaterial(Material.DIAMOND_HOE).setCustomModel("weapon/blaster/pistol").build(),
                balancingSupplier = ConfigurationFiles.pvpBalancing().energyWeapons::pistol
            )
        )

		register(
            CustomItemKeys.BLASTER_RIFLE, Blaster(
                key = CustomItemKeys.BLASTER_RIFLE,
                displayName = Component.text("Blaster Rifle", NamedTextColor.RED, TextDecoration.BOLD),
                itemFactory = ItemFactory.Preset.builder().setMaterial(Material.IRON_HOE).setCustomModel("weapon/blaster/rifle").build(),
                balancingSupplier = ConfigurationFiles.pvpBalancing().energyWeapons::rifle
            )
        )

		register(CustomItemKeys.SUBMACHINE_BLASTER, object : Blaster<PVPBalancingConfiguration.EnergyWeapons.Singleshot>(
			key = CustomItemKeys.SUBMACHINE_BLASTER,
			itemFactory = ItemFactory.Preset.builder().setMaterial(Material.IRON_HOE).setCustomModel("weapon/blaster/submachine_blaster").build(),
			displayName = Component.text("Submachine Blaster", NamedTextColor.RED, TextDecoration.BOLD).decoration(TextDecoration.ITALIC, false),
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

		register(CustomItemKeys.BLASTER_SHOTGUN, object : Blaster<PVPBalancingConfiguration.EnergyWeapons.Multishot>(
			key = CustomItemKeys.BLASTER_SHOTGUN,
			displayName = Component.text("Blaster Shotgun", NamedTextColor.RED, TextDecoration.BOLD).decoration(TextDecoration.ITALIC, false),
			itemFactory = ItemFactory.Preset.builder().setMaterial(Material.GOLDEN_HOE).setCustomModel("weapon/blaster/shotgun").build(),
			balancingSupplier = ConfigurationFiles.pvpBalancing().energyWeapons::shotgun
		) {
			override fun fireProjectiles(livingEntity: LivingEntity) {
				repeat(balancing.shotCount) { super.fireProjectiles(livingEntity) }
			}
		})

		register(
            CustomItemKeys.BLASTER_SNIPER, Blaster(
                key = CustomItemKeys.BLASTER_SNIPER,
                displayName = Component.text("Blaster Sniper", NamedTextColor.RED, TextDecoration.BOLD).decoration(TextDecoration.ITALIC, false),
                itemFactory = ItemFactory.Preset.builder().setMaterial(Material.GOLDEN_HOE).setCustomModel("weapon/blaster/sniper").build(),
                balancingSupplier = ConfigurationFiles.pvpBalancing().energyWeapons::sniper
            )
        )

		register(
            CustomItemKeys.BLASTER_CANNON, Blaster(
                key = CustomItemKeys.BLASTER_CANNON,
                displayName = Component.text("Blaster Cannon", NamedTextColor.RED, TextDecoration.BOLD).decoration(TextDecoration.ITALIC, false),
                itemFactory = ItemFactory.Preset.builder().setMaterial(Material.IRON_HOE).setCustomModel("weapon/blaster/cannon").build(),
                balancingSupplier = ConfigurationFiles.pvpBalancing().energyWeapons::cannon
            )
        )
	}

	private fun registerGunParts() {
		simple(CustomItemKeys.GUN_BARREL, Component.text("Gun Barrel"), ItemFactory.Preset.unStackableCustomItem("industry/gun_barrel"))
		simple(CustomItemKeys.CIRCUITRY, Component.text("Circuitry"), ItemFactory.Preset.unStackableCustomItem("industry/circuitry"))

		simple(CustomItemKeys.PISTOL_RECEIVER, Component.text("Pistol Receiver"), ItemFactory.Preset.unStackableCustomItem("industry/pistol_receiver"))
		simple(CustomItemKeys.RIFLE_RECEIVER, Component.text("Rifle Receiver"), ItemFactory.Preset.unStackableCustomItem("industry/rifle_receiver"))
		simple(CustomItemKeys.SMB_RECEIVER, Component.text("SMB Receiver"), ItemFactory.Preset.unStackableCustomItem("industry/smb_receiver"))
		simple(CustomItemKeys.SNIPER_RECEIVER, Component.text("Sniper Receiver"), ItemFactory.Preset.unStackableCustomItem("industry/sniper_receiver"))
		simple(CustomItemKeys.SHOTGUN_RECEIVER, Component.text("Shotgun Receiver"), ItemFactory.Preset.unStackableCustomItem("industry/shotgun_receiver"))
		simple(CustomItemKeys.CANNON_RECEIVER, Component.text("Cannon Receiver"), ItemFactory.Preset.unStackableCustomItem("industry/cannon_receiver"))
	}

	// Minerals start
	private fun registerMinerals() {
		fun registerRawOre(key: IonRegistryKey<CustomItem, out CustomItem>, name: String) = simple(key,
            Component.text("Raw ${name.replaceFirstChar { it.uppercase() }}"),
            ItemFactory.Preset.stackableCustomItem(model = "mineral/raw_$name")
        )
		fun registerOreIngot(key: IonRegistryKey<CustomItem, out CustomItem>, name: String, useSuffix: Boolean) =
			simple(key, Component.text("${name.replaceFirstChar { it.uppercase() }}${if (useSuffix) " Ingot" else ""}"), ItemFactory.Preset.stackableCustomItem(model = "mineral/$name"))

		fun registerOreBlock(key: IonRegistryKey<CustomItem, out CustomItem>, name: String, block: IonRegistryKey<CustomBlock, out CustomBlock>) =
			customBlockItem(key, "mineral/${name}_ore", Component.text("${name.replaceFirstChar { it.uppercase() }} Ore"), block)

		fun registerIngotBlock(key: IonRegistryKey<CustomItem, out CustomItem>, name: String, block: IonRegistryKey<CustomBlock, out CustomBlock>) =
			customBlockItem(key, "mineral/${name}_block", Component.text("${name.replaceFirstChar { it.uppercase() }} Block"), block)

		fun registerRawBlock(key: IonRegistryKey<CustomItem, out CustomItem>, name: String, block: IonRegistryKey<CustomBlock, out CustomBlock>) =
			customBlockItem(key, "mineral/raw_${name}_block", Component.text("Raw ${name.replaceFirstChar { it.uppercase() }} Block"), block)

		registerOreIngot(CustomItemKeys.ALUMINUM_INGOT, "aluminum", true)
		registerRawOre(CustomItemKeys.RAW_ALUMINUM, "aluminum")
		registerOreBlock(CustomItemKeys.ALUMINUM_ORE, "aluminum", block = CustomBlockKeys.ALUMINUM_ORE)
		registerIngotBlock(CustomItemKeys.ALUMINUM_BLOCK, "aluminum", block = CustomBlockKeys.ALUMINUM_BLOCK)
		registerRawBlock(CustomItemKeys.RAW_ALUMINUM_BLOCK, "aluminum", block = CustomBlockKeys.RAW_ALUMINUM_BLOCK)

		registerOreIngot(CustomItemKeys.CHETHERITE, "chetherite", false)
		registerOreBlock(CustomItemKeys.CHETHERITE_ORE, "chetherite", block = CustomBlockKeys.CHETHERITE_ORE)
		registerIngotBlock(CustomItemKeys.CHETHERITE_BLOCK, "chetherite", block = CustomBlockKeys.CHETHERITE_BLOCK)

		registerOreIngot(CustomItemKeys.TITANIUM_INGOT, "titanium", true)
		registerRawOre(CustomItemKeys.RAW_TITANIUM, "titanium")
		registerOreBlock(CustomItemKeys.TITANIUM_ORE, "titanium", block = CustomBlockKeys.TITANIUM_ORE)
		registerIngotBlock(CustomItemKeys.TITANIUM_BLOCK, "titanium", block = CustomBlockKeys.TITANIUM_BLOCK)
		registerRawBlock(CustomItemKeys.RAW_TITANIUM_BLOCK, "titanium", block = CustomBlockKeys.RAW_TITANIUM_BLOCK)

		registerOreIngot(CustomItemKeys.URANIUM, name = "uranium", false)
		registerRawOre(CustomItemKeys.RAW_URANIUM, name = "uranium")
		registerOreBlock(CustomItemKeys.URANIUM_ORE, name = "uranium", block = CustomBlockKeys.URANIUM_ORE)
		registerIngotBlock(CustomItemKeys.URANIUM_BLOCK, name = "uranium", block = CustomBlockKeys.URANIUM_BLOCK)
		registerRawBlock(CustomItemKeys.RAW_URANIUM_BLOCK, name = "uranium", block = CustomBlockKeys.RAW_URANIUM_BLOCK)
	}

	private fun registerIndustry() {
		customBlockItem(key = CustomItemKeys.NETHERITE_CASING, model = "industry/netherite_casing", displayName = Component.text("Netherite Casing"), customBlock = CustomBlockKeys.NETHERITE_CASING)
		stackable(key = CustomItemKeys.ENRICHED_URANIUM, Component.text("Enriched Uranium"), "industry/enriched_uranium")
		customBlockItem(
			key = CustomItemKeys.ENRICHED_URANIUM_BLOCK,
			model = "industry/enriched_uranium_block",
			displayName = Component.text("Enriched Uranium Block"),
			customBlock = CustomBlockKeys.ENRICHED_URANIUM_BLOCK
		)
		unStackable(key = CustomItemKeys.URANIUM_CORE, model = "industry/uranium_core", displayName = Component.text("Uranium Core"))
		unStackable(key = CustomItemKeys.URANIUM_ROD, model = "industry/uranium_rod", displayName = Component.text("Uranium Rod"))
		unStackable(key = CustomItemKeys.FUEL_ROD_CORE, model = "industry/fuel_rod_core", displayName = Component.text("Fuel Rod Core"))
		unStackable(key = CustomItemKeys.FUEL_CELL, model = "industry/fuel_cell", displayName = Component.text("Fuel Cell"))
		unStackable(key = CustomItemKeys.FUEL_CONTROL, model = "industry/fuel_control", displayName = Component.text("Fuel Control"))

		unStackable(key = CustomItemKeys.REACTIVE_COMPONENT, model = "industry/reactive_component", displayName = Component.text("Reactive Component"))
		unStackable(key = CustomItemKeys.REACTIVE_HOUSING, model = "industry/reactive_housing", displayName = Component.text("Reactive Housing"))
		unStackable(key = CustomItemKeys.REACTIVE_PLATING, model = "industry/reactive_plating", displayName = Component.text("Reactive Plating"))
		unStackable(key = CustomItemKeys.REACTIVE_CHASSIS, model = "industry/reactive_chassis", displayName = Component.text("Reactive Chassis"))
		unStackable(key = CustomItemKeys.REACTIVE_MEMBRANE, model = "industry/reactive_membrane", displayName = Component.text("Reactive Membrane"))
		unStackable(key = CustomItemKeys.REACTIVE_ASSEMBLY, model = "industry/reactive_assembly", displayName = Component.text("Reactive Assembly"))
		unStackable(key = CustomItemKeys.FABRICATED_ASSEMBLY, model = "industry/fabricated_assembly", displayName = Component.text("Fabricated Assembly"))

		unStackable(key = CustomItemKeys.CIRCUIT_BOARD, model = "industry/circuit_board", displayName = Component.text("Circuit Board"))
		unStackable(key = CustomItemKeys.MOTHERBOARD, model = "industry/motherboard", displayName = Component.text("Motherboard"))
		unStackable(key = CustomItemKeys.REACTOR_CONTROL, model = "industry/reactor_control", displayName = Component.text("Reactor Control", NamedTextColor.YELLOW))

		unStackable(key = CustomItemKeys.SUPERCONDUCTOR, model = "industry/superconductor", displayName = Component.text("Superconductor"))
		customBlockItem(
			key = CustomItemKeys.SUPERCONDUCTOR_BLOCK,
			model = "industry/superconductor_block",
			displayName = Component.text("Superconductor Block"),
			customBlock = CustomBlockKeys.SUPERCONDUCTOR_BLOCK
		)
		unStackable(key = CustomItemKeys.SUPERCONDUCTOR_CORE, model = "industry/superconductor_core", displayName = Component.text("Superconductor Core", NamedTextColor.YELLOW))

		stackable(key = CustomItemKeys.STEEL_INGOT, Component.text("Steel Ingot"), "industry/steel_ingot")
		customBlockItem(key = CustomItemKeys.STEEL_BLOCK, model = "industry/steel_block", displayName = Component.text("Steel Block"), customBlock = CustomBlockKeys.STEEL_BLOCK)
		unStackable(key = CustomItemKeys.STEEL_PLATE, model = "industry/steel_plate", displayName = Component.text("Steel Plate"))
		unStackable(key = CustomItemKeys.STEEL_CHASSIS, model = "industry/steel_chassis", displayName = Component.text("Steel Chassis"))
		unStackable(key = CustomItemKeys.STEEL_MODULE, model = "industry/steel_module", displayName = Component.text("Steel Module"))
		unStackable(key = CustomItemKeys.STEEL_ASSEMBLY, model = "industry/steel_assembly", displayName = Component.text("Steel Assembly"))
		unStackable(key = CustomItemKeys.REINFORCED_FRAME, model = "industry/reinforced_frame", displayName = Component.text("Reinforced Frame"))
		unStackable(key = CustomItemKeys.REACTOR_FRAME, model = "industry/reactor_frame", displayName = Component.text("Reactor Frame", NamedTextColor.YELLOW))

		unStackable(key = CustomItemKeys.UNLOADED_SHELL, model = "industry/unloaded_shell", displayName = Component.text("Unloaded Shell"))
		stackable(key = CustomItemKeys.LOADED_SHELL, model = "industry/loaded_shell", displayName = Component.text("Loaded Shell"))
		unStackable(key = CustomItemKeys.UNCHARGED_SHELL, model = "industry/uncharged_shell", displayName = Component.text("Uncharged Shell"))
		stackable(key = CustomItemKeys.CHARGED_SHELL, model = "industry/charged_shell", displayName = Component.text("Charged Shell"))

		stackable(key = CustomItemKeys.ARSENAL_MISSILE, model = "projectile/arsenal_missile", displayName = Component.text("Arsenal Missile"))
		unStackable(key = CustomItemKeys.UNLOADED_ARSENAL_MISSILE, model = "projectile/unloaded_arsenal_missile", displayName = Component.text("Unloaded Arsenal Missile"))
		unStackable(key = CustomItemKeys.ACTIVATED_ARSENAL_MISSILE, model = "projectile/activated_arsenal_missile", displayName = Component.text("Activated Arsenal Missile", NamedTextColor.RED))

		register(CustomItemKeys.PROGRESS_HOLDER, ProgressHolder)
	}

	private fun registerShipCores() {
		customBlockItem(key = CustomItemKeys.BATTLECRUISER_REACTOR_CORE, model = "starship/battlecruiser_reactor_core", displayName = Component.text(
            "Battlecruiser Reactor Core",
            NamedTextColor.WHITE,
            TextDecoration.BOLD
        ), customBlock = CustomBlockKeys.BATTLECRUISER_REACTOR_CORE)
		customBlockItem(key = CustomItemKeys.BARGE_REACTOR_CORE, model = "starship/barge_reactor_core", displayName = Component.text("Barge Reactor Core", NamedTextColor.WHITE, TextDecoration.BOLD), customBlock = CustomBlockKeys.BARGE_REACTOR_CORE)
		customBlockItem(key = CustomItemKeys.CRUISER_REACTOR_CORE, model = "starship/cruiser_reactor_core", displayName = Component.text(
            "Cruiser Reactor Core",
            NamedTextColor.WHITE,
            TextDecoration.BOLD
        ), customBlock = CustomBlockKeys.CRUISER_REACTOR_CORE)
	}

	private fun registerGasCanisters() {
		fun canisterName(gasName: Component): Component = ofChildren(gasName, Component.text(" Gas Canister", NamedTextColor.GRAY))

		unStackable(CustomItemKeys.GAS_CANISTER_EMPTY, model = "gas/gas_canister_empty", displayName = Component.text("Empty Gas Canister"))
		register(
            CustomItemKeys.GAS_CANISTER_HYDROGEN,
            GasCanister(CustomItemKeys.GAS_CANISTER_HYDROGEN, "gas/gas_canister_hydrogen", canisterName(Component.text("Hydrogen", NamedTextColor.RED)), AtmosphericGasKeys.HYDROGEN)
        )
		register(
            CustomItemKeys.GAS_CANISTER_NITROGEN,
            GasCanister(CustomItemKeys.GAS_CANISTER_NITROGEN, "gas/gas_canister_nitrogen", canisterName(Component.text("Nitrogen", NamedTextColor.RED)), AtmosphericGasKeys.NITROGEN)
        )
		register(
            CustomItemKeys.GAS_CANISTER_METHANE,
            GasCanister(CustomItemKeys.GAS_CANISTER_METHANE, "gas/gas_canister_methane", canisterName(Component.text("Methane", NamedTextColor.RED)), AtmosphericGasKeys.METHANE)
        )
		register(
            CustomItemKeys.GAS_CANISTER_OXYGEN,
            GasCanister(CustomItemKeys.GAS_CANISTER_OXYGEN, "gas/gas_canister_oxygen", canisterName(Component.text("Oxygen", NamedTextColor.YELLOW)), AtmosphericGasKeys.OXYGEN)
        )
		register(
            CustomItemKeys.GAS_CANISTER_CHLORINE,
            GasCanister(CustomItemKeys.GAS_CANISTER_CHLORINE, "gas/gas_canister_chlorine", canisterName(Component.text("Chlorine", NamedTextColor.YELLOW)), AtmosphericGasKeys.CHLORINE)
        )
		register(
            CustomItemKeys.GAS_CANISTER_FLUORINE,
            GasCanister(CustomItemKeys.GAS_CANISTER_FLUORINE, "gas/gas_canister_fluorine", canisterName(Component.text("Fluorine", NamedTextColor.YELLOW)), AtmosphericGasKeys.FLUORINE)
        )
		register(
            CustomItemKeys.GAS_CANISTER_HELIUM,
            GasCanister(CustomItemKeys.GAS_CANISTER_HELIUM, "gas/gas_canister_helium", canisterName(Component.text("Helium", NamedTextColor.BLUE)), AtmosphericGasKeys.HELIUM)
        )
		register(
            CustomItemKeys.GAS_CANISTER_CARBON_DIOXIDE,
            GasCanister(
                CustomItemKeys.GAS_CANISTER_CARBON_DIOXIDE,
                "gas/gas_canister_carbon_dioxide",
                canisterName(Component.text("Carbon Dioxide", NamedTextColor.BLUE)),
                AtmosphericGasKeys.CARBON_DIOXIDE
            )
        )
	}

	private fun registerTools() {
		register(CustomItemKeys.BATTERY_A, Battery(CustomItemKeys.BATTERY_A, 'A', NamedTextColor.RED, 1000))
		register(CustomItemKeys.BATTERY_M, Battery(CustomItemKeys.BATTERY_M, 'M', NamedTextColor.GREEN, 2500))
		register(CustomItemKeys.BATTERY_G, Battery(CustomItemKeys.BATTERY_G, 'G', NamedTextColor.GOLD, 7500))

		register(CustomItemKeys.CRATE_PLACER, CratePlacer)
		register(CustomItemKeys.MULTIMETER, MultimeterItem)

		register(CustomItemKeys.MULTIBLOCK_TOKEN, MultiblockToken)
		register(CustomItemKeys.PACKAGED_MULTIBLOCK, PackagedMultiblock)

		register(
			CustomItemKeys.MULTIBLOCK_WORKBENCH,
            CustomBlockItem(CustomItemKeys.MULTIBLOCK_WORKBENCH, "misc/multiblock_workbench", Component.text("Multiblock Workbench"), CustomBlockKeys.MULTIBLOCK_WORKBENCH)
		)
		register(CustomItemKeys.WRENCH, Wrench)

		customBlockItem(CustomItemKeys.ADVANCED_ITEM_EXTRACTOR, "misc/advanced_item_extractor", Component.text("Advanced Item Extractor"), CustomBlockKeys.ADVANCED_ITEM_EXTRACTOR)
		customBlockItem(CustomItemKeys.ITEM_FILTER, "misc/item_filter", Component.text("Item Filter"), CustomBlockKeys.ITEM_FILTER)

		fun formatToolName(tierName: String, tierColor: TextColor, toolName: String) = ofChildren(
            Component.text("$tierName ", tierColor),
            Component.text("Power ", NamedTextColor.GOLD),
            Component.text(toolName, NamedTextColor.GRAY)
        )

		register(
            CustomItemKeys.POWER_DRILL_BASIC, PowerDrill(
                key = CustomItemKeys.POWER_DRILL_BASIC,
                displayName = formatToolName("Basic", HEColorScheme.Companion.HE_LIGHT_ORANGE, "Drill"),
                modLimit = 2,
                basePowerCapacity = 50_000,
                model = "tool/power_drill_basic"
            )
        )

		register(
            CustomItemKeys.POWER_DRILL_ENHANCED, PowerDrill(
                key = CustomItemKeys.POWER_DRILL_ENHANCED,
                displayName = formatToolName("Enhanced", TextColor.fromHexString("#00FFA1")!!, "Drill"),
                modLimit = 4,
                basePowerCapacity = 75_000,
                model = "tool/power_drill_enhanced"
            )
        )

		register(
            CustomItemKeys.POWER_DRILL_ADVANCED, PowerDrill(
                key = CustomItemKeys.POWER_DRILL_ADVANCED,
                displayName = formatToolName("Advanced", TextColor.fromHexString("#B12BC9")!!, "Drill"),
                modLimit = 6,
                basePowerCapacity = 100_000,
                model = "tool/power_drill_advanced"
            )
        )

		register(
            CustomItemKeys.POWER_CHAINSAW_BASIC, PowerChainsaw(
                key = CustomItemKeys.POWER_CHAINSAW_BASIC,
                displayName = formatToolName("Basic", HEColorScheme.Companion.HE_LIGHT_ORANGE, "Chainsaw"),
                modLimit = 2,
                basePowerCapacity = 50_000,
                model = "tool/power_chainsaw_basic",
                initialBlocksBroken = 50
            )
        )
		register(
            CustomItemKeys.POWER_CHAINSAW_ENHANCED, PowerChainsaw(
                key = CustomItemKeys.POWER_CHAINSAW_ENHANCED,
                displayName = formatToolName("Enhanced", TextColor.fromHexString("#00FFA1")!!, "Chainsaw"),
                modLimit = 4,
                basePowerCapacity = 75_000,
                model = "tool/power_chainsaw_enhanced",
                initialBlocksBroken = 100
            )
        )
		register(
            CustomItemKeys.POWER_CHAINSAW_ADVANCED, PowerChainsaw(
                key = CustomItemKeys.POWER_CHAINSAW_ADVANCED,
                displayName = formatToolName("Advanced", TextColor.fromHexString("#B12BC9")!!, "Chainsaw"),
                modLimit = 6,
                basePowerCapacity = 100_000,
                model = "tool/power_chainsaw_advanced",
                initialBlocksBroken = 150
            )
        )

		register(
            CustomItemKeys.POWER_HOE_BASIC, PowerHoe(
                key = CustomItemKeys.POWER_HOE_BASIC,
                displayName = formatToolName("Basic", HEColorScheme.Companion.HE_LIGHT_ORANGE, "Hoe"),
                modLimit = 2,
                basePowerCapacity = 50_000,
                model = "tool/power_hoe_basic"
            )
        )
		register(
            CustomItemKeys.POWER_HOE_ENHANCED, PowerHoe(
                key = CustomItemKeys.POWER_HOE_ENHANCED,
                displayName = formatToolName("Enhanced", TextColor.fromHexString("#00FFA1")!!, "Hoe"),
                modLimit = 4,
                basePowerCapacity = 75_000,
                model = "tool/power_hoe_enhanced"
            )
        )
		register(
            CustomItemKeys.POWER_HOE_ADVANCED, PowerHoe(
                key = CustomItemKeys.POWER_HOE_ADVANCED,
                displayName = formatToolName("Advanced", TextColor.fromHexString("#B12BC9")!!, "Hoe"),
                modLimit = 6,
                basePowerCapacity = 100_000,
                model = "tool/power_hoe_advanced"
            )
        )

		register(CustomItemKeys.PERSONAL_TRANSPORTER, PersonalTransporter)

		register(
            CustomItemKeys.POWER_ARMOR_HELMET, PowerArmorItem(
                CustomItemKeys.POWER_ARMOR_HELMET,
                ofChildren(Component.text("Power ", NamedTextColor.GOLD), Component.text("Helmet", NamedTextColor.GRAY)),
                "power_armor/power_armor_helmet",
                EquipmentSlot.HEAD
            )
        )
		register(
            CustomItemKeys.POWER_ARMOR_CHESTPLATE, PowerArmorItem(
                CustomItemKeys.POWER_ARMOR_CHESTPLATE,
                ofChildren(Component.text("Power ", NamedTextColor.GOLD), Component.text("Chestplate", NamedTextColor.GRAY)),
                "power_armor/power_armor_chestplate",
                EquipmentSlot.CHEST
            )
        )
		register(
            CustomItemKeys.POWER_ARMOR_LEGGINGS, PowerArmorItem(
                CustomItemKeys.POWER_ARMOR_LEGGINGS,
                ofChildren(Component.text("Power ", NamedTextColor.GOLD), Component.text("Leggings", NamedTextColor.GRAY)),
                "power_armor/power_armor_leggings",
                EquipmentSlot.LEGS
            )
        )
		register(
            CustomItemKeys.POWER_ARMOR_BOOTS, PowerArmorItem(
                CustomItemKeys.POWER_ARMOR_BOOTS,
                ofChildren(Component.text("Power ", NamedTextColor.GOLD), Component.text("Boots", NamedTextColor.GRAY)),
                "power_armor/power_armor_boots",
                EquipmentSlot.FEET
            )
        )
	}

	private fun registerEnergySwords() {
		register(CustomItemKeys.ENERGY_SWORD_BLUE, EnergySword(CustomItemKeys.ENERGY_SWORD_BLUE, "BLUE", NamedTextColor.BLUE))
		register(CustomItemKeys.ENERGY_SWORD_RED, EnergySword(CustomItemKeys.ENERGY_SWORD_RED, "RED", NamedTextColor.RED))
		register(CustomItemKeys.ENERGY_SWORD_YELLOW, EnergySword(CustomItemKeys.ENERGY_SWORD_YELLOW, "YELLOW", NamedTextColor.YELLOW))
		register(CustomItemKeys.ENERGY_SWORD_GREEN, EnergySword(CustomItemKeys.ENERGY_SWORD_GREEN, "GREEN", NamedTextColor.GREEN))
		register(CustomItemKeys.ENERGY_SWORD_PURPLE, EnergySword(CustomItemKeys.ENERGY_SWORD_PURPLE, "PURPLE", NamedTextColor.DARK_PURPLE))
		register(CustomItemKeys.ENERGY_SWORD_ORANGE, EnergySword(CustomItemKeys.ENERGY_SWORD_ORANGE, "ORANGE", TextColor.fromHexString("#FF5F15")!!))
		register(CustomItemKeys.ENERGY_SWORD_PINK, EnergySword(CustomItemKeys.ENERGY_SWORD_PINK, "PINK", TextColor.fromHexString("#FFC0CB")!!))
		register(CustomItemKeys.ENERGY_SWORD_BLACK, EnergySword(CustomItemKeys.ENERGY_SWORD_BLACK, "BLACK", NamedTextColor.BLACK))
	}

	private fun registerModificationItems() {
		register(
			CustomItemKeys.ARMOR_MODIFICATION_ENVIRONMENT, ModificationItem(
                CustomItemKeys.ARMOR_MODIFICATION_ENVIRONMENT,
                ItemModKeys.ENVIRONMENT,
                "power_armor/module/environment",
                ofChildren(Component.text("Enviornment", NamedTextColor.GRAY), Component.text(" Module", NamedTextColor.GOLD)),
                Component.text("Allows the user to survive inhospitable planetary enviornments.")
            )
		)
		register(
			CustomItemKeys.ARMOR_MODIFICATION_NIGHT_VISION, ModificationItem(
                CustomItemKeys.ARMOR_MODIFICATION_NIGHT_VISION,
                ItemModKeys.NIGHT_VISION,
                "power_armor/module/night_vision",
                ofChildren(Component.text("Night Vision", NamedTextColor.GRAY), Component.text(" Module", NamedTextColor.GOLD)),
                Component.text("Allows the user to see in dark enviornments. ")
            )
		)
		register(
            CustomItemKeys.ARMOR_MODIFICATION_PRESSURE_FIELD,
            ModificationItem(
                CustomItemKeys.ARMOR_MODIFICATION_PRESSURE_FIELD,
                ItemModKeys.PRESSURE_FIELD,
                "power_armor/module/pressure_field",
                ofChildren(Component.text("Pressure Field", NamedTextColor.GRAY), Component.text(" Module", NamedTextColor.GOLD)),
                Component.text("Allows the user to breathe in space.")
            )
		)
		register(
            CustomItemKeys.ARMOR_MODIFICATION_ROCKET_BOOSTING,
            ModificationItem(
                CustomItemKeys.ARMOR_MODIFICATION_ROCKET_BOOSTING,
                ItemModKeys.ROCKET_BOOSTING,
                "power_armor/module/rocket_boosting",
                ofChildren(Component.text("Rocket Boosting", NamedTextColor.GRAY), Component.text(" Module", NamedTextColor.GOLD)),
                Component.text("Allows propelled flight.")
            )
		)
		register(
			CustomItemKeys.ARMOR_MODIFICATION_SHOCK_ABSORBING, ModificationItem(
                CustomItemKeys.ARMOR_MODIFICATION_SHOCK_ABSORBING,
                ItemModKeys.SHOCK_ABSORBING,
                "power_armor/module/shock_absorbing",
                ofChildren(Component.text("Shock Absorbing", NamedTextColor.GRAY), Component.text(" Module", NamedTextColor.GOLD)),
                Component.text("Reduces knockback.")
            )
		)
		register(
			CustomItemKeys.ARMOR_MODIFICATION_SPEED_BOOSTING, ModificationItem(
                CustomItemKeys.ARMOR_MODIFICATION_SPEED_BOOSTING,
                ItemModKeys.SPEED_BOOSTING,
                "power_armor/module/speed_boosting",
                ofChildren(Component.text("Speed Boosting", NamedTextColor.GRAY), Component.text(" Module", NamedTextColor.GOLD)),
                Component.text("Boosts the user's running speed.")
            )
		)

		register(
			CustomItemKeys.TOOL_MODIFICATION_RANGE_1, ModificationItem(
                CustomItemKeys.TOOL_MODIFICATION_RANGE_1,
                ItemModKeys.AOE_1,
                "tool/modification/drill_aoe_1",
                Component.text("Range Addon +1"),
                Component.text("Expands the working area by 1 block", NamedTextColor.GRAY)
            )
		)
		register(
			CustomItemKeys.TOOL_MODIFICATION_RANGE_2, ModificationItem(
                CustomItemKeys.TOOL_MODIFICATION_RANGE_2,
                ItemModKeys.AOE_2,
                "tool/modification/drill_aoe_2",
                Component.text("Range Addon +2"),
                Component.text("Expands the working area by 2 blocks", NamedTextColor.GRAY)
            )
		)
		register(
			CustomItemKeys.TOOL_MODIFICATION_VEIN_MINER_25, ModificationItem(
                CustomItemKeys.TOOL_MODIFICATION_VEIN_MINER_25,
                ItemModKeys.VEIN_MINER_25,
                "tool/modification/drill_vein_miner_25",
                Component.text("Vein Miner"),
                Component.text("Allows a drill to mine veins of connected blocks, up to 25.", NamedTextColor.GRAY)
            )
		)
		register(
			CustomItemKeys.TOOL_MODIFICATION_SILK_TOUCH_MOD, ModificationItem(
                CustomItemKeys.TOOL_MODIFICATION_SILK_TOUCH_MOD,
                ItemModKeys.SILK_TOUCH,
                "tool/modification/silk_touch",
                Component.text("Silk Touch Modifier"),
                Component.text("Applies silk touch to drops", NamedTextColor.GRAY),
                Component.text("Incurs a power usage penalty", NamedTextColor.RED)
            )
		)
		register(
			CustomItemKeys.TOOL_MODIFICATION_AUTO_SMELT, ModificationItem(
                CustomItemKeys.TOOL_MODIFICATION_AUTO_SMELT,
                ItemModKeys.AUTO_SMELT,
                "tool/modification/auto_smelt",
                Component.text("Auto Smelt Modifier"),
                Component.text("Sears the drops before they hit the ground", NamedTextColor.GRAY),
                Component.text("Incurs a power usage penalty", NamedTextColor.RED)
            )
		)
		register(
			CustomItemKeys.TOOL_MODIFICATION_FORTUNE_1, ModificationItem(
                CustomItemKeys.TOOL_MODIFICATION_FORTUNE_1,
                ItemModKeys.FORTUNE_1,
                "tool/modification/fortune_1",
                Component.text("Fortune 1 Modifier"),
                Component.text("Applies fortune 1 touch to drops", NamedTextColor.GRAY),
                Component.text("Incurs a power usage penalty", NamedTextColor.RED)
            )
		)
		register(
			CustomItemKeys.TOOL_MODIFICATION_FORTUNE_2, ModificationItem(
                CustomItemKeys.TOOL_MODIFICATION_FORTUNE_2,
                ItemModKeys.FORTUNE_2,
                "tool/modification/fortune_2",
                Component.text("Fortune 2 Modifier"),
                Component.text("Applies fortune 2 touch to drops", NamedTextColor.GRAY),
                Component.text("Incurs a power usage penalty", NamedTextColor.RED)
            )
		)
		register(
			CustomItemKeys.TOOL_MODIFICATION_FORTUNE_3, ModificationItem(
                CustomItemKeys.TOOL_MODIFICATION_FORTUNE_3,
                ItemModKeys.FORTUNE_3,
                "tool/modification/fortune_3",
                Component.text("Fortune 3 Modifier"),
                Component.text("Applies fortune 3 touch to drops", NamedTextColor.GRAY),
                Component.text("Incurs a power usage penalty", NamedTextColor.RED)
            )
		)
		register(
			CustomItemKeys.TOOL_MODIFICATION_POWER_CAPACITY_25, ModificationItem(
                CustomItemKeys.TOOL_MODIFICATION_POWER_CAPACITY_25,
                ItemModKeys.POWER_CAPACITY_25,
                "tool/modification/power_capacity_25",
                Component.text("Small Auxiliary battery"),
                ofChildren(
                    Component.text("Increases power storage by ", HEColorScheme.Companion.HE_MEDIUM_GRAY),
                    PowerEntityDisplayModule.Companion.powerPrefix,
                    Component.text(25000, NamedTextColor.GREEN)
                )
            )
		)
		register(
			CustomItemKeys.TOOL_MODIFICATION_POWER_CAPACITY_50, ModificationItem(
                CustomItemKeys.TOOL_MODIFICATION_POWER_CAPACITY_50,
                ItemModKeys.POWER_CAPACITY_50,
                "tool/modification/power_capacity_50",
                Component.text("Medium Auxiliary battery"),
                ofChildren(
                    Component.text("Increases power storage by ", HEColorScheme.Companion.HE_MEDIUM_GRAY),
                    PowerEntityDisplayModule.Companion.powerPrefix,
                    Component.text(50000, NamedTextColor.GREEN)
                )
            )
        )
		register(
			CustomItemKeys.TOOL_MODIFICATION_AUTO_REPLANT, ModificationItem(
                CustomItemKeys.TOOL_MODIFICATION_AUTO_REPLANT,
                ItemModKeys.AUTO_REPLANT,
                "tool/modification/auto_replant",
                Component.text("Auto Replant Modifier"),
                Component.text("Automatically plants back harvested crops and cut trees", NamedTextColor.GRAY)
            )
		)
		register(
			CustomItemKeys.TOOL_MODIFICATION_AUTO_COMPOST, ModificationItem(
                CustomItemKeys.TOOL_MODIFICATION_AUTO_COMPOST,
                ItemModKeys.AUTO_COMPOST,
                "tool/modification/auto_compost",
                Component.text("Auto Compost Modifier"),
                Component.text("Sends applicable drops through a composter, turning them into bonemeal.", NamedTextColor.GRAY)
            )
		)
		register(
			CustomItemKeys.TOOL_MODIFICATION_RANGE_3, ModificationItem(
                CustomItemKeys.TOOL_MODIFICATION_RANGE_3,
                ItemModKeys.AOE_3,
                "tool/modification/drill_aoe_3",
                Component.text("Range Addon +3"),
                Component.text("Expands the working area by 3 blocks", NamedTextColor.GRAY)
            )
		)
		register(
			CustomItemKeys.TOOL_MODIFICATION_EXTENDED_BAR, ModificationItem(
                CustomItemKeys.TOOL_MODIFICATION_EXTENDED_BAR,
                ItemModKeys.EXTENDED_BAR,
                "tool/modification/extended_bar",
                Component.text("Extended Chainsaw Bar"),
                Component.text("Allows a chainsaw to cut down larger trees", NamedTextColor.GRAY)
            )
		)
		register(
			CustomItemKeys.TOOL_MODIFICATION_FERTILIZER_DISPENSER, ModificationItem(
                CustomItemKeys.TOOL_MODIFICATION_FERTILIZER_DISPENSER,
                ItemModKeys.FERTILIZER_DISPENSER,
                "tool/modification/fertilizer_dispenser",
                Component.text("Fertilizer Sprayer"),
                Component.text("Applies bonemeal to crops in the effected area, if available in the user's inventory", NamedTextColor.GRAY)
            )
		)
		register(
			CustomItemKeys.TOOL_MODIFICATION_COLLECTOR, ModificationItem(
                CustomItemKeys.TOOL_MODIFICATION_COLLECTOR,
                ItemModKeys.COLLECTOR,
				"tool/modification/collector",
				Component.text("Collector Modifier"),
				Component.text("Sends dropped items directly to your inventory if there is room.", NamedTextColor.GRAY)
            )
		)
	}

	// Tools end

	private fun registerPlanetIcons() {
		unStackable(key = CustomItemKeys.AERACH, displayName = Component.text("Aerach"), model = "planet/aerach")
		unStackable(key = CustomItemKeys.ARET, displayName = Component.text("Aret"), model = "planet/aret")
		unStackable(key = CustomItemKeys.CHANDRA, displayName = Component.text("Chandra"), model = "planet/chandra")
		unStackable(key = CustomItemKeys.CHIMGARA, displayName = Component.text("Chimgara"), model = "planet/chimgara")
		unStackable(key = CustomItemKeys.DAMKOTH, displayName = Component.text("Damkoth"), model = "planet/damkoth")
		unStackable(key = CustomItemKeys.DISTERRA, displayName = Component.text("Disterra"), model = "planet/disterra")
		unStackable(key = CustomItemKeys.EDEN, displayName = Component.text("Eden"), model = "planet/eden")
		unStackable(key = CustomItemKeys.GAHARA, displayName = Component.text("Gahara"), model = "planet/gahara")
		unStackable(key = CustomItemKeys.HERDOLI, displayName = Component.text("Herdoli"), model = "planet/herdoli")
		unStackable(key = CustomItemKeys.ILIUS, displayName = Component.text("Ilius"), model = "planet/ilius")
		unStackable(key = CustomItemKeys.ISIK, displayName = Component.text("Isik"), model = "planet/isik")
		unStackable(key = CustomItemKeys.KOVFEFE, displayName = Component.text("Kovfefe"), model = "planet/kovfefe")
		unStackable(key = CustomItemKeys.KRIO, displayName = Component.text("Krio"), model = "planet/krio")
		unStackable(key = CustomItemKeys.LIODA, displayName = Component.text("Lioda"), model = "planet/lioda")
		unStackable(key = CustomItemKeys.LUXITERNA, displayName = Component.text("Luxiterna"), model = "planet/luxiterna")
		unStackable(key = CustomItemKeys.QATRA, displayName = Component.text("Qatra"), model = "planet/qatra")
		unStackable(key = CustomItemKeys.RUBACIEA, displayName = Component.text("Rubaciea"), model = "planet/rubaciea")
		unStackable(key = CustomItemKeys.TURMS, displayName = Component.text("Turms"), model = "planet/turms")
		unStackable(key = CustomItemKeys.VASK, displayName = Component.text("Vask"), model = "planet/vask")
		unStackable(key = CustomItemKeys.ASTERI, displayName = Component.text("Asteri"), model = "planet/asteri")
		unStackable(key = CustomItemKeys.HORIZON, displayName = Component.text("Horizon"), model = "planet/horizon")
		unStackable(key = CustomItemKeys.ILIOS, displayName = Component.text("Ilios"), model = "planet/ilios")
		unStackable(key = CustomItemKeys.REGULUS, displayName = Component.text("Regulus"), model = "planet/regulus")
		unStackable(key = CustomItemKeys.SIRIUS, displayName = Component.text("Sirius"), model = "planet/sirius")
		unStackable(key = CustomItemKeys.PLANET_SELECTOR, displayName = Component.text("PLANET_SELECTOR"), model = "planet/planet_selector")
	}

	private fun simple(key: IonRegistryKey<CustomItem, out CustomItem>, displayName: Component, factory: ItemFactory) {
		register(key, CustomItem(key, displayName, factory))
	}

	private fun stackable(key: IonRegistryKey<CustomItem, out CustomItem>, displayName: Component, model: String) {
		register(key, CustomItem(key, displayName, ItemFactory.stackableCustomItem(model = model)))
	}

	private fun unStackable(key: IonRegistryKey<CustomItem, out CustomItem>, displayName: Component, model: String) {
		register(key, CustomItem(key, displayName, ItemFactory.unStackableCustomItem(model = model)))
	}

	private fun customBlockItem(key: IonRegistryKey<CustomItem, out CustomItem>, model: String, displayName: Component, customBlock: IonRegistryKey<CustomBlock, out CustomBlock>) {
		register(key, CustomBlockItem(key, model, displayName, customBlock))
	}

	companion object {
		val ItemStack.customItem: CustomItem? get() {
			val serialized = persistentDataContainer.get(NamespacedKeys.CUSTOM_ITEM, PersistentDataType.STRING) ?: return null
			val key = CustomItemKeys[serialized] ?: return null
			return key.getValue()
		}
	}
}
