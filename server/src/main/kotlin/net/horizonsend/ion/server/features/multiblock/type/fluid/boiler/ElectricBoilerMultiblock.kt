package net.horizonsend.ion.server.features.multiblock.type.fluid.boiler

import net.horizonsend.ion.server.core.registration.keys.CustomBlockKeys
import net.horizonsend.ion.server.features.client.display.modular.DisplayHandlers
import net.horizonsend.ion.server.features.client.display.modular.TextDisplayHandler
import net.horizonsend.ion.server.features.client.display.modular.display.MATCH_SIGN_FONT_SIZE
import net.horizonsend.ion.server.features.client.display.modular.display.StatusDisplayModule
import net.horizonsend.ion.server.features.client.display.modular.display.fluid.ComplexFluidDisplayModule
import net.horizonsend.ion.server.features.client.display.modular.display.getLinePos
import net.horizonsend.ion.server.features.client.display.modular.display.gridenergy.GridEnergyDisplay
import net.horizonsend.ion.server.features.multiblock.entity.PersistentMultiblockData
import net.horizonsend.ion.server.features.multiblock.entity.type.GaugedMultiblockEntity
import net.horizonsend.ion.server.features.multiblock.entity.type.GaugedMultiblockEntity.MultiblockGauges
import net.horizonsend.ion.server.features.multiblock.entity.type.gridenergy.GridEnergyMultiblock
import net.horizonsend.ion.server.features.multiblock.entity.type.gridenergy.GridEnergyMultiblock.MultiblockGridEnergyManager
import net.horizonsend.ion.server.features.multiblock.entity.type.gridenergy.GridEnergyPortMetaData
import net.horizonsend.ion.server.features.multiblock.manager.MultiblockManager
import net.horizonsend.ion.server.features.multiblock.shape.MultiblockShape
import net.horizonsend.ion.server.features.multiblock.type.fluid.boiler.ElectricBoilerMultiblock.ElectricBoilerEntity
import net.horizonsend.ion.server.features.multiblock.util.PrepackagedPreset
import net.horizonsend.ion.server.features.transport.inputs.IOData
import net.horizonsend.ion.server.features.transport.inputs.IOPort
import net.horizonsend.ion.server.features.transport.inputs.IOType
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.RelativeFace
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.Component.text
import org.bukkit.Material
import org.bukkit.World
import org.bukkit.block.BlockFace
import org.bukkit.block.data.Bisected
import org.bukkit.block.data.type.Slab
import org.bukkit.block.data.type.Stairs
import java.time.Duration

object ElectricBoilerMultiblock : BoilerMultiblock<ElectricBoilerEntity>() {
	override val signText: Array<Component?> = createSignText(
		text("Electric Burner"),
		null,
		null,
		null
	)

