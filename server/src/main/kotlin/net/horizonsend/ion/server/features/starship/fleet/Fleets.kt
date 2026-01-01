package net.horizonsend.ion.server.features.starship.fleet

import net.horizonsend.ion.server.command.admin.debug
import net.horizonsend.ion.server.core.IonServerComponent
import net.horizonsend.ion.server.miscellaneous.utils.Tasks
import net.horizonsend.ion.server.miscellaneous.utils.debugAudience
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.player.PlayerQuitEvent
import java.util.concurrent.TimeUnit

object Fleets : IonServerComponent() {

	override fun onEnable() {
		Tasks.syncRepeat(200,20) {
			cleanUp()
			fleetList.forEach {
				it.logic?.tick() }
		}
	}

    private val fleetList = mutableListOf<Fleet>()

    fun findByMember(player: Player) = fleetList.find { it.contains(player) }

    fun findInvitesByMember(player: Player) = fleetList.filter { it.isInvited(player.toFleetMember()) }

    fun create(player: Player) = fleetList.add(Fleet(player.toFleetMember()))

	fun createAIFleet() : Fleet{
		val fleet = Fleet(null, initalized = false)
		fleetList.add(fleet)
		debugAudience.debug("Created fleet")
		return fleet
	}

    fun delete(fleet: Fleet) {
        if (fleetList.contains(fleet)) {
            fleet.delete()
            fleetList.remove(fleet)
			debugAudience.debug("removed fleet")
        }
    }

    @EventHandler
    fun onPlayerLeave(event: PlayerQuitEvent) {
        val player = event.player

		Tasks.syncDelay(2 * 60 * 20L) { //if still offline, kick the player
			if (Bukkit.getPlayer(player.uniqueId) != null) return@syncDelay
			findByMember(player)?.remove(player.toFleetMember()) ?: return@syncDelay
			for (fleet in findInvitesByMember(player)) {
				fleet.removeInvite(player.toFleetMember())
			}
		}

    }

	private fun cleanUp() {
		val toRemove = mutableSetOf<Fleet>()

		for (fleet in fleetList.filter { it.initalized }) {
			cleanupDeadAiMembers(fleet) ?: toRemove.add(fleet)

			if (fleet.leader?.isOnline() == true) continue

			reassignLeader(fleet)
		}

		val now = System.currentTimeMillis()
		for (uninitialized in fleetList.filterNot { it.initalized }) {
			if (TimeUnit.MILLISECONDS.toSeconds(now - uninitialized.createdAt) > 60) {
				toRemove.add(uninitialized)
			}
		}

		toRemove.forEach(::delete)
	}

	private fun cleanupDeadAiMembers(fleet: Fleet) : Fleet?{

		fleet.members.removeIf {
			it is FleetMember.AIShipMember && it.shipRef.get() == null
		}

		// If leader is an AiShipMember and was GC'd, reassign
		if (fleet.leader is FleetMember.AIShipMember && (fleet.leader as FleetMember.AIShipMember).shipRef.get() == null) {
			fleet.leader = null
		}

		if (fleet.members.isEmpty() && fleet.leader !is FleetMember.PlayerMember) {
			return null
		}
		return fleet
	}

	private fun reassignLeader(fleet: Fleet) {
		if (fleet.logic != null) return //let the logic handle reassignments if it exist
		fleet.leader = Fleet.firstPlayer(fleet) ?: Fleet.largestAIShip(fleet)
	}
}
