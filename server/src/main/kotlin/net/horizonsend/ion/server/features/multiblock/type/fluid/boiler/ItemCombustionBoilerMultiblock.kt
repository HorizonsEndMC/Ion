package net.horizonsend.ion.server.features.multiblock.type.fluid.boiler

import net.horizonsend.ion.server.core.registration.keys.CustomBlockKeys
import net.horizonsend.ion.server.features.multiblock.shape.MultiblockShape
import net.horizonsend.ion.server.features.multiblock.util.PrepackagedPreset
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.RelativeFace
import net.kyori.adventure.text.Component
import org.bukkit.Material
import org.bukkit.block.data.type.Slab

object ItemCombustionBoilerMultiblock : BoilerMultiblock() {
	override val signText: Array<Component?> = createSignText(
		Component.text("Item Burner"),
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
}
