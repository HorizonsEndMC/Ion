package net.horizonsend.ion.server.features.multiblock.type.starship.weapon.heavy

import net.horizonsend.ion.server.features.multiblock.shape.MultiblockShape
import net.horizonsend.ion.server.features.multiblock.type.DisplayNameMultilblock
import net.horizonsend.ion.server.features.multiblock.type.starship.weapon.SignlessStarshipWeaponMultiblock
import net.horizonsend.ion.server.features.multiblock.util.PrepackagedPreset
import net.horizonsend.ion.server.features.starship.active.ActiveStarship
import net.horizonsend.ion.server.features.starship.subsystem.weapon.secondary.SwarmMissileStarshipWeaponSubsystem
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.RelativeFace
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.Vec3i
import net.kyori.adventure.text.Component
import org.bukkit.Material
import org.bukkit.block.BlockFace
import org.bukkit.block.data.Bisected
import org.bukkit.block.data.type.Stairs

abstract class SwarmMissleStarshipWeaponMultiblock : SignlessStarshipWeaponMultiblock<SwarmMissileStarshipWeaponSubsystem>(), DisplayNameMultilblock {
    override val key: String = "swarm_missile"

    override fun createSubsystem(
        starship: ActiveStarship,
        pos: Vec3i,
        face: BlockFace
    ): SwarmMissileStarshipWeaponSubsystem {
        return SwarmMissileStarshipWeaponSubsystem(starship, pos, face, this)
    }

    override val description: Component
        get() = Component.text("Launches a swarm of missiles that tracks other starships.")

    abstract fun getFirePointOffset(): Vec3i
}

object HorizontalSwarmMissileStarshipWeaponMultiblock : SwarmMissleStarshipWeaponMultiblock() {
    override val displayName: Component
        get() = Component.text("Swarm Missile Launcher (Side)")

    override fun getFirePointOffset(): Vec3i = Vec3i(+0, +0, +8)

	override fun MultiblockShape.buildStructure() {
		z(0) {
			y(0) {
				x(1).anyStairs(PrepackagedPreset.stairs(RelativeFace.FORWARD, Bisected.Half.TOP, shape = Stairs.Shape.STRAIGHT))
				x(0).powerInput()
				x(-1).anyStairs(PrepackagedPreset.stairs(RelativeFace.FORWARD, Bisected.Half.TOP, shape = Stairs.Shape.STRAIGHT))
			}
			y(1) {
				x(1).ironBlock()
				x(0).sponge()
				x(-1).ironBlock()
			}
			y(2) {
				x(1).anyStairs(PrepackagedPreset.stairs(RelativeFace.FORWARD, Bisected.Half.BOTTOM, shape = Stairs.Shape.STRAIGHT))
				x(0).ironBlock()
				x(-1).anyStairs(PrepackagedPreset.stairs(RelativeFace.FORWARD, Bisected.Half.BOTTOM, shape = Stairs.Shape.STRAIGHT))
			}	}
		z(4) {
			y(0) {
				x(1).anyStairs(PrepackagedPreset.stairs(RelativeFace.BACKWARD, Bisected.Half.TOP, shape = Stairs.Shape.STRAIGHT))
				x(0).ironBlock()
				x(-1).anyStairs(PrepackagedPreset.stairs(RelativeFace.BACKWARD, Bisected.Half.TOP, shape = Stairs.Shape.STRAIGHT))
			}
			y(1) {
				x(1).anyDoubleSlab()
				x(-1).anyDoubleSlab()
			}
			y(2) {
				x(1).anyStairs(PrepackagedPreset.stairs(RelativeFace.BACKWARD, Bisected.Half.BOTTOM, shape = Stairs.Shape.STRAIGHT))
				x(0).ironBlock()
				x(-1).anyStairs(PrepackagedPreset.stairs(RelativeFace.BACKWARD, Bisected.Half.BOTTOM, shape = Stairs.Shape.STRAIGHT))
			}	}
		z(1) {
			y(1) {
				x(1).titaniumBlock()
				x(0).sponge()
				x(-1).titaniumBlock()
			}
			y(0) {
				x(0).aluminumBlock()		}
			y(2) {
				x(0).aluminumBlock()		}	}
		z(2) {
			y(1) {
				x(1).titaniumBlock()
				x(0).sponge()
				x(-1).titaniumBlock()
			}
			y(0) {
				x(0).anyGlass()		}
			y(2) {
				x(0).anyGlass()
			}	}
		z(3) {
			y(1) {
				x(1).titaniumBlock()
				x(0).dispenser()
				x(-1).titaniumBlock()		}
			y(0) {
				x(0).aluminumBlock()
			}
			y(2) {
				x(0).aluminumBlock()
			}
		}}

}

object TopSwarmMissileStarshipWeaponMultiblock : SwarmMissleStarshipWeaponMultiblock() {
    override val displayName: Component
        get() = Component.text("Swarm Missile Launcher (Top)")

    override fun getFirePointOffset(): Vec3i = Vec3i(+0, +8, +0)

