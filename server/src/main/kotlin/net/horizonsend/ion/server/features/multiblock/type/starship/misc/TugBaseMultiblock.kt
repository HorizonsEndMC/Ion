package net.horizonsend.ion.server.features.multiblock.type.starship.misc

import net.horizonsend.ion.common.utils.text.bracketed
import net.horizonsend.ion.server.core.registration.keys.CustomBlockKeys
import net.horizonsend.ion.server.features.multiblock.Multiblock
import net.horizonsend.ion.server.features.multiblock.shape.MultiblockShape
import net.horizonsend.ion.server.features.multiblock.type.starship.SubsystemMultiblock
import net.horizonsend.ion.server.features.multiblock.util.PrepackagedPreset
import net.horizonsend.ion.server.features.starship.active.ActiveStarship
import net.horizonsend.ion.server.features.starship.subsystem.misc.tug.TugSubsystem
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.RelativeFace
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.Vec3i
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.getRelative
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.Material
import org.bukkit.block.Block
import org.bukkit.block.BlockFace
import org.bukkit.block.data.Bisected
import org.bukkit.block.data.type.Stairs

sealed class TugBaseMultiblock : Multiblock(), SubsystemMultiblock<TugSubsystem> {
	override val name: String = "tug"

	override val signText: Array<Component?> = createSignText(
		bracketed(Component.text("Tractor Beam", NamedTextColor.AQUA)),
		null,
		null,
		null
	)

	abstract val firePosOffset: Vec3i

	abstract fun getTileOrigin(subsytemPos: Vec3i, subsystemDirection: BlockFace): Vec3i

	abstract fun getOriginRelativePosition(origin: Vec3i, subsystemDirection: BlockFace, length: Int): Vec3i

	private val capStructure by lazy { MultiblockShape().apply { buildTugEndStructure() } }
	protected abstract fun MultiblockShape.buildTugEndStructure()

	fun originMatchesCapStructure(origin: Block, direction: BlockFace, loadChunks: Boolean): Boolean {
		return capStructure.checkRequirementsSpecific(origin = origin, face = direction, loadChunks = loadChunks, particles = false)
	}

	private val tiledStructure by lazy { MultiblockShape().apply { buildTiledStructure() } }
	protected abstract fun MultiblockShape.buildTiledStructure()

	fun originMatchesTiledStructure(origin: Block, direction: BlockFace, loadChunks: Boolean): Boolean {
		return tiledStructure.checkRequirementsSpecific(origin = origin, face = direction, loadChunks = loadChunks, particles = false)
	}

	override fun createSubsystem(starship: ActiveStarship, pos: Vec3i, face: BlockFace): TugSubsystem {
		return TugSubsystem(starship, pos, face, this)
	}

	object HorizontalTugBaseMultiblock : TugBaseMultiblock() {
		override val firePosOffset: Vec3i = Vec3i(0, 0, 5)

		override fun getTileOrigin(subsytemPos: Vec3i, subsystemDirection: BlockFace): Vec3i {
			return subsytemPos.getRelative(subsystemDirection, 3)
		}

		override fun getOriginRelativePosition(origin: Vec3i, subsystemDirection: BlockFace, length: Int): Vec3i {
			return origin.getRelative(subsystemDirection, length)
		}

