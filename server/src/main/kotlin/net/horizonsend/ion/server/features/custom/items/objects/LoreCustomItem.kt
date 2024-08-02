package net.horizonsend.ion.server.features.custom.items.objects

import net.horizonsend.ion.server.features.custom.items.objects.ModdedCustomItem.ModLoreManager.setLine
import net.horizonsend.ion.server.miscellaneous.utils.Tasks
import net.horizonsend.ion.server.miscellaneous.utils.updateMeta
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.Component.empty
import org.bukkit.inventory.ItemStack

/**
 * For custom items that may need to juggle multiple lore entries.
 * Individual interfaces may use this to cause others to be regenerated upon changes
 **/
interface LoreCustomItem {
	/**
	 * Gets the list of relevant lore managers to this item
	 *
	 * Lore will be prioritized in the order of the managers.
	 **/
	fun getLoreManagers(): List<CustomItemLoreManager>

	/**
	 * Rebuilds the entire lore of the item, from each lore manager
	 **/
	fun rebuildLore(itemStack: ItemStack, asTask: Boolean = true) {
		val managers = getLoreManagers()

		val lore = mutableListOf<Component>()

		val iterator = managers.iterator()

		var index = 0
		while (iterator.hasNext()) {
			val manager = iterator.next()
			val lines = manager.getLineAllotment(itemStack)

			for (lineIndex in 0..< lines) {
				setLine(lore, index, manager.rebuildLine(itemStack, lineIndex))
				index++
			}

			// Add separator
			if (iterator.hasNext()) {
				setLine(lore, index, empty())
				index++
			}
		}

		if (asTask) Tasks.sync { itemStack.updateMeta { it.lore(lore) } } else itemStack.updateMeta { it.lore(lore) }
	}

	/**
	 * Indicates that a single line of lore needs changing
	 **/
	fun lineChanged(line: Int, itemStack: ItemStack) {
		var currentIndex = 0

		val manager = getLoreManagers().firstOrNull {
			val lastIndex = currentIndex + it.getLineAllotment(itemStack)

			if ((currentIndex..lastIndex).contains(line)) return@firstOrNull true

			currentIndex = (lastIndex + 1) // Add the separator index

			false
		} ?: return

		// If escaped, the current index will be the start of the range of the manager
		val localIndex = line - currentIndex

		Tasks.sync { itemStack.updateMeta {
			val oldLore = it.lore() ?: mutableListOf()
			setLine(oldLore, line, manager.rebuildLine(itemStack, localIndex))

			it.lore(oldLore)
		} }
	}

	abstract class CustomItemLoreManager {
		/**
		 * Returns the amount of lines that the lore of this item should take
		 **/
		abstract fun getLineAllotment(itemStack: ItemStack): Int

		/**
		 * Rebuilds this item's section of the lore
		 **/
		abstract fun rebuildLine(itemStack: ItemStack, line: Int): Component

		fun setLine(lore: MutableList<Component>, index: Int, inserted: Component) {
			if (lore.isEmpty() || index > lore.lastIndex) {
				lore.add(inserted)
			}

			lore[index] = inserted
		}

		/**
		 * Inserts a line into the lore at the specified index.
		 * If it inside the current lore, the old lore will be shifted downwards to make room.
		 * If it is outside the current lore, empty lines will be added to fill the gap.
		 **/
		fun upsertLine(lore: MutableList<Component>, insertionIndex: Int, inserted: Component) {
			if (lore.isEmpty()) return

			if (insertionIndex >= lore.size) {
				for (newIndex in lore.size..< insertionIndex) {
					lore.add(empty())
				}

				lore.add(inserted)

				return
			}

			val previousLastIndex = lore.lastIndex

			lore.add(lore.size, lore.last())

			for (index in previousLastIndex downTo insertionIndex) {
				lore[index] = lore[index - 1]
			}

			lore[insertionIndex] = inserted
		}
	}
}
