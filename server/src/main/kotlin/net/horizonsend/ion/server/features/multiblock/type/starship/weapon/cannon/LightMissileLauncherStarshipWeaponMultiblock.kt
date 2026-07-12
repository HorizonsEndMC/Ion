package net.horizonsend.ion.server.features.multiblock.type.starship.weapon.cannon

import net.horizonsend.ion.server.features.multiblock.shape.MultiblockShape
import net.horizonsend.ion.server.features.multiblock.type.DisplayNameMultilblock
import net.horizonsend.ion.server.features.multiblock.type.starship.weapon.SignlessStarshipWeaponMultiblock
import net.horizonsend.ion.server.features.multiblock.util.PrepackagedPreset
import net.horizonsend.ion.server.features.starship.active.ActiveStarship
import net.horizonsend.ion.server.features.starship.subsystem.weapon.primary.LightMissileLauncherStarshipWeaponSubsystem
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.RelativeFace
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.Vec3i
import net.kyori.adventure.text.Component
import org.bukkit.block.BlockFace
import org.bukkit.block.data.Bisected
import org.bukkit.block.data.type.Stairs

abstract class LightMissileLauncherStarshipWeaponMultiblock : SignlessStarshipWeaponMultiblock<LightMissileLauncherStarshipWeaponSubsystem>(), DisplayNameMultilblock {
    override val key: String = "light_missile"

    override fun createSubsystem(
        starship: ActiveStarship,
        pos: Vec3i,
        face: BlockFace
    ): LightMissileLauncherStarshipWeaponSubsystem {
        return LightMissileLauncherStarshipWeaponSubsystem(starship, pos, face, this)
    }

    override val description: Component
        get() = Component.text("Launches a small missile that tracks other starships.")

    abstract fun getFirePointOffset(): Vec3i
}

object TopLightMissileLauncherStarshipWeaponMultiblock : LightMissileLauncherStarshipWeaponMultiblock() {
	override val displayName: Component
		get() = Component.text("Light Missile Launcher (Top)")

	override fun getFirePointOffset(): Vec3i = Vec3i(+0, +6, +0)

	override fun MultiblockShape.buildStructure() {
		z(0) {
			y(0) {
				x(1).ironBlock()
				x(0).powerInput()
				x(-1).ironBlock()
			}
			y(1) {
				x(1).titaniumBlock()
				x(0).anyGlass()
				x(-1).titaniumBlock()
			}
			y(2) {
				x(1).titaniumBlock()
				x(0).anyGlass()
				x(-1).titaniumBlock()
			}
			y(3) {
				x(1).titaniumBlock()
				x(0).anyGlass()
				x(-1).titaniumBlock()
			}
			y(4) {
				x(1).ironBlock()
				x(0).anyStairs(PrepackagedPreset.stairs(RelativeFace.BACKWARD, Bisected.Half.BOTTOM, shape = Stairs.Shape.STRAIGHT))
				x(-1).ironBlock()
			}	}
		z(1)
		{		y(0) {
			x(1).anyStairs(PrepackagedPreset.stairs(RelativeFace.LEFT, Bisected.Half.TOP, shape = Stairs.Shape.STRAIGHT))
			x(0).sponge()
			x(-1).anyStairs(PrepackagedPreset.stairs(RelativeFace.RIGHT, Bisected.Half.TOP, shape = Stairs.Shape.STRAIGHT))		}
			y(1) {
				x(1).anyGlass()
				x(0).sponge()
				x(-1).anyGlass()
			}
			y(2) {
				x(1).anyGlass()
				x(0).sponge()
				x(-1).anyGlass()
			}
			y(3) {
				x(1).anyGlass()
				x(0).dispenser()
				x(-1).anyGlass()
			}
			y(4) {
				x(1).anyStairs(PrepackagedPreset.stairs(RelativeFace.RIGHT, Bisected.Half.BOTTOM, shape = Stairs.Shape.STRAIGHT))
				x(-1).anyStairs(PrepackagedPreset.stairs(RelativeFace.LEFT, Bisected.Half.BOTTOM, shape = Stairs.Shape.STRAIGHT))
			}	}
		z(2) {
			y(0) {
				x(1).ironBlock()
				x(0).anyStairs(PrepackagedPreset.stairs(RelativeFace.BACKWARD, Bisected.Half.TOP, shape = Stairs.Shape.STRAIGHT))
				x(-1).ironBlock()
			}
			y(1) {
				x(1).titaniumBlock()
				x(0).anyGlass()
				x(-1).titaniumBlock()
			}
			y(2) {
				x(1).titaniumBlock()
				x(0).anyGlass()
				x(-1).titaniumBlock()
			}
			y(3) {
				x(1).titaniumBlock()
				x(0).anyGlass()
				x(-1).titaniumBlock()
			}
			y(4) {
				x(1).ironBlock()
				x(0).anyStairs(PrepackagedPreset.stairs(RelativeFace.FORWARD, Bisected.Half.BOTTOM, shape = Stairs.Shape.STRAIGHT))
				x(-1).ironBlock()
			}
		}}
}