		override fun MultiblockShape.buildStructure() {
			z(0) {
				y(-1) {
					x(-1).anyStairs(PrepackagedPreset.stairs(RelativeFace.FORWARD, Bisected.Half.TOP, shape = Stairs.Shape.STRAIGHT))
					x(0).aluminumBlock()
					x(1).anyStairs(PrepackagedPreset.stairs(RelativeFace.FORWARD, Bisected.Half.TOP, shape = Stairs.Shape.STRAIGHT))
				}
				y(0) {
					x(-1).aluminumBlock()
					x(0).sponge()
					x(1).aluminumBlock()
				}
				y(1) {
					x(-1).anyStairs(PrepackagedPreset.stairs(RelativeFace.FORWARD, Bisected.Half.BOTTOM, shape = Stairs.Shape.STRAIGHT))
					x(0).aluminumBlock()
					x(1).anyStairs(PrepackagedPreset.stairs(RelativeFace.FORWARD, Bisected.Half.BOTTOM, shape = Stairs.Shape.STRAIGHT))
				}
			}
			z(1) {
				y(-1) {
					x(-1).anyStairs(PrepackagedPreset.stairs(RelativeFace.BACKWARD, Bisected.Half.TOP, shape = Stairs.Shape.STRAIGHT))
					x(0).aluminumBlock()
					x(1).anyStairs(PrepackagedPreset.stairs(RelativeFace.BACKWARD, Bisected.Half.TOP, shape = Stairs.Shape.STRAIGHT))
				}
				y(0) {
					x(-1).aluminumBlock()
					x(0).sponge()
					x(1).aluminumBlock()
				}
				y(1) {
					x(-1).anyStairs(PrepackagedPreset.stairs(RelativeFace.BACKWARD, Bisected.Half.BOTTOM, shape = Stairs.Shape.STRAIGHT))
					x(0).aluminumBlock()
					x(1).anyStairs(PrepackagedPreset.stairs(RelativeFace.BACKWARD, Bisected.Half.BOTTOM, shape = Stairs.Shape.STRAIGHT))
				}
			}
			z(2) {
				y(-1) {
					x(0).anyWall()
				}
				y(0) {
					x(-1).anyWall()
					x(0).sponge()
					x(1).anyWall()
				}
				y(1) {
					x(0).anyWall()
				}
			}
		}

		override fun MultiblockShape.buildTiledStructure() {
			z(0) {
				y(-1) {
					x(0).anyGlass()
				}
				y(0) {
					x(-1).anyGlass()
					x(0).customBlock(CustomBlockKeys.SUPERCONDUCTOR_BLOCK.getValue())
					x(1).anyGlass()
				}
				y(1) {
					x(0).anyGlass()
				}
			}
		}

		override fun MultiblockShape.buildTugEndStructure() {
			z(0) {
				y(-1) {
					x(0).anyWall()
				}
				y(0) {
					x(-1).anyWall()
					x(0).sponge()
					x(1).anyWall()
				}
				y(1) {
					x(0).anyWall()
				}
			}
			z(1) {
				y(-1) {
					x(0).anyGlassPane(PrepackagedPreset.pane(RelativeFace.BACKWARD))
				}
				y(0) {
					x(-1).anyGlassPane(PrepackagedPreset.pane(RelativeFace.RIGHT, RelativeFace.BACKWARD))
					x(0).thrusterBlock()
					x(1).anyGlassPane(PrepackagedPreset.pane(RelativeFace.LEFT, RelativeFace.BACKWARD))
				}
				y(1) {
					x(0).anyGlassPane(PrepackagedPreset.pane(RelativeFace.BACKWARD))
				}
			}
			z(2) {
				y(0) {
					x(0).customBlock(CustomBlockKeys.REINFORCED_FLUID_PIPE.getValue())
				}
			}
			z(3) {
				y(0) {
					x(0).endRod(PrepackagedPreset.simpleDirectional(RelativeFace.FORWARD, example = Material.END_ROD.createBlockData()))
				}
			}
		}
	}

	object VerticalTugBaseMultiblock : TugBaseMultiblock() {
		override val firePosOffset: Vec3i = Vec3i(0, 5, 0)

		override fun getTileOrigin(subsytemPos: Vec3i, subsystemDirection: BlockFace): Vec3i {
			return subsytemPos.getRelative(subsystemDirection).getRelative(BlockFace.UP, 3)
		}

		override fun getOriginRelativePosition(origin: Vec3i, subsystemDirection: BlockFace, length: Int): Vec3i {
			return origin.getRelative(BlockFace.UP, length)
		}

