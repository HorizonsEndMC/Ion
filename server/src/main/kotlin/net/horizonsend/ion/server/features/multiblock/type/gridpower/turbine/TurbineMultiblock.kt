package net.horizonsend.ion.server.features.multiblock.type.gridpower.turbine

import net.horizonsend.ion.common.utils.miscellaneous.roundToHundredth
import net.horizonsend.ion.common.utils.text.colors.HEColorScheme.Companion.HE_LIGHT_BLUE
import net.horizonsend.ion.common.utils.text.colors.HEColorScheme.Companion.HE_MEDIUM_GRAY
import net.horizonsend.ion.common.utils.text.ofChildren
import net.horizonsend.ion.server.core.registration.keys.FluidPropertyTypeKeys
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
import net.horizonsend.ion.server.features.transport.fluids.FluidUtils
import net.horizonsend.ion.server.features.transport.fluids.properties.FluidCategory
import net.horizonsend.ion.server.features.transport.fluids.properties.FluidProperty
import net.horizonsend.ion.server.features.transport.fluids.types.steam.Steam
import net.horizonsend.ion.server.features.transport.inputs.IOData
import net.horizonsend.ion.server.features.transport.inputs.IOPort
import net.horizonsend.ion.server.features.transport.inputs.IOType
import net.horizonsend.ion.server.miscellaneous.registrations.persistence.NamespacedKeys
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.RelativeFace
import net.horizonsend.ion.server.miscellaneous.utils.getAngularVelocity
import net.horizonsend.ion.server.miscellaneous.utils.getRotationalEnergy
import net.horizonsend.ion.server.miscellaneous.utils.gramsToKilograms
import net.horizonsend.ion.server.miscellaneous.utils.hertzToRPM
import net.horizonsend.ion.server.miscellaneous.utils.metersCubedToLiters
import net.horizonsend.ion.server.miscellaneous.utils.solveForAngularVelocity
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.Location
import org.bukkit.World
import org.bukkit.block.BlockFace
import org.bukkit.persistence.PersistentDataAdapterContext
import org.bukkit.persistence.PersistentDataType
import kotlin.math.PI
import kotlin.math.sqrt

abstract class TurbineMultiblock : Multiblock(), EntityMultiblock<TurbineMultiblockEntity> {
	override val name: String = "turbine"

	/**
	 * The cross-sectional area of the steam input, in meters squared
	 **/
	abstract val inletCrossSectionArea: Double

	abstract val efficiency: Double

	/**
	 * The moment of inertia of the rotor
	 **/
	abstract val rotorMomentOfInertia: Double

	abstract val targetRPM: Double

