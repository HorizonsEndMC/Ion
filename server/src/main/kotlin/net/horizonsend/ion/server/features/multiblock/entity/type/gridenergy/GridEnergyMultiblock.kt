package net.horizonsend.ion.server.features.multiblock.entity.type.gridenergy

import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet
import net.horizonsend.ion.server.features.multiblock.entity.MultiblockEntity
import net.horizonsend.ion.server.features.multiblock.entity.type.ticked.AsyncTickingMultiblockEntity
import net.horizonsend.ion.server.features.transport.inputs.IOData.BuiltInputData
import net.horizonsend.ion.server.features.transport.inputs.IOPort.RegisteredMetaDataInput
import net.horizonsend.ion.server.features.transport.inputs.IOType
import net.horizonsend.ion.server.features.transport.manager.graph.gridenergy.GridEnergyNetwork
import net.horizonsend.ion.server.miscellaneous.utils.Tasks
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.BlockKey
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.toBlockKey
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.toVec3i
import java.time.Duration

interface GridEnergyMultiblock : AsyncTickingMultiblockEntity {
	val gridEnergyManager: MultiblockGridEnergyManager

	/** Stores values for grid energy ticking */
	class MultiblockGridEnergyManager(private val multiblock: GridEnergyMultiblock) {
		var lastPowerAvailability: Double = 0.0
		var activeConsumption = 0.0
		var activeDurationEnd: Long = 0L

		private val updateListeners: MutableList<(GridEnergyMultiblock) -> Unit> = mutableListOf()

		fun registerUpdateListener(listener: (GridEnergyMultiblock) -> Unit) {
			updateListeners.add(listener)
		}

		fun deregisterUpdateListener(listener: (GridEnergyMultiblock) -> Unit) {
			updateListeners.remove(listener)
		}

		fun runUpdates() {
			Tasks.async {
				updateListeners.forEach { t -> t.invoke(multiblock) }
			}
		}
	}

	/** Returns the amount of power provided to the e2 network */
	fun getGridEnergyOutput(): Double = 0.0

	/** Returns the amount of power required from the e2 network */
	fun getTotalGridEnergyConsumption(): Double {
		return getActiveGridEnergyConsumption() + getPassiveGridEnergyConsumption()
	}

	fun getActiveGridEnergyConsumption(): Double = gridEnergyManager.activeConsumption

	fun setActiveGridEnergyConsumption(consumption: Double) {
		gridEnergyManager.activeConsumption = consumption
		gridEnergyManager.runUpdates()
	}

	fun getPassiveGridEnergyConsumption() = 0.0

	/** For usage by grid energy networks only. Marks a shortage during ticking of the network */
	fun markPowerShortage(availabilityFactor: Double) {
		gridEnergyManager.lastPowerAvailability = availabilityFactor
	}

	fun getAvailablePowerPercentage() = gridEnergyManager.lastPowerAvailability

	/** Returns if the available power is greater than 1.0 */
	fun hasFullPower() = getAvailablePowerPercentage() >= 1.0

	fun getGridEnergyOutputs(): Set<BuiltInputData<RegisteredMetaDataInput<GridEnergyPortMetaData>>> {
		this as MultiblockEntity
		return ioData.getOfType(IOType.GRID_ENERGY).filterTo(ObjectOpenHashSet()) { data -> data.get(this)?.metaData?.outputAllowed == true }
	}

	fun getGridEnergyInputs(): Set<BuiltInputData<RegisteredMetaDataInput<GridEnergyPortMetaData>>> {
		this as MultiblockEntity
		return ioData.getOfType(IOType.GRID_ENERGY).filterTo(ObjectOpenHashSet()) { data -> data.get(this)?.metaData?.inputAllowed == true }
	}

	fun bootstrapGridEnergyNetwork() {
		this as MultiblockEntity

		val gridEnergyManager = manager.getTransportManager().getGridEnergyGraphTransportManager()

		for (portLocation in getGridEnergyOutputs()) {
			val localPosition = toBlockKey(gridEnergyManager.transportManager.getLocalCoordinate(toVec3i(portLocation.getRealPos(this))))
			if (portLocation.get(this)?.metaData?.outputAllowed != true) continue

			val node = gridEnergyManager.getByLocation(localPosition)
			if (node != null) return

			gridEnergyManager.registerNewPosition(localPosition)
		}
	}

	fun getConnectedNetworks(): Map<BlockKey, GridEnergyNetwork> {
		val map = mutableMapOf<BlockKey, GridEnergyNetwork>()

		getGridEnergyInputs().forEach { data ->
			val portLocation = data.getRealPos(this as MultiblockEntity)
			val localPosition = toBlockKey((this as MultiblockEntity).manager.getTransportManager().getLocalCoordinate(toVec3i(portLocation)))

			val network = (this as MultiblockEntity).manager.getTransportManager().getGridEnergyGraphTransportManager().getByLocation(localPosition) as GridEnergyNetwork? ?: return@forEach
			map[portLocation] = network
		}

		return map
	}

	override fun tickAsync() {
		bootstrapGridEnergyNetwork()
	}

	fun tickActivePower() {
		if (System.currentTimeMillis() > gridEnergyManager.activeDurationEnd) {
			setActiveGridEnergyConsumption(0.0)
		}
	}

	fun setActiveDuration(duration: Duration) {
		gridEnergyManager.activeDurationEnd = System.currentTimeMillis() + duration.toMillis()
	}
}
