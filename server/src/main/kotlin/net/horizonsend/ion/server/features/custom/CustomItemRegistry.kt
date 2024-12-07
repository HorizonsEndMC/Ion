package net.horizonsend.ion.server.features.custom

import net.horizonsend.ion.server.IonServer
import net.horizonsend.ion.server.IonServerComponent
import net.horizonsend.ion.server.configuration.PVPBalancingConfiguration.EnergyWeapons.Multishot
import net.horizonsend.ion.server.configuration.PVPBalancingConfiguration.EnergyWeapons.Singleshot
import net.horizonsend.ion.server.features.custom.NewCustomItemListeners.sortCustomItemListeners
import net.horizonsend.ion.server.features.custom.items.blasters.NewBlaster
import net.horizonsend.ion.server.features.custom.items.util.ItemFactory
import net.horizonsend.ion.server.features.custom.items.util.ItemFactory.Preset.unStackableCustomItem
import net.horizonsend.ion.server.features.custom.items.util.ItemFactory.Preset.withModel
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
	val PISTOL = register(NewBlaster(
		identifier = "PISTOL",
		displayName = text("Blaster Pistol", RED, BOLD).itemName,
		itemFactory = ItemFactory.builder().setMaterial(DIAMOND_HOE).setCustomModel("item/weapon/blaster/pistol").build(),
		balancingSupplier = IonServer.pvpBalancing.energyWeapons::pistol
	))
	val RIFLE = register(NewBlaster(
		identifier = "RIFLE",
		displayName = text("Blaster Rifle", RED, BOLD).itemName,
		itemFactory = ItemFactory.builder().setMaterial(IRON_HOE).setCustomModel("item/weapon/blaster/rifle").build(),
		balancingSupplier = IonServer.pvpBalancing.energyWeapons::rifle
	))
	val SUBMACHINE_BLASTER = register(object : NewBlaster<Singleshot>(
		identifier = "SUBMACHINE_BLASTER",
		itemFactory = ItemFactory.builder().setMaterial(IRON_HOE).setCustomModel("item/weapon/blaster/submachine_blaster").build(),
		displayName = text("Submachine Blaster", RED, BOLD).decoration(ITALIC, false),
		balancingSupplier = IonServer.pvpBalancing.energyWeapons::submachineBlaster
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
	val SHOTGUN = register(object : NewBlaster<Multishot>(
		identifier = "SHOTGUN",
		displayName = text("Blaster Shotgun", RED, BOLD).decoration(ITALIC, false),
		itemFactory = ItemFactory.builder().setMaterial(GOLDEN_HOE).setCustomModel("item/weapon/blaster/shotgun").build(),
		balancingSupplier = IonServer.pvpBalancing.energyWeapons::shotgun
	) {
		override fun fireProjectiles(livingEntity: LivingEntity) {
			for (i in 1..balancing.shotCount) super.fireProjectiles(livingEntity)
		}
	})
	val SNIPER = register(NewBlaster(
		identifier = "SNIPER",
		displayName = text("Blaster Sniper", RED, BOLD).decoration(ITALIC, false),
		itemFactory = ItemFactory.builder().setMaterial(GOLDEN_HOE).setCustomModel("item/weapon/blaster/sniper").build(),
		balancingSupplier = IonServer.pvpBalancing.energyWeapons::sniper
	))
	val CANNON = register(NewBlaster(
		identifier = "CANNON",
		displayName = text("Blaster Cannon", RED, BOLD).decoration(ITALIC, false),
		itemFactory = ItemFactory.builder().setMaterial(IRON_HOE).setCustomModel("item/weapon/blaster/sniper").build(),
		balancingSupplier = IonServer.pvpBalancing.energyWeapons::cannon
	))

	val GUN_BARREL = register(NewCustomItem("GUN_BARREL", text("Gun Barrel"), unStackableCustomItem.withModel("item/industry/gun_barrel")))
	val CIRCUITRY = register(NewCustomItem("CIRCUITRY", text("Circuitry"), unStackableCustomItem.withModel("item/industry/circuitry")))

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
