package net.horizonsend.ion.server.features.multiblock.type.fluid.boiler

import net.horizonsend.ion.common.utils.text.colors.HEColorScheme
import net.horizonsend.ion.common.utils.text.ofChildren
import net.horizonsend.ion.server.core.registration.keys.FluidTypeKeys
import net.horizonsend.ion.server.features.client.display.modular.DisplayHandlers
import net.horizonsend.ion.server.features.client.display.modular.TextDisplayHandler
import net.horizonsend.ion.server.features.client.display.modular.display.MATCH_SIGN_FONT_SIZE
import net.horizonsend.ion.server.features.client.display.modular.display.StatusDisplayModule
import net.horizonsend.ion.server.features.client.display.modular.display.fluid.ComplexFluidDisplayModule
import net.horizonsend.ion.server.features.client.display.modular.display.getLinePos
import net.horizonsend.ion.server.features.industry.ItemFuelProperties
import net.horizonsend.ion.server.features.multiblock.Multiblock
import net.horizonsend.ion.server.features.multiblock.entity.MultiblockEntity
import net.horizonsend.ion.server.features.multiblock.entity.PersistentMultiblockData
import net.horizonsend.ion.server.features.multiblock.entity.type.DisplayMultiblockEntity
import net.horizonsend.ion.server.features.multiblock.entity.type.StatusMultiblockEntity
import net.horizonsend.ion.server.features.multiblock.entity.type.StatusMultiblockEntity.StatusManager
import net.horizonsend.ion.server.features.multiblock.entity.type.fluids.FluidInputMetadata
import net.horizonsend.ion.server.features.multiblock.entity.type.fluids.FluidStoringMultiblock
import net.horizonsend.ion.server.features.multiblock.entity.type.fluids.storage.FluidRestriction
import net.horizonsend.ion.server.features.multiblock.entity.type.fluids.storage.FluidStorageContainer
import net.horizonsend.ion.server.features.multiblock.entity.type.ticked.AsyncTickingMultiblockEntity
import net.horizonsend.ion.server.features.multiblock.entity.type.ticked.TickedMultiblockEntityParent
import net.horizonsend.ion.server.features.multiblock.manager.MultiblockManager
import net.horizonsend.ion.server.features.multiblock.shape.MultiblockShape
import net.horizonsend.ion.server.features.multiblock.type.EntityMultiblock
import net.horizonsend.ion.server.features.multiblock.type.fluid.boiler.BoilerMultiblockItemFuel.ItemBoilerEntity
import net.horizonsend.ion.server.features.multiblock.util.PrepackagedPreset
import net.horizonsend.ion.server.features.transport.fluids.FluidStack
import net.horizonsend.ion.server.features.transport.inputs.IOData
import net.horizonsend.ion.server.features.transport.inputs.IOPort
import net.horizonsend.ion.server.features.transport.inputs.IOType
import net.horizonsend.ion.server.miscellaneous.registrations.persistence.NamespacedKeys
import net.horizonsend.ion.server.miscellaneous.utils.Tasks
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.RelativeFace
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.Material
import org.bukkit.Particle
import org.bukkit.World
import org.bukkit.block.BlockFace
import org.bukkit.block.data.type.Slab
import org.bukkit.persistence.PersistentDataAdapterContext
import org.bukkit.persistence.PersistentDataType.DOUBLE
import org.bukkit.persistence.PersistentDataType.LONG
import kotlin.math.min
import kotlin.math.roundToInt
import kotlin.random.Random

object BoilerMultiblockItemFuel : Multiblock(), EntityMultiblock<ItemBoilerEntity> {
	override val name: String = "boiler"
	override val signText: Array<Component?> = createSignText(
		ofChildren(text("Item ", NamedTextColor.RED), text("Boiler", HEColorScheme.HE_MEDIUM_GRAY)),
		null,
		null,
		null
	)

