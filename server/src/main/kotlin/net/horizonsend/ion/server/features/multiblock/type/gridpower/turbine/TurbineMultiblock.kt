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
import net.horizonsend.ion.server.features.transport.fluids.properties.FluidCategory
import net.horizonsend.ion.server.features.transport.fluids.properties.FluidProperty
import net.horizonsend.ion.server.features.transport.fluids.types.steam.Steam
import net.horizonsend.ion.server.features.transport.inputs.IOData
import net.horizonsend.ion.server.features.transport.inputs.IOPort
import net.horizonsend.ion.server.features.transport.inputs.IOType
import net.horizonsend.ion.server.miscellaneous.registrations.persistence.NamespacedKeys
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.RelativeFace
import net.horizonsend.ion.server.miscellaneous.utils.metersCubedToLiters
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.World
import org.bukkit.block.BlockFace
import org.bukkit.persistence.PersistentDataAdapterContext
import org.bukkit.persistence.PersistentDataType

abstract class TurbineMultiblock : Multiblock(), EntityMultiblock<TurbineMultiblockEntity> {
	override val name: String = "turbine"

	/**
	 * The amount of fluid processed from the input per second
	 **/
	abstract val flowRate: Double

	abstract val efficiency: Double

	/**
	 * The moment of inertia of the rotor
	 **/
	abstract val energyOutputAtTarget: Double

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

		override val ioData: IOData = IOData.builder(this)
			// Output
			.addPort(IOType.FLUID, -3, 0, 1) { IOPort.RegisteredMetaDataInput(this, FluidPortMetadata(connectedStore = steamInput, inputAllowed = true, outputAllowed = false)) }
			.addPort(IOType.FLUID, 3, 0, 1) { IOPort.RegisteredMetaDataInput(this, FluidPortMetadata(connectedStore = steamOutput, inputAllowed = false, outputAllowed = true)) }
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

		override fun getOutputEnergy(): Double {
			return (minOf(multiblock.targetRPM, rpm) / multiblock.targetRPM) * multiblock.energyOutputAtTarget
		}

		fun decreaseRPM(deltaSeconds: Double) {
			if (rpm <= 0.0) return

			// Decrement by 1
			rpm = maxOf(0.0, rpm - 50.0 * deltaSeconds)
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
			val massFlow = minOf(steamStack.amount, multiblock.flowRate * deltaSeconds)

			rpm = if (rpm < multiblock.targetRPM) minOf(multiblock.targetRPM, rpm + (100 * deltaSeconds)) else rpm

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
	}
}
