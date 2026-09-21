package net.horizonsend.ion.server.features.custom.items.type.weapon.sword

import net.horizonsend.ion.common.database.schema.nations.Nation
import net.horizonsend.ion.common.utils.miscellaneous.randomInt
import net.horizonsend.ion.server.IonServer
import net.horizonsend.ion.server.core.IonServerComponent
import net.horizonsend.ion.server.core.registration.registries.CustomItemRegistry.Companion.customItem
import net.horizonsend.ion.server.features.cache.PlayerCache
import net.horizonsend.ion.server.features.custom.items.type.weapon.blaster.Blaster
import net.horizonsend.ion.server.features.world.IonWorld.Companion.hasFlag
import net.horizonsend.ion.server.features.world.WorldFlag
import net.horizonsend.ion.server.miscellaneous.utils.Tasks
import net.kyori.adventure.text.minimessage.MiniMessage
import org.bukkit.Bukkit
import org.bukkit.event.EventHandler
import org.bukkit.event.entity.PlayerDeathEvent
import kotlin.math.roundToInt

object SwordListener : IonServerComponent() {
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
							player.world.playSound(player.location, "horizonsend:energy_sword.idle", 3.0f, 1.0f)
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
