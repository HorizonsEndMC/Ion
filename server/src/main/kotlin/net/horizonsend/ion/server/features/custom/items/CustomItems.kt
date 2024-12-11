package net.horizonsend.ion.server.features.custom.items

import net.horizonsend.ion.common.utils.text.colors.HEColorScheme.Companion.HE_LIGHT_ORANGE
import net.horizonsend.ion.common.utils.text.colors.HEColorScheme.Companion.HE_MEDIUM_GRAY
import net.horizonsend.ion.common.utils.text.ofChildren
import net.horizonsend.ion.common.utils.text.text
import net.horizonsend.ion.server.configuration.ConfigurationFiles
import net.horizonsend.ion.server.configuration.PVPBalancingConfiguration
import net.horizonsend.ion.server.features.custom.blocks.CustomBlock
import net.horizonsend.ion.server.features.custom.blocks.CustomBlocks
import net.horizonsend.ion.server.features.custom.items.misc.PersonalTransporter
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
import org.bukkit.Material.IRON_BLOCK
import org.bukkit.Material.IRON_INGOT
import org.bukkit.Material.WARPED_FUNGUS_ON_A_STICK
import org.bukkit.entity.Entity
import org.bukkit.entity.Item
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType.STRING
import java.util.function.Supplier

// budget minecraft registry lmao
@Suppress("unused")
object CustomItems {
	// If we want to be extra fancy we can replace this with some fastutils thing later .
	val ALL get() = customItems.values
	private val customItems: MutableMap<String, CustomItem> = mutableMapOf()

	// Starship Components Start
	val BATTLECRUISER_REACTOR_CORE = registerCustomBlockItem(identifier = "BATTLECRUISER_REACTOR_CORE", baseBlock = IRON_BLOCK, customModelData = 2000, displayName = text("Battlecruiser Reactor Core", BOLD)) { CustomBlocks.BATTLECRUISER_REACTOR_CORE }
	val BARGE_REACTOR_CORE = registerCustomBlockItem(identifier = "BARGE_REACTOR_CORE", baseBlock = IRON_BLOCK, customModelData = 2002, displayName = text("Barge Reactor Core", BOLD)) { CustomBlocks.BARGE_REACTOR_CORE }
	val CRUISER_REACTOR_CORE = registerCustomBlockItem(identifier = "CRUISER_REACTOR_CORE", baseBlock = IRON_BLOCK, customModelData = 2001, displayName = text("Cruiser Reactor Core", BOLD)) { CustomBlocks.CRUISER_REACTOR_CORE }
	// Starship Components End
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

	val DETONATOR = registerThrowable("DETONATOR", 1101, ofChildren(text("Thermal ", RED), text("Detonator", GRAY)).itemName, ConfigurationFiles.pvpBalancing().throwables::detonator) { item, maxTicks, source -> ThrownDetonator(item, maxTicks, source, ConfigurationFiles.pvpBalancing().throwables::detonator) }
	val SMOKE_GRENADE = registerThrowable("SMOKE_GRENADE", 1102, ofChildren(text("Smoke ", DARK_GREEN), text("Grenade", GRAY)).itemName, ConfigurationFiles.pvpBalancing().throwables::smokeGrenade) { item, maxTicks, source -> ThrownSmokeGrenade(item, maxTicks, source) }

	val PUMPKIN_GRENADE = register(object : ThrowableCustomItem("PUMPKIN_GRENADE", 0, ofChildren(text("Pumpkin ", GOLD), text("Grenade", GREEN)).itemName, ConfigurationFiles.pvpBalancing().throwables::detonator) {
		override fun constructItemStack(): ItemStack = super.constructItemStack().apply { type = Material.PUMPKIN }.updateMeta { it.lore(mutableListOf(text("Spooky", LIGHT_PURPLE))) }
		override fun constructThrownRunnable(item: Item, maxTicks: Int, damageSource: Entity?): ThrownCustomItem = ThrownPumpkinGrenade(item, maxTicks, damageSource, ConfigurationFiles.pvpBalancing().throwables::detonator)
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

	private fun registerCustomBlockItem(identifier: String, baseBlock: Material, customModelData: Int, displayName: Component, customBlock: Supplier<CustomBlock>): CustomBlockItem {
		val formattedDisplayName = text()
			.decoration(ITALIC, false)
			.append(displayName)
			.build()

		return CustomBlockItem(identifier, baseBlock, "", formattedDisplayName, customBlock)
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
