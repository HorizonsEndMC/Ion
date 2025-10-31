package net.horizonsend.ion.server.features.multiblock.type.fluid.boiler

import net.horizonsend.ion.server.core.registration.keys.CustomBlockKeys
import net.horizonsend.ion.server.core.registration.keys.FluidPropertyTypeKeys.FLAMMABILITY
import net.horizonsend.ion.server.features.client.display.modular.DisplayHandlers
import net.horizonsend.ion.server.features.client.display.modular.TextDisplayHandler
import net.horizonsend.ion.server.features.client.display.modular.display.MATCH_SIGN_FONT_SIZE
import net.horizonsend.ion.server.features.client.display.modular.display.StatusDisplayModule
import net.horizonsend.ion.server.features.client.display.modular.display.fluid.ComplexFluidDisplayModule
import net.horizonsend.ion.server.features.client.display.modular.display.fluid.SimpleFluidDisplayModule
import net.horizonsend.ion.server.features.client.display.modular.display.getLinePos
import net.horizonsend.ion.server.features.multiblock.entity.PersistentMultiblockData
import net.horizonsend.ion.server.features.multiblock.entity.type.GaugedMultiblockEntity
import net.horizonsend.ion.server.features.multiblock.entity.type.GaugedMultiblockEntity.MultiblockGauges
import net.horizonsend.ion.server.features.multiblock.entity.type.fluids.FluidPortMetadata
import net.horizonsend.ion.server.features.multiblock.entity.type.fluids.storage.FluidRestriction
import net.horizonsend.ion.server.features.multiblock.entity.type.fluids.storage.FluidStorageContainer
import net.horizonsend.ion.server.features.multiblock.manager.MultiblockManager
import net.horizonsend.ion.server.features.multiblock.shape.MultiblockShape
import net.horizonsend.ion.server.features.multiblock.type.fluid.boiler.FluidCombustionBoilerMultiblock.FluidBoilerEntity
import net.horizonsend.ion.server.features.multiblock.util.PrepackagedPreset
import net.horizonsend.ion.server.features.transport.fluids.FluidStack
import net.horizonsend.ion.server.features.transport.inputs.IOData
import net.horizonsend.ion.server.features.transport.inputs.IOPort
import net.horizonsend.ion.server.features.transport.inputs.IOType
import net.horizonsend.ion.server.miscellaneous.registrations.persistence.NamespacedKeys
import net.horizonsend.ion.server.miscellaneous.utils.axis
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.RelativeFace
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.Component.text
import org.bukkit.Axis
import org.bukkit.Material
import org.bukkit.Particle
import org.bukkit.World
import org.bukkit.block.BlockFace
import org.bukkit.block.data.Bisected
import org.bukkit.block.data.type.Slab
import org.bukkit.block.data.type.Stairs
import kotlin.random.Random

object FluidCombustionBoilerMultiblock : BoilerMultiblock<FluidBoilerEntity>() {
	override val signText: Array<Component?> = createSignText(
		text("Fluid Burner"),
		null,
		null,
		null
	)

