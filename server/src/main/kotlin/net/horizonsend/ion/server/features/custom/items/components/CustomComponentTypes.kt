package net.horizonsend.ion.server.features.custom.items.components

import io.papermc.paper.event.block.BlockPreDispenseEvent
import net.horizonsend.ion.server.features.custom.items.components.CustomItemComponentManager.ComponentType
import net.horizonsend.ion.server.features.custom.items.components.CustomItemComponentManager.ComponentTypeData
import net.horizonsend.ion.server.features.custom.items.components.CustomItemComponentManager.ComponentTypeData.AllowMultiple
import net.horizonsend.ion.server.features.custom.items.components.CustomItemComponentManager.ComponentTypeData.OnlyOne
import org.bukkit.event.entity.EntityShootBowEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.event.player.PlayerSwapHandItemsEvent

class CustomComponentTypes<T : CustomItemComponent, Z : ComponentTypeData<T>> private constructor(val storageType: ComponentType) {
	fun castData(customItemComponent: ComponentTypeData<*>): Z {
		@Suppress("UNCHECKED_CAST")
		return customItemComponent as Z
	}

	companion object {
		private inline fun <reified T : CustomItemComponent, Z : ComponentTypeData<T>> newComponentType(type: ComponentType): CustomComponentTypes<T, Z> {
			return CustomComponentTypes(type)
		}

		/**
		 * Allows an item to store ammunition.
		 **/
		val AMMUNITION = newComponentType<Ammunition, OnlyOne<Ammunition>>(ComponentType.ONLY_ONE)

		/**
		 * Specifies a magazine type for reloading
		 **/
		val MAGAZINE_TYPE = newComponentType<MagazineType<*>, OnlyOne<MagazineType<*>>>(ComponentType.ONLY_ONE)

		/**
		 * Allows an item to store items
		 **/
		val POWERED_ITEM = newComponentType<Power, OnlyOne<Power>>(ComponentType.ONLY_ONE)

		/**
		 * Allows an item to store items
		 **/
		val GAS_STORAGE = newComponentType<GasStorage, OnlyOne<GasStorage>>(ComponentType.ONLY_ONE)

		/**
		 * Allows an item to store items
		 **/
		val MODDED_ITEM = newComponentType<ModManager, OnlyOne<ModManager>>(ComponentType.ONLY_ONE)

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
		 * General interact listener
		 **/
		val LISTENER_DISPENSE = newComponentType<Listener<BlockPreDispenseEvent, *>, AllowMultiple<Listener<BlockPreDispenseEvent, *>>>(ComponentType.ALLOW_MULTIPLE)

		/**
		 * General interact listener
		 **/
		val LISTENER_ENTITY_SHOOT_BOW = newComponentType<Listener<EntityShootBowEvent, *>, AllowMultiple<Listener<EntityShootBowEvent, *>>>(ComponentType.ALLOW_MULTIPLE)
	}
}
