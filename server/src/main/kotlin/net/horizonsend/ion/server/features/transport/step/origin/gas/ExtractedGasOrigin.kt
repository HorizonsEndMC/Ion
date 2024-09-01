package net.horizonsend.ion.server.features.transport.step.origin.gas

import net.horizonsend.ion.server.features.transport.network.FluidNetwork
import net.horizonsend.ion.server.features.transport.node.gas.FluidExtractorNode
import net.horizonsend.ion.server.features.transport.step.origin.StepOrigin

class ExtractedGasOrigin(
	val extractorNode: FluidExtractorNode
) : StepOrigin<FluidNetwork>
