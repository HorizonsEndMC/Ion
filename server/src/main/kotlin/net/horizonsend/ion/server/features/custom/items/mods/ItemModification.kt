package net.horizonsend.ion.server.features.custom.items.mods

import net.horizonsend.ion.server.features.custom.NewCustomItem
import net.horizonsend.ion.server.features.custom.items.attribute.CustomItemAttribute
import net.kyori.adventure.text.Component
import org.bukkit.inventory.ItemStack
import java.util.function.Supplier
import kotlin.reflect.KClass

interface ItemModification {
	val identifier: String
	val displayName: Component
	val applicableTo: Array<KClass<out NewCustomItem>>
	val incompatibleWithMods: Array<KClass<out ItemModification>>
	val modItem: Supplier<ModificationItem?>

	val crouchingDisables: Boolean

	/** Logic to be run when this mod is added to a tool */
	fun onAdd(itemStack: ItemStack) {}

	/** Logic to be run when this mod is removed from a tool */
	fun onRemove(itemStack: ItemStack) {}

	fun getAttributes(): List<CustomItemAttribute>
}
