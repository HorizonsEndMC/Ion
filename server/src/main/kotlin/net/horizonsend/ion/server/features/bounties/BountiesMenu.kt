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
import org.litote.kmongo.and
import org.litote.kmongo.eq
import org.litote.kmongo.lte

object BountiesMenu {
	private fun getBounties(sender: SLPlayerId): List<BountyPlayer> {
		val bounties = mutableListOf<BountyPlayer>()

		for (id: SLPlayerId in SLPlayer.allIds()) {
			// Has been on in the past 2 days, has a bounty, and isn't the person opening the menu
			val query = and(SLPlayer::lastSeen lte coolDown, SLPlayer::bounty lte 0.0, SLPlayer::_id eq sender)
			if (SLPlayer.matches(id, query)) continue

			val player = SLPlayer.findById(id) ?: continue

			bounties.add(
				BountyPlayer(
					player.lastKnownName,
					player._id.uuid,
					skullItem(player._id.uuid, player.lastKnownName),
					player.bounty
				)
			)
		}

		return bounties
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
