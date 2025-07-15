package net.horizonsend.ion.server.features.multiblock.type.starship.weapon.turret

import net.horizonsend.ion.server.features.multiblock.Multiblock
import net.horizonsend.ion.server.features.multiblock.shape.MultiblockShape
import net.horizonsend.ion.server.features.multiblock.type.starship.SubsystemMultiblock
import net.horizonsend.ion.server.features.starship.active.ActiveStarship
import net.horizonsend.ion.server.features.starship.subsystem.weapon.secondary.CustomTurretSubsystem
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.Vec3i
import net.kyori.adventure.text.Component
import org.bukkit.Material
import org.bukkit.block.BlockFace

sealed class CustomTurretBaseMultiblock : Multiblock(), SubsystemMultiblock<CustomTurretSubsystem> {
	override val name: String = "turret"
	override val signText: Array<Component?> = arrayOf(null, null, null, null)

	abstract val detectionOrigin: Vec3i
	abstract val furnaceOffset: Vec3i

	init {
		shape.signCentered()
		shape.ignoreDirection()
	}

	override fun createSubsystem(starship: ActiveStarship, pos: Vec3i, face: BlockFace): CustomTurretSubsystem {
		return CustomTurretSubsystem(starship, pos, face, this)
	}

	object CustomTurretBaseMultiblockTop : CustomTurretBaseMultiblock() {
		override val detectionOrigin: Vec3i = Vec3i(0, 2, 0)
		override val furnaceOffset: Vec3i = Vec3i(0, 1, 0)

		override fun MultiblockShape.buildStructure() {
			z(1) {
				y(1) {
					x(-1).titaniumBlock()
					x(0).titaniumBlock()
					x(1).titaniumBlock()
				}
			}
			z(0) {
				y(1) {
					x(-1).titaniumBlock()
					x(0).type(Material.BLAST_FURNACE)
					x(1).titaniumBlock()
				}
			}
			z(-1) {
				y(1) {
					x(-1).titaniumBlock()
					x(0).titaniumBlock()
					x(1).titaniumBlock()
				}
			}
		}
	}

	object CustomTurretBaseMultiblockBottom : CustomTurretBaseMultiblock() {
		override val detectionOrigin: Vec3i = Vec3i(0, -2, 0)
		override val furnaceOffset: Vec3i = Vec3i(0, -1, 0)

		override fun MultiblockShape.buildStructure() {
			z(1) {
				y(-1) {
					x(-1).titaniumBlock()
					x(0).titaniumBlock()
					x(1).titaniumBlock()
				}
			}
			z(0) {
				y(-1) {
					x(-1).titaniumBlock()
					x(0).type(Material.BLAST_FURNACE)
					x(1).titaniumBlock()
				}
			}
			z(-1) {
				y(-1) {
					x(-1).titaniumBlock()
					x(0).titaniumBlock()
					x(1).titaniumBlock()
				}
			}
		}
	}
}
