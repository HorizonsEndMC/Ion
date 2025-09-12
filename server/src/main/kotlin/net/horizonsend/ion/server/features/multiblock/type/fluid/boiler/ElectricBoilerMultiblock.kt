package net.horizonsend.ion.server.features.multiblock.type.fluid.boiler

import net.horizonsend.ion.server.features.multiblock.shape.MultiblockShape
import net.horizonsend.ion.server.features.multiblock.util.PrepackagedPreset
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.RelativeFace
import net.kyori.adventure.text.Component
import org.bukkit.Material
import org.bukkit.block.data.Bisected
import org.bukkit.block.data.type.Slab
import org.bukkit.block.data.type.Stairs

object ElectricBoilerMultiblock : BoilerMultiblock() {
	override val signText: Array<Component?> = createSignText(
		Component.text("Electric Burner"),
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
				x(-3).type(Material.WAXED_COPPER_BLOCK)
				x(-2).type(Material.MUD_BRICKS)
				x(-1).type(Material.MUD_BRICKS)
				x(0).type(Material.MUD_BRICKS)
				x(1).type(Material.MUD_BRICKS)
				x(2).type(Material.MUD_BRICKS)
				x(3).type(Material.WAXED_COPPER_BLOCK)
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
}
