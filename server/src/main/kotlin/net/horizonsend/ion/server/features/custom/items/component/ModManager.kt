package net.horizonsend.ion.server.features.custom.items.component

import com.manya.pdc.base.array.StringArrayDataType
import net.horizonsend.ion.common.utils.text.ITALIC
import net.horizonsend.ion.common.utils.text.colors.HEColorScheme.Companion.HE_DARK_GRAY
import net.horizonsend.ion.common.utils.text.colors.HEColorScheme.Companion.HE_LIGHT_GRAY
import net.horizonsend.ion.common.utils.text.colors.HEColorScheme.Companion.HE_MEDIUM_GRAY
import net.horizonsend.ion.common.utils.text.ofChildren
import net.horizonsend.ion.server.features.custom.items.CustomItem
import net.horizonsend.ion.server.features.custom.items.attribute.CustomItemAttribute
import net.horizonsend.ion.server.features.custom.items.type.tool.mods.ItemModRegistry
import net.horizonsend.ion.server.features.custom.items.type.tool.mods.ItemModification
import net.horizonsend.ion.server.features.custom.items.util.serialization.SerializationManager
import net.horizonsend.ion.server.features.custom.items.util.serialization.token.ItemModificationToken
import net.horizonsend.ion.server.features.custom.items.util.serialization.token.ListToken
import net.horizonsend.ion.server.miscellaneous.registrations.NamespacedKeys.TOOL_MODIFICATIONS
import net.horizonsend.ion.server.miscellaneous.utils.text.itemLore
import net.horizonsend.ion.server.miscellaneous.utils.updatePersistentDataContainer
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.Component.text
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataAdapterContext
import org.bukkit.persistence.PersistentDataType
import java.nio.charset.Charset

class ModManager(val maxMods: Int) : CustomItemComponent, LoreManager {
	override fun shouldIncludeSeparator(): Boolean = true
	override val priority: Int = 50

	override fun getLines(customItem: CustomItem, itemStack: ItemStack): List<Component> {
		val list = mutableListOf(ofChildren(modPrefix, text(maxMods, HE_LIGHT_GRAY), text("):", HE_MEDIUM_GRAY)).itemLore)

		val mods = getMods(itemStack)
		mods.forEach { list.add(ofChildren(namePrefix, it.displayName).itemLore) }

		return list
	}

	override fun decorateBase(baseItem: ItemStack, customItem: CustomItem) {
		setMods(baseItem, customItem, arrayOf())
	}

	override fun getAttributes(baseItem: ItemStack): Iterable<CustomItemAttribute> {
		val mods = getMods(baseItem)
		return mods.flatMap { it.getAttributes() }
	}

	fun getMods(item: ItemStack): Array<ItemModification> = item.itemMeta.persistentDataContainer.getOrDefault(TOOL_MODIFICATIONS, ModManager, arrayOf())

	fun setMods(item: ItemStack, customItem: CustomItem, mods: Array<ItemModification>) {
		item.updatePersistentDataContainer { set(TOOL_MODIFICATIONS, ModList, mods) }

		customItem.refreshLore(item)
	}

	fun addMod(item: ItemStack, customItem: CustomItem, mod: ItemModification): Boolean {
		val mods = getMods(item)
		if (mods.contains(mod)) return false

		val without = mods.toMutableList()
		without.add(mod)

		setMods(item, customItem, without.toTypedArray())
		return true
	}

	fun removeMod(item: ItemStack, customItem: CustomItem, mod: ItemModification): Boolean {
		val mods = getMods(item)
		if (!mods.contains(mod)) return false

		val without = mods.toMutableList()
		without.remove(mod)

		setMods(item, customItem, without.toTypedArray())
		return true
	}

	fun openMenu(player: Player, customItem: CustomItem, item: ItemStack) {
		net.horizonsend.ion.server.features.custom.items.type.tool.mods.ToolModMenu.create(player, item, customItem, this).open()
	}

	companion object ModList : PersistentDataType<ByteArray, Array<ItemModification>> {
		private val modPrefix = text("Mods (limit: ", HE_MEDIUM_GRAY)
		private val namePrefix = text(" • ", HE_DARK_GRAY).decoration(ITALIC, false)

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

	override fun registerSerializers(serializationManager: SerializationManager) {
		serializationManager.addSerializedData(
			"mods",
			ListToken(ItemModificationToken()),
			{ customItem, itemStack -> customItem.getComponent(CustomComponentTypes.MOD_MANAGER).getMods(itemStack).toList() },
			{ customItem: CustomItem, itemStack: ItemStack, data: List<ItemModification> -> customItem.getComponent(CustomComponentTypes.MOD_MANAGER).setMods(itemStack, customItem, data.toTypedArray()) }
		)
	}
}
