package net.horizonsend.ion.server.features.gear

import net.horizonsend.ion.server.IonServer
import net.horizonsend.ion.server.IonServerComponent
import net.horizonsend.ion.server.features.custom.items.CustomItemRegistry.customItem
import net.horizonsend.ion.server.features.custom.items.type.weapon.sword.EnergySword
import net.horizonsend.ion.server.miscellaneous.utils.Tasks
import org.bukkit.Bukkit

object Gear : IonServerComponent() {
	override fun onEnable() {
		// Energy sword idle sound
		// Use async task and while loop with thread sleep so when it lags it doesnt sound weird
		// The timing of the sounds is very important
		Tasks.async {
			while (IonServer.isEnabled) {
				Tasks.sync {
					for (player in Bukkit.getOnlinePlayers()) {
						val main = player.inventory.itemInMainHand
						val offhand = player.inventory.itemInOffHand

						val mainCustomItem = main.customItem
						val offhandCustomItem = offhand.customItem

						if (mainCustomItem != null && mainCustomItem is EnergySword ||
							offhandCustomItem != null && offhandCustomItem is EnergySword
						) {
							player.world.playSound(player.location, "horizonsend:energy_sword.idle", 5.0f, 1.0f)
						}
					}
				}

				try {
					Thread.sleep(2000)
				} catch (e: InterruptedException) {
					e.printStackTrace()
				}
			}
		}
	}
}
