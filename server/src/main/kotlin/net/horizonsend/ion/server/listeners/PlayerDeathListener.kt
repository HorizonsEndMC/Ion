package net.horizonsend.ion.server.listeners

import net.horizonsend.ion.common.database.collections.PlayerData
import net.horizonsend.ion.common.database.enums.Achievement
import net.horizonsend.ion.common.database.update
import net.horizonsend.ion.server.extensions.sendServerError
import net.horizonsend.ion.server.items.CustomItems.customItem
import net.horizonsend.ion.server.items.objects.Blaster
import net.horizonsend.ion.server.legacy.utilities.rewardAchievement
import net.horizonsend.ion.server.vaultEconomy
import net.kyori.adventure.text.minimessage.MiniMessage
import net.starlegacy.database.schema.misc.SLPlayer
import net.starlegacy.database.schema.nations.Nation
import net.starlegacy.feature.progression.Levels
import org.bukkit.Material
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.PlayerDeathEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.SkullMeta
import java.lang.System.currentTimeMillis
import java.util.UUID
import kotlin.math.roundToInt

@Suppress("Unused")
class PlayerDeathListener : Listener {
	private val cooldowns = mutableMapOf<UUID, Long>()

	@EventHandler
	@Suppress("Unused")
	fun onPlayerDeathEvent(event: PlayerDeathEvent) {
		// Skulls start

		val killer = event.entity.killer ?: return // Only player kills
		val victim = event.player

		// Player Head Drops
		val headDropCooldownEnd = cooldowns.getOrDefault(victim.uniqueId, 0)
		cooldowns[victim.uniqueId] = currentTimeMillis() + 1000 * 60 * 60
		if (headDropCooldownEnd > currentTimeMillis()) return

		val head = ItemStack(Material.PLAYER_HEAD)
		head.editMeta(SkullMeta::class.java) {
			it.owningPlayer = victim
		}

		event.entity.world.dropItem(victim.location, head)

		// Skulls end

		// Bounties start

		if (killer !== victim) killer.rewardAchievement(Achievement.KILL_PLAYER) // Kill a Player Achievement

		// Bounties
		val killerData = PlayerData[killer.uniqueId]
		val killerLevel = Levels[event.player]
		val victimData = PlayerData[victim.uniqueId]

		killerData.update {
			bounty += (killerLevel * killerLevel) + (200 * killerLevel) + 5000

			if (killerData.acceptedBounty == victim.uniqueId) {
				if (vaultEconomy != null) {
					val bounty = victimData.bounty

					vaultEconomy.depositPlayer(victim, bounty.toDouble())

					acceptedBounty = null

					victimData.update {
						this.bounty = 0
					}

					killer.sendRichMessage("<gray>Claimed </gray>$bounty<gray> bounty on </gray>${victimData.minecraftUsername}")
				} else {
					killer.sendServerError("Vault Economy is not loaded! Cannot reward bounty!")
				}
			}
		}

		// Bounties end

		// Custom death message start
		killer.inventory.itemInMainHand.customItem?.let {
			if (it !is Blaster<*>) {
				println("failed at blaster")
				return@let
			}

			val blaster = it.displayName

			val victimColor = "<#" + Integer.toHexString((SLPlayer[victim.uniqueId]?.nation?.let { Nation.findById(it) }?.color ?: 16777215)) + ">"
			val killerColor = "<#" + Integer.toHexString((SLPlayer[killer.uniqueId]?.nation?.let { Nation.findById(it) }?.color ?: 16777215)) + ">"

			val distance = killer.location.distance(victim.location)

			val newMessage = MiniMessage.miniMessage()
				.deserialize(
					"$victimColor${victim.name}<reset> was sniped by $killerColor${killer.name}<reset> from ${distance.roundToInt()} blocks away, using "
				)
				.append(blaster)

			println("message created")

			event.deathMessage(newMessage)
		}
	}
}
