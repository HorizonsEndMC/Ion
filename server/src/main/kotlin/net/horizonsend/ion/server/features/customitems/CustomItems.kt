package net.horizonsend.ion.server.features.customitems

import net.horizonsend.ion.server.IonServer
import net.horizonsend.ion.server.configuration.BalancingConfiguration
import net.horizonsend.ion.server.configuration.BalancingConfiguration.EnergyWeapons.Multishot
import net.horizonsend.ion.server.configuration.BalancingConfiguration.EnergyWeapons.Singleshot
import net.horizonsend.ion.server.features.customitems.blasters.objects.Blaster
import net.horizonsend.ion.server.features.customitems.blasters.objects.Magazine
import net.horizonsend.ion.server.miscellaneous.registrations.NamespacedKeys.CUSTOM_ITEM
import net.horizonsend.ion.server.miscellaneous.utils.Tasks
import net.horizonsend.ion.server.miscellaneous.utils.updateMeta
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.format.NamedTextColor.BLUE
import net.kyori.adventure.text.format.NamedTextColor.GRAY
import net.kyori.adventure.text.format.NamedTextColor.RED
import net.kyori.adventure.text.format.NamedTextColor.YELLOW
import net.kyori.adventure.text.format.TextDecoration.BOLD
import net.kyori.adventure.text.format.TextDecoration.ITALIC
import org.bukkit.Material.DIAMOND_HOE
import org.bukkit.Material.GOLDEN_HOE
import org.bukkit.Material.IRON_HOE
import org.bukkit.Material.WARPED_FUNGUS_ON_A_STICK
import org.bukkit.entity.LivingEntity
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType.STRING
import kotlin.math.roundToInt

// budget minecraft registry lmao
object CustomItems {
	// If we want to be extra fancy we can replace this with some fastutils thing later .
	val ALL get() = customItems.values
	private val customItems: MutableMap<String, CustomItem> = mutableMapOf()

	// Magazines Start

	val STANDARD_MAGAZINE =
		register(
			object : Magazine<BalancingConfiguration.EnergyWeapons.AmmoStorage>(
				identifier = "STANDARD_MAGAZINE",
				material = WARPED_FUNGUS_ON_A_STICK,
				customModelData = 1,
				displayName = text("Standard Magazine").decoration(ITALIC, false),
				balancingSupplier = IonServer.balancing.energyWeapons::standardMagazine
			) {}
		)

	val SPECIAL_MAGAZINE =
		register(
			object : Magazine<BalancingConfiguration.EnergyWeapons.AmmoStorage>(
				identifier = "SPECIAL_MAGAZINE",
				material = WARPED_FUNGUS_ON_A_STICK,
				customModelData = 2,
				displayName = text("Special Magazine").decoration(ITALIC, false),
				balancingSupplier = IonServer.balancing.energyWeapons::specialMagazine
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
				balancingSupplier = IonServer.balancing.energyWeapons::pistol
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
				balancingSupplier = IonServer.balancing.energyWeapons::rifle
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
				balancingSupplier = IonServer.balancing.energyWeapons::submachineBlaster
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
				balancingSupplier = IonServer.balancing.energyWeapons::shotgun
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
				balancingSupplier = IonServer.balancing.energyWeapons::sniper
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
				balancingSupplier = IonServer.balancing.energyWeapons::cannon
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
			maximumFill = 300,
			customModelData = 1001,
			gasIdentifier = "HYDROGEN",
			displayName = canisterName(text("Hydrogen", RED))
		) {}
	)
	val GAS_CANISTER_NITROGEN = register(
		object : GasCanister(
			identifier = "GAS_CANISTER_NITROGEN",
			maximumFill = 100,
			customModelData = 1002,
			gasIdentifier = "NITROGEN",
			displayName = canisterName(text("Nitrogen", RED))
		) {}
	)
	val GAS_CANISTER_METHANE = register(
		object : GasCanister(
			identifier = "GAS_CANISTER_METHANE",
			maximumFill = 150,
			customModelData = 1003,
			gasIdentifier = "METHANE",
			displayName = canisterName(text("Methane", RED))
		) {}
	)

	// Oxidizers
	val GAS_CANISTER_OXYGEN = register(
		object : GasCanister(
			identifier = "GAS_CANISTER_OXYGEN",
			maximumFill = 300,
			customModelData = 1010,
			gasIdentifier = "OXYGEN",
			displayName = canisterName(text("Oxygen", YELLOW))
		) {}
	)
	val GAS_CANISTER_CHLORINE = register(
		object : GasCanister(
			identifier = "GAS_CANISTER_CHLORINE",
			maximumFill = 100,
			customModelData = 1011,
			gasIdentifier = "CHLORINE",
			displayName = canisterName(text("Chlorine", YELLOW))
		) {}
	)
	val GAS_CANISTER_FLUORINE = register(
		object : GasCanister(
			identifier = "GAS_CANISTER_FLUORINE",
			maximumFill = 150,
			customModelData = 1012,
			gasIdentifier = "FLUORINE",
			displayName = canisterName(text("Fluorine", YELLOW))
		) {}
	)

	// Other
	val GAS_CANISTER_HELIUM = register(
		object : GasCanister(
			identifier = "GAS_CANISTER_HELIUM",
			maximumFill = 300,
			customModelData = 1020,
			gasIdentifier = "HELIUM",
			displayName = canisterName(text("Helium", BLUE))
		) {}
	)
	val GAS_CANISTER_CARBON_DIOXIDE = register(
		object : GasCanister(
			identifier = "GAS_CANISTER_CARBON_DIOXIDE",
			maximumFill = 100,
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

	// This is just a convenient alias for items that don't do anything or are placeholders.
	private fun register(identifier: String, customModelData: Int, component: Component): CustomItem {
		return register(object : CustomItem(identifier) {
			override fun constructItemStack(): ItemStack {
				return ItemStack(WARPED_FUNGUS_ON_A_STICK).updateMeta {
					it.setCustomModelData(customModelData)
					it.displayName(component.decoration(ITALIC, false))
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
			return customItems[itemMeta?.persistentDataContainer?.get(CUSTOM_ITEM, STRING) ?: return null]
		}

	val identifiers = customItems.keys

	fun getByIdentifier(identifier: String): CustomItem? = customItems[identifier]
}

abstract class CustomItem(val identifier: String) {
	open fun handleSecondaryInteract(livingEntity: LivingEntity, itemStack: ItemStack) {}
	open fun handleTertiaryInteract(livingEntity: LivingEntity, itemStack: ItemStack) {}
	abstract fun constructItemStack(): ItemStack
}
