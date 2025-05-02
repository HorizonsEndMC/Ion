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

object TurretBaseMultiblock : Multiblock(), SubsystemMultiblock<CustomTurretSubsystem> {
	override val name: String = "Turret Base"
	override val signText: Array<Component?> = arrayOf(null, null, null, null)

	init {
		shape.signCentered()
		shape.ignoreDirection()
	}

	override fun MultiblockShape.buildStructure() {
		z(-1) {
			y(0) {
				x(-1).titaniumBlock()
				x(+0).titaniumBlock()
				x(+1).titaniumBlock()
			}
		}
		z(0) {
			y(0) {
				x(-1).titaniumBlock()
				x(+0).type(Material.LOOM)
				x(+1).titaniumBlock()
			}
		}
		z(+1) {
			y(0) {
				x(-1).titaniumBlock()
				x(+0).titaniumBlock()
				x(+1).titaniumBlock()
			}
		}
	}

	override fun createSubsystem(starship: ActiveStarship, pos: Vec3i, face: BlockFace): CustomTurretSubsystem {
		return CustomTurretSubsystem(starship, pos, face)
	}
}
