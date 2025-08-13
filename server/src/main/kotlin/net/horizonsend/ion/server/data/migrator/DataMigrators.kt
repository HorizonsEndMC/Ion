package net.horizonsend.ion.server.data.migrator

import com.github.stefvanschie.inventoryframework.gui.type.ChestGui
import io.papermc.paper.datacomponent.DataComponentTypes
import io.papermc.paper.datacomponent.item.ItemAttributeModifiers
import net.horizonsend.ion.server.core.IonServerComponent
import net.horizonsend.ion.server.core.registration.keys.CustomItemKeys
import net.horizonsend.ion.server.core.registration.registries.CustomItemRegistry.Companion.customItem
import net.horizonsend.ion.server.data.migrator.types.item.MigratorResult
import net.horizonsend.ion.server.data.migrator.types.item.legacy.LegacyCustomItemMigrator
import net.horizonsend.ion.server.data.migrator.types.item.modern.migrator.AspectMigrator
import net.horizonsend.ion.server.data.migrator.types.item.modern.migrator.LegacyNameFixer
import net.horizonsend.ion.server.data.migrator.types.item.modern.migrator.ReplacementMigrator
import net.horizonsend.ion.server.features.custom.items.component.CustomComponentTypes.Companion.MOD_MANAGER
import net.horizonsend.ion.server.miscellaneous.registrations.legacy.LegacyPowerArmorModule
import net.horizonsend.ion.server.miscellaneous.registrations.persistence.NamespacedKeys
import net.horizonsend.ion.server.miscellaneous.utils.isPipedInventory
import org.bukkit.Chunk
import org.bukkit.Material
import org.bukkit.attribute.Attribute
import org.bukkit.attribute.AttributeModifier
import org.bukkit.craftbukkit.inventory.CraftBlockInventoryHolder
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.inventory.InventoryOpenEvent
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.world.ChunkLoadEvent
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.InventoryHolder
import org.bukkit.persistence.PersistentDataType

@Suppress("UnstableApiUsage")
object DataMigrators : IonServerComponent() {
	override fun onEnable() {
		registerDataVersions()
	}

	private val dataVersions = mutableListOf<DataVersion>()
	private val lastDataVersion get() = dataVersions.lastIndex

