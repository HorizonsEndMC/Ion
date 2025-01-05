package net.horizonsend.ion.server.features.custom.items.component

import io.papermc.paper.event.block.BlockPreDispenseEvent
import net.horizonsend.ion.server.features.custom.items.component.CustomItemComponentManager.ComponentType
import net.horizonsend.ion.server.features.custom.items.component.CustomItemComponentManager.ComponentTypeData
import net.horizonsend.ion.server.features.custom.items.component.CustomItemComponentManager.ComponentTypeData.AllowMultiple
import net.horizonsend.ion.server.features.custom.items.component.CustomItemComponentManager.ComponentTypeData.OnlyOne
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.entity.EntityShootBowEvent
import org.bukkit.event.inventory.PrepareItemCraftEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.event.player.PlayerSwapHandItemsEvent

class CustomComponentTypes<T : CustomItemComponent, Z : ComponentTypeData<T>> private constructor(val storageType: ComponentType, val componentName: String) {
	fun castData(customItemComponent: ComponentTypeData<*>): Z {
		@Suppress("UNCHECKED_CAST")
		return customItemComponent as Z
	}

	override fun toString(): String {
		return "CustomItemComponentType[$componentName]"
	}

	companion object {
		private inline fun <reified T : CustomItemComponent, Z : ComponentTypeData<T>> newComponentType(type: ComponentType): CustomComponentTypes<T, Z> {
			return CustomComponentTypes(type, T::class.simpleName ?: "Anonymous")
		}

		/**
		 * Specifies a magazine type for reloading
		 **/
		val MAGAZINE_TYPE = newComponentType<MagazineType<*>, OnlyOne<MagazineType<*>>>(ComponentType.ONLY_ONE)

		/**
		 * Allows an item to store ammunition.
		 **/
		val AMMUNITION_STORAGE = newComponentType<AmmunitionStorage, OnlyOne<AmmunitionStorage>>(ComponentType.ONLY_ONE)

		/**
		 * Allows an item to store power
		 **/
		val POWER_STORAGE = newComponentType<PowerStorage, OnlyOne<PowerStorage>>(ComponentType.ONLY_ONE)

		/**
		 * Allows an item to store gas
		 **/
		val GAS_STORAGE = newComponentType<GasStorage, OnlyOne<GasStorage>>(ComponentType.ONLY_ONE)

		/**
		 * Allows an item to have / store item modifications
		 **/
		val MOD_MANAGER = newComponentType<ModManager, OnlyOne<ModManager>>(ComponentType.ONLY_ONE)

		/**
		 * Specifiies a result from smelting this item
		 **/
		val SMELTABLE = newComponentType<Smeltable, OnlyOne<Smeltable>>(ComponentType.ONLY_ONE)

		/**
		 * General interact listener
		 **/
		val LISTENER_PLAYER_INTERACT = newComponentType<Listener<PlayerInteractEvent, *>, AllowMultiple<Listener<PlayerInteractEvent, *>>>(ComponentType.ALLOW_MULTIPLE)

		/**
		 * General interact listener
		 **/
		val LISTENER_PLAYER_SWAP_HANDS = newComponentType<Listener<PlayerSwapHandItemsEvent, *>, AllowMultiple<Listener<PlayerSwapHandItemsEvent, *>>>(ComponentType.ALLOW_MULTIPLE)

		/**
		 * Called when this item is dispensed
		 **/
		val LISTENER_DISPENSE = newComponentType<Listener<BlockPreDispenseEvent, *>, AllowMultiple<Listener<BlockPreDispenseEvent, *>>>(ComponentType.ALLOW_MULTIPLE)

		/**
		 * Calls when someone attempts to craft this item
		 **/
		val LISTENER_PREPARE_CRAFT = newComponentType<Listener<PrepareItemCraftEvent, *>, AllowMultiple<Listener<PrepareItemCraftEvent, *>>>(ComponentType.ALLOW_MULTIPLE)

		/**
		 * Called when someone is damaged by this item
		 **/
		val LISTENER_DAMAGE_ENTITY = newComponentType<Listener<EntityDamageByEntityEvent, *>, AllowMultiple<Listener<EntityDamageByEntityEvent, *>>>(ComponentType.ALLOW_MULTIPLE)

		/**
		 * Called when someone is damaged while holding this item
		 **/
		val LISTENER_DAMAGED_HOLDING = newComponentType<Listener<EntityDamageByEntityEvent, *>, AllowMultiple<Listener<EntityDamageByEntityEvent, *>>>(ComponentType.ALLOW_MULTIPLE)

		/**
		 * General interact listener
		 **/
		val LISTENER_ENTITY_SHOOT_BOW = newComponentType<Listener<EntityShootBowEvent, *>, AllowMultiple<Listener<EntityShootBowEvent, *>>>(ComponentType.ALLOW_MULTIPLE)

		/**
		 * Recieves ticks, idk what else to say
		 **/
		val TICK_RECIEVER = newComponentType<TickRecievierModule, AllowMultiple<TickRecievierModule>>(ComponentType.ALLOW_MULTIPLE)
	}
}
