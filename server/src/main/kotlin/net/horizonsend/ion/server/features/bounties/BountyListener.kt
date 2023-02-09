package net.horizonsend.ion.server.features.bounties

import net.horizonsend.ion.common.database.collections.PlayerData
import net.horizonsend.ion.common.database.update
import net.horizonsend.ion.server.extensions.sendServerError
import net.horizonsend.ion.server.miscellaneous.vaultEconomy
import net.starlegacy.feature.progression.Levels
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.PlayerDeathEvent

class BountyListener : Listener {
	@Suppress("Unused")
	@EventHandler
	fun onPlayerDeathEvent(event: PlayerDeathEvent) {
		val killer = event.entity.killer ?: return // Only player kills
		val victim = event.player
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
	}
}
