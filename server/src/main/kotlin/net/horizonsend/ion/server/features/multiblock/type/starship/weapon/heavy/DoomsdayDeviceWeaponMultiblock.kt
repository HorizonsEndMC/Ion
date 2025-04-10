package net.horizonsend.ion.server.features.multiblock.type.starship.weapon.heavy

import net.horizonsend.ion.server.features.multiblock.shape.MultiblockShape
import net.horizonsend.ion.server.features.multiblock.type.starship.weapon.SignlessStarshipWeaponMultiblock
import net.horizonsend.ion.server.features.starship.active.ActiveStarship
import net.horizonsend.ion.server.features.starship.subsystem.weapon.primary.DoomsdayDeviceWeaponSubsystem
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.Vec3i
import org.bukkit.Material
import org.bukkit.block.BlockFace

object DoomsdayDeviceWeaponMultiblock : SignlessStarshipWeaponMultiblock<DoomsdayDeviceWeaponSubsystem>() {
	override val key: String = "doomsday_device"
    override fun createSubsystem(starship: ActiveStarship, pos: Vec3i, face: BlockFace): DoomsdayDeviceWeaponSubsystem {
        return DoomsdayDeviceWeaponSubsystem(starship, pos, face)
    }

    override fun MultiblockShape.buildStructure() {
        repeat(+5) { z ->
            z(z) {
                y(-1) {
                    x(-1).aluminumBlock()
                    x(+0).anyGlass()
                    x(+1).aluminumBlock()
                }

                y(+0) {
                    x(-1).anyGlass()
                    x(+0).enrichedUraniumBlock()
                    x(+1).anyGlass()
                }

                y(+1) {
                    x(-1).aluminumBlock()
                    x(+0).anyGlass()
                    x(+1).aluminumBlock()
                }
            }
        }

        z(+5) {
            y(-2) {
                x(-1).anyStairs()
                x(+0).anySlab()
                x(+1).anyStairs()
            }

            y(-1) {
                x(-2).anyStairs()
                x(-1).ironBlock()
                x(+0).titaniumBlock()
                x(+1).ironBlock()
                x(+2).anyStairs()
            }

            y(+0) {
                x(-2).netheriteCasing()
                x(-1).ironBlock()
                x(+0).redstoneBlock()
                x(+1).ironBlock()
                x(+2).netheriteCasing()
            }

            y(+1) {
                x(-2).anyStairs()
                x(-1).ironBlock()
                x(+0).titaniumBlock()
                x(+1).ironBlock()
                x(+2).anyStairs()
            }

            y(+2) {
                x(-1).anyStairs()
                x(+0).anySlab()
                x(+1).anyStairs()
            }
        }

        z(+6) {
            y(-1) {
                x(+0).type(Material.IRON_BARS)
            }

            y(+0) {
                x(-1).type(Material.IRON_BARS)
                x(+0).lodestone()
                x(+1).type(Material.IRON_BARS)
            }

            y(+1) {
                x(+0).type(Material.IRON_BARS)
            }
        }
    }
}