	private fun registerDataVersions() {
		registerDataVersion(DataVersion.builder(0).build()) // Base server version

		registerDataVersion(DataVersion
			.builder(1)
			.addMigrator(LegacyCustomItemMigrator(
				predicate = { it.type == Material.BOW && it.itemMeta.hasCustomModelData() && it.itemMeta.customModelData == 1 && it.customItem == null },
				converter = { MigratorResult.Replacement(CustomItemKeys.BLASTER_PISTOL.getValue().constructItemStack()) }
			))
			.addMigrator(LegacyCustomItemMigrator(
				predicate = { it.type == Material.BOW && it.itemMeta.hasCustomModelData() && it.itemMeta.customModelData == 2 && it.customItem == null },
				converter = { MigratorResult.Replacement(CustomItemKeys.BLASTER_RIFLE.getValue().constructItemStack()) }
			))
			.addMigrator(LegacyCustomItemMigrator(
				predicate = { it.type == Material.BOW && it.itemMeta.hasCustomModelData() && it.itemMeta.customModelData == 3 && it.customItem == null },
				converter = { MigratorResult.Replacement(CustomItemKeys.BLASTER_SNIPER.getValue().constructItemStack()) }
			))
			.addMigrator(LegacyCustomItemMigrator(
				predicate = { it.type == Material.BOW && it.itemMeta.hasCustomModelData() && it.itemMeta.customModelData == 4 && it.customItem == null },
				converter = { MigratorResult.Replacement(CustomItemKeys.BLASTER_CANNON.getValue().constructItemStack()) }
			))
			.addMigrator(LegacyCustomItemMigrator(
				predicate = {
					it.type == Material.DIAMOND_PICKAXE
						&& it.itemMeta.hasCustomModelData()
						&& it.itemMeta.customModelData == 1
						&& it.customItem == null
				},
				converter = { MigratorResult.Replacement(CustomItemKeys.POWER_DRILL_BASIC.getValue().constructItemStack()) }
			))
			// Start minerals
			.addMigrator(LegacyCustomItemMigrator(
				predicate = {
					it.type == Material.IRON_INGOT
						&& it.itemMeta.hasCustomModelData()
						&& it.itemMeta.customModelData == 1
						&& it.customItem == null
				},
				converter = { MigratorResult.Replacement(CustomItemKeys.ALUMINUM_INGOT.getValue().constructItemStack()) }
			))
			.addMigrator(LegacyCustomItemMigrator(
				predicate = {
					it.type == Material.IRON_ORE
						&& it.itemMeta.hasCustomModelData()
						&& it.itemMeta.customModelData == 1
						&& it.customItem == null
				},
				converter = { MigratorResult.Replacement(CustomItemKeys.ALUMINUM_ORE.getValue().constructItemStack()) }
			))
			.addMigrator(LegacyCustomItemMigrator(
				predicate = {
					it.type == Material.IRON_BLOCK
						&& it.itemMeta.hasCustomModelData()
						&& it.itemMeta.customModelData == 1
						&& it.customItem == null
				},
				converter = { MigratorResult.Replacement(CustomItemKeys.ALUMINUM_BLOCK.getValue().constructItemStack()) }
			))
			.addMigrator(LegacyCustomItemMigrator(
				predicate = {
					it.type == Material.IRON_INGOT
						&& it.itemMeta.hasCustomModelData()
						&& it.itemMeta.customModelData == 2
						&& it.customItem == null
				},
				converter = { MigratorResult.Replacement(CustomItemKeys.CHETHERITE.getValue().constructItemStack()) }
			))
			.addMigrator(LegacyCustomItemMigrator(
				predicate = {
					it.type == Material.IRON_ORE
						&& it.itemMeta.hasCustomModelData()
						&& it.itemMeta.customModelData == 2
						&& it.customItem == null
				},
				converter = { MigratorResult.Replacement(CustomItemKeys.CHETHERITE_ORE.getValue().constructItemStack()) }
			))
			.addMigrator(LegacyCustomItemMigrator(
				predicate = {
					it.type == Material.IRON_BLOCK
						&& it.itemMeta.hasCustomModelData()
						&& it.itemMeta.customModelData == 2
						&& it.customItem == null
				},
				converter = { MigratorResult.Replacement(CustomItemKeys.CHETHERITE_BLOCK.getValue().constructItemStack()) }
			))
			.addMigrator(LegacyCustomItemMigrator(
				predicate = {
					it.type == Material.IRON_INGOT
						&& it.itemMeta.hasCustomModelData()
						&& it.itemMeta.customModelData == 3
						&& it.customItem == null
				},
				converter = { MigratorResult.Replacement(CustomItemKeys.TITANIUM_INGOT.getValue().constructItemStack()) }
			))
			.addMigrator(LegacyCustomItemMigrator(
				predicate = {
					it.type == Material.IRON_ORE
						&& it.itemMeta.hasCustomModelData()
						&& it.itemMeta.customModelData == 3
						&& it.customItem == null
				},
				converter = { MigratorResult.Replacement(CustomItemKeys.TITANIUM_ORE.getValue().constructItemStack()) }
			))
			.addMigrator(LegacyCustomItemMigrator(
				predicate = {
					it.type == Material.IRON_BLOCK
						&& it.itemMeta.hasCustomModelData()
						&& it.itemMeta.customModelData == 3
						&& it.customItem == null
				},
				converter = { MigratorResult.Replacement(CustomItemKeys.TITANIUM_BLOCK.getValue().constructItemStack()) }
			))
			.addMigrator(LegacyCustomItemMigrator(
				predicate = {
					it.type == Material.IRON_INGOT
						&& it.itemMeta.hasCustomModelData()
						&& it.itemMeta.customModelData == 4
						&& it.customItem == null
				},
				converter = { MigratorResult.Replacement(CustomItemKeys.URANIUM.getValue().constructItemStack()) }
			))
			.addMigrator(LegacyCustomItemMigrator(
				predicate = {
					it.type == Material.IRON_ORE
						&& it.itemMeta.hasCustomModelData()
						&& it.itemMeta.customModelData == 4
						&& it.customItem == null
				},
				converter = { MigratorResult.Replacement(CustomItemKeys.URANIUM_ORE.getValue().constructItemStack()) }
			))
			.addMigrator(LegacyCustomItemMigrator(
				predicate = {
					it.type == Material.IRON_BLOCK
						&& it.itemMeta.hasCustomModelData()
						&& it.itemMeta.customModelData == 4
						&& it.customItem == null
				},
				converter = { MigratorResult.Replacement(CustomItemKeys.URANIUM_BLOCK.getValue().constructItemStack()) }
			))
			.addMigrator(LegacyCustomItemMigrator(
				predicate = {
					it.type == Material.SHEARS
						&& it.itemMeta.hasCustomModelData()
						&& it.itemMeta.customModelData == 1
						&& it.customItem == null
				},
				converter = { MigratorResult.Replacement(CustomItemKeys.DETONATOR.getValue().constructItemStack()) }
			))
			// Minerals end
			// Batteries
			.addMigrator(LegacyCustomItemMigrator(
				predicate = {
					it.type == Material.SNOWBALL
						&& it.itemMeta.hasCustomModelData()
						&& it.itemMeta.customModelData == 7
						&& it.customItem == null
				},
				converter = { MigratorResult.Replacement(CustomItemKeys.BATTERY_A.getValue().constructItemStack(it.amount)) }
			))
			.addMigrator(LegacyCustomItemMigrator(
				predicate = {
					it.type == Material.SNOWBALL
						&& it.itemMeta.hasCustomModelData()
						&& it.itemMeta.customModelData == 8
						&& it.customItem == null
				},
				converter = { MigratorResult.Replacement(CustomItemKeys.BATTERY_M.getValue().constructItemStack(it.amount)) }
			))
			.addMigrator(LegacyCustomItemMigrator(
				predicate = {
					it.type == Material.SNOWBALL
						&& it.itemMeta.hasCustomModelData()
						&& it.itemMeta.customModelData == 9
						&& it.customItem == null
				},
				converter = { MigratorResult.Replacement(CustomItemKeys.BATTERY_G.getValue().constructItemStack(it.amount)) }
			))
			.addMigrator(LegacyCustomItemMigrator(
				predicate = {
					it.type == Material.LEATHER_HELMET
						&& it.itemMeta.hasCustomModelData()
						&& it.itemMeta.customModelData == 1
						&& it.customItem == null
				},
				converter = { old ->
					@Suppress("DEPRECATION")
					val oldMods = old.lore
						?.filter { it.startsWith("Module: ") }
						?.mapNotNull { LegacyPowerArmorModule[it.split(" ")[1]]?.modern }
						?.toSet()
						?: setOf()

					val new = CustomItemKeys.POWER_ARMOR_HELMET.getValue().constructItemStack()
					CustomItemKeys.POWER_ARMOR_HELMET.getValue().getComponent(MOD_MANAGER).setMods(new, CustomItemKeys.POWER_ARMOR_HELMET.getValue(), oldMods.toTypedArray())
					old.getData(DataComponentTypes.DYED_COLOR)?.let { color -> new.setData(DataComponentTypes.DYED_COLOR, color) }
					MigratorResult.Replacement(new)
				}
			))
			.addMigrator(LegacyCustomItemMigrator(
				predicate = {
					it.type == Material.LEATHER_CHESTPLATE
						&& it.itemMeta.hasCustomModelData()
						&& it.itemMeta.customModelData == 1
						&& it.customItem == null
				},
				converter = { old ->
					@Suppress("DEPRECATION")
					val oldMods = old.lore
						?.filter { it.startsWith("Module: ") }
						?.mapNotNull { LegacyPowerArmorModule[it.split(" ")[1]]?.modern }
						?.toSet()
						?: setOf()

					val new = CustomItemKeys.POWER_ARMOR_CHESTPLATE.getValue().constructItemStack()
					CustomItemKeys.POWER_ARMOR_CHESTPLATE.getValue().getComponent(MOD_MANAGER).setMods(new, CustomItemKeys.POWER_ARMOR_CHESTPLATE.getValue(), oldMods.toTypedArray())
					old.getData(DataComponentTypes.DYED_COLOR)?.let { color -> new.setData(DataComponentTypes.DYED_COLOR, color) }
					MigratorResult.Replacement(new)
				}
			))
			.addMigrator(LegacyCustomItemMigrator(
				predicate = {
					it.type == Material.LEATHER_LEGGINGS
						&& it.itemMeta.hasCustomModelData()
						&& it.itemMeta.customModelData == 1
						&& it.customItem == null
				},
				converter = { old ->
					@Suppress("DEPRECATION")
					val oldMods = old.lore
						?.filter { it.startsWith("Module: ") }
						?.mapNotNull { LegacyPowerArmorModule[it.split(" ")[1]]?.modern }
						?.toSet()
						?: setOf()

					val new = CustomItemKeys.POWER_ARMOR_LEGGINGS.getValue().constructItemStack()
					CustomItemKeys.POWER_ARMOR_LEGGINGS.getValue().getComponent(MOD_MANAGER).setMods(new, CustomItemKeys.POWER_ARMOR_LEGGINGS.getValue(), oldMods.toTypedArray())
					old.getData(DataComponentTypes.DYED_COLOR)?.let { color -> new.setData(DataComponentTypes.DYED_COLOR, color) }
					MigratorResult.Replacement(new)
				}
			))
			.addMigrator(LegacyCustomItemMigrator(
				predicate = {
					it.type == Material.LEATHER_BOOTS
						&& it.itemMeta.hasCustomModelData()
						&& it.itemMeta.customModelData == 1
						&& it.customItem == null
				},
				converter = { old ->
					@Suppress("DEPRECATION")
					val oldMods = old.lore
						?.filter { it.startsWith("Module: ") }
						?.mapNotNull { LegacyPowerArmorModule[it.split(" ")[1]]?.modern }
						?.toSet()
						?: setOf()

					val new = CustomItemKeys.POWER_ARMOR_BOOTS.getValue().constructItemStack()
					CustomItemKeys.POWER_ARMOR_BOOTS.getValue().getComponent(MOD_MANAGER).setMods(new, CustomItemKeys.POWER_ARMOR_BOOTS.getValue(), oldMods.toTypedArray())
					old.getData(DataComponentTypes.DYED_COLOR)?.let { color -> new.setData(DataComponentTypes.DYED_COLOR, color) }
					MigratorResult.Replacement(new)
				}
			))
			.addMigrator(LegacyCustomItemMigrator(
				predicate = {
					(it.type == Material.FLINT_AND_STEEL)
						&& (it.itemMeta.hasCustomModelData())
						&& (it.itemMeta.customModelData == 1)
						&& (it.customItem == null)
				},
				converter = { MigratorResult.Replacement(CustomItemKeys.ARMOR_MODIFICATION_SHOCK_ABSORBING.getValue().constructItemStack()) }
			))
			.addMigrator(LegacyCustomItemMigrator(
				predicate = {
					it.type == Material.FLINT_AND_STEEL
						&& it.itemMeta.hasCustomModelData()
						&& it.itemMeta.customModelData == 2
						&& it.customItem == null
				},
				converter = { MigratorResult.Replacement(CustomItemKeys.ARMOR_MODIFICATION_SPEED_BOOSTING.getValue().constructItemStack()) }
			))
			.addMigrator(LegacyCustomItemMigrator(
				predicate = {
					it.type == Material.FLINT_AND_STEEL
						&& it.itemMeta.hasCustomModelData()
						&& it.itemMeta.customModelData == 3
						&& it.customItem == null
				},
				converter = { MigratorResult.Replacement(CustomItemKeys.ARMOR_MODIFICATION_ROCKET_BOOSTING.getValue().constructItemStack()) }
			))
			.addMigrator(LegacyCustomItemMigrator(
				predicate = {
					it.type == Material.FLINT_AND_STEEL
						&& it.itemMeta.hasCustomModelData()
						&& it.itemMeta.customModelData == 4
						&& it.customItem == null
				},
				converter = { MigratorResult.Replacement(CustomItemKeys.ARMOR_MODIFICATION_NIGHT_VISION.getValue().constructItemStack()) }
			))
			.addMigrator(LegacyCustomItemMigrator(
				predicate = {
					it.type == Material.FLINT_AND_STEEL
						&& it.itemMeta.hasCustomModelData()
						&& it.itemMeta.customModelData == 5
						&& it.customItem == null
				},
				converter = { MigratorResult.Replacement(CustomItemKeys.ARMOR_MODIFICATION_ENVIRONMENT.getValue().constructItemStack()) }
			))
			.addMigrator(LegacyCustomItemMigrator(
				predicate = {
					it.type == Material.FLINT_AND_STEEL
						&& it.itemMeta.hasCustomModelData()
						&& it.itemMeta.customModelData == 6
						&& it.customItem == null
				},
				converter = { MigratorResult.Replacement(CustomItemKeys.ARMOR_MODIFICATION_PRESSURE_FIELD.getValue().constructItemStack()) }
			))
			.addMigrator(LegacyCustomItemMigrator(
				predicate = {
					it.type == Material.SHIELD
						&& it.itemMeta.hasCustomModelData()
						&& it.itemMeta.customModelData == 1
						&& it.customItem == null
				},
				converter = { MigratorResult.Replacement(CustomItemKeys.ENERGY_SWORD_BLUE.getValue().constructItemStack()) }
			))
			.addMigrator(LegacyCustomItemMigrator(
				predicate = {
					it.type == Material.SHIELD
						&& it.itemMeta.hasCustomModelData()
						&& it.itemMeta.customModelData == 2
						&& it.customItem == null
				},
				converter = { MigratorResult.Replacement(CustomItemKeys.ENERGY_SWORD_RED.getValue().constructItemStack()) }
			))
			.addMigrator(LegacyCustomItemMigrator(
				predicate = {
					it.type == Material.SHIELD
						&& it.itemMeta.hasCustomModelData()
						&& it.itemMeta.customModelData == 3
						&& it.customItem == null
				},
				converter = { MigratorResult.Replacement(CustomItemKeys.ENERGY_SWORD_YELLOW.getValue().constructItemStack()) }
			))
			.addMigrator(LegacyCustomItemMigrator(
				predicate = {
					it.type == Material.SHIELD
						&& it.itemMeta.hasCustomModelData()
						&& it.itemMeta.customModelData == 4
						&& it.customItem == null
				},
				converter = { MigratorResult.Replacement(CustomItemKeys.ENERGY_SWORD_GREEN.getValue().constructItemStack()) }
			))
			.addMigrator(LegacyCustomItemMigrator(
				predicate = {
					it.type == Material.SHIELD
						&& it.itemMeta.hasCustomModelData()
						&& it.itemMeta.customModelData == 5
						&& it.customItem == null
				},
				converter = { MigratorResult.Replacement(CustomItemKeys.ENERGY_SWORD_PURPLE.getValue().constructItemStack()) }
			))
			.addMigrator(LegacyCustomItemMigrator(
				predicate = {
					it.type == Material.SHIELD
						&& it.itemMeta.hasCustomModelData()
						&& it.itemMeta.customModelData == 6
						&& it.customItem == null
				},
				converter = { MigratorResult.Replacement(CustomItemKeys.ENERGY_SWORD_ORANGE.getValue().constructItemStack()) }
			))
			.addMigrator(LegacyCustomItemMigrator(
				predicate = {
					it.type == Material.SHIELD
						&& it.itemMeta.hasCustomModelData()
						&& it.itemMeta.customModelData == 7
						&& it.customItem == null
				},
				converter = { MigratorResult.Replacement(CustomItemKeys.ENERGY_SWORD_PINK.getValue().constructItemStack()) }
			))
			.addMigrator(LegacyCustomItemMigrator(
				predicate = {
					it.type == Material.SHIELD
						&& it.itemMeta.hasCustomModelData()
						&& it.itemMeta.customModelData == 8
						&& it.customItem == null
				},
				converter = { MigratorResult.Replacement(CustomItemKeys.ENERGY_SWORD_BLACK.getValue().constructItemStack()) }
			))
			.build()
		)

		registerDataVersion(DataVersion
			.builder(2)
			.addMigrator(LegacyNameFixer(
				"DETONATOR", "SMOKE_GRENADE", "PUMPKIN_GRENADE", "GUN_BARREL", "CIRCUITRY", "PISTOL_RECEIVER", "RIFLE_RECEIVER",
				"SMB_RECEIVER", "SNIPER_RECEIVER", "SHOTGUN_RECEIVER", "CANNON_RECEIVER", "ALUMINUM_INGOT", "ALUMINUM_BLOCK", "RAW_ALUMINUM_BLOCK",
				"CHETHERITE", "CHETHERITE_BLOCK", "TITANIUM_INGOT", "TITANIUM_BLOCK", "RAW_TITANIUM_BLOCK", "URANIUM", "URANIUM_BLOCK",
				"RAW_URANIUM_BLOCK", "NETHERITE_CASING", "ENRICHED_URANIUM", "ENRICHED_URANIUM_BLOCK", "URANIUM_CORE", "URANIUM_ROD", "FUEL_ROD_CORE",
				"FUEL_CELL", "FUEL_CONTROL", "REACTIVE_COMPONENT", "REACTIVE_HOUSING", "REACTIVE_PLATING", "REACTIVE_CHASSIS", "REACTIVE_MEMBRANE",
				"REACTIVE_ASSEMBLY", "FABRICATED_ASSEMBLY", "CIRCUIT_BOARD", "MOTHERBOARD", "REACTOR_CONTROL", "SUPERCONDUCTOR", "SUPERCONDUCTOR_BLOCK",
				"SUPERCONDUCTOR_CORE", "STEEL_INGOT", "STEEL_BLOCK", "STEEL_PLATE", "STEEL_CHASSIS", "STEEL_MODULE", "STEEL_ASSEMBLY",
				"REINFORCED_FRAME", "REACTOR_FRAME", "PROGRESS_HOLDER", "BATTLECRUISER_REACTOR_CORE", "BARGE_REACTOR_CORE", "CRUISER_REACTOR_CORE", "UNLOADED_SHELL",
				"LOADED_SHELL", "UNCHARGED_SHELL", "CHARGED_SHELL", "ARSENAL_MISSILE", "PUMPKIN_GRENADE", "UNLOADED_ARSENAL_MISSILE", "ACTIVATED_ARSENAL_MISSILE",
				"GAS_CANISTER_EMPTY",
			))
			.addMigrator(AspectMigrator
				.builder(CustomItemKeys.BLASTER_RIFLE)
				.addAdditionalIdentifier("RIFLE")
				.setModel("weapon/blaster/rifle")
				.pullLore(CustomItemKeys.BLASTER_RIFLE)
				.changeIdentifier("RIFLE", "BLASTER_RIFLE")
				.setDataComponent(DataComponentTypes.ATTRIBUTE_MODIFIERS, ItemAttributeModifiers.itemAttributes().build())
				.build()
			)
			.addMigrator(AspectMigrator
				.builder(CustomItemKeys.BLASTER_PISTOL)
				.addAdditionalIdentifier("PISTOL")
				.setModel("weapon/blaster/pistol")
				.pullLore(CustomItemKeys.BLASTER_PISTOL)
				.changeIdentifier("PISTOL", "BLASTER_PISTOL")
				.setDataComponent(DataComponentTypes.ATTRIBUTE_MODIFIERS, ItemAttributeModifiers.itemAttributes().build())
				.build()
			)
			.addMigrator(AspectMigrator
				.builder(CustomItemKeys.BLASTER_SHOTGUN)
				.addAdditionalIdentifier("SHOTGUN")
				.setModel("weapon/blaster/shotgun")
				.pullLore(CustomItemKeys.BLASTER_SHOTGUN)
				.changeIdentifier("SHOTGUN", "BLASTER_SHOTGUN")
				.setDataComponent(DataComponentTypes.ATTRIBUTE_MODIFIERS, ItemAttributeModifiers.itemAttributes().build())
				.build()
			)
			.addMigrator(AspectMigrator
				.builder(CustomItemKeys.BLASTER_SNIPER)
				.addAdditionalIdentifier("SNIPER")
				.setModel("weapon/blaster/sniper")
				.pullLore(CustomItemKeys.BLASTER_SNIPER)
				.changeIdentifier("SNIPER", "BLASTER_SNIPER")
				.setDataComponent(DataComponentTypes.ATTRIBUTE_MODIFIERS, ItemAttributeModifiers.itemAttributes().build())
				.build()
			)
			.addMigrator(AspectMigrator
				.builder(CustomItemKeys.BLASTER_CANNON)
				.addAdditionalIdentifier("CANNON")
				.setModel("weapon/blaster/cannon")
				.pullLore(CustomItemKeys.BLASTER_CANNON)
				.changeIdentifier("CANNON", "BLASTER_CANNON")
				.setDataComponent(DataComponentTypes.ATTRIBUTE_MODIFIERS, ItemAttributeModifiers.itemAttributes().build())
				.build()
			)
			.addMigrator(AspectMigrator.fixModel(CustomItemKeys.POWER_DRILL_BASIC))
			.addMigrator(AspectMigrator.fixModel(CustomItemKeys.POWER_DRILL_ENHANCED))
			.addMigrator(AspectMigrator.fixModel(CustomItemKeys.POWER_DRILL_ADVANCED))
			.addMigrator(AspectMigrator.fixModel(CustomItemKeys.POWER_CHAINSAW_BASIC))
			.addMigrator(AspectMigrator.fixModel(CustomItemKeys.POWER_CHAINSAW_ENHANCED))
			.addMigrator(AspectMigrator.fixModel(CustomItemKeys.POWER_CHAINSAW_ADVANCED))
			.addMigrator(AspectMigrator.fixModel(CustomItemKeys.POWER_HOE_BASIC))
			.addMigrator(AspectMigrator.fixModel(CustomItemKeys.POWER_HOE_ENHANCED))
			.addMigrator(AspectMigrator.fixModel(CustomItemKeys.POWER_HOE_ADVANCED))
			.addMigrator(AspectMigrator.fixModel(CustomItemKeys.TOOL_MODIFICATION_RANGE_1))
			.addMigrator(AspectMigrator.fixModel(CustomItemKeys.TOOL_MODIFICATION_RANGE_2))
			.addMigrator(AspectMigrator.fixModel(CustomItemKeys.TOOL_MODIFICATION_VEIN_MINER_25))
			.addMigrator(AspectMigrator.fixModel(CustomItemKeys.TOOL_MODIFICATION_SILK_TOUCH_MOD))
			.addMigrator(AspectMigrator.fixModel(CustomItemKeys.TOOL_MODIFICATION_AUTO_SMELT))
			.addMigrator(AspectMigrator.fixModel(CustomItemKeys.TOOL_MODIFICATION_FORTUNE_1))
			.addMigrator(AspectMigrator.fixModel(CustomItemKeys.TOOL_MODIFICATION_FORTUNE_2))
			.addMigrator(AspectMigrator.fixModel(CustomItemKeys.TOOL_MODIFICATION_FORTUNE_3))
			.addMigrator(AspectMigrator.fixModel(CustomItemKeys.TOOL_MODIFICATION_POWER_CAPACITY_25))
			.addMigrator(AspectMigrator.fixModel(CustomItemKeys.TOOL_MODIFICATION_POWER_CAPACITY_50))
			.addMigrator(AspectMigrator.fixModel(CustomItemKeys.TOOL_MODIFICATION_AUTO_REPLANT))
			.addMigrator(AspectMigrator.fixModel(CustomItemKeys.TOOL_MODIFICATION_AUTO_COMPOST))
			.addMigrator(AspectMigrator.fixModel(CustomItemKeys.TOOL_MODIFICATION_RANGE_3))
			.addMigrator(AspectMigrator.fixModel(CustomItemKeys.TOOL_MODIFICATION_EXTENDED_BAR))
			.addMigrator(AspectMigrator.fixModel(CustomItemKeys.TOOL_MODIFICATION_FERTILIZER_DISPENSER))
			.addMigrator(AspectMigrator.fixModel(CustomItemKeys.PERSONAL_TRANSPORTER))
			.addMigrator(AspectMigrator.fixModel(CustomItemKeys.AERACH))
			.addMigrator(AspectMigrator.fixModel(CustomItemKeys.ARET))
			.addMigrator(AspectMigrator.fixModel(CustomItemKeys.CHANDRA))
			.addMigrator(AspectMigrator.fixModel(CustomItemKeys.CHIMGARA))
			.addMigrator(AspectMigrator.fixModel(CustomItemKeys.DAMKOTH))
			.addMigrator(AspectMigrator.fixModel(CustomItemKeys.DISTERRA))
			.addMigrator(AspectMigrator.fixModel(CustomItemKeys.EDEN))
			.addMigrator(AspectMigrator.fixModel(CustomItemKeys.GAHARA))
			.addMigrator(AspectMigrator.fixModel(CustomItemKeys.HERDOLI))
			.addMigrator(AspectMigrator.fixModel(CustomItemKeys.ILIUS))
			.addMigrator(AspectMigrator.fixModel(CustomItemKeys.ISIK))
			.addMigrator(AspectMigrator.fixModel(CustomItemKeys.KOVFEFE))
			.addMigrator(AspectMigrator.fixModel(CustomItemKeys.KRIO))
			.addMigrator(AspectMigrator.fixModel(CustomItemKeys.LIODA))
			.addMigrator(AspectMigrator.fixModel(CustomItemKeys.LUXITERNA))
			.addMigrator(AspectMigrator.fixModel(CustomItemKeys.QATRA))
			.addMigrator(AspectMigrator.fixModel(CustomItemKeys.RUBACIEA))
			.addMigrator(AspectMigrator.fixModel(CustomItemKeys.TURMS))
			.addMigrator(AspectMigrator.fixModel(CustomItemKeys.VASK))
			.addMigrator(AspectMigrator.fixModel(CustomItemKeys.ASTERI))
			.addMigrator(AspectMigrator.fixModel(CustomItemKeys.HORIZON))
			.addMigrator(AspectMigrator.fixModel(CustomItemKeys.ILIOS))
			.addMigrator(AspectMigrator.fixModel(CustomItemKeys.REGULUS))
			.addMigrator(AspectMigrator.fixModel(CustomItemKeys.SIRIUS))
			.addMigrator(AspectMigrator.fixModel(CustomItemKeys.PLANET_SELECTOR))
			.addMigrator(AspectMigrator.fixModel(CustomItemKeys.GAS_CANISTER_EMPTY))
			.addMigrator(AspectMigrator.fixModel(CustomItemKeys.GAS_CANISTER_HYDROGEN))
			.addMigrator(AspectMigrator.fixModel(CustomItemKeys.GAS_CANISTER_NITROGEN))
			.addMigrator(AspectMigrator.fixModel(CustomItemKeys.GAS_CANISTER_METHANE))
			.addMigrator(AspectMigrator.fixModel(CustomItemKeys.GAS_CANISTER_OXYGEN))
			.addMigrator(AspectMigrator.fixModel(CustomItemKeys.GAS_CANISTER_CHLORINE))
			.addMigrator(AspectMigrator.fixModel(CustomItemKeys.GAS_CANISTER_FLUORINE))
			.addMigrator(AspectMigrator.fixModel(CustomItemKeys.GAS_CANISTER_HELIUM))
			.addMigrator(AspectMigrator.fixModel(CustomItemKeys.GAS_CANISTER_CARBON_DIOXIDE))
			.addMigrator(AspectMigrator
				.builder(CustomItemKeys.BATTLECRUISER_REACTOR_CORE)
				.pullModel(CustomItemKeys.BATTLECRUISER_REACTOR_CORE)
				.setItemMaterial(Material.WARPED_WART_BLOCK)
				.build()
			)
			.addMigrator(AspectMigrator
				.builder(CustomItemKeys.BARGE_REACTOR_CORE)
				.pullModel(CustomItemKeys.BARGE_REACTOR_CORE)
				.setItemMaterial(Material.WARPED_WART_BLOCK)
				.build()
			)
			.addMigrator(AspectMigrator
				.builder(CustomItemKeys.CRUISER_REACTOR_CORE)
				.pullModel(CustomItemKeys.CRUISER_REACTOR_CORE)
				.setItemMaterial(Material.WARPED_WART_BLOCK)
				.build()
			)
			.addMigrator(AspectMigrator
				.builder(CustomItemKeys.ARSENAL_MISSILE)
				.pullModel(CustomItemKeys.ARSENAL_MISSILE)
				.setItemMaterial(Material.WARPED_FUNGUS_ON_A_STICK)
				.build()
			)
			.addMigrator(AspectMigrator.fixModel(CustomItemKeys.UNLOADED_ARSENAL_MISSILE))
			.addMigrator(AspectMigrator.fixModel(CustomItemKeys.ACTIVATED_ARSENAL_MISSILE))
			.addMigrator(AspectMigrator.fixModel(CustomItemKeys.UNLOADED_SHELL))
			.addMigrator(AspectMigrator.fixModel(CustomItemKeys.LOADED_SHELL))
			.addMigrator(AspectMigrator.fixModel(CustomItemKeys.UNCHARGED_SHELL))
			.addMigrator(AspectMigrator
				.builder(CustomItemKeys.CHARGED_SHELL)
				.pullModel(CustomItemKeys.CHARGED_SHELL)
				.setItemMaterial(Material.WARPED_FUNGUS_ON_A_STICK)
				.build()
			)
			.addMigrator(AspectMigrator
				.builder(CustomItemKeys.STEEL_INGOT)
				.pullModel(CustomItemKeys.STEEL_INGOT)
				.setItemMaterial(Material.WARPED_FUNGUS_ON_A_STICK)
				.build()
			)
			.addMigrator(AspectMigrator
				.builder(CustomItemKeys.STEEL_BLOCK)
				.pullModel(CustomItemKeys.STEEL_BLOCK)
				.setItemMaterial(Material.WARPED_WART_BLOCK)
				.build()
			)
			.addMigrator(AspectMigrator.fixModel(CustomItemKeys.STEEL_PLATE))
			.addMigrator(AspectMigrator.fixModel(CustomItemKeys.STEEL_CHASSIS))
			.addMigrator(AspectMigrator.fixModel(CustomItemKeys.STEEL_MODULE))
			.addMigrator(AspectMigrator.fixModel(CustomItemKeys.STEEL_ASSEMBLY))
			.addMigrator(AspectMigrator.fixModel(CustomItemKeys.REINFORCED_FRAME))
			.addMigrator(AspectMigrator.fixModel(CustomItemKeys.REACTOR_FRAME))
			.addMigrator(AspectMigrator.fixModel(CustomItemKeys.SUPERCONDUCTOR))
			.addMigrator(AspectMigrator
				.builder(CustomItemKeys.SUPERCONDUCTOR_BLOCK)
				.pullModel(CustomItemKeys.SUPERCONDUCTOR_BLOCK)
				.setItemMaterial(Material.WARPED_WART_BLOCK)
				.build()
			)
			.addMigrator(AspectMigrator.fixModel(CustomItemKeys.SUPERCONDUCTOR_CORE))
			.addMigrator(AspectMigrator.fixModel(CustomItemKeys.CIRCUIT_BOARD))
			.addMigrator(AspectMigrator.fixModel(CustomItemKeys.MOTHERBOARD))
			.addMigrator(AspectMigrator.fixModel(CustomItemKeys.REACTOR_CONTROL))
			.addMigrator(AspectMigrator.fixModel(CustomItemKeys.REACTIVE_COMPONENT))
			.addMigrator(AspectMigrator.fixModel(CustomItemKeys.REACTIVE_HOUSING))
			.addMigrator(AspectMigrator.fixModel(CustomItemKeys.REACTIVE_PLATING))
			.addMigrator(AspectMigrator.fixModel(CustomItemKeys.REACTIVE_CHASSIS))
			.addMigrator(AspectMigrator.fixModel(CustomItemKeys.REACTIVE_MEMBRANE))
			.addMigrator(AspectMigrator.fixModel(CustomItemKeys.REACTIVE_ASSEMBLY))
			.addMigrator(AspectMigrator.fixModel(CustomItemKeys.FABRICATED_ASSEMBLY))
			.addMigrator(AspectMigrator
				.builder(CustomItemKeys.NETHERITE_CASING)
				.pullModel(CustomItemKeys.NETHERITE_CASING)
				.setItemMaterial(Material.WARPED_WART_BLOCK)
				.build()
			)
			.addMigrator(AspectMigrator
				.builder(CustomItemKeys.ENRICHED_URANIUM)
				.pullModel(CustomItemKeys.ENRICHED_URANIUM)
				.setItemMaterial(Material.WARPED_FUNGUS_ON_A_STICK)
				.build()
			)
			.addMigrator(AspectMigrator
				.builder(CustomItemKeys.ENRICHED_URANIUM_BLOCK)
				.pullModel(CustomItemKeys.ENRICHED_URANIUM_BLOCK)
				.setItemMaterial(Material.WARPED_WART_BLOCK)
				.build()
			)
			.addMigrator(AspectMigrator.fixModel(CustomItemKeys.URANIUM_CORE))
			.addMigrator(AspectMigrator.fixModel(CustomItemKeys.URANIUM_ROD))
			.addMigrator(AspectMigrator.fixModel(CustomItemKeys.FUEL_ROD_CORE))
			.addMigrator(AspectMigrator.fixModel(CustomItemKeys.FUEL_CELL))
			.addMigrator(AspectMigrator.fixModel(CustomItemKeys.FUEL_CONTROL))
			.addMigrator(AspectMigrator
				.builder(CustomItemKeys.URANIUM)
				.pullModel(CustomItemKeys.URANIUM)
				.setItemMaterial(Material.WARPED_FUNGUS_ON_A_STICK)
				.build()
			)
			.addMigrator(AspectMigrator
				.builder(CustomItemKeys.RAW_URANIUM)
				.pullModel(CustomItemKeys.RAW_URANIUM)
				.setItemMaterial(Material.WARPED_FUNGUS_ON_A_STICK)
				.build()
			)
			.addMigrator(AspectMigrator
				.builder(CustomItemKeys.URANIUM_ORE)
				.pullModel(CustomItemKeys.URANIUM_ORE)
				.setItemMaterial(Material.WARPED_WART_BLOCK)
				.build()
			)
			.addMigrator(AspectMigrator
				.builder(CustomItemKeys.URANIUM_BLOCK)
				.pullModel(CustomItemKeys.URANIUM_BLOCK)
				.setItemMaterial(Material.WARPED_WART_BLOCK)
				.build()
			)
			.addMigrator(AspectMigrator
				.builder(CustomItemKeys.RAW_URANIUM_BLOCK)
				.pullModel(CustomItemKeys.RAW_URANIUM_BLOCK)
				.setItemMaterial(Material.WARPED_WART_BLOCK)
				.build()
			)
			.addMigrator(AspectMigrator
				.builder(CustomItemKeys.TITANIUM_INGOT)
				.pullModel(CustomItemKeys.TITANIUM_INGOT)
				.setItemMaterial(Material.WARPED_FUNGUS_ON_A_STICK)
				.build()
			)
			.addMigrator(AspectMigrator
				.builder(CustomItemKeys.RAW_TITANIUM)
				.pullModel(CustomItemKeys.RAW_TITANIUM)
				.setItemMaterial(Material.WARPED_FUNGUS_ON_A_STICK)
				.build()
			)
			.addMigrator(AspectMigrator
				.builder(CustomItemKeys.TITANIUM_ORE)
				.pullModel(CustomItemKeys.TITANIUM_ORE)
				.setItemMaterial(Material.WARPED_WART_BLOCK)
				.build()
			)
			.addMigrator(AspectMigrator
				.builder(CustomItemKeys.TITANIUM_BLOCK)
				.pullModel(CustomItemKeys.TITANIUM_BLOCK)
				.setItemMaterial(Material.WARPED_WART_BLOCK)
				.build()
			)
			.addMigrator(AspectMigrator
				.builder(CustomItemKeys.RAW_TITANIUM_BLOCK)
				.pullModel(CustomItemKeys.RAW_TITANIUM_BLOCK)
				.setItemMaterial(Material.WARPED_WART_BLOCK)
				.build()
			)
			.addMigrator(AspectMigrator
				.builder(CustomItemKeys.CHETHERITE)
				.pullModel(CustomItemKeys.CHETHERITE)
				.setItemMaterial(Material.WARPED_FUNGUS_ON_A_STICK)
				.build()
			)
			.addMigrator(AspectMigrator
				.builder(CustomItemKeys.CHETHERITE_ORE)
				.pullModel(CustomItemKeys.CHETHERITE_ORE)
				.setItemMaterial(Material.WARPED_WART_BLOCK)
				.build()
			)
			.addMigrator(AspectMigrator
				.builder(CustomItemKeys.CHETHERITE_BLOCK)
				.pullModel(CustomItemKeys.CHETHERITE_BLOCK)
				.setItemMaterial(Material.WARPED_WART_BLOCK)
				.build()
			)
			.addMigrator(AspectMigrator
				.builder(CustomItemKeys.ALUMINUM_INGOT)
				.pullModel(CustomItemKeys.ALUMINUM_INGOT)
				.setItemMaterial(Material.WARPED_FUNGUS_ON_A_STICK)
				.build()
			)
			.addMigrator(AspectMigrator
				.builder(CustomItemKeys.RAW_ALUMINUM)
				.pullModel(CustomItemKeys.RAW_ALUMINUM)
				.setItemMaterial(Material.WARPED_FUNGUS_ON_A_STICK)
				.build()
			)
			.addMigrator(AspectMigrator
				.builder(CustomItemKeys.ALUMINUM_ORE)
				.pullModel(CustomItemKeys.ALUMINUM_ORE)
				.setItemMaterial(Material.WARPED_WART_BLOCK)
				.build()
			)
			.addMigrator(AspectMigrator
				.builder(CustomItemKeys.ALUMINUM_BLOCK)
				.pullModel(CustomItemKeys.ALUMINUM_BLOCK)
				.setItemMaterial(Material.WARPED_WART_BLOCK)
				.build()
			)
			.addMigrator(AspectMigrator
				.builder(CustomItemKeys.RAW_ALUMINUM_BLOCK)
				.pullModel(CustomItemKeys.RAW_ALUMINUM_BLOCK)
				.setItemMaterial(Material.WARPED_WART_BLOCK)
				.build()
			)
			.addMigrator(AspectMigrator.fixModel(CustomItemKeys.PISTOL_RECEIVER))
			.addMigrator(AspectMigrator.fixModel(CustomItemKeys.RIFLE_RECEIVER))
			.addMigrator(AspectMigrator.fixModel(CustomItemKeys.SMB_RECEIVER))
			.addMigrator(AspectMigrator.fixModel(CustomItemKeys.SNIPER_RECEIVER))
			.addMigrator(AspectMigrator.fixModel(CustomItemKeys.SHOTGUN_RECEIVER))
			.addMigrator(AspectMigrator.fixModel(CustomItemKeys.CANNON_RECEIVER))
			.addMigrator(AspectMigrator.fixModel(CustomItemKeys.GUN_BARREL))
			.addMigrator(AspectMigrator.fixModel(CustomItemKeys.CIRCUITRY))
			.build()
		)

		registerDataVersion(DataVersion
			.builder(3)
			.addMigrator(AspectMigrator
				.builder(CustomItemKeys.STANDARD_MAGAZINE)
				.pullModel(CustomItemKeys.STANDARD_MAGAZINE)
				.pullLore(CustomItemKeys.STANDARD_MAGAZINE)
				.pullName(CustomItemKeys.STANDARD_MAGAZINE)
				.build()
			)
			.addMigrator(AspectMigrator
				.builder(CustomItemKeys.SPECIAL_MAGAZINE)
				.pullModel(CustomItemKeys.SPECIAL_MAGAZINE)
				.pullLore(CustomItemKeys.SPECIAL_MAGAZINE)
				.pullName(CustomItemKeys.SPECIAL_MAGAZINE)
				.build()
			)
			.addMigrator(AspectMigrator.fixModel(CustomItemKeys.SMOKE_GRENADE))
			.addMigrator(AspectMigrator.fixModel(CustomItemKeys.DETONATOR))
			.build()
		)

		registerDataVersion(DataVersion.builder(4).build())

		registerDataVersion(DataVersion
			.builder(5)
			.addMigrator(ReplacementMigrator(
				CustomItemKeys.GUN_BARREL, CustomItemKeys.CIRCUITRY,
				CustomItemKeys.PISTOL_RECEIVER, CustomItemKeys.RIFLE_RECEIVER,
				CustomItemKeys.SMB_RECEIVER, CustomItemKeys.SNIPER_RECEIVER,
				CustomItemKeys.SHOTGUN_RECEIVER, CustomItemKeys.CANNON_RECEIVER
			))
			.addMigrator(ReplacementMigrator(
				CustomItemKeys.ALUMINUM_INGOT, CustomItemKeys.RAW_ALUMINUM, CustomItemKeys.ALUMINUM_ORE,
				CustomItemKeys.ALUMINUM_BLOCK, CustomItemKeys.RAW_ALUMINUM_BLOCK,
				CustomItemKeys.CHETHERITE, CustomItemKeys.CHETHERITE_ORE, CustomItemKeys.CHETHERITE_BLOCK,
				CustomItemKeys.TITANIUM_INGOT, CustomItemKeys.RAW_TITANIUM, CustomItemKeys.TITANIUM_ORE,
				CustomItemKeys.TITANIUM_BLOCK, CustomItemKeys.RAW_TITANIUM_BLOCK,
				CustomItemKeys.URANIUM, CustomItemKeys.RAW_URANIUM, CustomItemKeys.URANIUM_ORE,
				CustomItemKeys.URANIUM_BLOCK, CustomItemKeys.RAW_URANIUM_BLOCK
			))
			.addMigrator(ReplacementMigrator(
				CustomItemKeys.REACTIVE_COMPONENT, CustomItemKeys.REACTIVE_HOUSING, CustomItemKeys.REACTIVE_PLATING,
				CustomItemKeys.REACTIVE_CHASSIS, CustomItemKeys.REACTIVE_MEMBRANE, CustomItemKeys.REACTIVE_ASSEMBLY,
				CustomItemKeys.FABRICATED_ASSEMBLY, CustomItemKeys.CIRCUIT_BOARD, CustomItemKeys.MOTHERBOARD,
				CustomItemKeys.REACTOR_CONTROL
			))
			.addMigrator(ReplacementMigrator(
				CustomItemKeys.SUPERCONDUCTOR, CustomItemKeys.SUPERCONDUCTOR_BLOCK, CustomItemKeys.SUPERCONDUCTOR_CORE
			))
			.addMigrator(ReplacementMigrator(
				CustomItemKeys.STEEL_INGOT, CustomItemKeys.STEEL_BLOCK, CustomItemKeys.STEEL_PLATE,
				CustomItemKeys.STEEL_CHASSIS, CustomItemKeys.STEEL_MODULE, CustomItemKeys.STEEL_ASSEMBLY,
				CustomItemKeys.REINFORCED_FRAME, CustomItemKeys.REACTOR_FRAME
			))
			.addMigrator(ReplacementMigrator(
				CustomItemKeys.UNLOADED_SHELL, CustomItemKeys.LOADED_SHELL, CustomItemKeys.UNCHARGED_SHELL,
				CustomItemKeys.CHARGED_SHELL, CustomItemKeys.ARSENAL_MISSILE, CustomItemKeys.UNLOADED_ARSENAL_MISSILE,
				CustomItemKeys.ACTIVATED_ARSENAL_MISSILE
			))
			.build()
		)

		registerDataVersion(DataVersion.builder(6)
			.addMigrator(AspectMigrator
				.builder(CustomItemKeys.POWER_ARMOR_BOOTS)
				.addConsumer {
					it.setData(DataComponentTypes.ATTRIBUTE_MODIFIERS, ItemAttributeModifiers
						.itemAttributes()
						.addModifier(
							Attribute.ARMOR,
							AttributeModifier(
								NamespacedKeys.key(CustomItemKeys.POWER_ARMOR_BOOTS.key),
								2.0,
								AttributeModifier.Operation.ADD_NUMBER,
								CustomItemKeys.POWER_ARMOR_BOOTS.getValue().slot.group
							)
						)
						.build()
					)
				}
				.build()
			)
			.addMigrator(AspectMigrator
				.builder(CustomItemKeys.POWER_ARMOR_LEGGINGS)
				.addConsumer {
					it.setData(DataComponentTypes.ATTRIBUTE_MODIFIERS, ItemAttributeModifiers
						.itemAttributes()
						.addModifier(
							Attribute.ARMOR,
							AttributeModifier(
								NamespacedKeys.key(CustomItemKeys.POWER_ARMOR_LEGGINGS.key),
								2.0,
								AttributeModifier.Operation.ADD_NUMBER,
								CustomItemKeys.POWER_ARMOR_LEGGINGS.getValue().slot.group
							)
						)
						.build()
					)
				}
				.build()
			)
			.addMigrator(AspectMigrator
				.builder(CustomItemKeys.POWER_ARMOR_CHESTPLATE)
				.addConsumer {
					it.setData(DataComponentTypes.ATTRIBUTE_MODIFIERS, ItemAttributeModifiers
						.itemAttributes()
						.addModifier(
							Attribute.ARMOR,
							AttributeModifier(
								NamespacedKeys.key(CustomItemKeys.POWER_ARMOR_CHESTPLATE.key),
								2.0,
								AttributeModifier.Operation.ADD_NUMBER,
								CustomItemKeys.POWER_ARMOR_CHESTPLATE.getValue().slot.group
							)
						)
						.build()
					)
				}
				.build()
			)
			.addMigrator(AspectMigrator
				.builder(CustomItemKeys.POWER_ARMOR_HELMET)
				.addConsumer {
					it.setData(DataComponentTypes.ATTRIBUTE_MODIFIERS, ItemAttributeModifiers
						.itemAttributes()
						.addModifier(
							Attribute.ARMOR,
							AttributeModifier(
								NamespacedKeys.key(CustomItemKeys.POWER_ARMOR_HELMET.key),
								2.0,
								AttributeModifier.Operation.ADD_NUMBER,
								CustomItemKeys.POWER_ARMOR_HELMET.getValue().slot.group
							)
						)
						.build()
					)
				}
				.build()
			)
			.build()
		)
	}

