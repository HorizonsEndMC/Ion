package net.horizonsend.ion.server.features.multiblock.entity.type.e2

import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet
import net.horizonsend.ion.server.features.multiblock.entity.MultiblockEntity
import net.horizonsend.ion.server.features.multiblock.entity.type.ticked.AsyncTickingMultiblockEntity
import net.horizonsend.ion.server.features.transport.inputs.IOData.BuiltInputData
import net.horizonsend.ion.server.features.transport.inputs.IOPort.RegisteredMetaDataInput
import net.horizonsend.ion.server.features.transport.inputs.IOType
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.toBlockKey
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.toVec3i

interface E2Multiblock : AsyncTickingMultiblockEntity {
	val e2Manager: E2Manager

	/** Stores values for E2 ticking */
	class E2Manager(private val multiblock: E2Multiblock) {
		var lastPowerAvailability: Double = 0.0
	}

	/** Returns the amount of power provided to the e2 network */
	fun getE2Output(): Double = 0.0

	/** Returns the amount of power required from the e2 network */
	fun getE2Consumption(): Double = 0.0

	/** For usage by e2 networks only. Marks a shortage during ticking of the network */
	fun markPowerShortage(availabilityFactor: Double) {
		e2Manager.lastPowerAvailability = availabilityFactor
	}

	fun getAvailablePowerPercentage() = e2Manager.lastPowerAvailability

	/** Returns if the available power is greater than 1.0 */
	fun hasFullPower() = getAvailablePowerPercentage() >= 1.0

	fun getE2Outputs(): Set<BuiltInputData<RegisteredMetaDataInput<E2PortMetaData>>> {
		this as MultiblockEntity
		return ioData.getOfType(IOType.E2).filterTo(ObjectOpenHashSet()) { data -> data.get(this)?.metaData?.outputAllowed == true }
	}

	fun getE2Inputs(): Set<BuiltInputData<RegisteredMetaDataInput<E2PortMetaData>>> {
		this as MultiblockEntity
		return ioData.getOfType(IOType.E2).filterTo(ObjectOpenHashSet()) { data -> data.get(this)?.metaData?.inputAllowed == true }
	}

	fun bootstrapE2Network() {
		this as MultiblockEntity

		val e2Manager = manager.getTransportManager().getE2GraphTransportManager()

		for (portLocation in getE2Outputs()) {
			val localPosition = toBlockKey(e2Manager.transportManager.getLocalCoordinate(toVec3i(portLocation.getRealPos(this))))
			if (portLocation.get(this)?.metaData?.outputAllowed != true) continue

			val node = e2Manager.getByLocation(localPosition)
			if (node != null) return

			e2Manager.registerNewPosition(localPosition)
		}
	}

	override fun tickAsync() {
		bootstrapE2Network()
	}
}