object BottomLightMissileLauncherStarshipWeaponMultiblock : LightMissileLauncherStarshipWeaponMultiblock() {
	override val displayName: Component
		get() = Component.text("Light Missile Launcher (Bottom)")

	override fun getFirePointOffset(): Vec3i = Vec3i(+0, -6, +0)

	override fun MultiblockShape.buildStructure() {
		z(0) {
			y(-4) {
				x(1).ironBlock()
				x(0).anyStairs(PrepackagedPreset.stairs(RelativeFace.BACKWARD, Bisected.Half.TOP, shape = Stairs.Shape.STRAIGHT))
				x(-1).ironBlock()
			}
			y(-3) {
				x(1).titaniumBlock()
				x(0).anyGlass()
				x(-1).titaniumBlock()
			}
			y(-2) {
				x(1).titaniumBlock()
				x(0).anyGlass()
				x(-1).titaniumBlock()
			}
			y(-1) {
				x(1).titaniumBlock()
				x(0).anyGlass()
				x(-1).titaniumBlock()
			}
			y(0) {
				x(1).ironBlock()
				x(0).powerInput()
				x(-1).ironBlock()
			}	}
		z(1) {
			y(-4) {
				x(1).anyStairs(PrepackagedPreset.stairs(RelativeFace.RIGHT, Bisected.Half.TOP, shape = Stairs.Shape.STRAIGHT))
				x(-1).anyStairs(PrepackagedPreset.stairs(RelativeFace.LEFT, Bisected.Half.TOP, shape = Stairs.Shape.STRAIGHT))
			}
			y(-3) {
				x(1).anyGlass()
				x(0).dispenser()
				x(-1).anyGlass()
			}
			y(-2) {
				x(1).anyGlass()
				x(0).sponge()
				x(-1).anyGlass()
			}
			y(-1) {
				x(1).anyGlass()
				x(0).sponge()
				x(-1).anyGlass()
			}
			y(0) {
				x(1).anyStairs(PrepackagedPreset.stairs(RelativeFace.LEFT, Bisected.Half.BOTTOM, shape = Stairs.Shape.STRAIGHT))
				x(0).sponge()
				x(-1).anyStairs(PrepackagedPreset.stairs(RelativeFace.RIGHT, Bisected.Half.BOTTOM, shape = Stairs.Shape.STRAIGHT))
			}	}
		z(2) {
			y(-4) {
				x(1).ironBlock()
				x(0).anyStairs(PrepackagedPreset.stairs(RelativeFace.FORWARD, Bisected.Half.TOP, shape = Stairs.Shape.STRAIGHT))
				x(-1).ironBlock()
			}
			y(-3) {
				x(1).titaniumBlock()
				x(0).anyGlass()
				x(-1).titaniumBlock()
			}
			y(-2) {
				x(1).titaniumBlock()
				x(0).anyGlass()
				x(-1).titaniumBlock()
			}
			y(-1) {
				x(1).titaniumBlock()
				x(0).anyGlass()
				x(-1).titaniumBlock()
			}
			y(0) {
				x(1).ironBlock()
				x(0).anyStairs(PrepackagedPreset.stairs(RelativeFace.BACKWARD, Bisected.Half.BOTTOM, shape = Stairs.Shape.STRAIGHT))
				x(-1).ironBlock()
			}	}}
}