	private fun registerDataVersion(dataVersion: DataVersion) {
		dataVersions.add(dataVersion)
	}

	fun migrate(chunk: Chunk) {
		val chunkVersion = chunk.persistentDataContainer.getOrDefault(NamespacedKeys.DATA_VERSION, PersistentDataType.INTEGER, 0)
		if (chunkVersion == lastDataVersion) return

		val toApply = getVersions(chunkVersion)

		val snapshot = chunk.chunkSnapshot

		for (x in 0..15) for (y in chunk.world.minHeight until chunk.world.maxHeight) for (z in 0..15) {
			val type = snapshot.getBlockType(x, y, z)
			if (type.isPipedInventory) {
				val state = chunk.getBlock(x, y, z).state as InventoryHolder
				migrateInventory(state.inventory, toApply)
			}
		}

		chunk.persistentDataContainer.set(NamespacedKeys.DATA_VERSION, PersistentDataType.INTEGER, lastDataVersion)
	}

	fun migrate(player: Player) {
		val playerVersion = player.persistentDataContainer.getOrDefault(NamespacedKeys.PLAYER_DATA_VERSION, PersistentDataType.INTEGER, 0)
		if (playerVersion == lastDataVersion) return

		log.info("Migrating ${player.name}'s inventory from $playerVersion to $lastDataVersion")
		migrateInventory(player.inventory, getVersions(playerVersion).apply { log.info("Applying $size versions") })

		player.persistentDataContainer.set(NamespacedKeys.PLAYER_DATA_VERSION, PersistentDataType.INTEGER, lastDataVersion)
	}

	fun getVersions(dataVersion: Int): List<DataVersion> {
		return dataVersions.subList(dataVersion + 1 /* Inclusive */, lastDataVersion + 1 /* Exclusive */)
	}

	fun migrateInventory(inventory: Inventory, versions: List<DataVersion>) {
		if (inventory.holder is CraftBlockInventoryHolder || inventory.holder is ChestGui || inventory.holder == null) return

		for (dataVersion in versions) {
			dataVersion.migrateInventory(inventory)
		}
	}

	@EventHandler(priority = EventPriority.MONITOR)
	fun onPlayerLogin(event: PlayerJoinEvent) {
		migrate(event.player)
	}

	@EventHandler(priority = EventPriority.MONITOR)
	fun onChunkLoad(event: ChunkLoadEvent) {
		migrate(event.chunk)
	}

	@EventHandler(priority = EventPriority.MONITOR)
	fun onOpenInventory(event: InventoryOpenEvent) {
		migrateInventory(event.inventory, getVersions(0))
	}
}
