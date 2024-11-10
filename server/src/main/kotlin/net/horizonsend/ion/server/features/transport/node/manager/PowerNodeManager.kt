package net.horizonsend.ion.server.features.transport.node.manager

import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet
import net.horizonsend.ion.server.features.transport.NewTransport
import net.horizonsend.ion.server.features.transport.cache.PowerTransportCache
import net.horizonsend.ion.server.features.transport.node.TransportNode
import net.horizonsend.ion.server.features.transport.node.manager.holders.NetworkHolder
import net.horizonsend.ion.server.features.transport.node.type.power.PowerExtractorNode
import net.horizonsend.ion.server.features.transport.node.type.power.PowerFlowMeter
import net.horizonsend.ion.server.features.transport.node.type.power.PowerInputNode
import net.horizonsend.ion.server.features.transport.node.type.power.PowerNodeFactory
import net.horizonsend.ion.server.features.transport.node.type.power.SolarPanelNode
import net.horizonsend.ion.server.features.transport.node.util.NetworkType
import net.horizonsend.ion.server.features.transport.node.util.calculatePathResistance
import net.horizonsend.ion.server.features.transport.node.util.getIdealPath
import net.horizonsend.ion.server.features.transport.node.util.getNetworkDestinations
import net.horizonsend.ion.server.miscellaneous.registrations.persistence.NamespacedKeys.POWER_TRANSPORT
import org.bukkit.NamespacedKey
import java.util.concurrent.Future
import kotlin.math.roundToInt

class PowerNodeManager(holder: NetworkHolder<PowerTransportCache>) : NodeManager<PowerExtractorNode>(holder) {
	override val type: NetworkType = NetworkType.POWER
	override val namespacedKey: NamespacedKey = POWER_TRANSPORT
	override val nodeFactory: PowerNodeFactory = PowerNodeFactory(this)

	/** Store solar panels for ticking */
	val solarPanels: ObjectOpenHashSet<SolarPanelNode> = ObjectOpenHashSet()

	override val dataVersion: Int = 0 //TODO 1

	override fun clearData() {
		nodes.clear()
		solarPanels.clear()
		extractors.clear()
	}

	fun tickTransport() {
	}
}
