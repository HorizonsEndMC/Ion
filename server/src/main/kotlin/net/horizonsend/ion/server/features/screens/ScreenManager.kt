package net.horizonsend.ion.server.features.screens

import org.bukkit.entity.Player
import org.bukkit.inventory.Inventory

object ScreenManager {
	private val screens = mutableMapOf<Player, Screen>()

	fun Player.openScreen(screen: Screen) {
		screens[this] = screen
		openInventory(screen.inventory)
	}

	fun Player.closeScreen(): Screen? {
		val screen = screens.remove(this)

		screen?.inventory?.close()

		return screen
	}

	val Player.isInScreen: Boolean get() = screens.containsKey(this)
	val Player.screen: Screen? get() = screens[this]

	val Inventory.isScreen: Boolean get() = screens.filter { it.value.inventory == this }.isNotEmpty()
}
