package net.horizonsend.ion.server.features.customitems

import net.horizonsend.ion.server.IonServer
import net.horizonsend.ion.server.configuration.PVPBalancingConfiguration
import net.horizonsend.ion.server.configuration.PVPBalancingConfiguration.EnergyWeapons.Multishot
import net.horizonsend.ion.server.configuration.PVPBalancingConfiguration.EnergyWeapons.Singleshot
import net.horizonsend.ion.server.features.customitems.blasters.objects.Blaster
import net.horizonsend.ion.server.features.customitems.blasters.objects.Magazine
import net.horizonsend.ion.server.features.customitems.minerals.Smeltable
import net.horizonsend.ion.server.features.customitems.minerals.objects.MineralItem
import net.horizonsend.ion.server.features.customitems.shipammo.objects.ShellItem
import net.horizonsend.ion.server.features.customitems.throwables.ThrownDetonator
import net.horizonsend.ion.server.features.customitems.throwables.ThrownPumpkinGrenade
import net.horizonsend.ion.server.features.customitems.throwables.objects.ThrowableCustomItem
import net.horizonsend.ion.server.features.customitems.throwables.objects.ThrownCustomItem
import net.horizonsend.ion.server.miscellaneous.registrations.NamespacedKeys.CUSTOM_ITEM
import net.horizonsend.ion.server.miscellaneous.utils.Tasks
import net.horizonsend.ion.server.miscellaneous.utils.updateMeta
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.format.NamedTextColor.BLUE
import net.kyori.adventure.text.format.NamedTextColor.GOLD
import net.kyori.adventure.text.format.NamedTextColor.GRAY
import net.kyori.adventure.text.format.NamedTextColor.GREEN
import net.kyori.adventure.text.format.NamedTextColor.LIGHT_PURPLE
import net.kyori.adventure.text.format.NamedTextColor.RED
import net.kyori.adventure.text.format.NamedTextColor.YELLOW
import net.kyori.adventure.text.format.TextDecoration
import net.kyori.adventure.text.format.TextDecoration.BOLD
import net.kyori.adventure.text.format.TextDecoration.ITALIC
import org.bukkit.Material
import org.bukkit.Material.BONE
import org.bukkit.Material.DIAMOND_HOE
import org.bukkit.Material.GOLDEN_HOE
import org.bukkit.Material.IRON_BLOCK
import org.bukkit.Material.IRON_HOE
import org.bukkit.Material.IRON_INGOT
import org.bukkit.Material.IRON_ORE
import org.bukkit.Material.HEART_OF_THE_SEA
import org.bukkit.Material.RAW_IRON
import org.bukkit.Material.RAW_IRON_BLOCK
import org.bukkit.Material.WARPED_FUNGUS_ON_A_STICK
import org.bukkit.block.Dispenser
import org.bukkit.entity.Entity
import org.bukkit.entity.Item
import org.bukkit.entity.LivingEntity
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType.STRING
import kotlin.math.roundToInt

// budget minecraft registry lmao
object CustomItems {
	// If we want to be extra fancy we can replace this with some fastutils thing later .
	val ALL get() = customItems.values
	private val customItems: MutableMap<String, CustomItem> = mutableMapOf()


	// Ship Ammunition Start

	val UNLOADED_TURRET_SHELL =
		register(
			object : ShellItem(
				identifier = "UNLOADED_SHELL",
				material = BONE,
				customModelData = 511,
				displayName = text("Unloaded Shell").decoration(ITALIC, false)
			) {}
		)

	val LOADED_TURRET_SHELL =
		register(
			object : ShellItem(
				identifier = "LOADED_SHELL",
				material = BONE,
				customModelData = 512,
				displayName = text("Loaded Shell").decoration(ITALIC, false)
			) {}
		)

	// Ship Ammunition End
	// Magazines Start

