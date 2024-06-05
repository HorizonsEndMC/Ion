package net.horizonsend.ion.server.features.custom.items.objects

import net.horizonsend.ion.server.features.custom.items.mods.ToolModRegistry
import net.horizonsend.ion.server.features.custom.items.mods.ToolModification
import net.horizonsend.ion.server.miscellaneous.registrations.NamespacedKeys.TOOL_MODIFICATIONS
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataAdapterContext
import org.bukkit.persistence.PersistentDataType

interface ModdedCustomItem {
	fun getMods(item: ItemStack): Array<ToolModification> = item.itemMeta.persistentDataContainer.getOrDefault(TOOL_MODIFICATIONS, ModList, arrayOf())

	companion object ModList : PersistentDataType<Array<String>, Array<ToolModification>> {
		override fun getComplexType(): Class<Array<ToolModification>> = Array<ToolModification>::class.java

		override fun getPrimitiveType(): Class<Array<String>> = Array<String>::class.java

		override fun toPrimitive(complex: Array<ToolModification>, context: PersistentDataAdapterContext): Array<String> {
			return Array(complex.size) { complex[it].identifier }
		}

		override fun fromPrimitive(primitive: Array<String>, context: PersistentDataAdapterContext): Array<ToolModification> {
			return Array(primitive.size) { ToolModRegistry[primitive[it]] }
		}
	}
}
