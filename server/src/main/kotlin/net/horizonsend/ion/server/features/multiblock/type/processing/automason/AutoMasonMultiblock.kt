package net.horizonsend.ion.server.features.multiblock.type.processing.automason

import net.horizonsend.ion.common.utils.text.colors.HEColorScheme.Companion.HE_LIGHT_ORANGE
import net.horizonsend.ion.common.utils.text.ofChildren
import net.horizonsend.ion.server.features.multiblock.Multiblock
import net.horizonsend.ion.server.features.multiblock.entity.PersistentMultiblockData
import net.horizonsend.ion.server.features.multiblock.manager.MultiblockManager
import net.horizonsend.ion.server.features.multiblock.shape.MultiblockShape
import net.horizonsend.ion.server.features.multiblock.type.DisplayNameMultilblock
import net.horizonsend.ion.server.features.multiblock.type.EntityMultiblock
import net.horizonsend.ion.server.features.multiblock.util.PrepackagedPreset
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.RelativeFace
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.Vec3i
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.Component.empty
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.NamedTextColor.RED
import org.bukkit.Material
import org.bukkit.World
import org.bukkit.block.BlockFace
import org.bukkit.block.data.Bisected
import org.bukkit.block.data.type.Stairs

sealed class AutoMasonMultiblock(val mergeEnabled: Boolean, left: Boolean) : Multiblock(), EntityMultiblock<AutoMasonMultiblockEntity>, DisplayNameMultilblock {
	override val name: String = "automason"

	abstract val inputOffset: Vec3i?
	abstract val outputOffset: Vec3i?

	override val signText: Array<Component?> = createSignText(
		ofChildren(text("Auto ", NamedTextColor.DARK_GRAY), text("Mason", HE_LIGHT_ORANGE)),
		if (mergeEnabled) text("Mergable", RED) else empty(),
		null,
		null
	)

	override val displayName: Component = ofChildren(
		text("Auto ", NamedTextColor.DARK_GRAY),
		text("Mason", HE_LIGHT_ORANGE),
		if (left) text(" Left", NamedTextColor.GRAY) else text(" Right", NamedTextColor.GRAY),
		if (mergeEnabled) text(" Mergable", RED) else empty()
	)

	override val description: Component = text("Executes the type of stonecutter recipe of the block displayed in the center. Input items are consumed to craft the output.")

	override fun createEntity(manager: MultiblockManager, data: PersistentMultiblockData, world: World, x: Int, y: Int, z: Int, structureDirection: BlockFace): AutoMasonMultiblockEntity {
		return AutoMasonMultiblockEntity(data, this, manager, x, y, z, world, structureDirection)
	}

	data object AutoMasonRight : AutoMasonMultiblock(false, false) {
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
					x(0).endRod(PrepackagedPreset.simpleDirectional(RelativeFace.BACKWARD, example = Material.END_ROD.createBlockData()))
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
					x(-1).endRod(PrepackagedPreset.simpleDirectional(RelativeFace.RIGHT, example = Material.END_ROD.createBlockData()))
					x(1).endRod(PrepackagedPreset.simpleDirectional(RelativeFace.LEFT, example = Material.END_ROD.createBlockData()))
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
					x(0).endRod(PrepackagedPreset.simpleDirectional(RelativeFace.FORWARD, example = Material.END_ROD.createBlockData()))
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

