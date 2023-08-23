package net.horizonsend.ion.server.features.bounties

import net.horizonsend.ion.common.database.schema.misc.SLPlayer
import net.horizonsend.ion.common.database.schema.misc.SLPlayerId
import net.horizonsend.ion.common.database.uuid
import net.horizonsend.ion.common.extensions.userError
import net.horizonsend.ion.server.features.bounties.Bounties.BountyPlayer
import net.horizonsend.ion.server.features.bounties.Bounties.coolDown
import net.horizonsend.ion.server.features.nations.gui.playerClicker
import net.horizonsend.ion.server.features.nations.gui.skullItem
import net.horizonsend.ion.server.miscellaneous.utils.MenuHelper
import net.horizonsend.ion.server.miscellaneous.utils.Tasks
import net.horizonsend.ion.server.miscellaneous.utils.slPlayerId
import org.bukkit.entity.Player

object BountiesMenu {
	/** Only use this function async **/
	private fun getBounties(sender: SLPlayerId): List<BountyPlayer> {
		val bounties = mutableListOf<BountyPlayer>()

		for (id: SLPlayerId in SLPlayer.allIds()) {
			val player = SLPlayer.findById(id) ?: continue
			// Has been on in the past 2 days, has a bounty, and isn't the person opening the menu
			// If they were last on more than 2 days ago,
			// Or their bounty is zero
			// Or they are the sender
			// Skip them
			if (player.lastSeen <= coolDown || player.bounty <= 0 || player._id == sender) continue
			if (!Bounties.canClaim(sender, id)) continue

			bounties.add(
				BountyPlayer(
					player.lastKnownName,
					player._id.uuid,
					skullItem(player._id.uuid, player.lastKnownName),
					player.bounty
				)
			)
		}

		return bounties.sortedBy { it.bounty }
	}

	fun openMenuAsync(player: Player) = Tasks.async {
		MenuHelper.run {
			val bounties = getBounties(player.slPlayerId).map {
				val guiItem = guiButton(it.skull) {
					if (Bounties.isNotSurvival()) return@guiButton playerClicker.userError("You can only do that on the Survival server!")
					Bounties.claimBounty(player, it)
				}.apply {
					this.setLore("Bounty: ${it.bounty}")
				}

				return@map guiItem
			}

			Tasks.sync {
				player.openPaginatedMenu(
					title = "Claim a Bounty",
					items = bounties
				)
			}
		}
	}
}
