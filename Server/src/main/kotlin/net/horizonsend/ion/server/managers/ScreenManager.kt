package net.horizonsend.ion.server.managers

import net.horizonsend.ion.server.screens.Screen
import org.bukkit.entity.HumanEntity
import org.bukkit.inventory.Inventory

object ScreenManager {
	private val screens = mutableMapOf<HumanEntity, Screen>()

	fun HumanEntity.openScreen(screen: Screen) {
		screens[this] = screen
		openInventory(screen.inventory)
	}

	fun HumanEntity.closeScreen(): Screen? {
		val screen = screens.remove(this)

		screen?.inventory?.close()

		return screen
	}

	val HumanEntity.isInScreen: Boolean get() = screens.containsKey(this)

	val Inventory.isScreen: Boolean get() = screens.filter { it.value.inventory == this }.isNotEmpty()
}