	override fun MultiblockShape.buildStructure() {
		z(6) {
			y(-1) {
				x(-3).anySlab(PrepackagedPreset.slab(Slab.Type.TOP))
				x(-2).titaniumBlock()
				x(-1).titaniumBlock()
				x(0).titaniumBlock()
				x(1).titaniumBlock()
				x(2).titaniumBlock()
				x(3).anySlab(PrepackagedPreset.slab(Slab.Type.TOP))
			}
			y(0) {
				x(-3).anyWall()
				x(-2).type(Material.MUD_BRICKS)
				x(-1).type(Material.MUD_BRICKS)
				x(0).type(Material.MUD_BRICKS)
				x(1).type(Material.MUD_BRICKS)
				x(2).type(Material.MUD_BRICKS)
				x(3).anyWall()
			}
			y(1) {
				x(-3).anySlab(PrepackagedPreset.slab(Slab.Type.BOTTOM))
				x(-2).titaniumBlock()
				x(-1).titaniumBlock()
				x(0).fluidInput()
				x(1).titaniumBlock()
				x(2).titaniumBlock()
				x(3).anySlab(PrepackagedPreset.slab(Slab.Type.BOTTOM))
			}
			y(2) {
				x(0).anyFluidPipe()
			}
		}
		z(5) {
			y(-1) {
				x(-3).titaniumBlock()
				x(-2).titaniumBlock()
				x(-1).type(Material.MUD_BRICKS)
				x(0).type(Material.MUD_BRICKS)
				x(1).type(Material.MUD_BRICKS)
				x(2).titaniumBlock()
				x(3).titaniumBlock()
			}
			y(0) {
				x(-3).type(Material.MUD_BRICKS)
				x(-2).type(Material.MUD_BRICKS)
				x(2).type(Material.MUD_BRICKS)
				x(3).type(Material.MUD_BRICKS)
			}
			y(1) {
				x(-3).titaniumBlock()
				x(-2).titaniumBlock()
				x(-1).type(Material.MUD_BRICKS)
				x(0).type(Material.MUD_BRICKS)
				x(1).type(Material.MUD_BRICKS)
				x(2).titaniumBlock()
				x(3).titaniumBlock()
			}
			y(2) {
				x(-1).steelBlock()
				x(0).steelBlock()
				x(1).steelBlock()
			}
			y(3) {
				x(-1).titaniumBlock()
				x(0).titaniumBlock()
				x(1).titaniumBlock()
			}
			y(4) {
				x(-1).titaniumBlock()
				x(0).anyCopperGrate()
				x(1).titaniumBlock()
			}
			y(5) {
				x(-1).titaniumBlock()
				x(0).anyCopperGrate()
				x(1).titaniumBlock()
			}
			y(6) {
				x(-1).titaniumBlock()
				x(0).titaniumBlock()
				x(1).titaniumBlock()
			}
			y(7) {
				x(-1).anySlab(PrepackagedPreset.slab(Slab.Type.BOTTOM))
				x(0).titaniumBlock()
				x(1).anySlab(PrepackagedPreset.slab(Slab.Type.BOTTOM))
			}
		}
		z(4) {
			y(-1) {
				x(-3).titaniumBlock()
				x(-2).type(Material.MUD_BRICKS)
				x(-1).type(Material.MUD_BRICKS)
				x(0).type(Material.MUD_BRICKS)
				x(1).type(Material.MUD_BRICKS)
				x(2).type(Material.MUD_BRICKS)
				x(3).titaniumBlock()
			}
			y(0) {
				x(-3).type(Material.MUD_BRICKS)
				x(3).type(Material.MUD_BRICKS)
			}
			y(1) {
				x(-3).titaniumBlock()
				x(-2).type(Material.MUD_BRICKS)
				x(0).type(Material.MUD_BRICKS)
				x(2).type(Material.MUD_BRICKS)
				x(3).titaniumBlock()
			}
			y(2) {
				x(-2).steelBlock()
				x(2).steelBlock()
			}
			y(3) {
				x(-2).titaniumBlock()
				x(2).titaniumBlock()
			}
			y(4) {
				x(-2).titaniumBlock()
				x(2).titaniumBlock()
			}
			y(5) {
				x(-2).titaniumBlock()
				x(2).titaniumBlock()
			}
			y(6) {
				x(-2).titaniumBlock()
				x(2).titaniumBlock()
			}
			y(7) {
				x(-2).anySlab(PrepackagedPreset.slab(Slab.Type.BOTTOM))
				x(-1).titaniumBlock()
				x(1).titaniumBlock()
				x(2).anySlab(PrepackagedPreset.slab(Slab.Type.BOTTOM))
			}
			y(8) {
				x(-1).anySlab(PrepackagedPreset.slab(Slab.Type.BOTTOM))
				x(0).titaniumBlock()
				x(1).anySlab(PrepackagedPreset.slab(Slab.Type.BOTTOM))
			}
		}
		z(3) {
			y(-1) {
				x(-3).titaniumBlock()
				x(-2).type(Material.MUD_BRICKS)
				x(-1).type(Material.MUD_BRICKS)
				x(0).type(Material.MUD_BRICKS)
				x(1).type(Material.MUD_BRICKS)
				x(2).type(Material.MUD_BRICKS)
				x(3).titaniumBlock()
			}
			y(0) {
				x(-3).fluidInput()
				x(3).fluidInput()
			}
			y(1) {
				x(-3).titaniumBlock()
				x(-2).type(Material.MUD_BRICKS)
				x(-1).type(Material.MUD_BRICKS)
				x(0).type(Material.MUD_BRICKS)
				x(1).type(Material.MUD_BRICKS)
				x(2).type(Material.MUD_BRICKS)
				x(3).titaniumBlock()
			}
			y(2) {
				x(-2).steelBlock()
				x(2).steelBlock()
			}
			y(3) {
				x(-2).titaniumBlock()
				x(2).titaniumBlock()
			}
			y(4) {
				x(-2).anyCopperGrate()
				x(2).anyCopperGrate()
			}
			y(5) {
				x(-2).anyCopperGrate()
				x(2).anyCopperGrate()
			}
			y(6) {
				x(-2).titaniumBlock()
				x(2).titaniumBlock()
			}
			y(7) {
				x(-2).titaniumBlock()
				x(2).titaniumBlock()
			}
			y(8) {
				x(-1).titaniumBlock()
				x(0).titaniumBlock()
				x(1).titaniumBlock()
			}
		}
		z(2) {
			y(-1) {
				x(-3).titaniumBlock()
				x(-2).type(Material.MUD_BRICKS)
				x(-1).type(Material.MUD_BRICKS)
				x(0).type(Material.MUD_BRICKS)
				x(1).type(Material.MUD_BRICKS)
				x(2).type(Material.MUD_BRICKS)
				x(3).titaniumBlock()
			}
			y(0) {
				x(-3).type(Material.MUD_BRICKS)
				x(3).type(Material.MUD_BRICKS)
			}
			y(1) {
				x(-3).titaniumBlock()
				x(-2).type(Material.MUD_BRICKS)
				x(0).type(Material.MUD_BRICKS)
				x(2).type(Material.MUD_BRICKS)
				x(3).titaniumBlock()
			}
			y(2) {
				x(-2).steelBlock()
				x(2).steelBlock()
			}
			y(3) {
				x(-2).titaniumBlock()
				x(2).titaniumBlock()
			}
			y(4) {
				x(-2).titaniumBlock()
				x(2).titaniumBlock()
			}
			y(5) {
				x(-2).titaniumBlock()
				x(2).titaniumBlock()
			}
			y(6) {
				x(-2).titaniumBlock()
				x(2).titaniumBlock()
			}
			y(7) {
				x(-2).anySlab(PrepackagedPreset.slab(Slab.Type.BOTTOM))
				x(-1).titaniumBlock()
				x(1).titaniumBlock()
				x(2).anySlab(PrepackagedPreset.slab(Slab.Type.BOTTOM))
			}
			y(8) {
				x(-1).anySlab(PrepackagedPreset.slab(Slab.Type.BOTTOM))
				x(0).titaniumBlock()
				x(1).anySlab(PrepackagedPreset.slab(Slab.Type.BOTTOM))
			}
		}
		z(1) {
			y(-1) {
				x(-3).titaniumBlock()
				x(-2).titaniumBlock()
				x(-1).type(Material.MUD_BRICKS)
				x(0).type(Material.MUD_BRICKS)
				x(1).type(Material.MUD_BRICKS)
				x(2).titaniumBlock()
				x(3).titaniumBlock()
			}
			y(0) {
				x(-3).type(Material.MUD_BRICKS)
				x(-2).type(Material.MUD_BRICKS)
				x(-1).type(Material.IRON_BARS)
				x(0).type(Material.IRON_BARS)
				x(1).type(Material.IRON_BARS)
				x(2).type(Material.MUD_BRICKS)
				x(3).type(Material.MUD_BRICKS)
			}
			y(1) {
				x(-3).titaniumBlock()
				x(-2).titaniumBlock()
				x(-1).type(Material.MUD_BRICKS)
				x(0).type(Material.MUD_BRICKS)
				x(1).type(Material.MUD_BRICKS)
				x(2).titaniumBlock()
				x(3).titaniumBlock()
			}
			y(2) {
				x(-1).steelBlock()
				x(0).steelBlock()
				x(1).steelBlock()
			}
			y(3) {
				x(-1).titaniumBlock()
				x(0).titaniumBlock()
				x(1).titaniumBlock()
			}
			y(4) {
				x(-1).titaniumBlock()
				x(0).anyCopperGrate()
				x(1).titaniumBlock()
			}
			y(5) {
				x(-1).titaniumBlock()
				x(0).anyCopperGrate()
				x(1).titaniumBlock()
			}
			y(6) {
				x(-1).titaniumBlock()
				x(0).titaniumBlock()
				x(1).titaniumBlock()
			}
			y(7) {
				x(-1).anySlab(PrepackagedPreset.slab(Slab.Type.BOTTOM))
				x(0).titaniumBlock()
				x(1).anySlab(PrepackagedPreset.slab(Slab.Type.BOTTOM))
			}
		}
		z(0) {
			y(-1) {
				x(-3).anySlab(PrepackagedPreset.slab(Slab.Type.TOP))
				x(-2).titaniumBlock()
				x(-1).titaniumBlock()
				x(0).anyPipedInventory()
				x(1).titaniumBlock()
				x(2).titaniumBlock()
				x(3).anySlab(PrepackagedPreset.slab(Slab.Type.TOP))
			}
			y(0) {
				x(-3).anyWall()
				x(-2).type(Material.MUD_BRICKS)
				x(-1).anyGlassPane(PrepackagedPreset.pane(RelativeFace.FORWARD, RelativeFace.RIGHT, RelativeFace.LEFT))
				x(0).anyGlass()
				x(1).anyGlassPane(PrepackagedPreset.pane(RelativeFace.FORWARD, RelativeFace.RIGHT, RelativeFace.LEFT))
				x(2).type(Material.MUD_BRICKS)
				x(3).anyWall()
			}
			y(1) {
				x(-3).anySlab(PrepackagedPreset.slab(Slab.Type.BOTTOM))
				x(-2).titaniumBlock()
				x(-1).titaniumBlock()
				x(0).titaniumBlock()
				x(1).titaniumBlock()
				x(2).titaniumBlock()
				x(3).anySlab(PrepackagedPreset.slab(Slab.Type.BOTTOM))
			}
		}
	}

