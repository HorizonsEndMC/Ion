package net.horizonsend.ion.server.features.multiblock.type.gridpower.turbine

import net.horizonsend.ion.server.core.registration.keys.FluidPropertyTypeKeys
import net.horizonsend.ion.server.core.registration.keys.FluidTypeKeys
import net.horizonsend.ion.server.features.client.display.modular.DisplayHandlers
import net.horizonsend.ion.server.features.client.display.modular.TextDisplayHandler
import net.horizonsend.ion.server.features.client.display.modular.display.MATCH_SIGN_FONT_SIZE
import net.horizonsend.ion.server.features.client.display.modular.display.StatusDisplayModule
import net.horizonsend.ion.server.features.client.display.modular.display.fluid.SplitFluidDisplayModule
import net.horizonsend.ion.server.features.client.display.modular.display.getLinePos
import net.horizonsend.ion.server.features.multiblock.Multiblock
import net.horizonsend.ion.server.features.multiblock.entity.MultiblockEntity
import net.horizonsend.ion.server.features.multiblock.entity.PersistentMultiblockData
import net.horizonsend.ion.server.features.multiblock.entity.type.DisplayMultiblockEntity
import net.horizonsend.ion.server.features.multiblock.entity.type.StatusMultiblockEntity
import net.horizonsend.ion.server.features.multiblock.entity.type.fluids.FluidPortMetadata
import net.horizonsend.ion.server.features.multiblock.entity.type.fluids.FluidStoringMultiblock
import net.horizonsend.ion.server.features.multiblock.entity.type.fluids.storage.FluidRestriction
import net.horizonsend.ion.server.features.multiblock.entity.type.fluids.storage.FluidStorageContainer
import net.horizonsend.ion.server.features.multiblock.entity.type.gridenergy.RotationConsumer
import net.horizonsend.ion.server.features.multiblock.entity.type.gridenergy.RotationProvider
import net.horizonsend.ion.server.features.multiblock.entity.type.ticked.AsyncTickingMultiblockEntity
import net.horizonsend.ion.server.features.multiblock.entity.type.ticked.TickedMultiblockEntityParent
import net.horizonsend.ion.server.features.multiblock.manager.MultiblockManager
import net.horizonsend.ion.server.features.multiblock.type.EntityMultiblock
import net.horizonsend.ion.server.features.multiblock.type.gridpower.turbine.TurbineMultiblock.TurbineMultiblockEntity
import net.horizonsend.ion.server.features.transport.fluids.FluidStack
import net.horizonsend.ion.server.features.transport.fluids.properties.FluidProperty
import net.horizonsend.ion.server.features.transport.inputs.IOData
import net.horizonsend.ion.server.features.transport.inputs.IOPort
import net.horizonsend.ion.server.features.transport.inputs.IOType
import net.horizonsend.ion.server.miscellaneous.registrations.persistence.NamespacedKeys
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.RelativeFace
import net.horizonsend.ion.server.miscellaneous.utils.metersCubedToLiters
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.Location
import org.bukkit.World
import org.bukkit.block.BlockFace
import org.bukkit.persistence.PersistentDataAdapterContext
import kotlin.math.pow

abstract class TurbineMultiblock : Multiblock(), EntityMultiblock<TurbineMultiblockEntity> {
	override val name: String = "turbine"

	abstract val inletCrossSectionArea: Double
	abstract val efficiency: Double

	override val signText: Array<Component?> = createSignText(
		Component.text("Turbine"),
		null,
		null,
		null
	)

	override fun createEntity(manager: MultiblockManager, data: PersistentMultiblockData, world: World, x: Int, y: Int, z: Int, structureDirection: BlockFace): TurbineMultiblockEntity {
		return TurbineMultiblockEntity(manager, data, this, world, x, y, z, structureDirection)
	}

