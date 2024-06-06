package net.horizonsend.ion.server.features.custom.items.objects

import com.manya.pdc.base.array.StringArrayDataType
import net.horizonsend.ion.server.features.custom.items.mods.ItemModRegistry
import net.horizonsend.ion.server.features.custom.items.mods.ItemModification
import net.horizonsend.ion.server.miscellaneous.registrations.NamespacedKeys.TOOL_MODIFICATIONS
import net.horizonsend.ion.server.miscellaneous.utils.updateMeta
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataAdapterContext
import org.bukkit.persistence.PersistentDataType
import java.nio.charset.Charset

interface ModdedCustomItem {
	fun getMods(item: ItemStack): Array<ItemModification> = item.itemMeta.persistentDataContainer.getOrDefault(TOOL_MODIFICATIONS, ModList, arrayOf())
	fun setMods(item: ItemStack, mods: Array<ItemModification>) = item.updateMeta {
		it.persistentDataContainer.set(TOOL_MODIFICATIONS, ModList, mods)
	}

	fun addMod(item: ItemStack, mod: ItemModification): Boolean {
		val mods = getMods(item)
		if (mods.contains(mod)) return false

		val without = mods.toMutableList()
		without.add(mod)

		setMods(item, without.toTypedArray())
		return true
	}

	fun removeMod(item: ItemStack, mod: ItemModification): Boolean {
		val mods = getMods(item)
		if (!mods.contains(mod)) return false

		val without = mods.toMutableList()
		without.remove(mod)

		setMods(item, without.toTypedArray())
		return true
	}

	companion object ModList : PersistentDataType<ByteArray, Array<ItemModification>> {
		private val stringArrayType = StringArrayDataType(Charset.defaultCharset())

		override fun getComplexType(): Class<Array<ItemModification>> = Array<ItemModification>::class.java

		override fun getPrimitiveType(): Class<ByteArray> = ByteArray::class.java

		override fun toPrimitive(complex: Array<ItemModification>, context: PersistentDataAdapterContext): ByteArray {
			return stringArrayType.toPrimitive(Array(complex.size) { complex[it].identifier }, context)
		}

		override fun fromPrimitive(primitive: ByteArray, context: PersistentDataAdapterContext): Array<ItemModification> {
			val stringArray = stringArrayType.fromPrimitive(primitive, context)

			return Array(stringArray.size) {
				ItemModRegistry[stringArray[it]]!!
			}
		}
	}
}
