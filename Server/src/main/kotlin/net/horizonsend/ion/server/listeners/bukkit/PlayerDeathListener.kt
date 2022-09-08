package net.horizonsend.ion.server.listeners.bukkit

import java.lang.System.currentTimeMillis
import java.util.UUID
import net.horizonsend.ion.common.database.Achievement
import net.horizonsend.ion.server.annotations.BukkitListener
import net.horizonsend.ion.server.utilities.rewardAchievement
import net.kyori.adventure.text.minimessage.MiniMessage.miniMessage
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.entity.PlayerDeathEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.SkullMeta

@BukkitListener
@Suppress("Unused")
class PlayerDeathListener : Listener {
	private val cooldowns = mutableMapOf<UUID, Long>()

	@EventHandler(priority = EventPriority.MONITOR)
	fun onPlayerDeathEvent(event: PlayerDeathEvent) {
		if (event.entity.killer !is Player) return

		val headCooldownEnd = cooldowns.getOrDefault(event.player.uniqueId, 0)
		cooldowns[event.player.uniqueId] = currentTimeMillis() + 1000 * 60 * 10
		if (headCooldownEnd > currentTimeMillis()) return

		val head = ItemStack(Material.PLAYER_HEAD)

		head.editMeta(SkullMeta::class.java) {
			it.displayName(event.entity.name())
			it.owningPlayer = event.entity
			it.lore(
				listOf(
					miniMessage().deserialize("<#ff8888>Was killed by ${event.entity.killer!!.name}.")
				)
			)
		}

		event.entity.world.dropItem(event.entity.location, head)
		event.player.killer!!.rewardAchievement(Achievement.KILL_PLAYER)
	}
}