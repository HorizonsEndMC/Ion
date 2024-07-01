package net.horizonsend.ion.server.features.cache

import net.horizonsend.ion.common.database.Oid
import net.horizonsend.ion.common.database.boolean
import net.horizonsend.ion.common.database.cache.Cache
import net.horizonsend.ion.common.database.cache.ManualCache
import net.horizonsend.ion.common.database.get
import net.horizonsend.ion.common.database.oid
import net.horizonsend.ion.common.database.schema.misc.ClaimedBounty
import net.horizonsend.ion.common.database.schema.misc.SLPlayerId
import net.horizonsend.ion.server.miscellaneous.utils.Tasks
import net.horizonsend.ion.server.miscellaneous.utils.listen
import net.horizonsend.ion.server.miscellaneous.utils.multimapOf
import net.horizonsend.ion.server.miscellaneous.utils.slPlayerId
import org.bukkit.event.EventPriority
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.litote.kmongo.and
import org.litote.kmongo.eq

object BountyCache: ManualCache(), Cache {
	private val claimedBounties = multimapOf<SLPlayerId, ClaimedBounty>()
	private val allBounties = mutableMapOf<Oid<ClaimedBounty>, ClaimedBounty>()

	override fun load() {
		listen<PlayerQuitEvent> { event ->
			removePlayerBounties(event.player.slPlayerId)
		}

		listen<PlayerJoinEvent>(priority = EventPriority.LOWEST) { event ->
			cachePlayerBounties(event.player.slPlayerId)
		}

		ClaimedBounty.watchUpdates { change ->
			change[ClaimedBounty::completed]?.let {
				// If it is not set to completed return
				if (!it.boolean()) return@watchUpdates

				val oid = change.oid

				val bounty = allBounties[oid] ?: return@watchUpdates
				val hunter = bounty.hunter

				removeBounty(hunter, oid)
			}
		}

		ClaimedBounty.watchInserts { change ->
			val bounty = change.fullDocument ?: return@watchInserts
			val hunter = bounty.hunter

			addBounty(hunter, bounty)
		}

		ClaimedBounty.watchDeletes { change ->
			val bounty = change.fullDocument ?: return@watchDeletes
			val hunter = bounty.hunter
			val objectId = bounty._id

			removeBounty(hunter, objectId)
		}
	}

	private fun cachePlayerBounties(player: SLPlayerId) = Tasks.async {
		val new = ClaimedBounty.find(and(ClaimedBounty::hunter eq player, ClaimedBounty::completed eq false))

		claimedBounties[player].addAll(new)
		allBounties.putAll(new.associateBy { it._id })
	}

	private fun removePlayerBounties(player: SLPlayerId) = Tasks.async {
		val removed = claimedBounties.removeAll(player)
		allBounties.keys.removeAll(removed.mapTo(mutableSetOf()) { it._id })
	}

	private fun removeBounty(player: SLPlayerId, bounty: Oid<ClaimedBounty>) {
		claimedBounties[player].removeAll { it._id == bounty }
		allBounties.remove(bounty)
	}

	private fun addBounty(player: SLPlayerId, bounty: ClaimedBounty) {
		claimedBounties[player].add(bounty)
		allBounties[bounty._id] = bounty
	}

	operator fun get(hunter: SLPlayerId, target: SLPlayerId): ClaimedBounty? {
		return claimedBounties[hunter]
			.filter { it.target == target && !it.completed }
			.maxByOrNull { it.claimTime }
	}

	operator fun get(bounty: Oid<ClaimedBounty>): ClaimedBounty? {
		return allBounties[bounty]
	}
}
