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
        z(5) {
            y(-1) {
                x(0).ironBlock()
            }
            y(0) {
                x(1).ironBlock()
                x(0).dispenser()
                x(-1).ironBlock()
            }
            y(1) {
                x(0).ironBlock()
            }
        }
        z(4) {
            y(-1) {
                x(1).anyStairs(PrepackagedPreset.stairs(RelativeFace.LEFT, Bisected.Half.TOP, shape = Stairs.Shape.STRAIGHT))
                x(0).anyGlass()
                x(-1).anyStairs(PrepackagedPreset.stairs(RelativeFace.RIGHT, Bisected.Half.TOP, shape = Stairs.Shape.STRAIGHT))
            }
            y(0) {
                x(1).anyGlass()
                x(0).type(Material.COAL_BLOCK)
                x(-1).anyGlass()
            }
            y(1) {
                x(1).anyStairs(PrepackagedPreset.stairs(RelativeFace.LEFT, Bisected.Half.BOTTOM, shape = Stairs.Shape.STRAIGHT))
                x(0).anyGlass()
                x(-1).anyStairs(PrepackagedPreset.stairs(RelativeFace.RIGHT, Bisected.Half.BOTTOM, shape = Stairs.Shape.STRAIGHT))
            }
        }
        z(3) {
            y(-1) {
                x(1).anyStairs(PrepackagedPreset.stairs(RelativeFace.LEFT, Bisected.Half.TOP, shape = Stairs.Shape.STRAIGHT))
                x(0).aluminumBlock()
                x(-1).anyStairs(PrepackagedPreset.stairs(RelativeFace.RIGHT, Bisected.Half.TOP, shape = Stairs.Shape.STRAIGHT))
            }
            y(0) {
                x(1).titaniumBlock()
                x(0).type(Material.COAL_BLOCK)
                x(-1).titaniumBlock()
            }
            y(1) {
                x(1).anyStairs(PrepackagedPreset.stairs(RelativeFace.LEFT, Bisected.Half.BOTTOM, shape = Stairs.Shape.STRAIGHT))
                x(0).anyGlass()
                x(-1).anyStairs(PrepackagedPreset.stairs(RelativeFace.RIGHT, Bisected.Half.BOTTOM, shape = Stairs.Shape.STRAIGHT))
            }
        }
        z(2) {
            y(-1) {
                x(0).sponge()
            }
            y(0) {
                x(1).sponge()
                x(0).type(Material.COAL_BLOCK)
                x(-1).sponge()
            }
            y(1) {
                x(0).sponge()
            }
        }
        z(1) {
            y(-1) {
                x(0).sponge()
            }
            y(0) {
                x(1).sponge()
                x(0).type(Material.COAL_BLOCK)
                x(-1).sponge()
            }
            y(1) {
                x(0).sponge()
            }
        }
        z(0) {
            y(0) {
                x(0).type(Material.COAL_BLOCK)
            }
        }
    }
}

object TopSwarmMissileStarshipWeaponMultiblock : SwarmMissleStarshipWeaponMultiblock() {
    override val displayName: Component
        get() = Component.text("Swarm Missile Launcher (Top)")

    override fun getFirePointOffset(): Vec3i = Vec3i(+0, +8, +0)

    override fun MultiblockShape.buildStructure() {
        z(1) {
            y(1) {
                x(0).sponge()
            }
            y(2) {
                x(0).sponge()
            }
            y(3) {
                x(1).anyStairs(PrepackagedPreset.stairs(RelativeFace.LEFT, Bisected.Half.TOP, shape = Stairs.Shape.STRAIGHT))
                x(0).titaniumBlock()
                x(-1).anyStairs(PrepackagedPreset.stairs(RelativeFace.RIGHT, Bisected.Half.TOP, shape = Stairs.Shape.STRAIGHT))
            }
            y(4) {
                x(1).anyStairs(PrepackagedPreset.stairs(RelativeFace.LEFT, Bisected.Half.BOTTOM, shape = Stairs.Shape.STRAIGHT))
                x(0).anyGlass()
                x(-1).anyStairs(PrepackagedPreset.stairs(RelativeFace.RIGHT, Bisected.Half.BOTTOM, shape = Stairs.Shape.STRAIGHT))
            }
            y(5) {
                x(0).ironBlock()
            }
        }
        z(0) {
            y(0) {
                x(0).type(Material.COAL_BLOCK)
            }
            y(1) {
                x(1).sponge()
                x(0).type(Material.COAL_BLOCK)
                x(-1).sponge()
            }
            y(2) {
                x(1).sponge()
                x(0).type(Material.COAL_BLOCK)
                x(-1).sponge()
            }
            y(3) {
                x(1).aluminumBlock()
                x(0).type(Material.COAL_BLOCK)
                x(-1).anyGlass()
            }
            y(4) {
                x(1).anyGlass()
                x(0).type(Material.COAL_BLOCK)
                x(-1).anyGlass()
            }
            y(5) {
                x(1).ironBlock()
                x(0).dispenser()
                x(-1).ironBlock()
            }
        }
        z(-1) {
            y(1) {
                x(0).sponge()
            }
            y(2) {
                x(0).sponge()
            }
            y(3) {
                x(1).anyStairs(PrepackagedPreset.stairs(RelativeFace.LEFT, Bisected.Half.TOP, shape = Stairs.Shape.STRAIGHT))
                x(0).titaniumBlock()
                x(-1).anyStairs(PrepackagedPreset.stairs(RelativeFace.RIGHT, Bisected.Half.TOP, shape = Stairs.Shape.STRAIGHT))
            }
            y(4) {
                x(1).anyStairs(PrepackagedPreset.stairs(RelativeFace.LEFT, Bisected.Half.BOTTOM, shape = Stairs.Shape.STRAIGHT))
                x(0).anyGlass()
                x(-1).anyStairs(PrepackagedPreset.stairs(RelativeFace.RIGHT, Bisected.Half.BOTTOM, shape = Stairs.Shape.STRAIGHT))
            }
            y(5) {
                x(0).ironBlock()
            }
        }
    }
}

