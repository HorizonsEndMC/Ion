package net.horizonsend.ion.server.features.multiblock.type.fluid.boiler

import net.horizonsend.ion.common.utils.text.ofChildren
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
import net.horizonsend.ion.server.features.transport.fluids.FluidType
import net.horizonsend.ion.server.features.transport.inputs.IOData
import net.horizonsend.ion.server.features.transport.inputs.IOPort
import net.horizonsend.ion.server.features.transport.inputs.IOType
import net.horizonsend.ion.server.miscellaneous.registrations.persistence.NamespacedKeys
import net.horizonsend.ion.server.miscellaneous.utils.Tasks
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
		val fluidOutput = FluidStorageContainer(data, "primaryout", text("Primary Output"), NamespacedKeys.key("primaryout"), 100.0, FluidRestriction.Unlimited)

		override val ioData: IOData = IOData.builder(this)
			.addPort(IOType.FLUID, -3, 0, 3) { IOPort.RegisteredMetaDataInput(this, FluidPortMetadata(connectedStore = fluidInput, inputAllowed = true, outputAllowed = false)) }
			.addPort(IOType.FLUID, 3, 0, 3) { IOPort.RegisteredMetaDataInput(this, FluidPortMetadata(connectedStore = fluidOutput, inputAllowed = false, outputAllowed = true)) }
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
			val deltaT = deltaTMS / 1000.0

			val outputContents = fluidOutput.getContents()
			if (outputContents.isNotEmpty()) {
				val temperature = outputContents.getDataOrDefault(FluidPropertyTypeKeys.TEMPERATURE, location).value
				if (temperature > 600.0 && fluidOutput.getRemainingRoom() <= 0) {
					Tasks.sync {
						val location = getBlockRelative(0, 5, 3).location.toCenterLocation()
						world.createExplosion(location, 30.0f)
					}
					fluidOutput.clear()
					fluidInput.clear()

					return
				}
			}

			if (!preTick(deltaT)) return
			heatFluid(deltaT)
			postTick(deltaT)
		}

		open fun preTick(deltaSeconds: Double): Boolean = true

		fun heatFluid(deltaSeconds: Double) {
			val input = fluidInput.getContents()
			if (input.isEmpty()) {
				setRunning(false)
				statusManager.setStatus(text("Idle"))
				return
			}

			setRunning(true)

			val heatingResult = input.type.getValue().getHeatingResult(
				stack = input,
				resultContainer = fluidOutput,
				appliedEnergyJoules = getHeatProductionJoulesPerSecond() * deltaSeconds,
				maximumTemperature = 650.0,
				location = location
			)

			input.setData(FluidPropertyTypeKeys.TEMPERATURE.getValue(), heatingResult.newTemperature)

			when (heatingResult) {
				is FluidType.HeatingResult.TemperatureIncreaseInPlace -> {
					setStatus(FluidPropertyTypeKeys.TEMPERATURE.getValue().formatValue(heatingResult.newTemperature))
				}
				is FluidType.HeatingResult.TemperatureIncreasePassthrough -> {
					val clone = fluidInput.getContents().clone()
					clone.setData(FluidPropertyTypeKeys.TEMPERATURE.getValue(), heatingResult.newTemperature)
					fluidInput.clear()
					fluidOutput.addFluid(clone, location)
					setStatus(FluidPropertyTypeKeys.TEMPERATURE.getValue().formatValue(heatingResult.newTemperature))
				}
				is FluidType.HeatingResult.Boiling -> {
					if (heatingResult.newFluidStack.isNotEmpty()) fluidOutput.addFluid(heatingResult.newFluidStack, location)
					fluidInput.removeAmount(heatingResult.inputRemovalAmount)
					setStatus(ofChildren(text("Boiling "), FluidPropertyTypeKeys.TEMPERATURE.getValue().formatValue(heatingResult.newTemperature)))
				}
			}
		}

		open fun postTick(deltaSeconds: Double) {}

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
