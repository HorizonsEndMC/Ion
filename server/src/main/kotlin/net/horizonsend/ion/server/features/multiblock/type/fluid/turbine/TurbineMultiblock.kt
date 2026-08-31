package net.horizonsend.ion.server.features.multiblock.type.fluid.turbine

import net.horizonsend.ion.common.utils.text.colors.HEColorScheme.Companion.HE_LIGHT_BLUE
import net.horizonsend.ion.common.utils.text.colors.HEColorScheme.Companion.HE_MEDIUM_GRAY
import net.horizonsend.ion.common.utils.text.ofChildren
import net.horizonsend.ion.server.core.registration.keys.FluidTypeKeys
import net.horizonsend.ion.server.features.client.display.modular.DisplayHandlers
import net.horizonsend.ion.server.features.client.display.modular.TextDisplayHandler
import net.horizonsend.ion.server.features.client.display.modular.display.MATCH_SIGN_FONT_SIZE
import net.horizonsend.ion.server.features.client.display.modular.display.PowerEntityDisplayModule
import net.horizonsend.ion.server.features.client.display.modular.display.fluid.SplitFluidDisplayModule
import net.horizonsend.ion.server.features.client.display.modular.display.getLinePos
import net.horizonsend.ion.server.features.multiblock.Multiblock
import net.horizonsend.ion.server.features.multiblock.entity.PersistentMultiblockData
import net.horizonsend.ion.server.features.multiblock.entity.type.fluids.FluidInputMetadata
import net.horizonsend.ion.server.features.multiblock.entity.type.fluids.FluidStoringMultiblock
import net.horizonsend.ion.server.features.multiblock.entity.type.fluids.storage.FluidRestriction
import net.horizonsend.ion.server.features.multiblock.entity.type.fluids.storage.FluidStorageContainer
import net.horizonsend.ion.server.features.multiblock.entity.type.power.SimplePoweredEntity
import net.horizonsend.ion.server.features.multiblock.entity.type.ticked.AsyncTickingMultiblockEntity
import net.horizonsend.ion.server.features.multiblock.entity.type.ticked.TickedMultiblockEntityParent
import net.horizonsend.ion.server.features.multiblock.manager.MultiblockManager
import net.horizonsend.ion.server.features.multiblock.type.EntityMultiblock
import net.horizonsend.ion.server.features.multiblock.type.fluid.turbine.TurbineMultiblock.TurbineMultiblockEntity
import net.horizonsend.ion.server.features.transport.inputs.IOData
import net.horizonsend.ion.server.features.transport.inputs.IOPort
import net.horizonsend.ion.server.features.transport.inputs.IOType
import net.horizonsend.ion.server.miscellaneous.registrations.persistence.NamespacedKeys
import net.kyori.adventure.text.Component
import org.bukkit.World
import org.bukkit.block.BlockFace
import org.bukkit.persistence.PersistentDataAdapterContext
import org.bukkit.persistence.PersistentDataType
import kotlin.math.floor

abstract class TurbineMultiblock : Multiblock(), EntityMultiblock<TurbineMultiblockEntity> {
	override val name: String = "turbine"

	abstract val maximumSteamConsumptionPerSecond: Double
	abstract val maximumPowerGenerationPerSecond: Double
	abstract val steamInputCapacity: Double

	override val signText: Array<Component?> = createSignText(
		ofChildren(Component.text("Steam", HE_LIGHT_BLUE), Component.text(" Turbine", HE_MEDIUM_GRAY)),
		null,
		null,
		null
	)

	override fun createEntity(
		manager: MultiblockManager,
		data: PersistentMultiblockData,
		world: World,
		x: Int,
		y: Int,
		z: Int,
		structureDirection: BlockFace
	): TurbineMultiblockEntity {
		return TurbineMultiblockEntity(data, manager, this, world, x, y, z, structureDirection)
	}