	override fun MultiblockShape.buildStructure() {
		z(5) {
			y(-1) {
				x(-3).anyStairs(PrepackagedPreset.stairs(RelativeFace.RIGHT, Bisected.Half.TOP, shape = Stairs.Shape.STRAIGHT))
				x(-2).steelBlock()
				x(-1).type(Material.MUD_BRICKS)
				x(0).type(Material.MUD_BRICKS)
				x(1).type(Material.MUD_BRICKS)
				x(2).steelBlock()
				x(3).anyStairs(PrepackagedPreset.stairs(RelativeFace.LEFT, Bisected.Half.TOP, shape = Stairs.Shape.STRAIGHT))
			}
			y(0) {
				x(-3).anyGlassPane(PrepackagedPreset.pane(RelativeFace.RIGHT, RelativeFace.BACKWARD))
				x(-2).anyGlass()
				x(-1).type(Material.MUD_BRICKS)
				x(0).type(Material.MUD_BRICKS)
				x(1).type(Material.MUD_BRICKS)
				x(2).anyGlass()
				x(3).anyGlassPane(PrepackagedPreset.pane(RelativeFace.BACKWARD, RelativeFace.LEFT))
			}
			y(1) {
				x(-3).anyStairs(PrepackagedPreset.stairs(RelativeFace.RIGHT, Bisected.Half.BOTTOM, shape = Stairs.Shape.STRAIGHT))
				x(-2).steelBlock()
				x(-1).type(Material.MUD_BRICKS)
				x(0).type(Material.MUD_BRICKS)
				x(1).type(Material.MUD_BRICKS)
				x(2).steelBlock()
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
				x(-3).aluminumBlock()
				x(-2).type(Material.MUD_BRICKS)
				x(-1).redstoneBlock()
				x(0).type(Material.MUD_BRICKS)
				x(1).redstoneBlock()
				x(2).type(Material.MUD_BRICKS)
				x(3).aluminumBlock()
			}
			y(0) {
				x(-3).anyGlassPane(PrepackagedPreset.pane(RelativeFace.FORWARD, RelativeFace.RIGHT, RelativeFace.BACKWARD))
				x(-2).type(Material.MUD_BRICKS)
				x(-1).type(Material.PALE_OAK_WOOD)
				x(1).type(Material.PALE_OAK_WOOD)
				x(2).type(Material.MUD_BRICKS)
				x(3).anyGlassPane(PrepackagedPreset.pane(RelativeFace.FORWARD, RelativeFace.BACKWARD, RelativeFace.LEFT))
			}
			y(1) {
				x(-3).aluminumBlock()
				x(-2).type(Material.MUD_BRICKS)
				x(-1).type(Material.PALE_OAK_WOOD)
				x(1).type(Material.PALE_OAK_WOOD)
				x(2).type(Material.MUD_BRICKS)
				x(3).aluminumBlock()
			}
			y(2) {
				x(-2).steelBlock()
				x(-1).type(Material.PALE_OAK_WOOD)
				x(1).type(Material.PALE_OAK_WOOD)
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
				x(-3).customBlock(CustomBlockKeys.PRESSURE_GAUGE.getValue())
				x(-2).type(Material.MUD_BRICKS)
				x(-1).type(Material.MUD_BRICKS)
				x(0).type(Material.MUD_BRICKS)
				x(1).type(Material.MUD_BRICKS)
				x(2).type(Material.MUD_BRICKS)
				x(3).customBlock(CustomBlockKeys.PRESSURE_GAUGE.getValue())
			}
			y(0) {
				x(-3).fluidPort()
				x(-2).type(Material.MUD_BRICKS)
				x(2).type(Material.MUD_BRICKS)
				x(3).fluidPort()
			}
			y(1) {
				x(-3).type(Material.WAXED_COPPER_BLOCK)
				x(-2).type(Material.MUD_BRICKS)
				x(2).type(Material.MUD_BRICKS)
				x(3).type(Material.WAXED_COPPER_BLOCK)
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
				x(-3).aluminumBlock()
				x(-2).type(Material.MUD_BRICKS)
				x(-1).redstoneBlock()
				x(0).type(Material.MUD_BRICKS)
				x(1).redstoneBlock()
				x(2).type(Material.MUD_BRICKS)
				x(3).aluminumBlock()
			}
			y(0) {
				x(-3).anyGlassPane(PrepackagedPreset.pane(RelativeFace.FORWARD, RelativeFace.RIGHT, RelativeFace.BACKWARD))
				x(-2).type(Material.MUD_BRICKS)
				x(-1).type(Material.PALE_OAK_WOOD)
				x(1).type(Material.PALE_OAK_WOOD)
				x(2).type(Material.MUD_BRICKS)
				x(3).anyGlassPane(PrepackagedPreset.pane(RelativeFace.FORWARD, RelativeFace.BACKWARD, RelativeFace.LEFT))
			}
			y(1) {
				x(-3).aluminumBlock()
				x(-2).type(Material.MUD_BRICKS)
				x(-1).type(Material.PALE_OAK_WOOD)
				x(1).type(Material.PALE_OAK_WOOD)
				x(2).type(Material.MUD_BRICKS)
				x(3).aluminumBlock()
			}
			y(2) {
				x(-2).steelBlock()
				x(-1).type(Material.PALE_OAK_WOOD)
				x(1).type(Material.PALE_OAK_WOOD)
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
				x(-3).anyStairs(PrepackagedPreset.stairs(RelativeFace.RIGHT, Bisected.Half.TOP, shape = Stairs.Shape.STRAIGHT))
				x(-2).steelBlock()
				x(-1).type(Material.MUD_BRICKS)
				x(0).type(Material.MUD_BRICKS)
				x(1).type(Material.MUD_BRICKS)
				x(2).steelBlock()
				x(3).anyStairs(PrepackagedPreset.stairs(RelativeFace.LEFT, Bisected.Half.TOP, shape = Stairs.Shape.STRAIGHT))
			}
			y(0) {
				x(-3).anyGlassPane(PrepackagedPreset.pane(RelativeFace.FORWARD, RelativeFace.RIGHT))
				x(-2).anyGlass()
				x(-1).type(Material.MUD_BRICKS)
				x(0).type(Material.MUD_BRICKS)
				x(1).type(Material.MUD_BRICKS)
				x(2).anyGlass()
				x(3).anyGlassPane(PrepackagedPreset.pane(RelativeFace.FORWARD, RelativeFace.LEFT))
			}
			y(1) {
				x(-3).anyStairs(PrepackagedPreset.stairs(RelativeFace.RIGHT, Bisected.Half.BOTTOM, shape = Stairs.Shape.STRAIGHT))
				x(-2).steelBlock()
				x(-1).type(Material.MUD_BRICKS)
				x(0).type(Material.MUD_BRICKS)
				x(1).type(Material.MUD_BRICKS)
				x(2).steelBlock()
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
		z(6) {
			y(-1) {
				x(-2).anyStairs(PrepackagedPreset.stairs(RelativeFace.BACKWARD, Bisected.Half.TOP, shape = Stairs.Shape.STRAIGHT))
				x(-1).aluminumBlock()
				x(0).type(Material.WAXED_COPPER_BLOCK)
				x(1).aluminumBlock()
				x(2).anyStairs(PrepackagedPreset.stairs(RelativeFace.BACKWARD, Bisected.Half.TOP, shape = Stairs.Shape.STRAIGHT))
			}
			y(0) {
				x(-2).anyGlassPane(PrepackagedPreset.pane(RelativeFace.RIGHT, RelativeFace.BACKWARD))
				x(-1).anyGlassPane(PrepackagedPreset.pane(RelativeFace.RIGHT, RelativeFace.BACKWARD, RelativeFace.LEFT))
				x(0).sponge()
				x(1).anyGlassPane(PrepackagedPreset.pane(RelativeFace.RIGHT, RelativeFace.BACKWARD, RelativeFace.LEFT))
				x(2).anyGlassPane(PrepackagedPreset.pane(RelativeFace.BACKWARD, RelativeFace.LEFT))
			}
			y(1) {
				x(-2).anyStairs(PrepackagedPreset.stairs(RelativeFace.BACKWARD, Bisected.Half.BOTTOM, shape = Stairs.Shape.STRAIGHT))
				x(-1).aluminumBlock()
				x(0).type(Material.WAXED_COPPER_BLOCK)
				x(1).aluminumBlock()
				x(2).anyStairs(PrepackagedPreset.stairs(RelativeFace.BACKWARD, Bisected.Half.BOTTOM, shape = Stairs.Shape.STRAIGHT))
			}
		}
		z(0) {
			y(-1) {
				x(-2).anyStairs(PrepackagedPreset.stairs(RelativeFace.FORWARD, Bisected.Half.TOP, shape = Stairs.Shape.STRAIGHT))
				x(-1).aluminumBlock()
				x(0).gridEnergyPort()
				x(1).aluminumBlock()
				x(2).anyStairs(PrepackagedPreset.stairs(RelativeFace.FORWARD, Bisected.Half.TOP, shape = Stairs.Shape.STRAIGHT))
			}
			y(0) {
				x(-2).anyGlassPane(PrepackagedPreset.pane(RelativeFace.FORWARD, RelativeFace.RIGHT))
				x(-1).anyGlassPane(PrepackagedPreset.pane(RelativeFace.FORWARD, RelativeFace.RIGHT, RelativeFace.LEFT))
				x(0).anyGlass()
				x(1).anyGlassPane(PrepackagedPreset.pane(RelativeFace.FORWARD, RelativeFace.RIGHT, RelativeFace.LEFT))
				x(2).anyGlassPane(PrepackagedPreset.pane(RelativeFace.FORWARD, RelativeFace.LEFT))
			}
			y(1) {
				x(-2).anyStairs(PrepackagedPreset.stairs(RelativeFace.FORWARD, Bisected.Half.BOTTOM, shape = Stairs.Shape.STRAIGHT))
				x(-1).aluminumBlock()
				x(0).type(Material.WAXED_COPPER_BLOCK)
				x(1).aluminumBlock()
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
	): ElectricBoilerEntity {
		return ElectricBoilerEntity(manager, data, world, x, y, z, structureDirection)
	}

	class ElectricBoilerEntity(
		manager: MultiblockManager,
		data: PersistentMultiblockData,
		world: World,
		x: Int,
		y: Int,
		z: Int,
		structureDirection: BlockFace
	) : BoilerMultiblockEntity(manager, data, ElectricBoilerMultiblock, world, x, y, z, structureDirection), GridEnergyMultiblock, GaugedMultiblockEntity {
		override val gridEnergyManager: MultiblockGridEnergyManager = MultiblockGridEnergyManager(this)

		override val gauges: MultiblockGauges = MultiblockGauges.builder(this)
			.addGauge(3, -1, 3, GaugedMultiblockEntity.GaugeData.fluidPressureGauge(fluidOutput, this))
			.build()

		override fun IOData.Builder.registerAdditionalIO(): IOData.Builder {
			return addPort(IOType.GRID_ENERGY, 0, -1, 0) { IOPort.RegisteredMetaDataInput<GridEnergyPortMetaData>(this@ElectricBoilerEntity, GridEnergyPortMetaData(inputAllowed = false, outputAllowed = true)) }
		}

		override val displayHandler: TextDisplayHandler = DisplayHandlers.newMultiblockSignOverlay(this,
			{ ComplexFluidDisplayModule(handler = it, container = fluidInput, title = text("Input"), offsetLeft = 3.5, offsetUp = 1.15, offsetBack = -4.0 + 0.39, scale = 0.7f, RelativeFace.RIGHT) },
			{ ComplexFluidDisplayModule(handler = it, container = fluidOutput, title = text("Output"), offsetLeft = -3.5, offsetUp = 1.15, offsetBack = -4.0 + 0.39, scale = 0.7f, RelativeFace.LEFT) },
			{ GridEnergyDisplay(handler = it, multiblock = this, offsetLeft = 0.0, offsetUp = getLinePos(3), offsetBack = 0.0, scale = MATCH_SIGN_FONT_SIZE, relativeFace = RelativeFace.FORWARD) },
			{ StatusDisplayModule(handler = it, statusSupplier = statusManager, offsetLeft = 0.0, offsetUp = getLinePos(4), offsetBack = 0.0, scale = MATCH_SIGN_FONT_SIZE) },
		)

		override fun tickAsync() {
			super<BoilerMultiblockEntity>.tickAsync()
			bootstrapGridEnergyNetwork()
		}

		override fun getHeatProductionJoulesPerSecond(): Double {
			return POWER_DRAW_WATTS
		}

		override fun postTick() {
			if (isRunning) {
				setActiveDuration(Duration.ofSeconds(2))
				setActiveGridEnergyConsumption(POWER_DRAW_WATTS)
			} else {
				setActiveGridEnergyConsumption(0.0)
			}
		}

		override fun preTick(): Boolean {
			tickGauges()
			return true
		}

		override fun getPassiveGridEnergyConsumption(): Double = 1.0

		private val POWER_DRAW_WATTS get () = 500_000.0 // Watts
	}
}
