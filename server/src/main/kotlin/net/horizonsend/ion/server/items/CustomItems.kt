package net.horizonsend.ion.server.items

import net.horizonsend.ion.server.BalancingConfiguration
import net.horizonsend.ion.server.BalancingConfiguration.EnergyWeapon.Multishot
import net.horizonsend.ion.server.BalancingConfiguration.EnergyWeapon.Singleshot
import net.horizonsend.ion.server.IonServer.Companion.Ion
import net.horizonsend.ion.server.NamespacedKeys.CUSTOM_ITEM
import net.horizonsend.ion.server.items.objects.Blaster
import net.horizonsend.ion.server.items.objects.Magazine
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.format.NamedTextColor.RED
import net.kyori.adventure.text.format.TextDecoration.BOLD
import net.starlegacy.util.Tasks
import net.starlegacy.util.updateMeta
import org.bukkit.Material.DIAMOND_HOE
import org.bukkit.Material.IRON_HOE
import org.bukkit.Material.WARPED_FUNGUS_ON_A_STICK
import org.bukkit.entity.LivingEntity
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType.STRING
import kotlin.math.roundToInt

// budget minecraft registry lmao
object CustomItems {
	// If we want to be extra fancy we can replace this with some fastutils thing later.
	private val customItems: MutableMap<String, CustomItem> = mutableMapOf()

	@Suppress("Unused") val PISTOL = register(object : Blaster<Singleshot>("PISTOL", DIAMOND_HOE, 1, text("Blaster Pistol", RED, BOLD), { Ion.balancing.energyWeapons.pistol }) {})

	@Suppress("Unused") val RIFLE = register(object : Blaster<Singleshot>("RIFLE", IRON_HOE, 1, text("Blaster Rifle", RED, BOLD), { Ion.balancing.energyWeapons.rifle }) {})

	@Suppress("Unused") val SUBMACHINE_BLASTER = register(object : Blaster<Singleshot>("SUBMACHINE_BLASTER", IRON_HOE, 4, text("Submachine Blaster", RED, BOLD), { Ion.balancing.energyWeapons.submachineBlaster }) {
		override fun handleSecondaryInteract(livingEntity: LivingEntity, itemStack: ItemStack) { // Allows fire above 300 rpm
			val repeatCount = if (balancing.timeBetweenShots >= 4) { 1 } else { (4.0 / balancing.timeBetweenShots).roundToInt() }
			val division = 4.0 / balancing.timeBetweenShots
			for (count in 0 until repeatCount) {
				val delay = (count * division).toLong()
				if (delay > 0) { Tasks.syncDelay(delay) { super.handleSecondaryInteract(livingEntity, itemStack) } } else {
					super.handleSecondaryInteract(livingEntity, itemStack)
				}
			}
		}
	})

	@Suppress("Unused") val SHOTGUN = register(object : Blaster<Multishot>("SHOTGUN", IRON_HOE, 2, text("Blaster Shotgun", RED, BOLD), { Ion.balancing.energyWeapons.shotgun }) {
		override fun fireProjectiles(livingEntity: LivingEntity) {
			for (i in 1..balancing.shotCount) super.fireProjectiles(livingEntity)
		}
	})

	@Suppress("Unused") val SNIPER = register(object : Blaster<Singleshot>("SNIPER", IRON_HOE, 3, text("Blaster Sniper", RED, BOLD), { Ion.balancing.energyWeapons.sniper }) {})

	@Suppress("Unused") val STANDARD_MAGAZINE = register(object : Magazine<BalancingConfiguration.EnergyWeapon.AmmoStorage>("STANDARD_MAGAZINE", WARPED_FUNGUS_ON_A_STICK, 1, text("Magazine"), { Ion.balancing.energyWeapons.standardMagazine }) {})

	val GUN_BARREL = register("GUN_BARREL", 500, text("Gun Barrel"))
	val CIRCUITRY = register("CIRCUITRY", 501, text("Circuitry"))

	// This is just a convenient alias for items that don't do anything or are placeholders.
	private fun register(identifier: String, customModelData: Int, component: Component): CustomItem {
		return register(object : CustomItem(identifier) {
			override fun constructItemStack(): ItemStack {
				return ItemStack(WARPED_FUNGUS_ON_A_STICK).updateMeta {
					it.setCustomModelData(customModelData)
					it.displayName(component)
					it.persistentDataContainer.set(CUSTOM_ITEM, STRING, identifier)
				}
			}
		})
	}

	private fun <T : CustomItem> register(customItem: T): T {
		customItems[customItem.identifier] = customItem
		return customItem
	}

	val ItemStack.customItem: CustomItem? get() {
		// Who tf annotated itemMeta with "UndefinedNullability"
		// if ya cant promise its not null, then mark it nullable
		return customItems[itemMeta?.persistentDataContainer?.get(CUSTOM_ITEM, STRING) ?: return null]
	}

	val identifiers = customItems.keys

	fun getByIdentifier(identifier: String): CustomItem? = customItems[identifier]
}