	override val signText: Array<Component?> = createSignText(
		ofChildren(Component.text("Steam", HE_LIGHT_BLUE), Component.text(" Turbine", HE_MEDIUM_GRAY)),
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
		val secondaryStatusModule: StatusMultiblockEntity.StatusManager = StatusMultiblockEntity.StatusManager()

		val steamInput = FluidStorageContainer(data, "steam_input", Component.text("Steam Input"), STEAM_INPUT, 1_000_000.0, FluidRestriction.FluidCategoryWhitelist(setOf(FluidCategory.STEAM)))
		val steamOutput = FluidStorageContainer(data, "steam_output", Component.text("Steam Output"), STEAM_OUTPUT, 1_000_000.0, FluidRestriction.Unlimited)

		override val displayHandler: TextDisplayHandler = DisplayHandlers.newMultiblockSignOverlay(
			this,
			{ SplitFluidDisplayModule(handler = it, storage = steamInput, offsetLeft = 3.0, offsetUp = getLinePos(4), offsetBack = 0.0, scale = MATCH_SIGN_FONT_SIZE) },
			{ SplitFluidDisplayModule(handler = it, storage = steamOutput, offsetLeft = -3.0, offsetUp = getLinePos(4), offsetBack = 0.0, scale = MATCH_SIGN_FONT_SIZE) },
			{ StatusDisplayModule(it, secondaryStatusModule, offsetUp = getLinePos(3)) },
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

			private val USAGE_MULTIPLIER get() = 0.025

			private val RPM_KEY = NamespacedKeys.key("rpm")
		}

		override fun storeAdditionalData(store: PersistentMultiblockData, adapterContext: PersistentDataAdapterContext) {
			saveStorageData(store)
			store.addAdditionalData(RPM_KEY, PersistentDataType.DOUBLE, rpm)
		}

		private val rotationLinkage = createLinkage(0, 0, 1, RelativeFace.FORWARD) { it is RotationConsumer }

		private var rpm = data.getAdditionalDataOrDefault(RPM_KEY, PersistentDataType.DOUBLE, 0.0)

		override fun tickAsync() {
			bootstrapFluidNetwork()

			// Calculate delta before any returns so values stay consistent
			val deltaSeconds = deltaTMS / 1000.0

			decreaseRPM(deltaSeconds)
			tickSteam(deltaSeconds)
		}

		override fun getRotationInertia(): Double {
			return getRotationalEnergy()
		}

		val minimumSlowingJoules get() = 1000.0

		fun decreaseRPM(deltaSeconds: Double) {
			if (rpm <= 0.0) return

			val opposingForce = (rotationLinkage.get() as? RotationConsumer)?.getSlowingJoules()
			val slowingJoules = maxOf(minimumSlowingJoules, opposingForce ?: 0.0) * deltaSeconds

			var rotationalEnergy = getRotationalEnergy()

			val resistance = sqrt(rotationalEnergy)

			println("Slowed by ${(slowingJoules + resistance)}, $rotationalEnergy total")
			rotationalEnergy -= (slowingJoules + resistance)

			if (rotationalEnergy <= 0) {
				rpm = 0.0
				return
			}

			// Decrease energy and solve for new rpm
			val newRPM = getRPM(rotationalEnergy)

			// Decrement by 1
			rpm = maxOf(0.0, newRPM)
		}

		fun tickSteam(deltaSeconds: Double) {
			val steamStack = steamInput.getContents()

			secondaryStatusModule.setStatus(Component.text("${rpm.roundToHundredth()} RPM"))

			if (steamStack.isEmpty()) {
				// Rpm will decrease
				setStatus(Component.text("Empty", NamedTextColor.RED))
				return
			}

			// This shouldn't happen since its category restricted
			if (!steamStack.type.getValue().categories.contains(FluidCategory.STEAM)) {
				// Rpm will decrease
				setStatus(Component.text("Invalid Input!", NamedTextColor.RED))
				return
			}

			val type = steamStack.type.getValue() as Steam

			// The mass flow, in kilograms
			val inputWeight = gramsToKilograms(FluidUtils.getFluidWeight(steamStack, location))
			val massFlow = minOf(inputWeight, getMassFlowRate(steamStack, location) * deltaSeconds)

			// The specific enthalpy, in joules per kilogram
			val specificEnthalpy = type.turbineWorkPerKilogram

			// The amount of work that can be done is calculated by the difference in energy
			// between the input and output. Here, the output energy efficiency is hardcoded,
			// so the amount of work done is equal to specific enthalpy divded the remaining energy of
			// the output
			// Stored in
			val workPerMassFlow = multiblock.efficiency * specificEnthalpy

			// Simply multiply the work per mass flow by the mass flow
			val work = workPerMassFlow * massFlow

			println("Added $work")
			var rotationalEnergy = getRotationalEnergy()
			rotationalEnergy += work

			// Increase energy and solve for new rpm
			var newRPM = getRPM(rotationalEnergy)

			// Decrease the mass flow if speed is going beyond target
			if (newRPM > multiblock.targetRPM) { //TODO deduplicate
				newRPM = multiblock.targetRPM
			}

			rpm = newRPM

			// Get the removed volume using the density of the steam and the mass flow rate
			val idealRemoved = metersCubedToLiters(massFlow / steamStack.type.getValue().getDensity(steamStack, location))
			val removedVolume = minOf(steamStack.amount, idealRemoved * USAGE_MULTIPLIER)

			// Clone before removed
			val new = steamStack.asAmount(removedVolume)
			new.type = type.turbineResult

			steamInput.removeAmount(removedVolume)

			// Scale the properties down by the efficiency
			val baseTemperature = FluidPropertyTypeKeys.TEMPERATURE.getValue().getDefaultProperty(location).value
			val outletTemperature = ((steamStack.getDataOrDefault(FluidPropertyTypeKeys.TEMPERATURE.getValue(), location).value - baseTemperature) * multiblock.efficiency) + baseTemperature
			new.setData(FluidPropertyTypeKeys.TEMPERATURE.getValue(), FluidProperty.Temperature(outletTemperature))

			steamOutput.addFluid(new, location)
			setStatus(Component.text("Working", NamedTextColor.GREEN))
		}

		/**
		 * Returns the mass flow rate, in kilograms per second
		 **/
		fun getMassFlowRate(stack: FluidStack, location: Location?): Double {
			val density = stack.type.getValue().getDensity(stack, location) // kg/m^3
			val crossSectionArea = (multiblock.inletCrossSectionArea) // m^2
			val velocity = 25.0 // m/s

			return density * crossSectionArea * velocity
		}

		fun getRotationalEnergy(): Double {
			return getRotationalEnergy(getAngularVelocity(rpm), multiblock.rotorMomentOfInertia)
		}

		fun getRPM(rotationalEnergy: Double): Double {
			val solved = solveForAngularVelocity(rotationalEnergy, multiblock.rotorMomentOfInertia)
			return hertzToRPM(solved / (2.0 * PI))
		}
	}
}
