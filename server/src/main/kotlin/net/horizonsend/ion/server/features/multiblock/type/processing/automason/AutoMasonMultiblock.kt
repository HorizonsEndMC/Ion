package net.horizonsend.ion.server.features.multiblock.type.processing.automason

import net.horizonsend.ion.server.features.multiblock.Multiblock
import net.horizonsend.ion.server.features.multiblock.entity.PersistentMultiblockData
import net.horizonsend.ion.server.features.multiblock.manager.MultiblockManager
import net.horizonsend.ion.server.features.multiblock.shape.MultiblockShape
import net.horizonsend.ion.server.features.multiblock.type.EntityMultiblock
import net.horizonsend.ion.server.features.multiblock.util.PrepackagedPreset
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.RelativeFace
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.Vec3i
import net.kyori.adventure.text.Component
import org.bukkit.Material
import org.bukkit.World
import org.bukkit.block.BlockFace
import org.bukkit.block.data.Bisected
import org.bukkit.block.data.type.Stairs

sealed class AutoMasonMultiblock : Multiblock(), EntityMultiblock<AutoMasonMultiblockEntity> {
	override val name: String = "automason"

	abstract val inputOffset: Vec3i?
	abstract val outputOffset: Vec3i?

	override val signText: Array<Component?> = createSignText(
		Component.text("Auto Mason"),
		null,
		null,
		null
	)

	override fun createEntity(manager: MultiblockManager, data: PersistentMultiblockData, world: World, x: Int, y: Int, z: Int, structureDirection: BlockFace): AutoMasonMultiblockEntity {
		return AutoMasonMultiblockEntity(data, this, manager, x, y, z, world, structureDirection)
	}

	data object StandardAutoMasonMultiblock : AutoMasonMultiblock() {
		override val outputOffset: Vec3i = Vec3i(3, 0, 3)
		override val inputOffset: Vec3i = Vec3i(-3, 0, 3)

