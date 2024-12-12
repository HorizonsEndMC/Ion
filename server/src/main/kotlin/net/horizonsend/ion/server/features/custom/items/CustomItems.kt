package net.horizonsend.ion.server.features.custom.items

import net.horizonsend.ion.common.utils.text.ofChildren
import net.horizonsend.ion.server.configuration.ConfigurationFiles
import net.horizonsend.ion.server.configuration.PVPBalancingConfiguration
import net.horizonsend.ion.server.features.custom.blocks.CustomBlock
import net.horizonsend.ion.server.features.custom.items.throwables.ThrowableCustomItem
import net.horizonsend.ion.server.features.custom.items.throwables.ThrownCustomItem
import net.horizonsend.ion.server.features.custom.items.throwables.ThrownPumpkinGrenade
import net.horizonsend.ion.server.features.custom.items.throwables.thrown.ThrownDetonator
import net.horizonsend.ion.server.features.custom.items.throwables.thrown.ThrownSmokeGrenade
import net.horizonsend.ion.server.miscellaneous.registrations.NamespacedKeys.CUSTOM_ITEM
import net.horizonsend.ion.server.miscellaneous.utils.text.itemName
import net.horizonsend.ion.server.miscellaneous.utils.updateMeta
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.format.NamedTextColor.BLUE
import net.kyori.adventure.text.format.NamedTextColor.DARK_GREEN
import net.kyori.adventure.text.format.NamedTextColor.GOLD
import net.kyori.adventure.text.format.NamedTextColor.GRAY
import net.kyori.adventure.text.format.NamedTextColor.GREEN
import net.kyori.adventure.text.format.NamedTextColor.LIGHT_PURPLE
import net.kyori.adventure.text.format.NamedTextColor.RED
import net.kyori.adventure.text.format.NamedTextColor.YELLOW
import net.kyori.adventure.text.format.TextDecoration.ITALIC
import org.bukkit.Material
import org.bukkit.Material.IRON_INGOT
import org.bukkit.Material.WARPED_FUNGUS_ON_A_STICK
import org.bukkit.entity.Entity
import org.bukkit.entity.Item
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType.STRING
import java.util.function.Supplier

@Suppress("unused")
object CustomItems {
	// If we want to be extra fancy we can replace this with some fastutils thing later .
	val ALL get() = customItems.values
	private val customItems: MutableMap<String, CustomItem> = mutableMapOf()

	// Gas Canisters Start
	val GAS_CANISTER_EMPTY = registerSimpleUnstackable("GAS_CANISTER_EMPTY", 1000, text("Empty Gas Canister"))
	val GAS_CANISTER_HYDROGEN = register(GasCanister("GAS_CANISTER_HYDROGEN", 1001, canisterName(text("Hydrogen", RED)),"HYDROGEN"))
	val GAS_CANISTER_NITROGEN = register(GasCanister("GAS_CANISTER_NITROGEN", 1002, canisterName(text("Nitrogen", RED)), "NITROGEN"))
	val GAS_CANISTER_METHANE = register(GasCanister("GAS_CANISTER_METHANE", 1003, canisterName(text("Methane", RED)), "METHANE"))
	val GAS_CANISTER_OXYGEN = register( GasCanister("GAS_CANISTER_OXYGEN", 1010, canisterName(text("Oxygen", YELLOW)), "OXYGEN"))
	val GAS_CANISTER_CHLORINE = register(GasCanister("GAS_CANISTER_CHLORINE", 1011, canisterName(text("Chlorine", YELLOW)), "CHLORINE"))
	val GAS_CANISTER_FLUORINE = register(GasCanister("GAS_CANISTER_FLUORINE", 1012, canisterName(text("Fluorine", YELLOW)), "FLUORINE"))
	val GAS_CANISTER_HELIUM = register(GasCanister("GAS_CANISTER_HELIUM", 1020, canisterName(text("Helium", BLUE)), "HELIUM"))
	val GAS_CANISTER_CARBON_DIOXIDE = register(GasCanister("GAS_CANISTER_CARBON_DIOXIDE", 1021, canisterName(text("Carbon Dioxide", BLUE)), "CARBON_DIOXIDE"))

	private fun canisterName(gasName: Component): Component = text()
		.decoration(ITALIC, false)
		.append(gasName)
		.append(text(" Gas Canister", GRAY))
		.build()

