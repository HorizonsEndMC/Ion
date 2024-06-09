package net.horizonsend.ion.server.features.custom

import net.horizonsend.ion.server.command.admin.ConvertCommand
import net.horizonsend.ion.server.listener.SLEventListener
import org.bukkit.event.EventHandler
import org.bukkit.event.inventory.InventoryOpenEvent
import org.bukkit.event.player.PlayerJoinEvent

object ItemConverters : SLEventListener() {
	@EventHandler
	@Suppress("Unused")
	fun onInventoryOpen(event: InventoryOpenEvent) {
		val inventory = event.inventory
		for (item in inventory.filterNotNull()) {
			val newVersion = ConvertCommand.convertCustomMineral(item)

			if (newVersion != null) {
				item.type = newVersion.type
				item.itemMeta = newVersion.itemMeta
				item.amount = newVersion.amount
			}

			val possibleDrill = ConvertCommand.tryConvertDrill(item)

			if (possibleDrill != null) {
				item.type = possibleDrill.type
				item.itemMeta = possibleDrill.itemMeta
				item.amount = possibleDrill.amount
			}
		}
	}

	@EventHandler
	fun onPlayerLogin(event: PlayerJoinEvent) {
		val inventory = event.player.inventory
		for (item in inventory.filterNotNull()) {
			val newVersion = ConvertCommand.convertCustomMineral(item)

			if (newVersion != null) {
				item.type = newVersion.type
				item.itemMeta = newVersion.itemMeta
				item.amount = newVersion.amount

				event.player.updateInventory()
			}

			val possibleDrill = ConvertCommand.tryConvertDrill(item)

			if (possibleDrill != null) {
				item.type = possibleDrill.type
				item.itemMeta = possibleDrill.itemMeta
				item.amount = possibleDrill.amount

				event.player.updateInventory()
			}
		}
	}
}
