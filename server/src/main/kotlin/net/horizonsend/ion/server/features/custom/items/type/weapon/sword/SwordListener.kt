package net.horizonsend.ion.server.features.custom.items.type.weapon.sword

import net.horizonsend.ion.server.IonServer
import net.horizonsend.ion.server.configuration.ConfigurationFiles
import net.horizonsend.ion.server.core.IonServerComponent
import net.horizonsend.ion.server.core.registration.registries.CustomItemRegistry.Companion.customItem
import net.horizonsend.ion.server.features.custom.items.type.weapon.sword.EnergySword.Companion.energySwordAttributes
import net.horizonsend.ion.server.features.custom.items.type.weapon.sword.EnergySword.Companion.migrateSword
import net.horizonsend.ion.server.miscellaneous.utils.Tasks
import net.kyori.adventure.key.Key.key
import net.kyori.adventure.sound.Sound
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.Material.SHIELD
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.entity.EntityDamageEvent

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

	@EventHandler
	fun onPlayerDamage(event: EntityDamageEvent) {

		val damaged = event.entity
		if (damaged !is Player) return
		if (!damaged.isBlocking) return
		if ((damaged as? LivingEntity)?.activeItem?.customItem is EnergySword ) return
		if ((damaged as? LivingEntity)?.activeItem?.type != SHIELD) return
		if (damaged.getCooldown(SHIELD) != 0) return

		val velocity = damaged.velocity
		Tasks.syncDelay(1) { damaged.velocity = velocity }

		event.damage = 0.0
		val balancing = ConfigurationFiles.pvpBalancing.get().meleeWeapons.energySwordBalancing
		damaged.setCooldown(SHIELD, balancing.blockCooldownTime)
		damaged.clearActiveItem()
		damaged.setArrowsInBody(/* count = */ 0, /* fireEvent = */ false)
	}
}
