package net.horizonsend.ion.server.features.gear

import net.horizonsend.ion.server.IonServer
import net.horizonsend.ion.server.IonServerComponent
import net.horizonsend.ion.server.features.gear.powerarmor.PowerArmorManager
import net.horizonsend.ion.server.features.misc.CustomItems
import net.horizonsend.ion.server.miscellaneous.utils.Tasks
import org.bukkit.Bukkit

object Gear : IonServerComponent() {
	override fun onEnable() {
		PowerArmorManager.init()

		// Energy sword idle sound
		// Use async task and while loop with thread sleep so when it lags it doesnt sound weird
		// The timing of the sounds is very important
		Tasks.async {
			while (IonServer.isEnabled) {
				Tasks.sync {
					for (player in Bukkit.getOnlinePlayers()) {
						val main = player.inventory.itemInMainHand
						val offhand = player.inventory.itemInOffHand

						val mainCustomItem = CustomItems[main]
						val offhandCustomItem = CustomItems[offhand]

						if (mainCustomItem != null && mainCustomItem.id.contains("sword") ||
							offhandCustomItem != null && offhandCustomItem.id.contains("sword")
						) {
							player.world.playSound(player.location, "energy_sword.idle", 5.0f, 1.0f)
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
