package net.horizonsend.ion.server.features.custom

import net.horizonsend.ion.server.IonServerComponent
import net.horizonsend.ion.server.configuration.ConfigurationFiles
import net.horizonsend.ion.server.configuration.PVPBalancingConfiguration.EnergyWeapons.Multishot
import net.horizonsend.ion.server.configuration.PVPBalancingConfiguration.EnergyWeapons.Singleshot
import net.horizonsend.ion.server.features.custom.NewCustomItemListeners.sortCustomItemListeners
import net.horizonsend.ion.server.features.custom.items.blasters.Blaster
import net.horizonsend.ion.server.features.custom.items.blasters.Magazine
import net.horizonsend.ion.server.features.custom.items.util.ItemFactory
import net.horizonsend.ion.server.features.custom.items.util.ItemFactory.Preset.unStackableCustomItem
import net.horizonsend.ion.server.miscellaneous.registrations.NamespacedKeys.CUSTOM_ITEM
import net.horizonsend.ion.server.miscellaneous.utils.Tasks
import net.horizonsend.ion.server.miscellaneous.utils.text.itemName
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.format.NamedTextColor.RED
import net.kyori.adventure.text.format.TextDecoration.BOLD
import net.kyori.adventure.text.format.TextDecoration.ITALIC
import org.bukkit.Material.DIAMOND_HOE
import org.bukkit.Material.GOLDEN_HOE
import org.bukkit.Material.IRON_HOE
import org.bukkit.entity.LivingEntity
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType.STRING
import kotlin.math.roundToInt

object CustomItemRegistry : IonServerComponent() {
	private val customItems = mutableMapOf<String, NewCustomItem>()
	val ALL get() = customItems.values

	// Guns Start
	val STANDARD_MAGAZINE = register(Magazine(
		identifier = "STANDARD_MAGAZINE",
		displayName = text("Standard Magazine").decoration(ITALIC, false),
		itemFactory = unStackableCustomItem("weapon/blaster/standard_magazine"),
		balancingSupplier = ConfigurationFiles.pvpBalancing().energyWeapons::standardMagazine
	))
	val SPECIAL_MAGAZINE = register(Magazine(
		identifier = "SPECIAL_MAGAZINE",
		displayName = text("Special Magazine").decoration(ITALIC, false),
		itemFactory = unStackableCustomItem("weapon/blaster/special_magazine"),
		balancingSupplier = ConfigurationFiles.pvpBalancing().energyWeapons::specialMagazine
	))

	val BLASTER_PISTOL = register(Blaster(
		identifier = "BLASTER_PISTOL",
		displayName = text("Blaster Pistol", RED, BOLD).itemName,
		itemFactory = ItemFactory.builder().setMaterial(DIAMOND_HOE).setCustomModel("weapon/blaster/pistol").build(),
		balancingSupplier = ConfigurationFiles.pvpBalancing().energyWeapons::pistol
	))
	val BLASTER_RIFLE = register(Blaster(
		identifier = "BLASTER_RIFLE",
		displayName = text("Blaster Rifle", RED, BOLD).itemName,
		itemFactory = ItemFactory.builder().setMaterial(IRON_HOE).setCustomModel("weapon/blaster/rifle").build(),
		balancingSupplier = ConfigurationFiles.pvpBalancing().energyWeapons::rifle
	))
	val SUBMACHINE_BLASTER = register(object : Blaster<Singleshot>(
		identifier = "SUBMACHINE_BLASTER",
		itemFactory = ItemFactory.builder().setMaterial(IRON_HOE).setCustomModel("weapon/blaster/submachine_blaster").build(),
		displayName = text("Submachine Blaster", RED, BOLD).decoration(ITALIC, false),
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
	val BLASTER_SHOTGUN = register(object : Blaster<Multishot>(
		identifier = "BLASTER_SHOTGUN",
		displayName = text("Blaster Shotgun", RED, BOLD).decoration(ITALIC, false),
		itemFactory = ItemFactory.builder().setMaterial(GOLDEN_HOE).setCustomModel("weapon/blaster/shotgun").build(),
		balancingSupplier = ConfigurationFiles.pvpBalancing().energyWeapons::shotgun
	) {
		override fun fireProjectiles(livingEntity: LivingEntity) {
			for (i in 1..balancing.shotCount) super.fireProjectiles(livingEntity)
		}
	})
	val BLASTER_SNIPER = register(Blaster(
		identifier = "BLASTER_SNIPER",
		displayName = text("Blaster Sniper", RED, BOLD).decoration(ITALIC, false),
		itemFactory = ItemFactory.builder().setMaterial(GOLDEN_HOE).setCustomModel("weapon/blaster/sniper").build(),
		balancingSupplier = ConfigurationFiles.pvpBalancing().energyWeapons::sniper
	))
	val BLASTER_CANNON = register(Blaster(
		identifier = "BLASTER_CANNON",
		displayName = text("Blaster Cannon", RED, BOLD).decoration(ITALIC, false),
		itemFactory = ItemFactory.builder().setMaterial(IRON_HOE).setCustomModel("weapon/blaster/cannon").build(),
		balancingSupplier = ConfigurationFiles.pvpBalancing().energyWeapons::cannon
	))

	val GUN_BARREL = register(NewCustomItem("GUN_BARREL", text("Gun Barrel"), unStackableCustomItem("industry/gun_barrel")))
	val CIRCUITRY = register(NewCustomItem("CIRCUITRY", text("Circuitry"), unStackableCustomItem("industry/circuitry")))

	init {
		sortCustomItemListeners()
	}

	fun <T : NewCustomItem> register(item: T): T {
		customItems[item.identifier] = item
		return item
	}

	val ItemStack.newCustomItem: NewCustomItem? get() {
		return customItems[persistentDataContainer.get(CUSTOM_ITEM, STRING) ?: return null]
	}

	val identifiers = customItems.keys

	fun getByIdentifier(identifier: String): NewCustomItem? = customItems[identifier]
}