	override fun MultiblockShape.buildStructure() {
		z(6) {
			y(-1) {
				x(-3).anyWall()
				x(-2).ironBlock()
				x(-1).anyStairs(PrepackagedPreset.stairs(RelativeFace.BACKWARD, Bisected.Half.BOTTOM, shape = Stairs.Shape.STRAIGHT))
				x(0).anyStairs(PrepackagedPreset.stairs(RelativeFace.BACKWARD, Bisected.Half.BOTTOM, shape = Stairs.Shape.STRAIGHT))
				x(1).anyStairs(PrepackagedPreset.stairs(RelativeFace.BACKWARD, Bisected.Half.BOTTOM, shape = Stairs.Shape.STRAIGHT))
				x(2).ironBlock()
				x(3).anyWall()
			}
			y(0) {
				x(-3).anyWall()
				x(-2).ironBlock()
				x(-1).anyGlassPane(PrepackagedPreset.pane(RelativeFace.RIGHT, RelativeFace.LEFT))
				x(0).anyGlassPane(PrepackagedPreset.pane(RelativeFace.RIGHT, RelativeFace.LEFT))
				x(1).anyGlassPane(PrepackagedPreset.pane(RelativeFace.RIGHT, RelativeFace.LEFT))
				x(2).ironBlock()
				x(3).anyWall()
			}
			y(1) {
				x(-2).anyStairs(PrepackagedPreset.stairs(RelativeFace.BACKWARD, Bisected.Half.BOTTOM, shape = Stairs.Shape.STRAIGHT))
				x(-1).anyTerracotta()
				x(0).fluidPort()
				x(1).anyTerracotta()
				x(2).anyStairs(PrepackagedPreset.stairs(RelativeFace.BACKWARD, Bisected.Half.BOTTOM, shape = Stairs.Shape.STRAIGHT))
			}
			y(2) {
				x(0).customBlock(CustomBlockKeys.FLUID_PIPE.getValue())
			}
		}
		z(5) {
			y(-1) {
				x(-3).ironBlock()
				x(-2).ironBlock()
				x(-1).type(Material.MUD_BRICKS)
				x(0).ironBlock()
				x(1).type(Material.MUD_BRICKS)
				x(2).ironBlock()
				x(3).ironBlock()
			}
			y(0) {
				x(-3).ironBlock()
				x(-2).type(Material.WAXED_COPPER_BLOCK)
				x(-1).customBlock(CustomBlockKeys.FLUID_PIPE.getValue())
				x(0).customBlock(CustomBlockKeys.FLUID_VALVE.getValue())
				x(1).customBlock(CustomBlockKeys.FLUID_PIPE.getValue())
				x(2).type(Material.WAXED_COPPER_BLOCK)
				x(3).ironBlock()
			}
			y(1) {
				x(-3).anyStairs(PrepackagedPreset.stairs(RelativeFace.RIGHT, Bisected.Half.BOTTOM, shape = Stairs.Shape.STRAIGHT))
				x(-2).ironBlock()
				x(-1).type(Material.MUD_BRICKS)
				x(0).ironBlock()
				x(1).type(Material.MUD_BRICKS)
				x(2).ironBlock()
				x(3).anyStairs(PrepackagedPreset.stairs(RelativeFace.LEFT, Bisected.Half.BOTTOM, shape = Stairs.Shape.STRAIGHT))
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
				x(0).type(Material.WAXED_COPPER_GRATE)
				x(1).titaniumBlock()
			}
			y(5) {
				x(-1).titaniumBlock()
				x(0).type(Material.WAXED_COPPER_GRATE)
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
				x(-3).anyStairs(PrepackagedPreset.stairs(RelativeFace.RIGHT, Bisected.Half.BOTTOM, shape = Stairs.Shape.STRAIGHT))
				x(-2).type(Material.MUD_BRICKS)
				x(-1).type(Material.MUD_BRICKS)
				x(0).type(Material.MUD_BRICKS)
				x(1).type(Material.MUD_BRICKS)
				x(2).type(Material.MUD_BRICKS)
				x(3).anyStairs(PrepackagedPreset.stairs(RelativeFace.LEFT, Bisected.Half.BOTTOM, shape = Stairs.Shape.STRAIGHT))
			}
			y(0) {
				x(-3).anyGlassPane(PrepackagedPreset.pane(RelativeFace.FORWARD, RelativeFace.RIGHT, RelativeFace.BACKWARD))
				x(-2).redstoneBlock()
				x(0).dropperOrDispenser()
				x(2).redstoneBlock()
				x(3).anyGlassPane(PrepackagedPreset.pane(RelativeFace.FORWARD, RelativeFace.BACKWARD, RelativeFace.LEFT))
			}
			y(1) {
				x(-3).anyTerracotta()
				x(-2).type(Material.MUD_BRICKS)
				x(-1).type(Material.MUD_BRICKS)
				x(0).type(Material.MUD_BRICKS)
				x(1).type(Material.MUD_BRICKS)
				x(2).type(Material.MUD_BRICKS)
				x(3).anyTerracotta()
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
				x(-3).anyGauge()
				x(-2).type(Material.MUD_BRICKS)
				x(-1).type(Material.MUD_BRICKS)
				x(0).type(Material.MUD_BRICKS)
				x(1).type(Material.MUD_BRICKS)
				x(2).type(Material.MUD_BRICKS)
				x(3).anyGauge()
			}
			y(0) {
				x(-3).fluidPort()
				x(-2).type(Material.MUD_BRICKS)
				x(2).type(Material.MUD_BRICKS)
				x(3).fluidPort()
			}
			y(1) {
				x(-3).anyTerracotta()
				x(-2).type(Material.MUD_BRICKS)
				x(-1).type(Material.MUD_BRICKS)
				x(0).type(Material.MUD_BRICKS)
				x(1).type(Material.MUD_BRICKS)
				x(2).type(Material.MUD_BRICKS)
				x(3).anyTerracotta()
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
				x(-2).type(Material.WAXED_COPPER_GRATE)
				x(2).type(Material.WAXED_COPPER_GRATE)
			}
			y(5) {
				x(-2).type(Material.WAXED_COPPER_GRATE)
				x(2).type(Material.WAXED_COPPER_GRATE)
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
				x(-3).anyStairs(PrepackagedPreset.stairs(RelativeFace.RIGHT, Bisected.Half.BOTTOM, shape = Stairs.Shape.STRAIGHT))
				x(-2).type(Material.MUD_BRICKS)
				x(-1).type(Material.MUD_BRICKS)
				x(0).type(Material.MUD_BRICKS)
				x(1).type(Material.MUD_BRICKS)
				x(2).type(Material.MUD_BRICKS)
				x(3).anyStairs(PrepackagedPreset.stairs(RelativeFace.LEFT, Bisected.Half.BOTTOM, shape = Stairs.Shape.STRAIGHT))
			}
			y(0) {
				x(-3).anyGlassPane(PrepackagedPreset.pane(RelativeFace.FORWARD, RelativeFace.RIGHT, RelativeFace.BACKWARD))
				x(-2).redstoneBlock()
				x(0).dropperOrDispenser()
				x(2).redstoneBlock()
				x(3).anyGlassPane(PrepackagedPreset.pane(RelativeFace.FORWARD, RelativeFace.BACKWARD, RelativeFace.LEFT))
			}
			y(1) {
				x(-3).anyTerracotta()
				x(-2).type(Material.MUD_BRICKS)
				x(-1).type(Material.MUD_BRICKS)
				x(0).type(Material.MUD_BRICKS)
				x(1).type(Material.MUD_BRICKS)
				x(2).type(Material.MUD_BRICKS)
				x(3).anyTerracotta()
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
				x(-3).ironBlock()
				x(-2).ironBlock()
				x(-1).type(Material.MUD_BRICKS)
				x(0).ironBlock()
				x(1).type(Material.MUD_BRICKS)
				x(2).ironBlock()
				x(3).ironBlock()
			}
			y(0) {
				x(-3).ironBlock()
				x(-2).type(Material.WAXED_COPPER_BLOCK)
				x(-1).customBlock(CustomBlockKeys.FLUID_PIPE.getValue())
				x(0).customBlock(CustomBlockKeys.FLUID_VALVE.getValue())
				x(1).customBlock(CustomBlockKeys.FLUID_PIPE.getValue())
				x(2).type(Material.WAXED_COPPER_BLOCK)
				x(3).ironBlock()
			}
			y(1) {
				x(-3).anyStairs(PrepackagedPreset.stairs(RelativeFace.RIGHT, Bisected.Half.BOTTOM, shape = Stairs.Shape.STRAIGHT))
				x(-2).ironBlock()
				x(-1).type(Material.MUD_BRICKS)
				x(0).ironBlock()
				x(1).type(Material.MUD_BRICKS)
				x(2).ironBlock()
				x(3).anyStairs(PrepackagedPreset.stairs(RelativeFace.LEFT, Bisected.Half.BOTTOM, shape = Stairs.Shape.STRAIGHT))
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
				x(0).type(Material.WAXED_COPPER_GRATE)
				x(1).titaniumBlock()
			}
			y(5) {
				x(-1).titaniumBlock()
				x(0).type(Material.WAXED_COPPER_GRATE)
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
				x(-3).anyWall()
				x(-2).ironBlock()
				x(-1).anyStairs(PrepackagedPreset.stairs(RelativeFace.FORWARD, Bisected.Half.BOTTOM, shape = Stairs.Shape.STRAIGHT))
				x(0).fluidPort()
				x(1).anyStairs(PrepackagedPreset.stairs(RelativeFace.FORWARD, Bisected.Half.BOTTOM, shape = Stairs.Shape.STRAIGHT))
				x(2).ironBlock()
				x(3).anyWall()
			}
			y(0) {
				x(-3).anyWall()
				x(-2).ironBlock()
				x(-1).anyGlassPane(PrepackagedPreset.pane(RelativeFace.RIGHT, RelativeFace.LEFT))
				x(0).anyGlass()
				x(1).anyGlassPane(PrepackagedPreset.pane(RelativeFace.RIGHT, RelativeFace.LEFT))
				x(2).ironBlock()
				x(3).anyWall()
			}
			y(1) {
				x(-2).anyStairs(PrepackagedPreset.stairs(RelativeFace.FORWARD, Bisected.Half.BOTTOM, shape = Stairs.Shape.STRAIGHT))
				x(-1).anyTerracotta()
				x(0).anyStairs(PrepackagedPreset.stairs(RelativeFace.FORWARD, Bisected.Half.TOP, shape = Stairs.Shape.STRAIGHT))
				x(1).anyTerracotta()
				x(2).anyStairs(PrepackagedPreset.stairs(RelativeFace.FORWARD, Bisected.Half.BOTTOM, shape = Stairs.Shape.STRAIGHT))
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
	): FluidBoilerEntity {
		return FluidBoilerEntity(manager, data, world, x, y, z, structureDirection)
	}

	class FluidBoilerEntity(
		manager: MultiblockManager,
		data: PersistentMultiblockData,
		world: World,
		x: Int,
		y: Int,
		z: Int,
		structureDirection: BlockFace
	) : BoilerMultiblockEntity(manager, data, FluidCombustionBoilerMultiblock, world, x, y, z, structureDirection), GaugedMultiblockEntity {
		val fuelStorage = FluidStorageContainer(data, "fuel_storage", text("Fuel Storage"), NamespacedKeys.key("fuel_storage"), 100_000.0, FluidRestriction.FluidPropertyWhitelist(FLAMMABILITY))
		val pollutionStorage = FluidStorageContainer(data, "pollution_out", text("Pollution Output"), NamespacedKeys.key("pollution_out"), 100_000.0, FluidRestriction.Unlimited)

		override val gauges: MultiblockGauges = MultiblockGauges.builder(this)
			.addGauge(3, -1, 3, GaugedMultiblockEntity.GaugeData.fluidTemperatureGauge(fluidOutput, this))
			.addGauge(3, -1, 3, GaugedMultiblockEntity.GaugeData.onOffGauge { isRunning })
			.build()

		override fun IOData.Builder.registerAdditionalIO(): IOData.Builder =
			addPort(IOType.FLUID, 0, -1, 0) { IOPort.RegisteredMetaDataInput(this@FluidBoilerEntity, FluidPortMetadata(connectedStore = fuelStorage, inputAllowed = true, outputAllowed = false)) }
			.addPort(IOType.FLUID, 0, 1, 6) { IOPort.RegisteredMetaDataInput(this@FluidBoilerEntity, FluidPortMetadata(connectedStore = pollutionStorage, inputAllowed = false, outputAllowed = true)) }

		override val displayHandler: TextDisplayHandler = DisplayHandlers.newMultiblockSignOverlay(this,
			{ ComplexFluidDisplayModule(handler = it, container = fluidInput, title = text("Input"), offsetLeft = 3.5, offsetUp = 1.15, offsetBack = -4.0 + 0.39, scale = 0.7f, RelativeFace.RIGHT) },
			{ ComplexFluidDisplayModule(handler = it, container = fluidOutput, title = text("Output"), offsetLeft = -3.5, offsetUp = 1.15, offsetBack = -4.0 + 0.39, scale = 0.7f, RelativeFace.LEFT) },
			{ SimpleFluidDisplayModule(handler = it, storage = fuelStorage, offsetLeft = 0.0, offsetUp = getLinePos(2), offsetBack = 0.0, scale = MATCH_SIGN_FONT_SIZE) },
			{ StatusDisplayModule(handler = it, statusSupplier = statusManager, offsetLeft = 0.0, offsetUp = getLinePos(4), offsetBack = 0.0, scale = MATCH_SIGN_FONT_SIZE) }
		)

		override fun getStores(): List<FluidStorageContainer> {
			return listOf(fluidInput, fluidOutput, fuelStorage, pollutionStorage)
		}

		override fun getHeatProductionJoulesPerSecond(): Double {
			return lastHeatValue
		}

		private var lastHeatValue = 0.0

		override fun preTick(deltaSeconds: Double): Boolean {
			val combustionContents = fuelStorage.getContents()
			if (combustionContents.isEmpty()) return false

			val combustionResult = combustionContents.getData(FLAMMABILITY) ?: return false
			val burnedAmount = minOf(BURNED_PER_SECOND * deltaSeconds, combustionContents.amount)

			val pollutionStack = FluidStack(combustionResult.resultFluid, burnedAmount * combustionResult.resultVolumeMultiplier)
			return pollutionStorage.canAdd(pollutionStack)
		}

		override fun postTick(deltaSeconds: Double) {
			tickGauges()

			if (!isRunning) return

			consumeFuel(deltaSeconds)
			displayBurningParticles()
		}

		fun consumeFuel(deltaSeconds: Double) {
			val combustibleContents = fuelStorage.getContents()
			val combustionResult = combustibleContents.getDataOrThrow(FLAMMABILITY)
			val burnedAmount = minOf(BURNED_PER_SECOND * deltaSeconds, combustibleContents.amount)

			val pollutionStack = FluidStack(combustionResult.resultFluid, burnedAmount * combustionResult.resultVolumeMultiplier)
			if (!pollutionStorage.canAdd(pollutionStack)) return

			fuelStorage.removeAmount(burnedAmount)
			pollutionStorage.addFluid(pollutionStack, location)
			lastHeatValue = combustionResult.joulesPerLiter * burnedAmount
		}

		fun displayBurningParticles() {
			val location = getBlockRelative(0, 0, 3).location.toCenterLocation()

			repeat(2) {
				val xRange = if (structureDirection.axis == Axis.X) 2.5 else 1.5
				val offsetX = Random.nextDouble(-xRange, xRange)
				val offsetY = Random.nextDouble(-0.45, 0.45)
				val zRange = if (structureDirection.axis == Axis.Z) 2.5 else 1.5
				val offsetZ = Random.nextDouble(-zRange, zRange)

				world.spawnParticle(Particle.FLAME, location.x + offsetX, location.y + offsetY, location.z + offsetZ, 1, 0.0, 0.0, 0.0, 0.0, null)
			}
		}

		companion object {
			private const val BURNED_PER_SECOND = 0.5
		}
	}
}
