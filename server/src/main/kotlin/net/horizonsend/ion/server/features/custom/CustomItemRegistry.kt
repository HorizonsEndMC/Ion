package net.horizonsend.ion.server.features.custom

import net.horizonsend.ion.server.IonServerComponent
import net.horizonsend.ion.server.configuration.ConfigurationFiles
import net.horizonsend.ion.server.configuration.PVPBalancingConfiguration.EnergyWeapons.Multishot
import net.horizonsend.ion.server.configuration.PVPBalancingConfiguration.EnergyWeapons.Singleshot
import net.horizonsend.ion.server.features.custom.NewCustomItemListeners.sortCustomItemListeners
import net.horizonsend.ion.server.features.custom.blocks.CustomBlock
import net.horizonsend.ion.server.features.custom.blocks.CustomBlocks
import net.horizonsend.ion.server.features.custom.items.CustomBlockItem
import net.horizonsend.ion.server.features.custom.items.blasters.Blaster
import net.horizonsend.ion.server.features.custom.items.blasters.Magazine
import net.horizonsend.ion.server.features.custom.items.components.CustomComponentType
import net.horizonsend.ion.server.features.custom.items.components.SmeltableComponent
import net.horizonsend.ion.server.features.custom.items.util.ItemFactory
import net.horizonsend.ion.server.features.custom.items.util.ItemFactory.Preset.stackableCustomItem
import net.horizonsend.ion.server.features.custom.items.util.ItemFactory.Preset.unStackableCustomItem
import net.horizonsend.ion.server.features.custom.items.util.withComponent
import net.horizonsend.ion.server.miscellaneous.registrations.NamespacedKeys.CUSTOM_ITEM
import net.horizonsend.ion.server.miscellaneous.utils.Tasks
import net.horizonsend.ion.server.miscellaneous.utils.map
import net.horizonsend.ion.server.miscellaneous.utils.text.itemName
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.format.NamedTextColor.RED
import net.kyori.adventure.text.format.TextDecoration.BOLD
import net.kyori.adventure.text.format.TextDecoration.ITALIC
import org.bukkit.Material.DIAMOND_HOE
import org.bukkit.Material.GOLDEN_HOE
import org.bukkit.Material.IRON_BLOCK
import org.bukkit.Material.IRON_HOE
import org.bukkit.Material.IRON_ORE
import org.bukkit.Material.RAW_IRON
import org.bukkit.Material.RAW_IRON_BLOCK
import org.bukkit.entity.LivingEntity
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType.STRING
import java.util.function.Supplier
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

	val GUN_BARREL = register("GUN_BARREL", text("Gun Barrel"), unStackableCustomItem("industry/gun_barrel"))
	val CIRCUITRY = register("CIRCUITRY", text("Circuitry"), unStackableCustomItem("industry/circuitry"))

	val PISTOL_RECEIVER = register("PISTOL_RECEIVER", text("Pistol Receiver"), unStackableCustomItem("industry/pistol_receiver"))
	val RIFLE_RECEIVER = register("RIFLE_RECEIVER", text("Rifle Receiver"), unStackableCustomItem("industry/rifle_receiver"))
	val SMB_RECEIVER = register("SMB_RECEIVER", text("SMB Receiver"), unStackableCustomItem("industry/smb_receiver"))
	val SNIPER_RECEIVER = register("SNIPER_RECEIVER", text("Sniper Receiver"), unStackableCustomItem("industry/sniper_receiver"))
	val SHOTGUN_RECEIVER = register("SHOTGUN_RECEIVER", text("Shotgun Receiver"), unStackableCustomItem("industry/shotgun_receiver"))
	val CANNON_RECEIVER = register("CANNON_RECEIVER", text("Cannon Receiver"), unStackableCustomItem("industry/cannon_receiver"))

	// Minerals
	private fun registerRawOre(identifier: String, name: String, smeltingResult: Supplier<NewCustomItem>) = register(identifier, text("Raw ${name.replaceFirstChar { it.uppercase() }}"), stackableCustomItem(RAW_IRON, model = "mineral/raw_$name")).withComponent(CustomComponentType.SMELTABLE, SmeltableComponent(smeltingResult.map { it.constructItemStack() }))
	private fun registerOreIngot(identifier: String, name: String) = register(identifier, text("${name.replaceFirstChar { it.uppercase() }} Ingot"), stackableCustomItem(RAW_IRON, model = "mineral/$name"))
	private fun registerOreBlock(identifier: String, name: String, block: Supplier<CustomBlock>, smeltingResult: Supplier<NewCustomItem>) = register(CustomBlockItem(identifier, IRON_ORE, "mineral/${name}_ore", text("${name.replaceFirstChar { it.uppercase() }} Ore"), block)).withComponent(CustomComponentType.SMELTABLE, SmeltableComponent(smeltingResult.map { it.constructItemStack() }))
	private fun registerIngotBlock(identifier: String, name: String, block: Supplier<CustomBlock>) = register(CustomBlockItem(identifier, IRON_BLOCK, "mineral/${name}_block", text("${name.replaceFirstChar { it.uppercase() }} Block"), block))
	private fun registerRawBlock(identifier: String, name: String, block: Supplier<CustomBlock>) = register(CustomBlockItem(identifier, RAW_IRON_BLOCK, "mineral/raw_${name}_block", text("Raw ${name.replaceFirstChar { it.uppercase() }} Block"), block))

	val ALUMINUM_INGOT = registerOreIngot("ALUMINUM_INGOT", "aluminum")
	val RAW_ALUMINUM = registerRawOre("RAW_ALUMINUM", "aluminum", smeltingResult = ::ALUMINUM_INGOT)
	val ALUMINUM_ORE = registerOreBlock("ALUMINUM_ORE", "aluminum", block = CustomBlocks::ALUMINUM_ORE, smeltingResult = ::ALUMINUM_INGOT)
	val ALUMINUM_BLOCK = registerIngotBlock("ALUMINUM_BLOCK", "aluminum", block = CustomBlocks::ALUMINUM_BLOCK)
	val RAW_ALUMINUM_BLOCK = registerRawBlock("RAW_ALUMINUM_BLOCK", "aluminum", block = CustomBlocks::RAW_ALUMINUM_BLOCK)

	val CHETHERITE = registerOreIngot("CHETHERITE", "chetherite")
	val CHETHERITE_ORE = registerOreBlock("CHETHERITE_ORE", "chetherite", block = CustomBlocks::CHETHERITE_ORE, smeltingResult = ::CHETHERITE)
	val CHETHERITE_BLOCK = registerIngotBlock("CHETHERITE_BLOCK", "chetherite", block = CustomBlocks::CHETHERITE_BLOCK)

	val TITANIUM_INGOT = registerOreIngot("TITANIUM_INGOT", "titanium")
	val RAW_TITANIUM = registerRawOre("RAW_TITANIUM", "titanium", smeltingResult = ::TITANIUM_INGOT)
	val TITANIUM_ORE = registerOreBlock("TITANIUM_ORE", "titanium", block = CustomBlocks::TITANIUM_ORE, smeltingResult = ::TITANIUM_INGOT)
	val TITANIUM_BLOCK = registerIngotBlock("TITANIUM_BLOCK", "titanium", block = CustomBlocks::TITANIUM_BLOCK)
	val RAW_TITANIUM_BLOCK = registerRawBlock("RAW_TITANIUM_BLOCK", "titanium", block = CustomBlocks::RAW_TITANIUM_BLOCK)

	val URANIUM = registerOreIngot(identifier = "URANIUM", name = "uranium")
	val RAW_URANIUM = registerRawOre(identifier = "RAW_URANIUM", name = "uranium", smeltingResult = CustomItemRegistry::URANIUM)
	val URANIUM_ORE = registerOreBlock(identifier = "URANIUM_ORE", name = "uranium", block = CustomBlocks::URANIUM_ORE, smeltingResult = ::URANIUM)
	val URANIUM_BLOCK = registerIngotBlock(identifier = "URANIUM_BLOCK", name = "uranium", block = CustomBlocks::URANIUM_BLOCK)
	val RAW_URANIUM_BLOCK = registerRawBlock(identifier = "RAW_URANIUM_BLOCK", name = "uranium", block = CustomBlocks::RAW_URANIUM_BLOCK)



	init {
		sortCustomItemListeners()
	}

	fun <T : NewCustomItem> register(item: T): T {
		customItems[item.identifier] = item
		return item
	}

	fun register(identifier: String, displayName: Component, factory: ItemFactory): NewCustomItem {
		return register(NewCustomItem(identifier, displayName, factory))
	}

	val ItemStack.newCustomItem: NewCustomItem? get() {
		return customItems[persistentDataContainer.get(CUSTOM_ITEM, STRING) ?: return null]
	}

	val identifiers = customItems.keys

	fun getByIdentifier(identifier: String): NewCustomItem? = customItems[identifier]
}
