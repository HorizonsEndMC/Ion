package net.horizonsend.ion.server.features.custom.items.type.tool.mods

import net.horizonsend.ion.server.core.registries.IonRegistryKey
import net.horizonsend.ion.server.core.registries.keys.Keyed
import net.horizonsend.ion.server.features.custom.items.CustomItem
import net.horizonsend.ion.server.features.custom.items.attribute.CustomItemAttribute
import net.kyori.adventure.text.Component
import org.bukkit.inventory.ItemStack
import kotlin.reflect.KClass

interface ItemModification : Keyed<ItemModification> {
	override val key: IonRegistryKey<ItemModification, out ItemModification>
	val displayName: Component
	val applicationPredicates: Array<ApplicationPredicate>
	val incompatibleWithMods: Array<KClass<out ItemModification>>
	val modItem: IonRegistryKey<CustomItem, out CustomItem>?

	val crouchingDisables: Boolean

	/** Logic to be run when this mod is added to a tool */
	fun onAdd(itemStack: ItemStack) {}

	/** Logic to be run when this mod is removed from a tool */
	fun onRemove(itemStack: ItemStack) {}

	fun getAttributes(): List<CustomItemAttribute>
}
