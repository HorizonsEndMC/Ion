package net.horizonsend.ion.server.features.transport.step.origin.gas

import net.horizonsend.ion.server.features.transport.network.GasNetwork
import net.horizonsend.ion.server.features.transport.node.gas.GasExtractorNode
import net.horizonsend.ion.server.features.transport.step.origin.StepOrigin

class ExtractedGasOrigin(
	val extractorNode: GasExtractorNode
) : StepOrigin<GasNetwork>
