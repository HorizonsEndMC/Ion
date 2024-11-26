package net.horizonsend.ion.server.features.transport.util

import net.horizonsend.ion.server.features.starship.active.ActiveStarship
import net.horizonsend.ion.server.features.transport.nodes.cache.FluidTransportCache
import net.horizonsend.ion.server.features.transport.nodes.cache.PowerTransportCache
import net.horizonsend.ion.server.features.transport.nodes.cache.TransportCache
import net.horizonsend.ion.server.features.transport.nodes.cache.solarpanel.SolarPanelCache
import net.horizonsend.ion.server.features.world.chunk.IonChunk
import net.horizonsend.ion.server.miscellaneous.registrations.persistence.NamespacedKeys
import org.bukkit.NamespacedKey

enum class CacheType(val namespacedKey: NamespacedKey) {
	POWER(NamespacedKeys.POWER_TRANSPORT) {
		override fun get(chunk: IonChunk): PowerTransportCache {
			return chunk.transportNetwork.powerNodeManager.cache
		}

		override fun get(ship: ActiveStarship): PowerTransportCache {
			return ship.transportManager.powerNodeManager.cache
		}
	},
	SOLAR_PANELS(NamespacedKeys.POWER_TRANSPORT) {
		override fun get(chunk: IonChunk): SolarPanelCache {
			return chunk.transportNetwork.solarPanelManager.cache
		}

		override fun get(ship: ActiveStarship): SolarPanelCache {
			return ship.transportManager.solarPanelManager.cache
		}
	},
	FLUID(NamespacedKeys.FLUID_TRANSPORT) {
		override fun get(chunk: IonChunk): FluidTransportCache {
			return chunk.transportNetwork.fluidNodeManager.cache
		}

		override fun get(ship: ActiveStarship): FluidTransportCache {
			return ship.transportManager.fluidNodeManager.cache
		}
	},

	;

	abstract fun get(chunk: IonChunk): TransportCache
	abstract fun get(ship: ActiveStarship): TransportCache

	companion object {
		private val byKey = entries.associateBy { it.namespacedKey }
		operator fun get(key: NamespacedKey): CacheType = byKey[key]!!
	}
}
