package net.horizonsend.ion.server.features.multiblock.manager

import net.horizonsend.ion.server.features.starship.Starship
import net.horizonsend.ion.server.features.transport.network.TransportNetwork
import net.horizonsend.ion.server.features.transport.node.NetworkType
import org.slf4j.Logger

class ShipMultiblockManager(val starship: Starship, logger: Logger) : MultiblockManager(logger) {
	override val world get() = starship.world

	override fun save() {}

	override fun getNetwork(type: NetworkType): TransportNetwork {
		return type.get(starship)
	}
}