		override fun MultiblockShape.buildStructure() {
			z(4) {
				y(-1) {
					x(-3).anyStairs(PrepackagedPreset.stairs(RelativeFace.BACKWARD, Bisected.Half.TOP, shape = Stairs.Shape.STRAIGHT))
					x(-2).ironBlock()
					x(-1).ironBlock()
					x(0).type(Material.STONECUTTER)
					x(1).ironBlock()
					x(2).ironBlock()
					x(3).anyStairs(PrepackagedPreset.stairs(RelativeFace.BACKWARD, Bisected.Half.TOP, shape = Stairs.Shape.STRAIGHT))
				}
				y(0) {
					x(-3).anyStairs(PrepackagedPreset.stairs(RelativeFace.BACKWARD, Bisected.Half.BOTTOM, shape = Stairs.Shape.STRAIGHT))
					x(-2).ironBlock()
					x(-1).ironBlock()
					x(0).endRod(PrepackagedPreset.simpleDirectional(RelativeFace.BACKWARD, example = Material.GRINDSTONE.createBlockData()))
					x(1).ironBlock()
					x(2).ironBlock()
					x(3).anyStairs(PrepackagedPreset.stairs(RelativeFace.BACKWARD, Bisected.Half.BOTTOM, shape = Stairs.Shape.STRAIGHT))
				}
			}
			z(3) {
				y(-1) {
					x(-3).anyGlass()
					x(-2).sponge()
					x(-1).type(Material.STONECUTTER)
					x(0).type(Material.PISTON)
					x(1).type(Material.STONECUTTER)
					x(2).sponge()
					x(3).extractor()
				}
				y(0) {
					x(-3).anyPipedInventory()
					x(-2).type(Material.PISTON)
					x(-1).endRod(PrepackagedPreset.simpleDirectional(RelativeFace.RIGHT, example = Material.GRINDSTONE.createBlockData()))
					x(1).endRod(PrepackagedPreset.simpleDirectional(RelativeFace.LEFT, example = Material.GRINDSTONE.createBlockData()))
					x(2).type(Material.PISTON)
					x(3).anyPipedInventory()
				}
			}
			z(2) {
				y(-1) {
					x(-3).anyStairs(PrepackagedPreset.stairs(RelativeFace.FORWARD, Bisected.Half.TOP, shape = Stairs.Shape.STRAIGHT))
					x(-2).ironBlock()
					x(-1).ironBlock()
					x(0).type(Material.STONECUTTER)
					x(1).ironBlock()
					x(2).ironBlock()
					x(3).anyStairs(PrepackagedPreset.stairs(RelativeFace.FORWARD, Bisected.Half.TOP, shape = Stairs.Shape.STRAIGHT))
				}
				y(0) {
					x(-3).anyStairs(PrepackagedPreset.stairs(RelativeFace.FORWARD, Bisected.Half.BOTTOM, shape = Stairs.Shape.STRAIGHT))
					x(-2).ironBlock()
					x(-1).ironBlock()
					x(0).endRod(PrepackagedPreset.simpleDirectional(RelativeFace.FORWARD, example = Material.GRINDSTONE.createBlockData()))
					x(1).ironBlock()
					x(2).ironBlock()
					x(3).anyStairs(PrepackagedPreset.stairs(RelativeFace.FORWARD, Bisected.Half.BOTTOM, shape = Stairs.Shape.STRAIGHT))
				}
			}
			z(6) {
				y(-1) {
					x(-2).anyStairs(PrepackagedPreset.stairs(RelativeFace.RIGHT, Bisected.Half.TOP, shape = Stairs.Shape.STRAIGHT))
					x(-1).steelBlock()
					x(0).steelBlock()
					x(1).steelBlock()
					x(2).anyStairs(PrepackagedPreset.stairs(RelativeFace.LEFT, Bisected.Half.TOP, shape = Stairs.Shape.STRAIGHT))
				}
				y(0) {
					x(-2).anyGlassPane(PrepackagedPreset.pane(RelativeFace.RIGHT, RelativeFace.BACKWARD))
					x(-1).anyGlassPane(PrepackagedPreset.pane(RelativeFace.RIGHT, RelativeFace.BACKWARD, RelativeFace.LEFT))
					x(0).anyGlassPane(PrepackagedPreset.pane(RelativeFace.RIGHT, RelativeFace.BACKWARD, RelativeFace.LEFT))
					x(1).anyGlassPane(PrepackagedPreset.pane(RelativeFace.RIGHT, RelativeFace.BACKWARD, RelativeFace.LEFT))
					x(2).anyGlassPane(PrepackagedPreset.pane(RelativeFace.BACKWARD, RelativeFace.LEFT))
				}
			}
			z(5) {
				y(-1) {
					x(-2).ironBlock()
					x(-1).sponge()
					x(0).sponge()
					x(1).sponge()
					x(2).ironBlock()
				}
				y(0) {
					x(-2).anyGlass()
					x(-1).sponge()
					x(0).type(Material.PISTON)
					x(1).sponge()
					x(2).anyGlass()
				}
			}
			z(1) {
				y(-1) {
					x(-2).ironBlock()
					x(-1).sponge()
					x(0).sponge()
					x(1).sponge()
					x(2).ironBlock()
				}
				y(0) {
					x(-2).anyGlass()
					x(-1).sponge()
					x(0).type(Material.PISTON)
					x(1).sponge()
					x(2).anyGlass()
				}
			}
			z(0) {
				y(-1) {
					x(-2).anyStairs(PrepackagedPreset.stairs(RelativeFace.RIGHT, Bisected.Half.TOP, shape = Stairs.Shape.STRAIGHT))
					x(-1).steelBlock()
					x(0).powerInput()
					x(1).steelBlock()
					x(2).anyStairs(PrepackagedPreset.stairs(RelativeFace.LEFT, Bisected.Half.TOP, shape = Stairs.Shape.STRAIGHT))
				}
				y(0) {
					x(-2).anyGlassPane(PrepackagedPreset.pane(RelativeFace.FORWARD, RelativeFace.RIGHT))
					x(-1).anyGlassPane(PrepackagedPreset.pane(RelativeFace.FORWARD, RelativeFace.RIGHT, RelativeFace.LEFT))
					x(0).anyGlass()
					x(1).anyGlassPane(PrepackagedPreset.pane(RelativeFace.FORWARD, RelativeFace.RIGHT, RelativeFace.LEFT))
					x(2).anyGlassPane(PrepackagedPreset.pane(RelativeFace.FORWARD, RelativeFace.LEFT))
				}
			}
		}
	}
}
