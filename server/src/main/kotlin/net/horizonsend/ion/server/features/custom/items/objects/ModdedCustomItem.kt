package net.horizonsend.ion.server.features.custom.items.objects

import com.manya.pdc.base.array.StringArrayDataType
import net.horizonsend.ion.common.utils.text.ITALIC
import net.horizonsend.ion.common.utils.text.colors.HEColorScheme.Companion.HE_DARK_GRAY
import net.horizonsend.ion.common.utils.text.colors.HEColorScheme.Companion.HE_LIGHT_GRAY
import net.horizonsend.ion.common.utils.text.colors.HEColorScheme.Companion.HE_MEDIUM_GRAY
import net.horizonsend.ion.common.utils.text.ofChildren
import net.horizonsend.ion.server.features.custom.items.CustomItems.customItem
import net.horizonsend.ion.server.features.custom.items.mods.ItemModRegistry
import net.horizonsend.ion.server.features.custom.items.mods.ItemModification
import net.horizonsend.ion.server.features.custom.items.mods.ToolModMenu
import net.horizonsend.ion.server.miscellaneous.registrations.NamespacedKeys.TOOL_MODIFICATIONS
import net.horizonsend.ion.server.miscellaneous.utils.updateMeta
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.Component.empty
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.format.TextDecoration
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataAdapterContext
import org.bukkit.persistence.PersistentDataType
import java.nio.charset.Charset

interface ModdedCustomItem : LoreCustomItem {
	val displayName: Component
	val modLimit: Int

	fun getMods(item: ItemStack): Array<ItemModification> = item.itemMeta.persistentDataContainer.getOrDefault(TOOL_MODIFICATIONS, ModList, arrayOf())

	fun setMods(item: ItemStack, mods: Array<ItemModification>) {
		item.updateMeta {
			it.persistentDataContainer.set(TOOL_MODIFICATIONS, ModList, mods)
		}

		rebuildLore(item)
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

	fun openMenu(player: Player, item: ItemStack) {
		ToolModMenu.create(player, item, this).open()
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

	object ModLoreManager : LoreCustomItem.CustomItemLoreManager() {
		private val modPrefix = text("Mods (limit: ", HE_MEDIUM_GRAY)
		private val namePrefix = text(" • ", HE_DARK_GRAY).decoration(ITALIC, false)

		override fun getLineAllotment(itemStack: ItemStack): Int {
			val custom = itemStack.customItem as? ModdedCustomItem ?: return 0
			val mods = custom.getMods(itemStack)

			return mods.size + 1
		}

		override fun rebuildLine(itemStack: ItemStack, line: Int): Component {
			val custom = itemStack.customItem as? ModdedCustomItem ?: return empty()
			val mods = custom.getMods(itemStack)

			return when	(line) {
				0 -> ofChildren(modPrefix, text(custom.modLimit, HE_LIGHT_GRAY), text("):", HE_MEDIUM_GRAY)).decoration(TextDecoration.ITALIC, false)
				else -> ofChildren(namePrefix, mods[line - 1].displayName)
			}
		}
	}
}