	val STANDARD_MAGAZINE =
		register(
			object : Magazine<PVPBalancingConfiguration.EnergyWeapons.AmmoStorage>(
				identifier = "STANDARD_MAGAZINE",
				material = WARPED_FUNGUS_ON_A_STICK,
				customModelData = 1,
				displayName = text("Standard Magazine").decoration(ITALIC, false),
				balancingSupplier = IonServer.pvpBalancing.energyWeapons::standardMagazine
			) {}
		)

	val SPECIAL_MAGAZINE =
		register(
			object : Magazine<PVPBalancingConfiguration.EnergyWeapons.AmmoStorage>(
				identifier = "SPECIAL_MAGAZINE",
				material = WARPED_FUNGUS_ON_A_STICK,
				customModelData = 2,
				displayName = text("Special Magazine").decoration(ITALIC, false),
				balancingSupplier = IonServer.pvpBalancing.energyWeapons::specialMagazine
			) {}
		)

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
				soundFire = "blaster.pistol.shoot",
				soundWhizz = "blaster.whizz.standard",
				soundShell = "blaster.pistol.shell",
				soundReloadStart = "blaster.pistol.reload.start",
				soundReloadFinish = "blaster.pistol.reload.finish",
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
				soundFire = "blaster.rifle.shoot",
				soundWhizz = "blaster.whizz.standard",
				soundShell = "blaster.rifle.shell",
				soundReloadStart = "blaster.rifle.reload.start",
				soundReloadFinish = "blaster.rifle.reload.finish",
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
				soundFire = "blaster.submachine_blaster.shoot",
				soundWhizz = "blaster.whizz.standard",
				soundShell = "blaster.submachine_blaster.shell",
				soundReloadStart = "blaster.submachine_blaster.reload.start",
				soundReloadFinish = "blaster.submachine_blaster.reload.finish",
				explosiveShot = false,
				balancingSupplier = IonServer.pvpBalancing.energyWeapons::submachineBlaster
			) {
				// Allows fire above 300 rpm
				override fun handleSecondaryInteract(
					livingEntity: LivingEntity,
					itemStack: ItemStack
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
							Tasks.syncDelay(delay) { super.handleSecondaryInteract(livingEntity, itemStack) }
						} else {
							super.handleSecondaryInteract(livingEntity, itemStack)
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
				soundFire = "blaster.shotgun.shoot",
				soundWhizz = "blaster.whizz.standard",
				soundShell = "blaster.shotgun.shell",
				soundReloadStart = "blaster.shotgun.reload.start",
				soundReloadFinish = "blaster.shotgun.reload.finish",
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
				soundFire = "blaster.sniper.shoot",
				soundWhizz = "blaster.whizz.sniper",
				soundShell = "blaster.sniper.shell",
				soundReloadStart = "blaster.sniper.reload.start",
				soundReloadFinish = "blaster.sniper.reload.finish",
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
				soundFire = "blaster.cannon.shoot",
				soundWhizz = "blaster.whizz.standard",
				soundShell = "blaster.sniper.shell",
				soundReloadStart = "blaster.cannon.reload.start",
				soundReloadFinish = "blaster.cannon.reload.finish",
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

	val OXYGEN_TANK = register(
		object : CustomBlockItem(
			identifier = "OXYGEN_TANK",
			material = IRON_BLOCK,
			customModelData = 601,
			displayName = text("Oxygen Tank").decoration(ITALIC, false),
			customBlockIdentifier = "OXYGEN_TANK"
		)	{}
	)

	val STEEL_INGOT = register(
		object : MineralItem(
			identifier = "STEEL_INGOT",
			material = IRON_INGOT,
			customModelData = 600,
			displayName = text("Steel Ingot").decoration(ITALIC, false)
		) {}
	)
	val STEEL_BLOCK = register(
		object : CustomBlockItem(
			identifier = "STEEL_BLOCK",
			material = IRON_BLOCK,
			customModelData = 600,
			displayName = text("Steel Block").decoration(ITALIC, false),
			customBlockIdentifier = "STEEL_BLOCK"
		) {}
	)

	val STEEL_PLATE = registerStackable("STEEL_PLATE", 10, text("Steel Plate"))

	val ALUMINUM_INGOT = register(
		object : MineralItem(
			identifier = "ALUMINUM_INGOT",
			material = IRON_INGOT,
			customModelData = 1,
			displayName = text("Aluminum Ingot").decoration(ITALIC, false)
		) {}
	)

	val RAW_ALUMINUM : MineralItem = register(
		object : MineralItem(
			identifier = "RAW_ALUMINUM",
			material = RAW_IRON,
			customModelData = 1,
			displayName = text("Raw Aluminum").decoration(ITALIC, false)
		), Smeltable { override val smeltResultIdentifier: String = "ALUMINUM_INGOT" }
	)

	val ALUMINUM_ORE : CustomBlockItem = register(
		object : CustomBlockItem(
			identifier = "ALUMINUM_ORE",
			material = IRON_ORE,
			customModelData = 1,
			displayName = text("Aluminum Ore").decoration(ITALIC, false),
			customBlockIdentifier = "ALUMINUM_ORE"
		), Smeltable { override val smeltResultIdentifier: String = "ALUMINUM_INGOT" }
	)

	val ALUMINUM_BLOCK = register(
		object : CustomBlockItem(
			identifier = "ALUMINUM_BLOCK",
			material = IRON_BLOCK,
			customModelData = 1,
			displayName = text("Aluminum Block").decoration(ITALIC, false),
			customBlockIdentifier = "ALUMINUM_BLOCK"
		) {}
	)

	 val RAW_ALUMINUM_BLOCK = register(
		 object : CustomBlockItem(
			 identifier = "RAW_ALUMINUM_BLOCK",
			 material = RAW_IRON_BLOCK,
			 customModelData = 1,
			 displayName = text("Raw Aluminum Block").decoration(ITALIC, false),
			 customBlockIdentifier = "RAW_ALUMINUM_BLOCK"
		 ) {}
	 )

	val CHETHERITE = register(
		object : MineralItem(
			identifier = "CHETHERITE",
			material = IRON_INGOT,
			customModelData = 2,
			displayName = text("Chetherite").decoration(ITALIC, false)
		) {}
	)

	val CHETHERITE_ORE : CustomBlockItem = register(
		object : CustomBlockItem(
			identifier = "CHETHERITE_ORE",
			material = IRON_ORE,
			customModelData = 2,
			displayName = text("Chetherite Ore").decoration(ITALIC, false),
			customBlockIdentifier = "CHETHERITE_ORE"
		), Smeltable { override val smeltResultIdentifier: String = "CHETHERITE" }
	)

	val CHETHERITE_BLOCK = register(
		object : CustomBlockItem(
			identifier = "CHETHERITE_BLOCK",
			material = IRON_BLOCK,
			customModelData = 2,
			displayName = text("Chetherite Block").decoration(ITALIC, false),
			customBlockIdentifier = "CHETHERITE_BLOCK"
		) {}
	)

	val TITANIUM_INGOT = register(
		object : MineralItem(
			identifier = "TITANIUM_INGOT",
			material = IRON_INGOT,
			customModelData = 3,
			displayName = text("Titanium Ingot").decoration(ITALIC, false)
		) {}
	)

	val RAW_TITANIUM : MineralItem = register(
		object : MineralItem(
			identifier = "RAW_TITANIUM",
			material = RAW_IRON,
			customModelData = 3,
			displayName = text("Raw Titanium").decoration(ITALIC, false)
		), Smeltable { override val smeltResultIdentifier: String = "TITANIUM_INGOT" }
	)

	val TITANIUM_ORE : CustomBlockItem = register(
		object : CustomBlockItem(
			identifier = "TITANIUM_ORE",
			material = IRON_ORE,
			customModelData = 3,
			displayName = text("Titanium Ore").decoration(ITALIC, false),
			customBlockIdentifier = "TITANIUM_ORE"
		), Smeltable { override val smeltResultIdentifier: String = "TITANIUM_INGOT" }
	)

	val TITANIUM_BLOCK = register(
		object : CustomBlockItem(
			identifier = "TITANIUM_BLOCK",
			material = IRON_BLOCK,
			customModelData = 3,
			displayName = text("Titanium Block").decoration(ITALIC, false),
			customBlockIdentifier = "TITANIUM_BLOCK"
		) {}
	)

	val RAW_TITANIUM_BLOCK = register(
		object : CustomBlockItem(
			identifier = "RAW_TITANIUM_BLOCK",
			material = RAW_IRON_BLOCK,
			customModelData = 3,
			displayName = text("Raw Titanium Block").decoration(ITALIC, false),
			customBlockIdentifier = "RAW_TITANIUM_BLOCK"
		) {}
	)

	val URANIUM = register(
		object : MineralItem(
			identifier = "URANIUM",
			material = IRON_INGOT,
			customModelData = 4,
			displayName = text("Uranium").decoration(ITALIC, false),
		) {}
	)

	val RAW_URANIUM : MineralItem = register(
		object : MineralItem(
			identifier = "RAW_URANIUM",
			material = RAW_IRON,
			customModelData = 4,
			displayName = text("Raw Uranium").decoration(ITALIC, false)
		), Smeltable { override val smeltResultIdentifier: String = "URANIUM" }
	)

	val URANIUM_ORE : CustomBlockItem = register(
		object : CustomBlockItem(
			identifier = "URANIUM_ORE",
			material = IRON_ORE,
			customModelData = 4,
			displayName = text("Uranium Ore").decoration(ITALIC, false),
			customBlockIdentifier = "URANIUM_ORE"
		), Smeltable { override val smeltResultIdentifier: String = "URANIUM" }
	)

	val URANIUM_BLOCK = register(
		object : CustomBlockItem(
			identifier = "URANIUM_BLOCK",
			material = IRON_BLOCK,
			customModelData = 4,
			displayName = text("Uranium Block").decoration(ITALIC, false),
			customBlockIdentifier = "URANIUM_BLOCK"
		) {}
	)

	val RAW_URANIUM_BLOCK = register(
		object : CustomBlockItem(
			identifier = "RAW_URANIUM_BLOCK",
			material = RAW_IRON_BLOCK,
			customModelData = 4,
			displayName = text("Raw Uranium Block").decoration(ITALIC, false),
			customBlockIdentifier = "RAW_URANIUM_BLOCK"
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

	// This is just a convenient alias for items that don't do anything or are placeholders.
	private fun register(identifier: String, customModelData: Int, displayName: Component): CustomItem {
		return register(object : CustomItem(identifier) {
			override fun constructItemStack(): ItemStack {
				return ItemStack(WARPED_FUNGUS_ON_A_STICK).updateMeta {
					it.setCustomModelData(customModelData)
					it.displayName(displayName.decoration(ITALIC, false))
					it.persistentDataContainer.set(CUSTOM_ITEM, STRING, identifier)
				}
			}
		})
	}

	private fun registerStackable(identifier: String, customModelData: Int, displayName: Component): CustomItem {
		return register(object : CustomItem(identifier) {
			override fun constructItemStack(): ItemStack {
				return ItemStack(HEART_OF_THE_SEA).updateMeta {
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
	open fun handlePrimaryInteract(livingEntity: LivingEntity, itemStack: ItemStack) {}
	open fun handleSecondaryInteract(livingEntity: LivingEntity, itemStack: ItemStack) {}
	open fun handleTertiaryInteract(livingEntity: LivingEntity, itemStack: ItemStack) {}
	open fun handleDispense(dispenser: Dispenser, slot: Int) {}
	abstract fun constructItemStack(): ItemStack
}