	class TurbineMultiblockEntity(
		data: PersistentMultiblockData,
		manager: MultiblockManager,
		override val multiblock: TurbineMultiblock,
		world: World,
		x: Int,
		y: Int,
		z: Int,
		structureDirection: BlockFace
	) : SimplePoweredEntity(data, multiblock, manager, x, y, z, world, structureDirection, MAXIMUM_POWER_STORAGE),
		AsyncTickingMultiblockEntity,
		FluidStoringMultiblock {
		override val tickingManager: TickedMultiblockEntityParent.TickingManager = TickedMultiblockEntityParent.TickingManager(2)

		val steamInput = FluidStorageContainer(
			data,
			"steam_input",
			Component.text("Dense Steam Input"),
			STEAM_INPUT_KEY,
			multiblock.steamInputCapacity,
			FluidRestriction.FluidTypeWhitelist(setOf(FluidTypeKeys.DENSE_STEAM))
		)

		private var generatedPowerRemainder = data.getAdditionalDataOrDefault(POWER_REMAINDER_KEY, PersistentDataType.DOUBLE, 0.0)

		override val ioData: IOData = IOData.builder(this)
			.addPort(IOType.FLUID, -1, 0, 0) {
				IOPort.RegisteredMetaDataInput(
					this,
					FluidInputMetadata(connectedStore = steamInput, inputAllowed = true, outputAllowed = false)
				)
			}
			.addPort(IOType.FLUID, 1, 0, 0) {
				IOPort.RegisteredMetaDataInput(
					this,
					FluidInputMetadata(connectedStore = steamInput, inputAllowed = true, outputAllowed = false)
				)
			}
			.addPowerInput(0, -1, 0)
			.build()

		override val displayHandler: TextDisplayHandler = DisplayHandlers.newMultiblockSignOverlay(
			this,
			{
				SplitFluidDisplayModule(
					handler = it,
					storage = steamInput,
					offsetLeft = 0.0,
					offsetUp = getLinePos(4),
					offsetBack = 0.0,
					scale = MATCH_SIGN_FONT_SIZE
				)
			},
			{
				PowerEntityDisplayModule(
					handler = it,
					multiblockEntity = this,
					offsetLeft = 0.0,
					offsetUp = getLinePos(2),
					offsetBack = 0.0,
					scale = MATCH_SIGN_FONT_SIZE
				)
			}
		)

		override fun getStores(): List<FluidStorageContainer> = listOf(steamInput)

		override fun storeAdditionalData(store: PersistentMultiblockData, adapterContext: PersistentDataAdapterContext) {
			savePowerData(store)
			saveStorageData(store)
			store.addAdditionalData(POWER_REMAINDER_KEY, PersistentDataType.DOUBLE, generatedPowerRemainder)
		}

		override fun tickAsync() {
			bootstrapNetwork()

			val deltaSeconds = minOf(deltaTMS, MAXIMUM_DELTA_MILLIS).toDouble() / 1000.0
			generatePower(deltaSeconds)
		}

		private fun generatePower(deltaSeconds: Double) {
			if (powerStorage.isFull()) return

			val steam = steamInput.getContents()
			if (steam.isEmpty()) return

			if (steam.type != FluidTypeKeys.DENSE_STEAM) return

			val powerPerLiter = multiblock.maximumPowerGenerationPerSecond / multiblock.maximumSteamConsumptionPerSecond
			val remainingPowerCapacity = powerStorage.getRemainingCapacity().toDouble() - generatedPowerRemainder
			if (remainingPowerCapacity <= EPSILON) return

			val steamAllowedByRate = multiblock.maximumSteamConsumptionPerSecond * deltaSeconds
			val steamAllowedByPowerStorage = remainingPowerCapacity / powerPerLiter
			val steamToConsume = minOf(steam.amount, steamAllowedByRate, steamAllowedByPowerStorage)
			if (steamToConsume <= EPSILON) return

			steamInput.removeAmount(steamToConsume)

			val exactGeneratedPower = generatedPowerRemainder + (steamToConsume * powerPerLiter)
			val wholeGeneratedPower = floor(exactGeneratedPower).toInt()
			generatedPowerRemainder = exactGeneratedPower - wholeGeneratedPower

			if (wholeGeneratedPower > 0) powerStorage.addPower(wholeGeneratedPower)
		}

		companion object {
			private const val MAXIMUM_POWER_STORAGE = 500_000
			private const val MAXIMUM_DELTA_MILLIS = 1_000L
			private const val EPSILON = 0.000_001

			private val STEAM_INPUT_KEY = NamespacedKeys.key("turbine_steam_input")
			private val POWER_REMAINDER_KEY = NamespacedKeys.key("turbine_power_remainder")
		}
	}
}
