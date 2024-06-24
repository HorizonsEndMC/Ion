package net.horizonsend.ion.server.features.transport.step.origin

import net.horizonsend.ion.server.features.transport.network.ChunkPowerNetwork
import net.horizonsend.ion.server.features.transport.node.power.SolarPanelNode

class SolarPowerOrigin(val origin: SolarPanelNode) : StepOrigin<ChunkPowerNetwork>
