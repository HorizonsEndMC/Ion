package net.horizonsend.ion.server.features.multiblock.type.starship.weapon.turret

import net.horizonsend.ion.server.features.multiblock.Multiblock
import net.horizonsend.ion.server.features.multiblock.shape.MultiblockShape
import net.horizonsend.ion.server.features.multiblock.type.starship.SubsystemMultiblock
import net.horizonsend.ion.server.features.starship.active.ActiveStarship
import net.horizonsend.ion.server.features.starship.subsystem.weapon.primary.DisintegratorBeamWeaponSubsystem
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.Vec3i
import org.bukkit.Material
import org.bukkit.block.BlockFace

sealed class DisintegratorBeamWeaponMultiblock : Multiblock(), SubsystemMultiblock<DisintegratorBeamWeaponSubsystem> {

    override val name: String = "disintegrator"
    override val signText = createSignText("&8Disintegrator", null, null, null)

    protected abstract fun getYFactor(): Int

    override fun MultiblockShape.buildStructure() {
        y(getYFactor() * 2) {
            z(-2) {
                x(+0).sponge()
            }

            z(-1) {
                x(-1).sponge()
                x(+1).sponge()
            }

            z(+0) {
                x(+0).sponge()
            }
        }

        y(getYFactor() * 3) {
            z(-2) {
                x(-1).redstoneBlock()
                x(+0).anyGlassPane()
                x(+1).redstoneBlock()
            }

            z(-1) {
                x(-1).anyGlassPane()
                x(+0).enrichedUraniumBlock()
                x(+1).anyGlassPane()
            }

            z(+0) {
                x(-1).redstoneBlock()
                x(+0).anyGlassPane()
                x(+1).redstoneBlock()
            }
        }

        y(getYFactor() * 4) {
            z(-3) {
                x(-1).anyStairs()
                x(+0).netheriteCasing()
                x(+1).anyStairs()
            }

            z(-2) {
                x(-2).anyStairs()
                x(-1).terracotta()
                x(+0).anyStairs()
                x(+1).terracotta()
                x(+2).anyStairs()
            }

            z(-1) {
                x(-2).netheriteCasing()
                x(-1).anyStairs()
                x(+0).enrichedUraniumBlock()
                x(+1).anyStairs()
                x(+2).netheriteCasing()
            }

            z(+0) {
                x(-2).anyStairs()
                x(-1).terracotta()
                x(+0).anyStairs()
                x(+1).terracotta()
                x(+2).anyStairs()
            }

            z(+1) {
                x(-1).anyStairs()
                x(+0).netheriteCasing()
                x(+1).anyStairs()
            }
        }

        y(getYFactor() * 5) {
            z(-2) {
                x(0).type(Material.IRON_BARS)
            }

            z(-1) {
                x(-1).type(Material.IRON_BARS)
                x(+0).netheriteBlock()
                x(+1).type(Material.IRON_BARS)
            }

            z(+0) {
                x(0).type(Material.IRON_BARS)
            }
        }
    }
}

object DisintegratorBeamWeaponMultiblockTop : DisintegratorBeamWeaponMultiblock() {
    override fun getYFactor(): Int = 1

    override fun createSubsystem(starship: ActiveStarship, pos: Vec3i, face: BlockFace): DisintegratorBeamWeaponSubsystem {
        return DisintegratorBeamWeaponSubsystem(starship, pos, BlockFace.UP, this)
    }
}

object DisintegratorBeamWeaponMultiblockBottom : DisintegratorBeamWeaponMultiblock() {
    override fun getYFactor(): Int = -1

    override fun createSubsystem(starship: ActiveStarship, pos: Vec3i, face: BlockFace): DisintegratorBeamWeaponSubsystem {
        return DisintegratorBeamWeaponSubsystem(starship, pos, BlockFace.DOWN, this)
    }
}
