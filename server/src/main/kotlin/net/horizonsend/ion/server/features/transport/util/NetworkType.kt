package net.horizonsend.ion.server.features.transport.util

import net.horizonsend.ion.server.features.starship.active.ActiveStarship
import net.horizonsend.ion.server.features.transport.cache.FluidTransportCache
import net.horizonsend.ion.server.features.transport.cache.PowerTransportCache
import net.horizonsend.ion.server.features.transport.cache.TransportCache
import net.horizonsend.ion.server.features.world.chunk.IonChunk
import net.horizonsend.ion.server.miscellaneous.registrations.persistence.NamespacedKeys
import org.bukkit.NamespacedKey

enum class NetworkType(val namespacedKey: NamespacedKey) {
	POWER(NamespacedKeys.POWER_TRANSPORT) {
		override fun get(chunk: IonChunk): PowerTransportCache {
			return chunk.transportNetwork.powerNodeManager.network
		}

		override fun get(ship: ActiveStarship): PowerTransportCache {
			return ship.transportManager.powerNodeManager.network
		}
	},
	FLUID(NamespacedKeys.FLUID_TRANSPORT) {
		override fun get(chunk: IonChunk): FluidTransportCache {
			return chunk.transportNetwork.fluidNodeManager.network
		}

		override fun get(ship: ActiveStarship): FluidTransportCache {
			return ship.transportManager.fluidNodeManager.network
		}
	},


	;

	abstract fun get(chunk: IonChunk): TransportCache
	abstract fun get(ship: ActiveStarship): TransportCache

	companion object {
		private val byKey = entries.associateBy { it.namespacedKey }
		operator fun get(key: NamespacedKey): NetworkType = byKey[key]!!
	}
}