	class TurbineMultiblockEntity(
		manager: MultiblockManager,
		data: PersistentMultiblockData,
		override val multiblock: TurbineMultiblock,
		world: World,
		x: Int,
		y: Int,
		z: Int,
		structureDirection: BlockFace
	) : MultiblockEntity(manager, multiblock, world, x, y, z, structureDirection), DisplayMultiblockEntity, AsyncTickingMultiblockEntity, StatusMultiblockEntity, RotationProvider, FluidStoringMultiblock {
		override val tickingManager: TickedMultiblockEntityParent.TickingManager = TickedMultiblockEntityParent.TickingManager(5)
		override val statusManager: StatusMultiblockEntity.StatusManager = StatusMultiblockEntity.StatusManager()

		val steamInput = FluidStorageContainer(data, "steam_input", Component.text("Steam Input"), STEAM_INPUT, 1_000_000.0, FluidRestriction.FluidTypeWhitelist(setOf(FluidTypeKeys.WATER)))
		val steamOutput = FluidStorageContainer(data, "steam_output", Component.text("Steam Output"), STEAM_OUTPUT, 1_000_000.0, FluidRestriction.FluidTypeWhitelist(setOf(FluidTypeKeys.WATER)))

		override val displayHandler: TextDisplayHandler = DisplayHandlers.newMultiblockSignOverlay(
			this,
			{ SplitFluidDisplayModule(handler = it, storage = steamInput, offsetLeft = 3.0, offsetUp = getLinePos(4), offsetBack = 0.0, scale = MATCH_SIGN_FONT_SIZE) },
			{ SplitFluidDisplayModule(handler = it, storage = steamOutput, offsetLeft = -3.0, offsetUp = getLinePos(4), offsetBack = 0.0, scale = MATCH_SIGN_FONT_SIZE) },
			{ StatusDisplayModule(it, statusManager) },
		)

		override val ioData: IOData = IOData.Companion.builder(this)
			// Output
			.addPort(IOType.FLUID, -3, 0, 1) { IOPort.RegisteredMetaDataInput<FluidPortMetadata>(this, FluidPortMetadata(connectedStore = steamInput, inputAllowed = true, outputAllowed = false)) }
			.addPort(IOType.FLUID, 3, 0, 1) { IOPort.RegisteredMetaDataInput<FluidPortMetadata>(this, FluidPortMetadata(connectedStore = steamOutput, inputAllowed = false, outputAllowed = true)) }
			.build()

		override fun getStores(): List<FluidStorageContainer> = arrayListOf(steamInput, steamOutput)

		companion object {
			val STEAM_INPUT = NamespacedKeys.key("steam_input")
			val STEAM_OUTPUT = NamespacedKeys.key("steam_output")

			private val USAGE_MULTIPLIER get() = 0.3
		}

		override fun storeAdditionalData(store: PersistentMultiblockData, adapterContext: PersistentDataAdapterContext) {
			saveStorageData(store)
		}

		val rotationLinkage = createLinkage(0, 0, 1, RelativeFace.FORWARD) { it is RotationConsumer }

		override fun tickAsync() {
			bootstrapFluidNetwork()

			tickSteam()
		}

		var lastEnergy = 0.0

		fun tickSteam() {
			val delta = deltaTMS / 1000.0

			val steamStack = steamInput.getContents()

			if (steamStack.isEmpty()) {
				lastEnergy = 0.0
				setStatus(Component.text("Empty", NamedTextColor.RED))
				return
			}

			if (steamStack.type != FluidTypeKeys.STEAM) {
				lastEnergy = 0.0
				setStatus(Component.text("Empty", NamedTextColor.RED))
				return
			}

			val massFlow = getMassFlowRate(steamStack, location) * delta

			val specificEnthalpy = getSpecificEnthalpy(steamStack, location)
			val workPerMassFlow = specificEnthalpy / (1 - multiblock.efficiency) * specificEnthalpy

			val work = (workPerMassFlow * massFlow) / 60

			lastEnergy = work

			val removedVolume = metersCubedToLiters(massFlow / steamStack.type.getValue().getDensity(steamStack, location)) * USAGE_MULTIPLIER

			steamInput.removeAmount(removedVolume)
			val new = steamStack.asAmount(removedVolume)

			val baseTemperature = FluidPropertyTypeKeys.TEMPERATURE.getValue().getDefaultProperty(location).value
			val outletTemperature = ((steamStack.getDataOrDefault(FluidPropertyTypeKeys.PRESSURE.getValue(), location).value - baseTemperature) * multiblock.efficiency) + baseTemperature
			new.setData(FluidPropertyTypeKeys.TEMPERATURE.getValue(), FluidProperty.Temperature(outletTemperature))

			val basePressure = FluidPropertyTypeKeys.PRESSURE.getValue().getDefaultProperty(location).value
			val outletPressure = ((steamStack.getDataOrDefault(FluidPropertyTypeKeys.PRESSURE.getValue(), location).value - basePressure) * multiblock.efficiency) + basePressure
			new.setData(FluidPropertyTypeKeys.PRESSURE.getValue(), FluidProperty.Pressure(outletPressure))

			steamOutput.getContents().combine(new, location)
			setStatus(Component.text("Working", NamedTextColor.GREEN))
		}

		override fun getRotationSpeed(): Double {
			return lastEnergy
		}

		fun getSaturationTemperature(stack: FluidStack): Double {
			val pressure = stack.getDataOrDefault(FluidPropertyTypeKeys.PRESSURE.getValue(), location)
			return pressure.value.pow(0.25) * 100.0
		}

		fun isSaturated(stack: FluidStack): Boolean {
			val temperature = stack.getDataOrDefault(FluidPropertyTypeKeys.TEMPERATURE.getValue(), location).value
			return temperature >= getSaturationTemperature(stack)
		}

		fun getSuperheatDegrees(stack: FluidStack): Double {
			val temperature = stack.getDataOrDefault(FluidPropertyTypeKeys.TEMPERATURE.getValue(), location).value
			return temperature - getSaturationTemperature(stack)
		}

		/**
		 * Returns the mass flow rate, in kilograms per second
		 **/
		fun getMassFlowRate(stack: FluidStack, location: Location?): Double {
			val density = stack.type.getValue().getDensity(stack, location) // kg/m^3
			val crossSectionArea = multiblock.inletCrossSectionArea // m^2
			val velocity = 25.0 // m/s

			return density * crossSectionArea * velocity
		}

		fun getSpecificEnthalpy(stack: FluidStack, location: Location?): Double {
			val pressure = stack.getDataOrDefault(FluidPropertyTypeKeys.PRESSURE.getValue(), location).value

			val specificVolume = 1.0 / stack.type.getValue().getDensity(stack, location)
			val steamHeatCapacity = 2000 // j / kg

			return steamHeatCapacity + (pressure * (1.0 / specificVolume))
		}
	}
}
