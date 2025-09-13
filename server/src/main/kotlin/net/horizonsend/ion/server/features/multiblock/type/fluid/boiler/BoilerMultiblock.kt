package net.horizonsend.ion.server.features.multiblock.type.fluid.boiler

import net.horizonsend.ion.server.core.registration.keys.FluidPropertyTypeKeys
import net.horizonsend.ion.server.features.multiblock.Multiblock
import net.horizonsend.ion.server.features.multiblock.entity.MultiblockEntity
import net.horizonsend.ion.server.features.multiblock.entity.PersistentMultiblockData
import net.horizonsend.ion.server.features.multiblock.entity.type.DisplayMultiblockEntity
import net.horizonsend.ion.server.features.multiblock.entity.type.StatusMultiblockEntity
import net.horizonsend.ion.server.features.multiblock.entity.type.fluids.FluidPortMetadata
import net.horizonsend.ion.server.features.multiblock.entity.type.fluids.FluidStoringMultiblock
import net.horizonsend.ion.server.features.multiblock.entity.type.fluids.storage.FluidRestriction
import net.horizonsend.ion.server.features.multiblock.entity.type.fluids.storage.FluidStorageContainer
import net.horizonsend.ion.server.features.multiblock.entity.type.ticked.AsyncTickingMultiblockEntity
import net.horizonsend.ion.server.features.multiblock.entity.type.ticked.TickedMultiblockEntityParent
import net.horizonsend.ion.server.features.multiblock.manager.MultiblockManager
import net.horizonsend.ion.server.features.multiblock.type.EntityMultiblock
import net.horizonsend.ion.server.features.multiblock.type.fluid.boiler.BoilerMultiblock.BoilerMultiblockEntity
import net.horizonsend.ion.server.features.transport.fluids.FluidUtils
import net.horizonsend.ion.server.features.transport.fluids.properties.FluidProperty.Temperature
import net.horizonsend.ion.server.features.transport.inputs.IOData
import net.horizonsend.ion.server.features.transport.inputs.IOPort
import net.horizonsend.ion.server.features.transport.inputs.IOType
import net.horizonsend.ion.server.miscellaneous.registrations.persistence.NamespacedKeys
import net.kyori.adventure.text.Component.text
import org.bukkit.World
import org.bukkit.block.BlockFace
import org.bukkit.persistence.PersistentDataAdapterContext

abstract class BoilerMultiblock<T : BoilerMultiblockEntity> : Multiblock(), EntityMultiblock<T> {
	override val name: String = "boiler"

	abstract class BoilerMultiblockEntity(
		manager: MultiblockManager,
		data: PersistentMultiblockData,
		multiblock: BoilerMultiblock<*>,
		world: World,
		x: Int,
		y: Int,
		z: Int,
		structureDirection: BlockFace
	) : MultiblockEntity(manager, multiblock, world, x, y, z, structureDirection), DisplayMultiblockEntity, AsyncTickingMultiblockEntity, FluidStoringMultiblock, StatusMultiblockEntity {
		override val tickingManager: TickedMultiblockEntityParent.TickingManager = TickedMultiblockEntityParent.TickingManager(2)
		override val statusManager: StatusMultiblockEntity.StatusManager = StatusMultiblockEntity.StatusManager()

		val fluidInput = FluidStorageContainer(data, "primaryin", text("Primary Input"), NamespacedKeys.key("primaryin"), 10_000.0, FluidRestriction.Unlimited)
		val fluidOutput = FluidStorageContainer(data, "primaryout", text("Primary Output"), NamespacedKeys.key("primaryout"), 10_000.0, FluidRestriction.Unlimited)

		override val ioData: IOData = IOData.builder(this)
			.addPort(IOType.FLUID, -3, 0, 3) { IOPort.RegisteredMetaDataInput<FluidPortMetadata>(this, FluidPortMetadata(connectedStore = fluidInput, inputAllowed = true, outputAllowed = false)) }
			.addPort(IOType.FLUID, 3, 0, 3) { IOPort.RegisteredMetaDataInput<FluidPortMetadata>(this, FluidPortMetadata(connectedStore = fluidOutput, inputAllowed = false, outputAllowed = true)) }
			.registerAdditionalIO()
			.build()

		protected abstract fun IOData.Builder.registerAdditionalIO(): IOData.Builder

		override fun getStores(): List<FluidStorageContainer> {
			return listOf(fluidInput, fluidOutput)
		}

		override fun storeAdditionalData(store: PersistentMultiblockData, adapterContext: PersistentDataAdapterContext) {
			saveStorageData(store)
		}

		override fun tickAsync() {
			bootstrapFluidNetwork()

			preTick()
			heatFluid()
			postTick()
		}

		open fun preTick() {}

		fun heatFluid() {
			val input = fluidInput.getContents()
			if (input.isEmpty()) {
				setRunning(false)
				return
			}

			setRunning(true)

			val currentHeat = input.getDataOrDefault(FluidPropertyTypeKeys.TEMPERATURE.getValue(), location).value

			val heatOutput = getHeatProductionJoulesPerSecond()
			val fluidWeightGrams = FluidUtils.getFluidWeight(input, location)
			val specificHeat = input.type.getValue().getIsobaricHeatCapacity(input)
			val kelvinHeat = heatOutput / (specificHeat * fluidWeightGrams)

			val newHeat = Temperature(currentHeat + kelvinHeat)
			input.setData(FluidPropertyTypeKeys.TEMPERATURE.getValue(), newHeat)

			setStatus(FluidPropertyTypeKeys.TEMPERATURE.getValue().formatValue(newHeat))
		}

		open fun postTick() {}

		/**
		 * Returns the heat currently being produced, in joules
		 **/
		abstract fun getHeatProductionJoulesPerSecond(): Double

		private var startedRunning: Long? = null

		protected val isRunning get() = startedRunning != null

		fun getRunningDurationMillis(): Long {
			val runningTicks = startedRunning ?: return -1L
			return System.currentTimeMillis() - runningTicks
		}

		fun setRunning(running: Boolean) {
			if (!running) startedRunning = null
			// Only set the start time if it hasn't been set
			else if (startedRunning == null) startedRunning = System.currentTimeMillis()
		}
	}
}