	override fun createEntity(
		manager: MultiblockManager,
		data: PersistentMultiblockData,
		world: World,
		x: Int,
		y: Int,
		z: Int,
		structureDirection: BlockFace
	): ItemBoilerEntity {
		return ItemBoilerEntity(manager, data, world, x, y, z, structureDirection)
	}

	class ItemBoilerEntity(
		manager: MultiblockManager,
		data: PersistentMultiblockData,
		world: World,
		x: Int,
		y: Int,
		z: Int,
		structureDirection: BlockFace
	) : MultiblockEntity(manager, BoilerMultiblockItemFuel, world, x, y, z, structureDirection),
		DisplayMultiblockEntity,
		FluidStoringMultiblock,
		StatusMultiblockEntity,
		AsyncTickingMultiblockEntity {
		override val tickingManager: TickedMultiblockEntityParent.TickingManager = TickedMultiblockEntityParent.TickingManager(2)
		override val statusManager: StatusManager = StatusManager()

		val fluidInput = FluidStorageContainer(
			data,
			"primaryin",
			text("Water Input"),
			NamespacedKeys.key("primaryin"),
			10_000.0,
			FluidRestriction.FluidTypeWhitelist(setOf(FluidTypeKeys.WATER))
		)
		val fluidOutput = FluidStorageContainer(
			data,
			"primaryout",
			text("Dense Steam Output"),
			NamespacedKeys.key("primaryout"),
			100.0,
			FluidRestriction.FluidTypeWhitelist(setOf(FluidTypeKeys.DENSE_STEAM))
		)
		val pollutionStorage = FluidStorageContainer(
			data,
			"pollution_out",
			text("Pollution Output"),
			NamespacedKeys.key("pollution_out"),
			100_000.0,
			FluidRestriction.FluidTypeWhitelist(setOf(FluidTypeKeys.POLLUTION))
		)

		private var boilerTemperature = data.getAdditionalDataOrDefault(TEMPERATURE_KEY, DOUBLE, AMBIENT_TEMPERATURE)
		private var burningEnds = data.getAdditionalDataOrDefault(BURNING_ENDS_KEY, LONG, 0L)
		private var burningOutput = data.getAdditionalDataOrDefault(BURNING_OUTPUT_KEY, DOUBLE, 0.0)
		@Volatile private var steamOutputUnlocked = fluidOutput.getContents().amount >= MINIMUM_STEAM_OUTPUT_RELEASE

		override val ioData: IOData = IOData.builder(this)
			.addPort(IOType.FLUID, -3, 0, 3) {
				IOPort.RegisteredMetaDataInput(this, FluidInputMetadata(fluidInput, inputAllowed = true, outputAllowed = false))
			}
			.addPort(IOType.FLUID, 3, 0, 3) {
				IOPort.RegisteredMetaDataInput(
					this,
					FluidInputMetadata(
						fluidOutput,
						inputAllowed = false,
						outputAllowed = true,
						outputCondition = ::canExtractSteam
					)
				)
			}
			.addPort(IOType.FLUID, 0, 1, 6) {
				IOPort.RegisteredMetaDataInput(this, FluidInputMetadata(pollutionStorage, inputAllowed = false, outputAllowed = true))
			}
			.build()

		override val displayHandler: TextDisplayHandler = DisplayHandlers.newMultiblockSignOverlay(this,
			{ ComplexFluidDisplayModule(handler = it, container = fluidInput, title = text("Input"), offsetLeft = 3.5, offsetUp = 1.15, offsetBack = -4.0 + 0.39, scale = 0.7f, RelativeFace.RIGHT) },
			{ ComplexFluidDisplayModule(handler = it, container = fluidOutput, title = text("Output"), offsetLeft = -3.5, offsetUp = 1.15, offsetBack = -4.0 + 0.39, scale = 0.7f, RelativeFace.LEFT) },
			{ StatusDisplayModule(handler = it, statusSupplier = statusManager, offsetLeft = 0.0, offsetUp = getLinePos(4), offsetBack = 0.0, scale = MATCH_SIGN_FONT_SIZE) },
		)

		override fun getStores(): List<FluidStorageContainer> = listOf(fluidInput, fluidOutput, pollutionStorage)

		override fun storeAdditionalData(store: PersistentMultiblockData, adapterContext: PersistentDataAdapterContext) {
			saveStorageData(store)
			store.addAdditionalData(TEMPERATURE_KEY, DOUBLE, boilerTemperature)
			store.addAdditionalData(BURNING_ENDS_KEY, LONG, burningEnds)
			store.addAdditionalData(BURNING_OUTPUT_KEY, DOUBLE, burningOutput)
		}

		override fun tickAsync() {
			bootstrapNetwork()
			val deltaSeconds = min(deltaTMS, MAXIMUM_DELTA_MILLIS).toDouble() / 1000.0
			val burning = continueOrStartBurning()

			if (burning) {
				boilerTemperature += (burningOutput * deltaSeconds) / BOILER_THERMAL_MASS
				boilerTemperature = min(boilerTemperature, MAXIMUM_TEMPERATURE)
				Tasks.sync { if (!removed) displayBurningParticles() }
			}

			boilWater(deltaSeconds)

			if (!burning) {
				boilerTemperature = maxOf(AMBIENT_TEMPERATURE, boilerTemperature - (PASSIVE_COOLING_PER_SECOND * deltaSeconds))
			}

			// Overheat explosions are intentionally disabled. A full steam output only stops further boiling.
			updateTemperatureDisplay()
		}

		private fun continueOrStartBurning(): Boolean {
			val now = System.currentTimeMillis()
			if (burningEnds > now) return true

			burningEnds = 0L
			burningOutput = 0.0

			return Tasks.getSyncBlocking { tryStartFuel(System.currentTimeMillis()) }
		}

		@Synchronized
		private fun canExtractSteam(): Boolean {
			val storedSteam = fluidOutput.getContents().amount

			if (storedSteam <= EPSILON) steamOutputUnlocked = false
			else if (!steamOutputUnlocked && storedSteam >= MINIMUM_STEAM_OUTPUT_RELEASE) steamOutputUnlocked = true

			return steamOutputUnlocked
		}

		private fun tryStartFuel(now: Long): Boolean {
			if (removed) return false

			val fuelInventory = getInventory(0, -1, 0) ?: return false

			for (slot in 0 until fuelInventory.size) {
				val itemStack = fuelInventory.getItem(slot) ?: continue
				val fuelProperties = ItemFuelProperties[itemStack] ?: continue

				// Pollution generation is disabled for now. Keep this block for an easy later re-enable.
				/*
				val pollutionStack = fuelProperties.pollutionResult.clone()

				if (!pollutionStorage.canAdd(pollutionStack) || !pollutionStorage.hasRoomFor(pollutionStack)) continue
				*/

				burningEnds = now + fuelProperties.burnDurationMillis
				burningOutput = fuelProperties.heatOutputJoulesPerSecond

				if (itemStack.amount <= 1) fuelInventory.setItem(slot, null)
				else itemStack.amount--

				// pollutionStorage.addFluid(pollutionStack, location)
				return true
			}

			return false
		}

		private fun boilWater(deltaSeconds: Double): Double {
			if (boilerTemperature <= BOILING_TEMPERATURE) return 0.0

			val inputContents = fluidInput.getContents()
			if (inputContents.isEmpty() || inputContents.type != FluidTypeKeys.WATER) return 0.0

			val outputContents = fluidOutput.getContents()
			if (!outputContents.isEmpty() && outputContents.type != FluidTypeKeys.DENSE_STEAM) return 0.0

			val availableEnergy = (boilerTemperature - BOILING_TEMPERATURE) * BOILER_THERMAL_MASS
			val waterAllowedByHeat = availableEnergy / WATER_LATENT_HEAT_PER_LITER
			val waterAllowedByOutput = fluidOutput.getRemainingRoom() / STEAM_EXPANSION_FACTOR
			val waterAllowedByRate = MAXIMUM_WATER_CONVERSION_PER_SECOND * deltaSeconds
			val waterToBoil = minOf(inputContents.amount, waterAllowedByHeat, waterAllowedByOutput, waterAllowedByRate)

			if (waterToBoil <= EPSILON) return 0.0

			val intendedSteam = waterToBoil * STEAM_EXPANSION_FACTOR
			val steamNotAdded = fluidOutput.addFluid(FluidStack(FluidTypeKeys.DENSE_STEAM, intendedSteam), location)
			val steamAdded = intendedSteam - steamNotAdded
			val waterActuallyBoiled = steamAdded / STEAM_EXPANSION_FACTOR

			if (waterActuallyBoiled <= EPSILON) return 0.0

			fluidInput.removeAmount(waterActuallyBoiled)
			boilerTemperature = maxOf(
				BOILING_TEMPERATURE,
				boilerTemperature - ((waterActuallyBoiled * WATER_LATENT_HEAT_PER_LITER) / BOILER_THERMAL_MASS)
			)

			return waterActuallyBoiled
		}

		private fun updateTemperatureDisplay() {
			setStatus(text("${boilerTemperature.roundToInt()}°C", NamedTextColor.WHITE))
		}

		private fun displayBurningParticles() {
			val location = getBlockRelative(0, 0, 3).location.toCenterLocation()

			repeat(2) {
				val offsetX = Random.nextDouble(-2.5, 2.5)
				val offsetY = Random.nextDouble(-0.45, 0.45)
				val offsetZ = Random.nextDouble(-2.5, 2.5)

				world.spawnParticle(Particle.FLAME, location.x + offsetX, location.y + offsetY, location.z + offsetZ, 1, 0.0, 0.0, 0.0, 0.0, null)
			}
		}

		companion object {
			private val TEMPERATURE_KEY = NamespacedKeys.key("item_boiler_temperature")
			private val BURNING_ENDS_KEY = NamespacedKeys.key("item_boiler_burning_ends")
			private val BURNING_OUTPUT_KEY = NamespacedKeys.key("item_boiler_burning_output")

			private const val AMBIENT_TEMPERATURE = 20.0
			private const val BOILING_TEMPERATURE = 100.0
			private const val MAXIMUM_TEMPERATURE = 650.0

			private const val BOILER_THERMAL_MASS = 100_000.0 // Joules required to raise the boiler by 1°C
			private const val WATER_LATENT_HEAT_PER_LITER = 2_257_000.0
			private const val PASSIVE_COOLING_PER_SECOND = 5.0
			private const val MAXIMUM_WATER_CONVERSION_PER_SECOND = 5.0
			private const val STEAM_EXPANSION_FACTOR = 6.0
			private const val MINIMUM_STEAM_OUTPUT_RELEASE = 5.0

			private const val MAXIMUM_DELTA_MILLIS = 1_000L
			private const val EPSILON = 0.000_001
		}
	}
}