	// Gas Canisters End

	// Throwables start
	private fun registerThrowable(identifier: String, customModelData: Int, displayName: Component, balancing: Supplier<PVPBalancingConfiguration.Throwables.ThrowableBalancing>, thrown: (Item, Int, Entity?) -> ThrownCustomItem) =
		register(object : ThrowableCustomItem(identifier = identifier, customModelData = customModelData, displayName = displayName, balancingSupplier = balancing) {
			override fun constructThrownRunnable(item: Item, maxTicks: Int, damageSource: Entity?): ThrownCustomItem = thrown.invoke(item, maxTicks, damageSource)
		})

	val DETONATOR = registerThrowable("DETONATOR", 1101, ofChildren(text("Thermal ", RED), text("Detonator", GRAY)).itemName, ConfigurationFiles.pvpBalancing().throwables::detonator) { item, maxTicks, source -> ThrownDetonator(item, maxTicks, source, ConfigurationFiles.pvpBalancing().throwables::detonator) }
	val SMOKE_GRENADE = registerThrowable("SMOKE_GRENADE", 1102, ofChildren(text("Smoke ", DARK_GREEN), text("Grenade", GRAY)).itemName, ConfigurationFiles.pvpBalancing().throwables::smokeGrenade) { item, maxTicks, source -> ThrownSmokeGrenade(item, maxTicks, source) }

	val PUMPKIN_GRENADE = register(object : ThrowableCustomItem("PUMPKIN_GRENADE", 0, ofChildren(text("Pumpkin ", GOLD), text("Grenade", GREEN)).itemName, ConfigurationFiles.pvpBalancing().throwables::detonator) {
		override fun constructItemStack(): ItemStack = super.constructItemStack().apply { type = Material.PUMPKIN }.updateMeta { it.lore(mutableListOf(text("Spooky", LIGHT_PURPLE))) }
		override fun constructThrownRunnable(item: Item, maxTicks: Int, damageSource: Entity?): ThrownCustomItem = ThrownPumpkinGrenade(item, maxTicks, damageSource, ConfigurationFiles.pvpBalancing().throwables::detonator)
	})
	// Throwables end
	// Tools begin

// Tools end

	// This is just a convenient alias for items that don't do anything or are placeholders.
	private fun registerSimpleUnstackable(identifier: String, customModelData: Int, displayName: Component): CustomItem = register(object : CustomItem(identifier) {
		override fun constructItemStack(): ItemStack {
			val formattedDisplayName = text()
				.decoration(ITALIC, false)
				.append(displayName)
				.build()

			return ItemStack(WARPED_FUNGUS_ON_A_STICK).updateMeta {
				it.setCustomModelData(customModelData)
				it.displayName(formattedDisplayName)
				it.persistentDataContainer.set(CUSTOM_ITEM, STRING, identifier)
			}
		}
	})

	private fun registerSimpleStackable(identifier: String, baseItem: Material = IRON_INGOT, customModelData: Int, displayName: Component): CustomItem = register(object : CustomItem(identifier) {
		override fun constructItemStack(): ItemStack {
			val formattedDisplayName = text()
				.decoration(ITALIC, false)
				.append(displayName)
				.build()

			return ItemStack(baseItem).updateMeta {
				it.setCustomModelData(customModelData)
				it.displayName(formattedDisplayName)
				it.persistentDataContainer.set(CUSTOM_ITEM, STRING, identifier)
			}
		}
	})

	private fun registerCustomBlockItem(identifier: String, baseBlock: Material, customModelData: Int, displayName: Component, customBlock: Supplier<CustomBlock>): CustomBlockItem {
		val formattedDisplayName = text()
			.decoration(ITALIC, false)
			.append(displayName)
			.build()

		return CustomBlockItem(identifier, baseBlock, "null", formattedDisplayName, customBlock)
	}

	private fun <T : CustomItem> register(customItem: T): T {
		customItems[customItem.identifier] = customItem
		return customItem
	}

	val ItemStack.customItem: CustomItem?
		get() {
			return customItems[itemMeta?.persistentDataContainer?.get(CUSTOM_ITEM, STRING) ?: return null]
		}

	val identifiers = customItems.keys

	fun getByIdentifier(identifier: String): CustomItem? = customItems[identifier]
}