	override fun MultiblockShape.buildStructure() {	z(0) {
		y(0) {
			x(1).anyStairs(PrepackagedPreset.stairs(RelativeFace.FORWARD, Bisected.Half.TOP, shape = Stairs.Shape.STRAIGHT))
			x(0).powerInput()
			x(-1).anyStairs(PrepackagedPreset.stairs(RelativeFace.FORWARD, Bisected.Half.TOP, shape = Stairs.Shape.STRAIGHT))
		}
		y(4) {
			x(1).anyStairs(PrepackagedPreset.stairs(RelativeFace.LEFT, Bisected.Half.BOTTOM, shape = Stairs.Shape.STRAIGHT))
			x(0).ironBlock()
			x(-1).anyStairs(PrepackagedPreset.stairs(RelativeFace.RIGHT, Bisected.Half.BOTTOM, shape = Stairs.Shape.STRAIGHT))
		}
		y(1) {
			x(0).aluminumBlock()
		}
		y(2) {
			x(0).anyGlass()
		}
		y(3) {
			x(0).aluminumBlock()
		}
	}
		z(1) {
			y(0) {
				x(1).ironBlock()
				x(0).sponge()
				x(-1).ironBlock()
			}
			y(1) {
				x(1).titaniumBlock()
				x(0).sponge()
				x(-1).titaniumBlock()
			}
			y(2) {
				x(1).titaniumBlock()
				x(0).sponge()
				x(-1).titaniumBlock()
			}
			y(3) {
				x(1).titaniumBlock()
				x(0).dispenser()
				x(-1).titaniumBlock()
			}
			y(4) {
				x(1).anyStairs(PrepackagedPreset.stairs(RelativeFace.LEFT, Bisected.Half.BOTTOM, shape = Stairs.Shape.STRAIGHT))
				x(-1).anyStairs(PrepackagedPreset.stairs(RelativeFace.RIGHT, Bisected.Half.BOTTOM, shape = Stairs.Shape.STRAIGHT))
			}	}
		z(2) {
			y(0) {
				x(1).anyStairs(PrepackagedPreset.stairs(RelativeFace.BACKWARD, Bisected.Half.TOP, shape = Stairs.Shape.STRAIGHT))
				x(0).ironBlock()
				x(-1).anyStairs(PrepackagedPreset.stairs(RelativeFace.BACKWARD, Bisected.Half.TOP, shape = Stairs.Shape.STRAIGHT))
			}
			y(4) {
				x(1).anyStairs(PrepackagedPreset.stairs(RelativeFace.LEFT, Bisected.Half.BOTTOM, shape = Stairs.Shape.STRAIGHT))
				x(0).ironBlock()
				x(-1).anyStairs(PrepackagedPreset.stairs(RelativeFace.RIGHT, Bisected.Half.BOTTOM, shape = Stairs.Shape.STRAIGHT))
			}
			y(1) {
				x(0).aluminumBlock()		}
			y(2) {
				x(0).anyGlass()
			}
			y(3) {
				x(0).aluminumBlock()
			}
		}}
}

object BottomSwarmMissileStarshipWeaponMultiblock : SwarmMissleStarshipWeaponMultiblock() {
    override val displayName: Component
        get() = Component.text("Swarm Missile Launcher (Bottom)")

    override fun getFirePointOffset(): Vec3i = Vec3i(+0, -8, +0)

	override fun MultiblockShape.buildStructure() {
		z(0) {
			y(-4) {
				x(1).anyStairs(PrepackagedPreset.stairs(RelativeFace.LEFT, Bisected.Half.TOP, shape = Stairs.Shape.STRAIGHT))
				x(0).ironBlock()
				x(-1).anyStairs(PrepackagedPreset.stairs(RelativeFace.RIGHT, Bisected.Half.TOP, shape = Stairs.Shape.STRAIGHT))
			}
			y(0) {
				x(1).anyStairs(PrepackagedPreset.stairs(RelativeFace.FORWARD, Bisected.Half.BOTTOM, shape = Stairs.Shape.STRAIGHT))
				x(0).powerInput()
				x(-1).anyStairs(PrepackagedPreset.stairs(RelativeFace.FORWARD, Bisected.Half.BOTTOM, shape = Stairs.Shape.STRAIGHT))
			}
			y(-3) {
				x(0).aluminumBlock()
			}
			y(-2) {
				x(0).anyGlass()
			}
			y(-1) {
				x(0).aluminumBlock()
			}
		}
		z(1) {
			y(-4) {
				x(1).anyStairs(PrepackagedPreset.stairs(RelativeFace.LEFT, Bisected.Half.TOP, shape = Stairs.Shape.STRAIGHT))
				x(-1).anyStairs(PrepackagedPreset.stairs(RelativeFace.RIGHT, Bisected.Half.TOP, shape = Stairs.Shape.STRAIGHT))
			}
			y(-3) {
				x(1).titaniumBlock()
				x(0).dispenser()
				x(-1).titaniumBlock()
			}
			y(-2) {
				x(1).titaniumBlock()
				x(0).sponge()
				x(-1).titaniumBlock()
			}
			y(-1) {
				x(1).titaniumBlock()
				x(0).sponge()
				x(-1).titaniumBlock()
			}
			y(0) {
				x(1).ironBlock()
				x(0).sponge()
				x(-1).ironBlock()
			}	}
		z(2) {
			y(-4) {
				x(1).anyStairs(PrepackagedPreset.stairs(RelativeFace.LEFT, Bisected.Half.TOP, shape = Stairs.Shape.STRAIGHT))
				x(0).ironBlock()
				x(-1).anyStairs(PrepackagedPreset.stairs(RelativeFace.RIGHT, Bisected.Half.TOP, shape = Stairs.Shape.STRAIGHT))
			}
			y(0) {
				x(1).anyStairs(PrepackagedPreset.stairs(RelativeFace.BACKWARD, Bisected.Half.BOTTOM, shape = Stairs.Shape.STRAIGHT))
				x(0).ironBlock()
				x(-1).anyStairs(PrepackagedPreset.stairs(RelativeFace.BACKWARD, Bisected.Half.BOTTOM, shape = Stairs.Shape.STRAIGHT))
			}
			y(-3) {
				x(0).aluminumBlock()
			}
			y(-2) {
				x(0).anyGlass()
			}
			y(-1) {
				x(0).aluminumBlock()
			}
		}}
}
