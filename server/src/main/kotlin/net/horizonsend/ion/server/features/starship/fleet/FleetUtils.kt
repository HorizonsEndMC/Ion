package net.horizonsend.ion.server.features.starship.fleet

import net.horizonsend.ion.server.features.starship.Starship
import org.bukkit.entity.Player
import java.lang.ref.WeakReference
import java.util.UUID


abstract class FleetLogic(val fleet: Fleet) {
	abstract fun tick()
}

sealed class FleetMember {
	data class PlayerMember(val uuid: UUID) : FleetMember() {
		override fun equals(other: Any?): Boolean {
			if (this === other) return true
			if (javaClass != other?.javaClass) return false

			other as PlayerMember

			return uuid == other.uuid
		}

		override fun hashCode(): Int {
			return uuid.hashCode()
		}
	}

	data class AIShipMember(val shipRef: WeakReference<Starship>) : FleetMember() {
		override fun equals(other: Any?): Boolean {
			if (this === other) return true
			if (javaClass != other?.javaClass) return false

			other as AIShipMember

			return shipRef == other.shipRef
		}

		override fun hashCode(): Int {
			return shipRef.hashCode()
		}
	}
}

fun Player.toFleetMember() = FleetMember.PlayerMember(uniqueId)

fun Starship.toFleetMember() = FleetMember.AIShipMember(WeakReference(this))
