package net.horizonsend.ion.server.features.starship.fleet

import net.horizonsend.ion.server.features.starship.Starship
import org.bukkit.entity.Player
import java.lang.ref.WeakReference
import java.util.*


abstract class FleetLogic(val fleet: Fleet) {
	abstract fun tick()
}

sealed class FleetMember {
	data class PlayerMember(val uuid: UUID) : FleetMember()
	data class AIShipMember(val shipRef: WeakReference<Starship>) : FleetMember()
}

fun Player.toFleetMember() = FleetMember.PlayerMember(uniqueId)

fun Starship.toFleetMember() = FleetMember.AIShipMember(WeakReference(this))