		override fun MultiblockShape.buildStructure() {
			z(2) {
				y(0) {
					x(-1).anyStairs(PrepackagedPreset.stairs(RelativeFace.BACKWARD, Bisected.Half.TOP, shape = Stairs.Shape.STRAIGHT))
					x(0).aluminumBlock()
					x(1).anyStairs(PrepackagedPreset.stairs(RelativeFace.BACKWARD, Bisected.Half.TOP, shape = Stairs.Shape.STRAIGHT))
				}
				y(1) {
					x(-1).anyStairs(PrepackagedPreset.stairs(RelativeFace.BACKWARD, Bisected.Half.BOTTOM, shape = Stairs.Shape.STRAIGHT))
					x(0).aluminumBlock()
					x(1).anyStairs(PrepackagedPreset.stairs(RelativeFace.BACKWARD, Bisected.Half.BOTTOM, shape = Stairs.Shape.STRAIGHT))
				}
				y(2) {
					x(0).anyWall()
				}
			}
			z(1) {
				y(0) {
					x(-1).aluminumBlock()
					x(0).sponge()
					x(1).aluminumBlock()
				}
				y(1) {
					x(-1).aluminumBlock()
					x(0).sponge()
					x(1).aluminumBlock()
				}
				y(2) {
					x(-1).anyWall()
					x(0).sponge()
					x(1).anyWall()
				}
			}
			z(0) {
				y(0) {
					x(-1).anyStairs(PrepackagedPreset.stairs(RelativeFace.FORWARD, Bisected.Half.TOP, shape = Stairs.Shape.STRAIGHT))
					x(0).aluminumBlock()
					x(1).anyStairs(PrepackagedPreset.stairs(RelativeFace.FORWARD, Bisected.Half.TOP, shape = Stairs.Shape.STRAIGHT))
				}
				y(1) {
					x(-1).anyStairs(PrepackagedPreset.stairs(RelativeFace.FORWARD, Bisected.Half.BOTTOM, shape = Stairs.Shape.STRAIGHT))
					x(0).aluminumBlock()
					x(1).anyStairs(PrepackagedPreset.stairs(RelativeFace.FORWARD, Bisected.Half.BOTTOM, shape = Stairs.Shape.STRAIGHT))
				}
				y(2) {
					x(0).anyWall()
				}
			}
		}

		override fun MultiblockShape.buildTiledStructure() {
			z(0) {
				y(0) {
					x(-1).anyGlass()
					x(0).customBlock(CustomBlockKeys.SUPERCONDUCTOR_BLOCK.getValue())
					x(1).anyGlass()
				}
			}
			z(1) {
				y(0) {
					x(0).anyGlass()
				}
			}
			z(-1) {
				y(0) {
					x(0).anyGlass()
				}
			}
		}

		override fun MultiblockShape.buildTugEndStructure() {
			z(0) {
				y(0) {
					x(-1).anyWall()
					x(0).sponge()
					x(1).anyWall()
				}
				y(1) {
					x(-1).anyGlassPane(PrepackagedPreset.pane(RelativeFace.RIGHT))
					x(0).thrusterBlock()
					x(1).anyGlassPane(PrepackagedPreset.pane(RelativeFace.LEFT))
				}
				y(2) {
					x(0).customBlock(CustomBlockKeys.REINFORCED_FLUID_PIPE.getValue())
				}
				y(3) {
					x(0).endRod(PrepackagedPreset.simpleDirectional(RelativeFace.UP, example = Material.END_ROD.createBlockData()))
				}
			}
			z(1) {
				y(0) {
					x(0).anyWall()
				}
				y(1) {
					x(0).anyGlassPane(PrepackagedPreset.pane(RelativeFace.BACKWARD))
				}
			}
			z(-1) {
				y(0) {
					x(0).anyWall()
				}
				y(1) {
					x(0).anyGlassPane(PrepackagedPreset.pane(RelativeFace.FORWARD))
				}
			}
		}
	}
}