object BottomSwarmMissileStarshipWeaponMultiblock : SwarmMissleStarshipWeaponMultiblock() {
    override val displayName: Component
        get() = Component.text("Swarm Missile Launcher (Bottom)")

    override fun getFirePointOffset(): Vec3i = Vec3i(+0, -8, +0)

    override fun MultiblockShape.buildStructure() {
        z(-1) {
            y(-5) {
                x(0).ironBlock()
            }
            y(-4) {
                x(-1).anyStairs(PrepackagedPreset.stairs(RelativeFace.RIGHT, Bisected.Half.TOP, shape = Stairs.Shape.STRAIGHT))
                x(0).anyGlass()
                x(1).anyStairs(PrepackagedPreset.stairs(RelativeFace.LEFT, Bisected.Half.TOP, shape = Stairs.Shape.STRAIGHT))
            }
            y(-3) {
                x(-1).anyStairs(PrepackagedPreset.stairs(RelativeFace.RIGHT, Bisected.Half.BOTTOM, shape = Stairs.Shape.STRAIGHT))
                x(0).titaniumBlock()
                x(1).anyStairs(PrepackagedPreset.stairs(RelativeFace.LEFT, Bisected.Half.BOTTOM, shape = Stairs.Shape.STRAIGHT))
            }
            y(-2) {
                x(0).sponge()
            }
            y(-1) {
                x(0).sponge()
            }
        }
        z(0) {
            y(-5) {
                x(-1).ironBlock()
                x(0).dispenser()
                x(1).ironBlock()
            }
            y(-4) {
                x(-1).anyGlass()
                x(0).type(Material.COAL_BLOCK)
                x(1).anyGlass()
            }
            y(-3) {
                x(-1).aluminumBlock()
                x(0).type(Material.COAL_BLOCK)
                x(1).anyGlass()
            }
            y(-2) {
                x(-1).sponge()
                x(0).type(Material.COAL_BLOCK)
                x(1).sponge()
            }
            y(-1) {
                x(-1).sponge()
                x(0).type(Material.COAL_BLOCK)
                x(1).sponge()
            }
            y(0) {
                x(0).type(Material.COAL_BLOCK)
            }
        }
        z(1) {
            y(-5) {
                x(0).ironBlock()
            }
            y(-4) {
                x(-1).anyStairs(PrepackagedPreset.stairs(RelativeFace.RIGHT, Bisected.Half.TOP, shape = Stairs.Shape.STRAIGHT))
                x(0).anyGlass()
                x(1).anyStairs(PrepackagedPreset.stairs(RelativeFace.LEFT, Bisected.Half.TOP, shape = Stairs.Shape.STRAIGHT))
            }
            y(-3) {
                x(-1).anyStairs(PrepackagedPreset.stairs(RelativeFace.RIGHT, Bisected.Half.BOTTOM, shape = Stairs.Shape.STRAIGHT))
                x(0).titaniumBlock()
                x(1).anyStairs(PrepackagedPreset.stairs(RelativeFace.LEFT, Bisected.Half.BOTTOM, shape = Stairs.Shape.STRAIGHT))
            }
            y(-2) {
                x(0).sponge()
            }
            y(-1) {
                x(0).sponge()
            }
        }
    }
}