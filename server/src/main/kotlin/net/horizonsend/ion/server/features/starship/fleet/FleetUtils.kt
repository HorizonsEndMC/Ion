package net.horizonsend.ion.server.features.starship.fleet

import net.horizonsend.ion.server.features.starship.Starship
import net.kyori.adventure.audience.Audience
import net.kyori.adventure.audience.ForwardingAudience
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import java.lang.ref.WeakReference
import java.util.UUID


abstract class FleetLogic(val fleet: Fleet) {
	abstract fun tick()
}

sealed class FleetMember : ForwardingAudience {
	abstract fun isOnline(): Boolean

	data class PlayerMember(val uuid: UUID, val name: String) : FleetMember() {
		override fun equals(other: Any?): Boolean {
			if (this === other) return true
			if (javaClass != other?.javaClass) return false

			other as PlayerMember

			return uuid == other.uuid
		}

		override fun hashCode(): Int {
			return uuid.hashCode()
		}

		override fun audiences(): Iterable<Audience> {
			return listOfNotNull(Bukkit.getPlayer(uuid))
		}

		override fun isOnline(): Boolean {
			return Bukkit.getPlayer(uuid) != null
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

		override fun audiences(): Iterable<Audience?> {
			return listOf()
		}

		override fun isOnline(): Boolean {
			return shipRef.get() != null
		}
	}
}

fun Player.toFleetMember() = FleetMember.PlayerMember(uniqueId, name)

fun Starship.toFleetMember() = FleetMember.AIShipMember(WeakReference(this))
