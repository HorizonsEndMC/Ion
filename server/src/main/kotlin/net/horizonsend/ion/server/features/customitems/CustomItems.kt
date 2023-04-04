package net.horizonsend.ion.server.features.customitems

import net.horizonsend.ion.server.IonServer
import net.horizonsend.ion.server.configuration.BalancingConfiguration
import net.horizonsend.ion.server.configuration.BalancingConfiguration.EnergyWeapon.Multishot
import net.horizonsend.ion.server.configuration.BalancingConfiguration.EnergyWeapon.Singleshot
import net.horizonsend.ion.server.features.blasters.objects.Blaster
import net.horizonsend.ion.server.features.blasters.objects.Magazine
import net.horizonsend.ion.server.miscellaneous.NamespacedKeys.CUSTOM_ITEM
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.format.NamedTextColor.RED
import net.kyori.adventure.text.format.TextDecoration.BOLD
import net.kyori.adventure.text.format.TextDecoration.ITALIC
import net.starlegacy.util.Tasks
import net.starlegacy.util.updateMeta
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

	val STANDARD_MAGAZINE =
		register(
			object : Magazine<BalancingConfiguration.EnergyWeapon.AmmoStorage>(
				identifier = "STANDARD_MAGAZINE",
				material = WARPED_FUNGUS_ON_A_STICK,
				customModelData = 1,
				displayName = text("Standard Magazine").decoration(ITALIC, false),
				balancingSupplier = IonServer.balancing.energyWeapons::standardMagazine
			) {}
		)

	val SPECIAL_MAGAZINE =
		register(
			object : Magazine<BalancingConfiguration.EnergyWeapon.AmmoStorage>(
				identifier = "SPECIAL_MAGAZINE",
				material = WARPED_FUNGUS_ON_A_STICK,
				customModelData = 2,
				displayName = text("Special Magazine").decoration(ITALIC, false),
				balancingSupplier = IonServer.balancing.energyWeapons::specialMagazine
			) {}
		)

	val PISTOL =
		register(
			object : Blaster<Singleshot>(
				identifier = "PISTOL",
				material = DIAMOND_HOE,
				customModelData = 1,
				displayName = text("Blaster Pistol", RED, BOLD).decoration(ITALIC, false),
				magazineType = STANDARD_MAGAZINE,
				soundRange = 100.0,
				soundFire = "blaster.pistol.shoot",
				soundWhizz = "blaster.whizz.standard",
				soundShell = "blaster.pistol.shell",
				soundReloadStart = "blaster.pistol.reload.start",
				soundReloadFinish = "blaster.pistol.reload.finish",
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
				soundRange = 100.0,
				soundFire = "blaster.rifle.shoot",
				soundWhizz = "blaster.whizz.standard",
				soundShell = "blaster.rifle.shell",
				soundReloadStart = "blaster.rifle.reload.start",
				soundReloadFinish = "blaster.rifle.reload.finish",
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
				soundRange = 100.0,
				soundFire = "blaster.submachine_blaster.shoot",
				soundWhizz = "blaster.whizz.standard",
				soundShell = "blaster.submachine_blaster.shell",
				soundReloadStart = "blaster.submachine_blaster.reload.start",
				soundReloadFinish = "blaster.submachine_blaster.reload.finish",
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
				soundRange = 100.0,
				soundFire = "blaster.shotgun.shoot",
				soundWhizz = "blaster.whizz.standard",
				soundShell = "blaster.shotgun.shell",
				soundReloadStart = "blaster.shotgun.reload.start",
				soundReloadFinish = "blaster.shotgun.reload.finish",
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
				soundRange = 200.0,
				soundFire = "blaster.sniper.shoot",
				soundWhizz = "blaster.whizz.sniper",
				soundShell = "blaster.sniper.shell",
				soundReloadStart = "blaster.sniper.reload.start",
				soundReloadFinish = "blaster.sniper.reload.finish",
				balancingSupplier = IonServer.balancing.energyWeapons::sniper
			) {}
		)

	val GUN_BARREL = register("GUN_BARREL", 500, text("Gun Barrel"))
	val CIRCUITRY = register("CIRCUITRY", 501, text("Circuitry"))

	val PISTOL_RECEIVER = register("PISTOL_RECEIVER", 502, text("Pistol Receiver"))
	val RIFLE_RECEIVER = register("RIFLE_RECEIVER", 503, text("Rifle Receiver"))
	val SMB_RECEIVER = register("SMB_RECEIVER", 504, text("SMB Receiver"))
	val SNIPER_RECEIVER = register("SNIPER_RECEIVER", 505, text("Sniper Receiver"))
	val SHOTGUN_RECEIVER = register("SHOTGUN_RECEIVER", 506, text("Shotgun Receiver"))

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
