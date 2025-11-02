package net.horizonsend.ion.server.features.multiblock.type.fluid.boiler

import net.horizonsend.ion.server.core.registration.keys.CustomBlockKeys
import net.horizonsend.ion.server.features.client.display.modular.DisplayHandlers
import net.horizonsend.ion.server.features.client.display.modular.TextDisplayHandler
import net.horizonsend.ion.server.features.client.display.modular.display.fluid.ComplexFluidDisplayModule
import net.horizonsend.ion.server.features.multiblock.entity.PersistentMultiblockData
import net.horizonsend.ion.server.features.multiblock.manager.MultiblockManager
import net.horizonsend.ion.server.features.multiblock.shape.MultiblockShape
import net.horizonsend.ion.server.features.multiblock.type.fluid.boiler.ItemCombustionBoilerMultiblock.ItemBoilerEntity
import net.horizonsend.ion.server.features.multiblock.util.PrepackagedPreset
import net.horizonsend.ion.server.features.transport.inputs.IOData
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.RelativeFace
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.Component.text
import org.bukkit.Material
import org.bukkit.World
import org.bukkit.block.BlockFace
import org.bukkit.block.data.type.Slab

object ItemCombustionBoilerMultiblock : BoilerMultiblock<ItemBoilerEntity>() {
	override val signText: Array<Component?> = createSignText(
		text("Item Burner"),
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
				x(0).fluidPort()
				x(1).titaniumBlock()
				x(2).titaniumBlock()
				x(3).anySlab(PrepackagedPreset.slab(Slab.Type.BOTTOM))
			}
			y(2) {
				x(0).customBlock(CustomBlockKeys.FLUID_PIPE.getValue())
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
				x(-3).fluidPort()
				x(3).fluidPort()
			}
			y(1) {
				x(-3).titaniumBlock()
				x(-2).type(Material.MUD_BRICKS)
				x(-1).type(Material.MUD_BRICKS)
				x(0).type(Material.TARGET)
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
				x(-1).type(Material.MUD_BRICKS)
				x(0).type(Material.PISTON)
				x(1).type(Material.MUD_BRICKS)
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
				x(-2).anyCustomBlockOrMaterial(
					listOf(CustomBlockKeys.REDSTONE_CONTROL_PORT),
					listOf(Material.MUD_BRICKS),
					"redstone control port or mud bricks",
				) { setExample(CustomBlockKeys.REDSTONE_CONTROL_PORT.getValue().blockData) }
				x(-1).anyGlassPane(PrepackagedPreset.pane(RelativeFace.FORWARD, RelativeFace.RIGHT, RelativeFace.LEFT))
				x(0).anyGlass()
				x(1).anyGlassPane(PrepackagedPreset.pane(RelativeFace.FORWARD, RelativeFace.RIGHT, RelativeFace.LEFT))
				x(2).anyCustomBlockOrMaterial(
					listOf(CustomBlockKeys.REDSTONE_CONTROL_PORT),
					listOf(Material.MUD_BRICKS),
					"redstone control port or mud bricks",
				) { setExample(Material.MUD_BRICKS) }
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
	) : BoilerMultiblockEntity(manager, data, ItemCombustionBoilerMultiblock, world, x, y, z, structureDirection) {
		// No additional IO
		override fun IOData.Builder.registerAdditionalIO(): IOData.Builder = this

		override val displayHandler: TextDisplayHandler = DisplayHandlers.newMultiblockSignOverlay(this,
			{ ComplexFluidDisplayModule(handler = it, container = fluidInput, title = text("Input"), offsetLeft = 3.5, offsetUp = 1.15, offsetBack = -4.0 + 0.39, scale = 0.7f, RelativeFace.RIGHT) },
			{ ComplexFluidDisplayModule(handler = it, container = fluidOutput, title = text("Output"), offsetLeft = -3.5, offsetUp = 1.15, offsetBack = -4.0 + 0.39, scale = 0.7f, RelativeFace.LEFT) },
		)

		override fun getHeatProductionJoulesPerSecond(): Double {
			return 10000.0
		}
	}
}
