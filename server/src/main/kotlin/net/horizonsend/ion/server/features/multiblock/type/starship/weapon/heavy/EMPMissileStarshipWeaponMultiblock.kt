package net.horizonsend.ion.server.features.multiblock.type.starship.weapon.heavy

import net.horizonsend.ion.server.features.multiblock.shape.MultiblockShape
import net.horizonsend.ion.server.features.multiblock.type.DisplayNameMultilblock
import net.horizonsend.ion.server.features.multiblock.type.starship.weapon.SignlessStarshipWeaponMultiblock
import net.horizonsend.ion.server.features.multiblock.util.PrepackagedPreset
import net.horizonsend.ion.server.features.starship.active.ActiveStarship
import net.horizonsend.ion.server.features.starship.subsystem.weapon.secondary.ArsenalRocketStarshipWeaponSubsystem
import net.horizonsend.ion.server.features.starship.subsystem.weapon.secondary.EMPMissileStarshipWeaponSubsystem
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.RelativeFace
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.Vec3i
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.Component.text
import org.bukkit.block.BlockFace

class EMPMissileStarshipWeaponMultiblock : SignlessStarshipWeaponMultiblock<EMPMissileStarshipWeaponSubsystem>(), DisplayNameMultilblock {
	override val key: String = "emp_missile"
	override fun createSubsystem(
		starship: ActiveStarship,
		pos: Vec3i,
		face: BlockFace,
	): EMPMissileStarshipWeaponSubsystem {
		return EMPMissileStarshipWeaponSubsystem(starship, pos, face, this)
	}

	fun getFirePointOffset(): Vec3i = Vec3i(+0, 0, 7)

	override val displayName: Component get() = text("EMP Missile")
	override val description: Component get() = text("Launches missiles that deal large damage and reduce shield strength. Consumes ammo.")

	override fun MultiblockShape.buildStructure() {
		z(1) {
			y(0) {
				x(-1).anyWall()
				x(0).ironBlock()
				x(1).anyWall()
			}
		}
		z(0) {
			y(0) {
				x(-1).anyGlassPane(PrepackagedPreset.pane(RelativeFace.FORWARD, RelativeFace.RIGHT))
				x(0).sponge()
				x(1).anyGlassPane(PrepackagedPreset.pane(RelativeFace.FORWARD, RelativeFace.LEFT))
			}
		}
		z(5) {
			y(0) {
				x(0).dispenser()
			}
		}
		z(4) {
			y(0) {
				x(0).titaniumBlock()
			}
		}
		z(3) {
			y(0) {
				x(0).titaniumBlock()
			}
		}
		z(2) {
			y(0) {
				x(0).kothBlock()
			}
		}
	}
}
