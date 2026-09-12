package net.horizonsend.ion.server.features.custom.items.type.weapon.sword

import net.horizonsend.ion.common.database.schema.nations.Nation
import net.horizonsend.ion.common.utils.miscellaneous.randomInt
import net.horizonsend.ion.server.IonServer
import net.horizonsend.ion.server.core.IonServerComponent
import net.horizonsend.ion.server.core.registration.registries.CustomItemRegistry.Companion.customItem
import net.horizonsend.ion.server.features.cache.PlayerCache
import net.horizonsend.ion.server.features.custom.items.type.weapon.blaster.Blaster
import net.horizonsend.ion.server.features.starship.control.signs.map.planetInRange
import net.horizonsend.ion.server.features.world.IonWorld.Companion.hasFlag
import net.horizonsend.ion.server.features.world.IonWorld.Companion.ion
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

	@EventHandler
	fun onDeath(event: PlayerDeathEvent) {
		val victim = event.player
		val killer = event.entity.killer ?: return
		val customItem = killer.inventory.itemInMainHand.customItem ?: return

		if (customItem !is Blaster<*>) return

		val arena: String = if (killer.world.hasFlag(WorldFlag.ARENA)) "<#555555>[<#ffff66>Arena<#555555>]<reset> " else ""

		val blaster = customItem.displayName
		val victimColor = if (victim.hasMetadata("NPC")) "<#FFFFFF>" else "<#" + Integer.toHexString((PlayerCache[victim].nationOid?.let { Nation.findById(it) }?.color ?: 16777215)) + ">"

		val killerColor = "<#" + Integer.toHexString((PlayerCache[killer].nationOid?.let { Nation.findById(it) }?.color ?: 16777215)) + ">"

		val distance = killer.location.distance(victim.location)
		val verb = when(randomInt(0, 32)){
			0-> "cut down"
			1-> "kebabed"
			2-> "stabbed"
			3-> "sliced"
			4-> "mauled"
			5-> "slain"
			6-> "pierced"
			7-> "slashed"
			8-> "clobbered"
			9-> "poked"
			10-> "felled"
			11-> "wrecked"
			12-> "cleaved"
			13-> "discombobulated"
			14-> "bamboozled"
			15-> "clowned on"
			16-> "diced"
			17-> "skewered"
			18-> "trashed"
			19-> "whacked"
			20-> "bested"
			21-> "executed"
			22-> "knocked out"
			23-> "killed"
			24-> "butchered"
			25-> "carved"
			26-> "vanquished"
			27-> "dispatched"
			28-> "gutted"
			29-> "destroyed"
			30-> "eliminated"
			31-> "smoked"
			32-> "neutralised"
			else -> "deezed" //should never happen
		}
		val newMessage = MiniMessage.miniMessage()
			.deserialize(
				"$arena$victimColor${victim.name}<reset> was $verb by $killerColor${killer.name}<reset> from ${distance.roundToInt()} blocks away, using "
			)
			.append(blaster)

		event.deathMessage(newMessage)
	}
}
