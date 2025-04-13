package net.horizonsend.ion.server.features.starship.fleet

import net.horizonsend.ion.server.IonServerComponent
import net.horizonsend.ion.server.miscellaneous.utils.Tasks
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.player.PlayerQuitEvent

object Fleets : IonServerComponent() {

	override fun onEnable() {
		Tasks.syncRepeat(200,20) {
			cleanUp()
			fleetList.forEach {
				it.logic?.tick() }
		}
	}

    private val fleetList = mutableListOf<Fleet>()

    fun findByMember(player: Player) = fleetList.find { it.get(player) }

    fun findInvitesByMember(player: Player) = fleetList.filter { it.isInvited(player.toFleetMember()) }

    fun create(player: Player) = fleetList.add(Fleet(player.toFleetMember()))

	fun createAIFleet() : Fleet{
		val fleet = Fleet(null)
		fleetList.add(fleet)
		return fleet
	}

    fun delete(fleet: Fleet) {
        if (fleetList.contains(fleet)) {
            fleet.delete()
            fleetList.remove(fleet)
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
		for (fleet in fleetList) {
			cleanupDeadAiMembers(fleet) ?: toRemove.add(fleet)
			reassignLeader(fleet)
		}
		toRemove.forEach(Fleets::delete)
	}

	private fun cleanupDeadAiMembers(fleet: Fleet) : Fleet?{

		fleet.members.removeIf {
			it is FleetMember.AIShipMember && it.shipRef.get() == null
		}

		// If leader is an AiShipMember and was GC'd, reassign
		if (fleet.leader is FleetMember.AIShipMember && (fleet.leader as FleetMember.AIShipMember).shipRef.get() == null) {
			fleet.leader = null
		}

		if (fleet.members.isEmpty()) {
			return fleet
		}
		return null
	}

	private fun reassignLeader(fleet: Fleet) {
		if (fleet.logic != null) return //let the logic handle reassignments if it exist
		fleet.leader = Fleet.firstPlayer(fleet) ?: Fleet.largestAIShip(fleet)
	}
}
