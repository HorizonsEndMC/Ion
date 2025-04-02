package net.horizonsend.ion.server.features.starship.fleet

import net.horizonsend.ion.server.IonServerComponent
import net.horizonsend.ion.server.miscellaneous.utils.Tasks
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.player.PlayerQuitEvent

object Fleets : IonServerComponent() {

	override fun onEnable() {
		Tasks.syncRepeat(200,20) {
			cleanupDeadAiMembers()
			fleetList.forEach {
				it.logic?.tick() }
		}
	}

    private val fleetList = mutableListOf<Fleet>()

    fun findByMember(player: Player) = fleetList.find { it.get(player) }

    fun findInvitesByMember(player: Player) = fleetList.filter { it.isInvited(player.toFleetMember()) }

    fun create(player: Player) = fleetList.add(Fleet(player.toFleetMember()))

    fun delete(fleet: Fleet) {
        if (fleetList.contains(fleet)) {
            fleet.delete()
            fleetList.remove(fleet)
        }
    }

    @EventHandler
    fun onPlayerLeave(event: PlayerQuitEvent) {
        val player = event.player

        findByMember(player)?.remove(player.toFleetMember()) ?: return
        for (fleet in findInvitesByMember(player)) {
            fleet.removeInvite(player.toFleetMember())
        }
    }

	private fun cleanupDeadAiMembers() {
		val toRemove = mutableListOf<Fleet>()

		for (fleet in fleetList) {
			fleet.members.removeIf {
				it is FleetMember.AIShipMember && it.shipRef.get() == null
			}

			// If leader is an AiShipMember and was GC'd, reassign
			if (fleet.leader is FleetMember.AIShipMember && (fleet.leader as FleetMember.AIShipMember).shipRef.get() == null) {
				fleet.leader = null
			}

			if (fleet.members.isEmpty()) {
				toRemove.add(fleet)
			}
		}

		toRemove.forEach(Fleets::delete)
	}
}