	data object AutoMasonLeft : AutoMasonMultiblock(mergeEnabled = false, left = true) {
		override val outputOffset: Vec3i = Vec3i(-3, 0, 3)
		override val inputOffset: Vec3i = Vec3i(3, 0, 3)

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
					x(0).endRod(PrepackagedPreset.simpleDirectional(RelativeFace.BACKWARD, example = Material.END_ROD.createBlockData()))
					x(1).ironBlock()
					x(2).ironBlock()
					x(3).anyStairs(PrepackagedPreset.stairs(RelativeFace.BACKWARD, Bisected.Half.BOTTOM, shape = Stairs.Shape.STRAIGHT))
				}
			}
			z(3) {
				y(-1) {
					x(-3).extractor()
					x(-2).sponge()
					x(-1).type(Material.STONECUTTER)
					x(0).type(Material.PISTON)
					x(1).type(Material.STONECUTTER)
					x(2).sponge()
					x(3).anyGlass()
				}
				y(0) {
					x(-3).anyPipedInventory()
					x(-2).type(Material.PISTON)
					x(-1).endRod(PrepackagedPreset.simpleDirectional(RelativeFace.RIGHT, example = Material.END_ROD.createBlockData()))
					x(1).endRod(PrepackagedPreset.simpleDirectional(RelativeFace.LEFT, example = Material.END_ROD.createBlockData()))
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
					x(0).endRod(PrepackagedPreset.simpleDirectional(RelativeFace.FORWARD, example = Material.END_ROD.createBlockData()))
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

	data object AutoMasonRightMergable : AutoMasonMultiblock(mergeEnabled = true, left = false) {
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
					x(3).netheriteBlock()
				}
				y(0) {
					x(-3).anyStairs(PrepackagedPreset.stairs(RelativeFace.BACKWARD, Bisected.Half.BOTTOM, shape = Stairs.Shape.STRAIGHT))
					x(-2).ironBlock()
					x(-1).ironBlock()
					x(0).endRod(PrepackagedPreset.simpleDirectional(RelativeFace.BACKWARD, example = Material.END_ROD.createBlockData()))
					x(1).ironBlock()
					x(2).ironBlock()
					x(3).netheriteBlock()
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
					x(3).type(Material.LODESTONE)
				}
				y(0) {
					x(-3).anyPipedInventory()
					x(-2).type(Material.PISTON)
					x(-1).endRod(PrepackagedPreset.simpleDirectional(RelativeFace.RIGHT, example = Material.END_ROD.createBlockData()))
					x(1).endRod(PrepackagedPreset.simpleDirectional(RelativeFace.LEFT, example = Material.END_ROD.createBlockData()))
					x(2).type(Material.PISTON)
					x(3).type(Material.LODESTONE)
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
					x(3).netheriteBlock()
				}
				y(0) {
					x(-3).anyStairs(PrepackagedPreset.stairs(RelativeFace.FORWARD, Bisected.Half.BOTTOM, shape = Stairs.Shape.STRAIGHT))
					x(-2).ironBlock()
					x(-1).ironBlock()
					x(0).endRod(PrepackagedPreset.simpleDirectional(RelativeFace.FORWARD, example = Material.END_ROD.createBlockData()))
					x(1).ironBlock()
					x(2).ironBlock()
					x(3).netheriteBlock()
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

	data object AutoMasonLeftMergable : AutoMasonMultiblock(mergeEnabled = true, left = true) {
		override val outputOffset: Vec3i = Vec3i(-3, 0, 3)
		override val inputOffset: Vec3i = Vec3i(3, 0, 3)

		override fun MultiblockShape.buildStructure() {
			z(4) {
				y(-1) {
					x(-3).netheriteBlock()
					x(-2).ironBlock()
					x(-1).ironBlock()
					x(0).type(Material.STONECUTTER)
					x(1).ironBlock()
					x(2).ironBlock()
					x(3).anyStairs(PrepackagedPreset.stairs(RelativeFace.BACKWARD, Bisected.Half.TOP, shape = Stairs.Shape.STRAIGHT))
				}
				y(0) {
					x(-3).netheriteBlock()
					x(-2).ironBlock()
					x(-1).ironBlock()
					x(0).endRod(PrepackagedPreset.simpleDirectional(RelativeFace.BACKWARD, example = Material.END_ROD.createBlockData()))
					x(1).ironBlock()
					x(2).ironBlock()
					x(3).anyStairs(PrepackagedPreset.stairs(RelativeFace.BACKWARD, Bisected.Half.BOTTOM, shape = Stairs.Shape.STRAIGHT))
				}
			}
			z(3) {
				y(-1) {
					x(-3).type(Material.LODESTONE)
					x(-2).sponge()
					x(-1).type(Material.STONECUTTER)
					x(0).type(Material.PISTON)
					x(1).type(Material.STONECUTTER)
					x(2).sponge()
					x(3).anyGlass()
				}
				y(0) {
					x(-3).type(Material.LODESTONE)
					x(-2).type(Material.PISTON)
					x(-1).endRod(PrepackagedPreset.simpleDirectional(RelativeFace.RIGHT, example = Material.END_ROD.createBlockData()))
					x(1).endRod(PrepackagedPreset.simpleDirectional(RelativeFace.LEFT, example = Material.END_ROD.createBlockData()))
					x(2).type(Material.PISTON)
					x(3).anyPipedInventory()
				}
			}
			z(2) {
				y(-1) {
					x(-3).netheriteBlock()
					x(-2).ironBlock()
					x(-1).ironBlock()
					x(0).type(Material.STONECUTTER)
					x(1).ironBlock()
					x(2).ironBlock()
					x(3).anyStairs(PrepackagedPreset.stairs(RelativeFace.FORWARD, Bisected.Half.TOP, shape = Stairs.Shape.STRAIGHT))
				}
				y(0) {
					x(-3).netheriteBlock()
					x(-2).ironBlock()
					x(-1).ironBlock()
					x(0).endRod(PrepackagedPreset.simpleDirectional(RelativeFace.FORWARD, example = Material.END_ROD.createBlockData()))
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
