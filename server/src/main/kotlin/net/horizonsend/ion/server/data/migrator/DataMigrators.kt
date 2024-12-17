package net.horizonsend.ion.server.data.migrator

import io.papermc.paper.datacomponent.DataComponentTypes
import io.papermc.paper.datacomponent.item.ItemAttributeModifiers
import net.horizonsend.ion.server.IonServerComponent
import net.horizonsend.ion.server.data.migrator.types.item.MigratorResult
import net.horizonsend.ion.server.data.migrator.types.item.legacy.LegacyCustomItemMigrator
import net.horizonsend.ion.server.data.migrator.types.item.modern.migrator.AspectMigrator
import net.horizonsend.ion.server.data.migrator.types.item.modern.migrator.LegacyNameFixer
import net.horizonsend.ion.server.features.custom.items.CustomItemRegistry
import net.horizonsend.ion.server.features.custom.items.CustomItemRegistry.customItem
import net.horizonsend.ion.server.features.custom.items.component.CustomComponentTypes.Companion.MOD_MANAGER
import net.horizonsend.ion.server.features.custom.items.component.CustomComponentTypes.Companion.POWER_STORAGE
import net.horizonsend.ion.server.features.gear.getPower
import net.horizonsend.ion.server.features.gear.powerarmor.LegacyPowerArmorModule
import net.horizonsend.ion.server.features.transport.pipe.Pipes
import net.horizonsend.ion.server.miscellaneous.registrations.NamespacedKeys
import org.bukkit.Chunk
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.world.ChunkLoadEvent
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.InventoryHolder
import org.bukkit.persistence.PersistentDataType

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
				converter = { MigratorResult.Replacement(CustomItemRegistry.BLASTER_PISTOL.constructItemStack()) }
			))
			.addMigrator(LegacyCustomItemMigrator(
				predicate = { it.type == Material.BOW && it.itemMeta.hasCustomModelData() && it.itemMeta.customModelData == 2 && it.customItem == null },
				converter = { MigratorResult.Replacement(CustomItemRegistry.BLASTER_RIFLE.constructItemStack()) }
			))
			.addMigrator(LegacyCustomItemMigrator(
				predicate = { it.type == Material.BOW && it.itemMeta.hasCustomModelData() && it.itemMeta.customModelData == 3 && it.customItem == null },
				converter = { MigratorResult.Replacement(CustomItemRegistry.BLASTER_SNIPER.constructItemStack()) }
			))
			.addMigrator(LegacyCustomItemMigrator(
				predicate = { it.type == Material.BOW && it.itemMeta.hasCustomModelData() && it.itemMeta.customModelData == 4 && it.customItem == null },
				converter = { MigratorResult.Replacement(CustomItemRegistry.BLASTER_CANNON.constructItemStack()) }
			))
			.addMigrator(LegacyCustomItemMigrator(
				predicate = {
					it.type == Material.DIAMOND_PICKAXE
						&& it.itemMeta.hasCustomModelData()
						&& it.itemMeta.customModelData == 1
						&& it.customItem == null
				},
				converter = { MigratorResult.Replacement(CustomItemRegistry.POWER_DRILL_BASIC.constructItemStack()) }
			))
			// Start minerals
			.addMigrator(LegacyCustomItemMigrator(
				predicate = {
					it.type == Material.IRON_INGOT
						&& it.itemMeta.hasCustomModelData()
						&& it.itemMeta.customModelData == 1
						&& it.customItem == null
				},
				converter = { MigratorResult.Replacement(CustomItemRegistry.ALUMINUM_INGOT.constructItemStack()) }
			))
			.addMigrator(LegacyCustomItemMigrator(
				predicate = {
					it.type == Material.IRON_ORE
						&& it.itemMeta.hasCustomModelData()
						&& it.itemMeta.customModelData == 1
						&& it.customItem == null
				},
				converter = { MigratorResult.Replacement(CustomItemRegistry.ALUMINUM_ORE.constructItemStack()) }
			))
			.addMigrator(LegacyCustomItemMigrator(
				predicate = {
					it.type == Material.IRON_BLOCK
						&& it.itemMeta.hasCustomModelData()
						&& it.itemMeta.customModelData == 1
						&& it.customItem == null
				},
				converter = { MigratorResult.Replacement(CustomItemRegistry.ALUMINUM_BLOCK.constructItemStack()) }
			))
			.addMigrator(LegacyCustomItemMigrator(
				predicate = {
					it.type == Material.IRON_INGOT
						&& it.itemMeta.hasCustomModelData()
						&& it.itemMeta.customModelData == 2
						&& it.customItem == null
				},
				converter = { MigratorResult.Replacement(CustomItemRegistry.CHETHERITE.constructItemStack()) }
			))
			.addMigrator(LegacyCustomItemMigrator(
				predicate = {
					it.type == Material.IRON_ORE
						&& it.itemMeta.hasCustomModelData()
						&& it.itemMeta.customModelData == 2
						&& it.customItem == null
				},
				converter = { MigratorResult.Replacement(CustomItemRegistry.CHETHERITE_ORE.constructItemStack()) }
			))
			.addMigrator(LegacyCustomItemMigrator(
				predicate = {
					it.type == Material.IRON_BLOCK
						&& it.itemMeta.hasCustomModelData()
						&& it.itemMeta.customModelData == 2
						&& it.customItem == null
				},
				converter = { MigratorResult.Replacement(CustomItemRegistry.CHETHERITE_BLOCK.constructItemStack()) }
			))
			.addMigrator(LegacyCustomItemMigrator(
				predicate = {
					it.type == Material.IRON_INGOT
						&& it.itemMeta.hasCustomModelData()
						&& it.itemMeta.customModelData == 3
						&& it.customItem == null
				},
				converter = { MigratorResult.Replacement(CustomItemRegistry.TITANIUM_INGOT.constructItemStack()) }
			))
			.addMigrator(LegacyCustomItemMigrator(
				predicate = {
					it.type == Material.IRON_ORE
						&& it.itemMeta.hasCustomModelData()
						&& it.itemMeta.customModelData == 3
						&& it.customItem == null
				},
				converter = { MigratorResult.Replacement(CustomItemRegistry.TITANIUM_ORE.constructItemStack()) }
			))
			.addMigrator(LegacyCustomItemMigrator(
				predicate = {
					it.type == Material.IRON_BLOCK
						&& it.itemMeta.hasCustomModelData()
						&& it.itemMeta.customModelData == 3
						&& it.customItem == null
				},
				converter = { MigratorResult.Replacement(CustomItemRegistry.TITANIUM_BLOCK.constructItemStack()) }
			))
			.addMigrator(LegacyCustomItemMigrator(
				predicate = {
					it.type == Material.IRON_INGOT
						&& it.itemMeta.hasCustomModelData()
						&& it.itemMeta.customModelData == 4
						&& it.customItem == null
				},
				converter = { MigratorResult.Replacement(CustomItemRegistry.URANIUM.constructItemStack()) }
			))
			.addMigrator(LegacyCustomItemMigrator(
				predicate = {
					it.type == Material.IRON_ORE
						&& it.itemMeta.hasCustomModelData()
						&& it.itemMeta.customModelData == 4
						&& it.customItem == null
				},
				converter = { MigratorResult.Replacement(CustomItemRegistry.URANIUM_ORE.constructItemStack()) }
			))
			.addMigrator(LegacyCustomItemMigrator(
				predicate = {
					it.type == Material.IRON_BLOCK
						&& it.itemMeta.hasCustomModelData()
						&& it.itemMeta.customModelData == 4
						&& it.customItem == null
				},
				converter = { MigratorResult.Replacement(CustomItemRegistry.URANIUM_BLOCK.constructItemStack()) }
			))
			.addMigrator(LegacyCustomItemMigrator(
				predicate = {
					it.type == Material.SHEARS
						&& it.itemMeta.hasCustomModelData()
						&& it.itemMeta.customModelData == 1
						&& it.customItem == null
				},
				converter = { MigratorResult.Replacement(CustomItemRegistry.DETONATOR.constructItemStack()) }
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
				converter = { MigratorResult.Replacement(CustomItemRegistry.BATTERY_A.constructItemStack()) }
			))
			.addMigrator(LegacyCustomItemMigrator(
				predicate = {
					it.type == Material.SNOWBALL
						&& it.itemMeta.hasCustomModelData()
						&& it.itemMeta.customModelData == 8
						&& it.customItem == null
				},
				converter = { MigratorResult.Replacement(CustomItemRegistry.BATTERY_M.constructItemStack()) }
			))
			.addMigrator(LegacyCustomItemMigrator(
				predicate = {
					it.type == Material.SNOWBALL
						&& it.itemMeta.hasCustomModelData()
						&& it.itemMeta.customModelData == 9
						&& it.customItem == null
				},
				converter = { MigratorResult.Replacement(CustomItemRegistry.BATTERY_G.constructItemStack()) }
			))
			.addMigrator(LegacyCustomItemMigrator(
				predicate = {
					it.type == Material.LEATHER_HELMET
						&& it.itemMeta.hasCustomModelData()
						&& it.itemMeta.customModelData == 1
						&& it.customItem == null
				},
				converter = { old ->
					val oldMods = old.lore
						?.filter { it.startsWith("Module: ") }
						?.mapNotNull { LegacyPowerArmorModule[it.split(" ")[1]]?.modern?.get() }
						?.toSet()
						?: setOf()

					val oldPower = getPower(old)

					val new = CustomItemRegistry.POWER_ARMOR_HELMET.constructItemStack()
					CustomItemRegistry.POWER_ARMOR_HELMET.getComponent(MOD_MANAGER).setMods(new, CustomItemRegistry.POWER_ARMOR_HELMET, oldMods.toTypedArray())
					CustomItemRegistry.POWER_ARMOR_HELMET.getComponent(POWER_STORAGE).setPower(CustomItemRegistry.POWER_ARMOR_HELMET, new, oldPower)
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
					val oldMods = old.lore
						?.filter { it.startsWith("Module: ") }
						?.mapNotNull { LegacyPowerArmorModule[it.split(" ")[1]]?.modern?.get() }
						?.toSet()
						?: setOf()

					val oldPower = getPower(old)

					val new = CustomItemRegistry.POWER_ARMOR_CHESTPLATE.constructItemStack()
					CustomItemRegistry.POWER_ARMOR_CHESTPLATE.getComponent(MOD_MANAGER).setMods(new, CustomItemRegistry.POWER_ARMOR_CHESTPLATE, oldMods.toTypedArray())
					CustomItemRegistry.POWER_ARMOR_CHESTPLATE.getComponent(POWER_STORAGE).setPower(CustomItemRegistry.POWER_ARMOR_CHESTPLATE, new, oldPower)
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
					val oldMods = old.lore
						?.filter { it.startsWith("Module: ") }
						?.mapNotNull { LegacyPowerArmorModule[it.split(" ")[1]]?.modern?.get() }
						?.toSet()
						?: setOf()

					val oldPower = getPower(old)

					val new = CustomItemRegistry.POWER_ARMOR_LEGGINGS.constructItemStack()
					CustomItemRegistry.POWER_ARMOR_LEGGINGS.getComponent(MOD_MANAGER).setMods(new, CustomItemRegistry.POWER_ARMOR_LEGGINGS, oldMods.toTypedArray())
					CustomItemRegistry.POWER_ARMOR_LEGGINGS.getComponent(POWER_STORAGE).setPower(CustomItemRegistry.POWER_ARMOR_LEGGINGS, new, oldPower)
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
					val oldMods = old.lore
						?.filter { it.startsWith("Module: ") }
						?.mapNotNull { LegacyPowerArmorModule[it.split(" ")[1]]?.modern?.get() }
						?.toSet()
						?: setOf()

					val oldPower = getPower(old)

					val new = CustomItemRegistry.POWER_ARMOR_BOOTS.constructItemStack()
					CustomItemRegistry.POWER_ARMOR_BOOTS.getComponent(MOD_MANAGER).setMods(new, CustomItemRegistry.POWER_ARMOR_BOOTS, oldMods.toTypedArray())
					CustomItemRegistry.POWER_ARMOR_BOOTS.getComponent(POWER_STORAGE).setPower(CustomItemRegistry.POWER_ARMOR_BOOTS, new, oldPower)
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
				converter = { MigratorResult.Replacement(CustomItemRegistry.ARMOR_MODIFICATION_SHOCK_ABSORBING.constructItemStack()) }
			))
			.addMigrator(LegacyCustomItemMigrator(
				predicate = {
					it.type == Material.FLINT_AND_STEEL
						&& it.itemMeta.hasCustomModelData()
						&& it.itemMeta.customModelData == 2
						&& it.customItem == null
				},
				converter = { MigratorResult.Replacement(CustomItemRegistry.ARMOR_MODIFICATION_SPEED_BOOSTING.constructItemStack()) }
			))
			.addMigrator(LegacyCustomItemMigrator(
				predicate = {
					it.type == Material.FLINT_AND_STEEL
						&& it.itemMeta.hasCustomModelData()
						&& it.itemMeta.customModelData == 3
						&& it.customItem == null
				},
				converter = { MigratorResult.Replacement(CustomItemRegistry.ARMOR_MODIFICATION_ROCKET_BOOSTING.constructItemStack()) }
			))
			.addMigrator(LegacyCustomItemMigrator(
				predicate = {
					it.type == Material.FLINT_AND_STEEL
						&& it.itemMeta.hasCustomModelData()
						&& it.itemMeta.customModelData == 4
						&& it.customItem == null
				},
				converter = { MigratorResult.Replacement(CustomItemRegistry.ARMOR_MODIFICATION_NIGHT_VISION.constructItemStack()) }
			))
			.addMigrator(LegacyCustomItemMigrator(
				predicate = {
					(it.type == Material.FLINT_AND_STEEL).apply { println("Condition 1 $this") }
						&& (it.itemMeta.hasCustomModelData()).apply { println("Condition 2 $this") }
						&& (it.itemMeta.customModelData == 5).apply { println("Condition 3 $this") }
						&& (it.customItem == null).apply { println("Condition 4 $this") }
				},
				converter = { MigratorResult.Replacement(CustomItemRegistry.ARMOR_MODIFICATION_ENVIRONMENT.constructItemStack()) }
			))
			.addMigrator(LegacyCustomItemMigrator(
				predicate = {
					it.type == Material.FLINT_AND_STEEL
						&& it.itemMeta.hasCustomModelData()
						&& it.itemMeta.customModelData == 6
						&& it.customItem == null
				},
				converter = { MigratorResult.Replacement(CustomItemRegistry.ARMOR_MODIFICATION_PRESSURE_FIELD.constructItemStack()) }
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
				.builder(CustomItemRegistry.BLASTER_RIFLE)
				.addAdditionalIdentifier("RIFLE")
				.setModel("weapon/blaster/rifle")
				.pullLore(CustomItemRegistry.BLASTER_RIFLE)
				.changeIdentifier("RIFLE", "BLASTER_RIFLE")
				.setDataComponent(DataComponentTypes.ATTRIBUTE_MODIFIERS, ItemAttributeModifiers.itemAttributes().build())
				.build()
			)
			.addMigrator(AspectMigrator
				.builder(CustomItemRegistry.BLASTER_PISTOL)
				.addAdditionalIdentifier("PISTOL")
				.setModel("weapon/blaster/pistol")
				.pullLore(CustomItemRegistry.BLASTER_PISTOL)
				.changeIdentifier("PISTOL", "BLASTER_PISTOL")
				.setDataComponent(DataComponentTypes.ATTRIBUTE_MODIFIERS, ItemAttributeModifiers.itemAttributes().build())
				.build()
			)
			.addMigrator(AspectMigrator
				.builder(CustomItemRegistry.BLASTER_SHOTGUN)
				.addAdditionalIdentifier("SHOTGUN")
				.setModel("weapon/blaster/shotgun")
				.pullLore(CustomItemRegistry.BLASTER_SHOTGUN)
				.changeIdentifier("SHOTGUN", "BLASTER_SHOTGUN")
				.setDataComponent(DataComponentTypes.ATTRIBUTE_MODIFIERS, ItemAttributeModifiers.itemAttributes().build())
				.build()
			)
			.addMigrator(AspectMigrator
				.builder(CustomItemRegistry.BLASTER_SNIPER)
				.addAdditionalIdentifier("SNIPER")
				.setModel("weapon/blaster/sniper")
				.pullLore(CustomItemRegistry.BLASTER_SNIPER)
				.changeIdentifier("SNIPER", "BLASTER_SNIPER")
				.setDataComponent(DataComponentTypes.ATTRIBUTE_MODIFIERS, ItemAttributeModifiers.itemAttributes().build())
				.build()
			)
			.addMigrator(AspectMigrator
				.builder(CustomItemRegistry.BLASTER_CANNON)
				.addAdditionalIdentifier("CANNON")
				.setModel("weapon/blaster/cannon")
				.pullLore(CustomItemRegistry.BLASTER_CANNON)
				.changeIdentifier("CANNON", "BLASTER_CANNON")
				.setDataComponent(DataComponentTypes.ATTRIBUTE_MODIFIERS, ItemAttributeModifiers.itemAttributes().build())
				.build()
			)
			.addMigrator(AspectMigrator.fixModel(CustomItemRegistry.POWER_DRILL_BASIC))
			.addMigrator(AspectMigrator.fixModel(CustomItemRegistry.POWER_DRILL_ENHANCED))
			.addMigrator(AspectMigrator.fixModel(CustomItemRegistry.POWER_DRILL_ADVANCED))
			.addMigrator(AspectMigrator.fixModel(CustomItemRegistry.POWER_CHAINSAW_BASIC))
			.addMigrator(AspectMigrator.fixModel(CustomItemRegistry.POWER_CHAINSAW_ENHANCED))
			.addMigrator(AspectMigrator.fixModel(CustomItemRegistry.POWER_CHAINSAW_ADVANCED))
			.addMigrator(AspectMigrator.fixModel(CustomItemRegistry.POWER_HOE_BASIC))
			.addMigrator(AspectMigrator.fixModel(CustomItemRegistry.POWER_HOE_ENHANCED))
			.addMigrator(AspectMigrator.fixModel(CustomItemRegistry.POWER_HOE_ADVANCED))
			.addMigrator(AspectMigrator.fixModel(CustomItemRegistry.RANGE_1))
			.addMigrator(AspectMigrator.fixModel(CustomItemRegistry.RANGE_2))
			.addMigrator(AspectMigrator.fixModel(CustomItemRegistry.VEIN_MINER_25))
			.addMigrator(AspectMigrator.fixModel(CustomItemRegistry.SILK_TOUCH_MOD))
			.addMigrator(AspectMigrator.fixModel(CustomItemRegistry.AUTO_SMELT))
			.addMigrator(AspectMigrator.fixModel(CustomItemRegistry.FORTUNE_1))
			.addMigrator(AspectMigrator.fixModel(CustomItemRegistry.FORTUNE_2))
			.addMigrator(AspectMigrator.fixModel(CustomItemRegistry.FORTUNE_3))
			.addMigrator(AspectMigrator.fixModel(CustomItemRegistry.POWER_CAPACITY_25))
			.addMigrator(AspectMigrator.fixModel(CustomItemRegistry.POWER_CAPACITY_50))
			.addMigrator(AspectMigrator.fixModel(CustomItemRegistry.AUTO_REPLANT))
			.addMigrator(AspectMigrator.fixModel(CustomItemRegistry.AUTO_COMPOST))
			.addMigrator(AspectMigrator.fixModel(CustomItemRegistry.RANGE_3))
			.addMigrator(AspectMigrator.fixModel(CustomItemRegistry.EXTENDED_BAR))
			.addMigrator(AspectMigrator.fixModel(CustomItemRegistry.FERTILIZER_DISPENSER))
			.addMigrator(AspectMigrator.fixModel(CustomItemRegistry.PERSONAL_TRANSPORTER))
			.addMigrator(AspectMigrator.fixModel(CustomItemRegistry.AERACH))
			.addMigrator(AspectMigrator.fixModel(CustomItemRegistry.ARET))
			.addMigrator(AspectMigrator.fixModel(CustomItemRegistry.CHANDRA))
			.addMigrator(AspectMigrator.fixModel(CustomItemRegistry.CHIMGARA))
			.addMigrator(AspectMigrator.fixModel(CustomItemRegistry.DAMKOTH))
			.addMigrator(AspectMigrator.fixModel(CustomItemRegistry.DISTERRA))
			.addMigrator(AspectMigrator.fixModel(CustomItemRegistry.EDEN))
			.addMigrator(AspectMigrator.fixModel(CustomItemRegistry.GAHARA))
			.addMigrator(AspectMigrator.fixModel(CustomItemRegistry.HERDOLI))
			.addMigrator(AspectMigrator.fixModel(CustomItemRegistry.ILIUS))
			.addMigrator(AspectMigrator.fixModel(CustomItemRegistry.ISIK))
			.addMigrator(AspectMigrator.fixModel(CustomItemRegistry.KOVFEFE))
			.addMigrator(AspectMigrator.fixModel(CustomItemRegistry.KRIO))
			.addMigrator(AspectMigrator.fixModel(CustomItemRegistry.LIODA))
			.addMigrator(AspectMigrator.fixModel(CustomItemRegistry.LUXITERNA))
			.addMigrator(AspectMigrator.fixModel(CustomItemRegistry.QATRA))
			.addMigrator(AspectMigrator.fixModel(CustomItemRegistry.RUBACIEA))
			.addMigrator(AspectMigrator.fixModel(CustomItemRegistry.TURMS))
			.addMigrator(AspectMigrator.fixModel(CustomItemRegistry.VASK))
			.addMigrator(AspectMigrator.fixModel(CustomItemRegistry.ASTERI))
			.addMigrator(AspectMigrator.fixModel(CustomItemRegistry.HORIZON))
			.addMigrator(AspectMigrator.fixModel(CustomItemRegistry.ILIOS))
			.addMigrator(AspectMigrator.fixModel(CustomItemRegistry.REGULUS))
			.addMigrator(AspectMigrator.fixModel(CustomItemRegistry.SIRIUS))
			.addMigrator(AspectMigrator.fixModel(CustomItemRegistry.PLANET_SELECTOR))
			.addMigrator(AspectMigrator.fixModel(CustomItemRegistry.GAS_CANISTER_EMPTY))
			.addMigrator(AspectMigrator.fixModel(CustomItemRegistry.GAS_CANISTER_HYDROGEN))
			.addMigrator(AspectMigrator.fixModel(CustomItemRegistry.GAS_CANISTER_NITROGEN))
			.addMigrator(AspectMigrator.fixModel(CustomItemRegistry.GAS_CANISTER_METHANE))
			.addMigrator(AspectMigrator.fixModel(CustomItemRegistry.GAS_CANISTER_OXYGEN))
			.addMigrator(AspectMigrator.fixModel(CustomItemRegistry.GAS_CANISTER_CHLORINE))
			.addMigrator(AspectMigrator.fixModel(CustomItemRegistry.GAS_CANISTER_FLUORINE))
			.addMigrator(AspectMigrator.fixModel(CustomItemRegistry.GAS_CANISTER_HELIUM))
			.addMigrator(AspectMigrator.fixModel(CustomItemRegistry.GAS_CANISTER_CARBON_DIOXIDE))
			.addMigrator(AspectMigrator.fixModel(CustomItemRegistry.BATTLECRUISER_REACTOR_CORE))
			.addMigrator(AspectMigrator.fixModel(CustomItemRegistry.BARGE_REACTOR_CORE))
			.addMigrator(AspectMigrator.fixModel(CustomItemRegistry.CRUISER_REACTOR_CORE))
			.addMigrator(AspectMigrator.fixModel(CustomItemRegistry.ARSENAL_MISSILE))
			.addMigrator(AspectMigrator.fixModel(CustomItemRegistry.UNLOADED_ARSENAL_MISSILE))
			.addMigrator(AspectMigrator.fixModel(CustomItemRegistry.ACTIVATED_ARSENAL_MISSILE))
			.addMigrator(AspectMigrator.fixModel(CustomItemRegistry.UNLOADED_SHELL))
			.addMigrator(AspectMigrator.fixModel(CustomItemRegistry.LOADED_SHELL))
			.addMigrator(AspectMigrator.fixModel(CustomItemRegistry.UNCHARGED_SHELL))
			.addMigrator(AspectMigrator.fixModel(CustomItemRegistry.CHARGED_SHELL))
			.addMigrator(AspectMigrator.fixModel(CustomItemRegistry.STEEL_INGOT))
			.addMigrator(AspectMigrator.fixModel(CustomItemRegistry.STEEL_BLOCK))
			.addMigrator(AspectMigrator.fixModel(CustomItemRegistry.STEEL_PLATE))
			.addMigrator(AspectMigrator.fixModel(CustomItemRegistry.STEEL_CHASSIS))
			.addMigrator(AspectMigrator.fixModel(CustomItemRegistry.STEEL_MODULE))
			.addMigrator(AspectMigrator.fixModel(CustomItemRegistry.STEEL_ASSEMBLY))
			.addMigrator(AspectMigrator.fixModel(CustomItemRegistry.REINFORCED_FRAME))
			.addMigrator(AspectMigrator.fixModel(CustomItemRegistry.REACTOR_FRAME))
			.addMigrator(AspectMigrator.fixModel(CustomItemRegistry.SUPERCONDUCTOR))
			.addMigrator(AspectMigrator.fixModel(CustomItemRegistry.SUPERCONDUCTOR_BLOCK))
			.addMigrator(AspectMigrator.fixModel(CustomItemRegistry.SUPERCONDUCTOR_CORE))
			.addMigrator(AspectMigrator.fixModel(CustomItemRegistry.CIRCUIT_BOARD))
			.addMigrator(AspectMigrator.fixModel(CustomItemRegistry.MOTHERBOARD))
			.addMigrator(AspectMigrator.fixModel(CustomItemRegistry.REACTOR_CONTROL))
			.addMigrator(AspectMigrator.fixModel(CustomItemRegistry.REACTIVE_COMPONENT))
			.addMigrator(AspectMigrator.fixModel(CustomItemRegistry.REACTIVE_HOUSING))
			.addMigrator(AspectMigrator.fixModel(CustomItemRegistry.REACTIVE_PLATING))
			.addMigrator(AspectMigrator.fixModel(CustomItemRegistry.REACTIVE_CHASSIS))
			.addMigrator(AspectMigrator.fixModel(CustomItemRegistry.REACTIVE_MEMBRANE))
			.addMigrator(AspectMigrator.fixModel(CustomItemRegistry.REACTIVE_ASSEMBLY))
			.addMigrator(AspectMigrator.fixModel(CustomItemRegistry.FABRICATED_ASSEMBLY))
			.addMigrator(AspectMigrator.fixModel(CustomItemRegistry.NETHERITE_CASING))
			.addMigrator(AspectMigrator.fixModel(CustomItemRegistry.ENRICHED_URANIUM))
			.addMigrator(AspectMigrator.fixModel(CustomItemRegistry.ENRICHED_URANIUM_BLOCK))
			.addMigrator(AspectMigrator.fixModel(CustomItemRegistry.URANIUM_CORE))
			.addMigrator(AspectMigrator.fixModel(CustomItemRegistry.URANIUM_ROD))
			.addMigrator(AspectMigrator.fixModel(CustomItemRegistry.FUEL_ROD_CORE))
			.addMigrator(AspectMigrator.fixModel(CustomItemRegistry.FUEL_CELL))
			.addMigrator(AspectMigrator.fixModel(CustomItemRegistry.FUEL_CONTROL))
			.addMigrator(AspectMigrator.fixModel(CustomItemRegistry.URANIUM))
			.addMigrator(AspectMigrator.fixModel(CustomItemRegistry.RAW_URANIUM))
			.addMigrator(AspectMigrator.fixModel(CustomItemRegistry.URANIUM_ORE))
			.addMigrator(AspectMigrator.fixModel(CustomItemRegistry.URANIUM_BLOCK))
			.addMigrator(AspectMigrator.fixModel(CustomItemRegistry.RAW_URANIUM_BLOCK))
			.addMigrator(AspectMigrator.fixModel(CustomItemRegistry.TITANIUM_INGOT))
			.addMigrator(AspectMigrator.fixModel(CustomItemRegistry.RAW_TITANIUM))
			.addMigrator(AspectMigrator.fixModel(CustomItemRegistry.TITANIUM_ORE))
			.addMigrator(AspectMigrator.fixModel(CustomItemRegistry.TITANIUM_BLOCK))
			.addMigrator(AspectMigrator.fixModel(CustomItemRegistry.RAW_TITANIUM_BLOCK))
			.addMigrator(AspectMigrator.fixModel(CustomItemRegistry.CHETHERITE))
			.addMigrator(AspectMigrator.fixModel(CustomItemRegistry.CHETHERITE_ORE))
			.addMigrator(AspectMigrator.fixModel(CustomItemRegistry.CHETHERITE_BLOCK))
			.addMigrator(AspectMigrator.fixModel(CustomItemRegistry.ALUMINUM_INGOT))
			.addMigrator(AspectMigrator.fixModel(CustomItemRegistry.RAW_ALUMINUM))
			.addMigrator(AspectMigrator.fixModel(CustomItemRegistry.ALUMINUM_ORE))
			.addMigrator(AspectMigrator.fixModel(CustomItemRegistry.ALUMINUM_BLOCK))
			.addMigrator(AspectMigrator.fixModel(CustomItemRegistry.RAW_ALUMINUM_BLOCK))
			.addMigrator(AspectMigrator.fixModel(CustomItemRegistry.PISTOL_RECEIVER))
			.addMigrator(AspectMigrator.fixModel(CustomItemRegistry.RIFLE_RECEIVER))
			.addMigrator(AspectMigrator.fixModel(CustomItemRegistry.SMB_RECEIVER))
			.addMigrator(AspectMigrator.fixModel(CustomItemRegistry.SNIPER_RECEIVER))
			.addMigrator(AspectMigrator.fixModel(CustomItemRegistry.SHOTGUN_RECEIVER))
			.addMigrator(AspectMigrator.fixModel(CustomItemRegistry.CANNON_RECEIVER))
			.addMigrator(AspectMigrator.fixModel(CustomItemRegistry.GUN_BARREL))
			.addMigrator(AspectMigrator.fixModel(CustomItemRegistry.CIRCUITRY))
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
			if (Pipes.isPipedInventory(type)) {
				val state = chunk.getBlock(x, y, z).state as InventoryHolder
				migrateInventory(state.inventory, toApply)
			}
		}
	}

	fun migrate(player: Player) {
		val playerVersion = player.persistentDataContainer.getOrDefault(NamespacedKeys.DATA_VERSION, PersistentDataType.INTEGER, 0)
		if (playerVersion == lastDataVersion) return

		log.info("Migrating ${player.name}'s inventory from $playerVersion to $lastDataVersion")
		migrateInventory(player.inventory, getVersions(playerVersion).apply { log.info("Applying $size versions") })
	}

	private fun getVersions(dataVersion: Int): List<DataVersion> {
		return dataVersions.subList(dataVersion + 1 /* Inclusive */, lastDataVersion + 1 /* Exclusive */)
	}

	private fun migrateInventory(inventory: Inventory, versions: List<DataVersion>) {
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
}
