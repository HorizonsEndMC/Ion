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
import org.bukkit.Material.NETHERITE_AXE
import org.bukkit.Material.NETHERITE_HOE
import org.bukkit.Material.NETHERITE_PICKAXE
import org.bukkit.Material.NETHERITE_SHOVEL
import org.bukkit.Material.NETHERITE_SWORD
import org.bukkit.Material.WARPED_FUNGUS_ON_A_STICK
import org.bukkit.entity.LivingEntity
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType.STRING

// budget minecraft registry lmao
object CustomItems {
	// If we want to be extra fancy we can replace this with some fastutils thing later.
	private val customItems: MutableMap<String, CustomItem> = mutableMapOf()

	@Suppress("Unused") val PISTOL = register(object : Blaster<Singleshot>("PISTOL", NETHERITE_AXE, 1, text("Blaster Pistol", RED, BOLD), { Ion.balancing.energyWeapons.pistol }) {})
	@Suppress("Unused") val RIFLE = register(object : Blaster<Singleshot>("RIFLE", NETHERITE_PICKAXE, 1, text("Blaster Rifle", RED, BOLD), { Ion.balancing.energyWeapons.rifle }) {})
	@Suppress("Unused") val AUTO_RIFLE = register(object : Blaster<Singleshot>("AUTO_RIFLE", NETHERITE_SHOVEL, 1, text("Automatic Blaster Rifle", RED, BOLD), { Ion.balancing.energyWeapons.autoRifle }) {
		override fun handleSecondaryInteract(livingEntity: LivingEntity, itemStack: ItemStack) { // Allows fire above 300 rpm
			val repeatCount = if (balancing.timeBetweenShots >= 4) { 1 } else { 4 / balancing.timeBetweenShots }
			for (count in 0..repeatCount) Tasks.syncDelay(count.toLong()) { super.handleSecondaryInteract(livingEntity, itemStack) }
		}
	})
	@Suppress("Unused") val SHOTGUN = register(object : Blaster<Multishot>("SHOTGUN", NETHERITE_SWORD, 1, text("Blaster Shotgun", RED, BOLD), { Ion.balancing.energyWeapons.shotgun }) {
		override fun fireProjectiles(livingEntity: LivingEntity) {
			for (i in 1..balancing.shotCount) super.fireProjectiles(livingEntity)
		}
	})
	@Suppress("Unused") val SNIPER = register(object : Blaster<Singleshot>("SNIPER", NETHERITE_HOE, 1, text("Blaster Sniper", RED, BOLD), { Ion.balancing.energyWeapons.sniper }) {})

	@Suppress("Unused") val STANDARD_MAGAZINE = register(object : Magazine<BalancingConfiguration.EnergyWeapon.AmmoStorage>("STANDARD_MAGAZINE", WARPED_FUNGUS_ON_A_STICK, 1, text("Magazine"),  { Ion.balancing.energyWeapons.standardMagazine }) {})

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

	private fun <T: CustomItem> register(customItem: T): T {
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
