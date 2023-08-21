package net.horizonsend.ion.server.features.bounties

import com.github.stefvanschie.inventoryframework.gui.GuiItem
import net.horizonsend.ion.common.database.schema.misc.SLPlayer
import net.horizonsend.ion.common.database.schema.misc.SLPlayerId
import net.horizonsend.ion.common.database.uuid
import net.horizonsend.ion.server.features.nations.gui.openPaginatedMenu
import net.horizonsend.ion.server.features.nations.gui.skullItem
import net.horizonsend.ion.server.miscellaneous.utils.MenuHelper.setLore
import net.horizonsend.ion.server.miscellaneous.utils.Tasks
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.litote.kmongo.gte
import java.util.Date
import java.util.UUID
import java.util.concurrent.TimeUnit

object BountiesMenu {
	val activeClaimable get() = Date(System.currentTimeMillis() - TimeUnit.DAYS.toMillis(1))

	private fun getBounties(): List<BountyPlayer> {
		val bounties = mutableListOf<BountyPlayer>()

		for (id: SLPlayerId in SLPlayer.allIds()) {
			if (SLPlayer.matches(id, SLPlayer::lastSeen gte activeClaimable)) {
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
		}

		return bounties
	}

	fun openMenuAsync(player: Player) = Tasks.async {
		val bounties = getBounties().map {
			val guiItem = GuiItem(it.skull) {

			}.apply {
				this.setLore("Bounty: ${it.bounty}")
			}

			return@map guiItem
		}

		player.openPaginatedMenu(
			title = "Claim a Bounty",
			items = bounties
		)
	}

	private data class BountyPlayer(
		val name: String,
		val uniqueId: UUID,
		val skull: ItemStack,
		val bounty: Double
	)
}
