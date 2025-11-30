package net.horizonsend.ion.server.features.multiblock.type.starship.weapon.cannon

import net.horizonsend.ion.server.features.multiblock.shape.MultiblockShape
import net.horizonsend.ion.server.features.multiblock.type.DisplayNameMultilblock
import net.horizonsend.ion.server.features.multiblock.type.starship.weapon.SignlessStarshipWeaponMultiblock
import net.horizonsend.ion.server.features.multiblock.util.PrepackagedPreset
import net.horizonsend.ion.server.features.starship.active.ActiveStarship
import net.horizonsend.ion.server.features.starship.subsystem.weapon.primary.RapidMissileLauncherStarshipWeaponSubsystem
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.RelativeFace
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.Vec3i
import net.kyori.adventure.text.Component
import org.bukkit.Material
import org.bukkit.block.BlockFace
import org.bukkit.block.data.Bisected
import org.bukkit.block.data.type.Stairs

abstract class RapidMissileLauncherStarshipWeaponMultiblock : SignlessStarshipWeaponMultiblock<RapidMissileLauncherStarshipWeaponSubsystem>(), DisplayNameMultilblock {
    override val key: String = "rapid_missile"

    override fun createSubsystem(
        starship: ActiveStarship,
        pos: Vec3i,
        face: BlockFace
    ): RapidMissileLauncherStarshipWeaponSubsystem {
        return RapidMissileLauncherStarshipWeaponSubsystem(starship, pos, face, this)
    }

    override val description: Component
        get() = Component.text("Launches a small swarm of missiles that tracks other starships.")

    abstract fun getFirePointOffset(): Vec3i
}

object TopRapidMissileLauncherStarshipWeaponMultiblock : RapidMissileLauncherStarshipWeaponMultiblock() {
    override val displayName: Component
        get() = Component.text("Rapid Missile Launcher (Top)")

    override fun getFirePointOffset(): Vec3i = Vec3i(+0, +6, +0)

	override fun MultiblockShape.buildStructure() {
		z(0) {
			y(0) {
				x(-1).ironBlock()
				x(0).powerInput()
			}
			y(1) {
				x(-1).sponge()
				x(0).sponge()
			}
			y(2) {
				x(-1).titaniumBlock()
				x(0).titaniumBlock()
			}
			y(3) {
				x(-1).titaniumBlock()
				x(0).titaniumBlock()
			}
			y(4) {
				x(-1).dispenser()
				x(0).dispenser()
			}
		}
		z(1) {
			y(0) {
				x(-1).anyStairs(PrepackagedPreset.stairs(RelativeFace.BACKWARD, Bisected.Half.TOP, shape = Stairs.Shape.STRAIGHT))
				x(0).anyStairs(PrepackagedPreset.stairs(RelativeFace.BACKWARD, Bisected.Half.TOP, shape = Stairs.Shape.STRAIGHT))
			}
			y(1) {
				x(-1).sponge()
				x(0).sponge()
			}
			y(2) {
				x(-1).titaniumBlock()
				x(0).titaniumBlock()
			}
			y(3) {
				x(-1).titaniumBlock()
				x(0).titaniumBlock()
			}
			y(4) {
				x(-1).dispenser()
				x(0).dispenser()
			}
		}
	}
}

object BottomRapidMissileLauncherStarshipWeaponMultiblock : RapidMissileLauncherStarshipWeaponMultiblock() {
	override val displayName: Component
		get() = Component.text("Rapid Missile Launcher (Bottom)")

	override fun getFirePointOffset(): Vec3i = Vec3i(+0, -6, +0)

	override fun MultiblockShape.buildStructure() {
		z(0) {
			y(-4) {
				x(-1).dispenser()
				x(0).dispenser()
			}
			y(-3) {
				x(-1).titaniumBlock()
				x(0).titaniumBlock()
			}
			y(-2) {
				x(-1).titaniumBlock()
				x(0).titaniumBlock()
			}
			y(-1) {
				x(-1).sponge()
				x(0).sponge()
			}
			y(0) {
				x(-1).ironBlock()
				x(0).powerInput()
			}
		}
		z(1) {
			y(-4) {
				x(-1).dispenser()
				x(0).dispenser()
			}
			y(-3) {
				x(-1).titaniumBlock()
				x(0).titaniumBlock()
			}
			y(-2) {
				x(-1).titaniumBlock()
				x(0).titaniumBlock()
			}
			y(-1) {
				x(-1).sponge()
				x(0).sponge()
			}
			y(0) {
				x(-1).anyStairs(PrepackagedPreset.stairs(RelativeFace.BACKWARD, Bisected.Half.BOTTOM, shape = Stairs.Shape.STRAIGHT))
				x(0).anyStairs(PrepackagedPreset.stairs(RelativeFace.BACKWARD, Bisected.Half.BOTTOM, shape = Stairs.Shape.STRAIGHT))
			}
		}
	}

}
