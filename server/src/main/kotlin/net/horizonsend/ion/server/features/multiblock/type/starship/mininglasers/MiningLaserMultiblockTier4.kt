package net.horizonsend.ion.server.features.multiblock.type.starship.mininglasers

import net.horizonsend.ion.common.utils.text.ofChildren
import net.horizonsend.ion.server.features.multiblock.shape.MultiblockShape
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.Vec3i
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.block.BlockFace

sealed class MiningLaserMultiblockTier4 : MiningLaserMultiblock() {
    val tierText = text("Tier 4").color(NamedTextColor.AQUA)
    override val signText: Array<Component?> = arrayOf(
        text("Mining ").color(NamedTextColor.DARK_GRAY)
            .append(text("Laser").color(NamedTextColor.GREEN)),
        tierText,
        text(""),
        text("")
    )
    override val maxPower: Int = 650000
    override val beamOrigin = Triple(0, 3, 1)
    final override val range: Double = 245.0
    final override val mineRadius = 11
    override val beamCount: Int = 10
    override val maxBroken: Int = 11
    override val sound: String = "horizonsend:starship.weapon.mining_laser.t4_loop"

    override val tier: Int = 4

    override val description: Component = text("Emits a beam $range blocks long that breaks blocks in a $mineRadius block radius.")
}

object MiningLaserMultiblockTier4Top : MiningLaserMultiblockTier4() {
    override val displayName: Component get() = ofChildren(tierText, text(" Mining Laser (Top)"))
    override val side = BlockFace.UP

    override fun getFirePointOffset(): Vec3i = Vec3i(+0, +6, +2)
	override val outputOffset: Vec3i = Vec3i(-1, -1, 0)

    override fun MultiblockShape.buildStructure() {
        z(+0) {
            y(-1) {
                x(-1).anyPipedInventory()
                x(+0).powerInput()
                x(+1).ironBlock()
            }

            y(+0) {
                x(-1).anyStairs()
                x(+0).anyGlass()
                x(+1).anyStairs()
            }

            y(+1) {
                x(-1).anyStairs()
                x(+0).anyStairs()
                x(+1).anyStairs()
            }

            y(+2) {
                x(-1).steelBlock()
                x(+0).netheriteCasing()
                x(+1).steelBlock()
            }

            y(+3) {
                x(-1).steelBlock()
                x(+0).netheriteCasing()
                x(+1).steelBlock()
            }

            y(+4) {
                x(-1).anyStairs()
                x(+0).anyTerracotta()
                x(+1).anyStairs()
            }
        }

        z(+1) {
            y(-1) {
                x(-2).anyStairs()
                x(-1).ironBlock()
                x(+0).anyGlass()
                x(+1).ironBlock()
                x(+2).anyStairs()
            }

            y(+0) {
                x(-2).ironBlock()
                x(-1).enrichedUraniumBlock()
                x(+0).anyGlass()
                x(+1).enrichedUraniumBlock()
                x(+2).ironBlock()
            }

            y(+1) {
                x(-2).anyStairs()
                x(-1).enrichedUraniumBlock()
                x(+0).anyGlass()
                x(+1).enrichedUraniumBlock()
                x(+2).anyStairs()
            }

            y(+2) {
                x(-2).steelBlock()
                x(-1).sponge()
                x(+0).sponge()
                x(+1).sponge()
                x(+2).steelBlock()
            }

            y(+3) {
                x(-2).steelBlock()
                x(-1).sponge()
                x(+0).sponge()
                x(+1).sponge()
                x(+2).steelBlock()
            }

            y(+4) {
                x(-2).anyStairs()
                x(-1).anyTerracotta()
                x(+0).anyStairs()
                x(+1).anyTerracotta()
                x(+2).anyStairs()
            }

            y(+5) {
                x(+0).anyGlassPane()
            }
        }

        z(+2) {
            y(-1) {
                x(-1).anyGlass()
                x(+0).enrichedUraniumBlock()
                x(+1).anyGlass()
            }

            y(+0) {
                x(-1).anyGlass()
                x(+0).enrichedUraniumBlock()
                x(+1).anyGlass()
            }

            y(+1) {
                x(-2).anyStairs()
                x(-1).anyGlass()
                x(+0).enrichedUraniumBlock()
                x(+1).anyGlass()
                x(+2).anyStairs()
            }

            y(+2) {
                x(-2).netheriteCasing()
                x(-1).sponge()
                x(+0).enrichedUraniumBlock()
                x(+1).sponge()
                x(+2).netheriteCasing()
            }

            y(+3) {
                x(-2).netheriteCasing()
                x(-1).sponge()
                x(+0).enrichedUraniumBlock()
                x(+1).sponge()
                x(+2).netheriteCasing()
            }

            y(+4) {
                x(-2).anyTerracotta()
                x(-1).anyStairs()
                x(+0).enrichedUraniumBlock()
                x(+1).anyStairs()
                x(+2).anyTerracotta()
            }

            y(+5) {
                x(-1).anyGlassPane()
                x(+0).lodestone()
                x(+1).anyGlassPane()
            }
        }

        z(+3) {
            y(-1) {
                x(-2).anyStairs()
                x(-1).ironBlock()
                x(+0).anyGlass()
                x(+1).ironBlock()
                x(+2).anyStairs()
            }

            y(+0) {
                x(-2).ironBlock()
                x(-1).enrichedUraniumBlock()
                x(+0).anyGlass()
                x(+1).enrichedUraniumBlock()
                x(+2).ironBlock()
            }

            y(+1) {
                x(-2).anyStairs()
                x(-1).enrichedUraniumBlock()
                x(+0).anyGlass()
                x(+1).enrichedUraniumBlock()
                x(+2).anyStairs()
            }

            y(+2) {
                x(-2).steelBlock()
                x(-1).sponge()
                x(+0).sponge()
                x(+1).sponge()
                x(+2).steelBlock()
            }

            y(+3) {
                x(-2).steelBlock()
                x(-1).sponge()
                x(+0).sponge()
                x(+1).sponge()
                x(+2).steelBlock()
            }

            y(+4) {
                x(-2).anyStairs()
                x(-1).anyTerracotta()
                x(+0).anyStairs()
                x(+1).anyTerracotta()
                x(+2).anyStairs()
            }

            y(+5) {
                x(+0).anyGlassPane()
            }
        }

        z(+4) {
            y(-1) {
                x(-1).anyStairs()
                x(+1).anyStairs()
            }

            y(+0) {
                x(-1).ironBlock()
                x(+1).ironBlock()
            }

            y(+1) {
                x(-1).anyStairs()
                x(+0).anyStairs()
                x(+1).anyStairs()
            }

            y(+2) {
                x(-1).steelBlock()
                x(+0).netheriteCasing()
                x(+1).steelBlock()
            }

            y(+3) {
                x(-1).steelBlock()
                x(+0).netheriteCasing()
                x(+1).steelBlock()
            }

            y(+4) {
                x(-1).anyStairs()
                x(+0).anyTerracotta()
                x(+1).anyStairs()
            }
        }
    }
}

object MiningLaserMultiblockTier4TopMirrored : MiningLaserMultiblockTier4() {
    override val displayName: Component get() = ofChildren(tierText, text(" Mining Laser (Top) (Mirrored)"))
    override val side = BlockFace.UP

    override fun getFirePointOffset(): Vec3i = Vec3i(+0, +6, -3)
	override val outputOffset: Vec3i = Vec3i(+1, -1, 0)

    override fun MultiblockShape.buildStructure() {
        z(+0) {
            y(-1) {
                x(-1).ironBlock()
                x(+0).powerInput()
                x(+1).anyPipedInventory()
            }

            y(+0) {
                x(-1).anyStairs()
                x(+0).anyGlass()
                x(+1).anyStairs()
            }

            y(+1) {
                x(-1).anyStairs()
                x(+0).anyStairs()
                x(+1).anyStairs()
            }

            y(+2) {
                x(-1).steelBlock()
                x(+0).netheriteCasing()
                x(+1).steelBlock()
            }

            y(+3) {
                x(-1).steelBlock()
                x(+0).netheriteCasing()
                x(+1).steelBlock()
            }

            y(+4) {
                x(-1).anyStairs()
                x(+0).anyTerracotta()
                x(+1).anyStairs()
            }
        }

        z(+1) {
            y(-1) {
                x(-2).anyStairs()
                x(-1).ironBlock()
                x(+0).anyGlass()
                x(+1).ironBlock()
                x(+2).anyStairs()
            }

            y(+0) {
                x(-2).ironBlock()
                x(-1).enrichedUraniumBlock()
                x(+0).anyGlass()
                x(+1).enrichedUraniumBlock()
                x(+2).ironBlock()
            }

            y(+1) {
                x(-2).anyStairs()
                x(-1).enrichedUraniumBlock()
                x(+0).anyGlass()
                x(+1).enrichedUraniumBlock()
                x(+2).anyStairs()
            }

            y(+2) {
                x(-2).steelBlock()
                x(-1).sponge()
                x(+0).sponge()
                x(+1).sponge()
                x(+2).steelBlock()
            }

            y(+3) {
                x(-2).steelBlock()
                x(-1).sponge()
                x(+0).sponge()
                x(+1).sponge()
                x(+2).steelBlock()
            }

            y(+4) {
                x(-2).anyStairs()
                x(-1).anyTerracotta()
                x(+0).anyStairs()
                x(+1).anyTerracotta()
                x(+2).anyStairs()
            }

            y(+5) {
                x(+0).anyGlassPane()
            }
        }

        z(+2) {
            y(-1) {
                x(-1).anyGlass()
                x(+0).enrichedUraniumBlock()
                x(+1).anyGlass()
            }

            y(+0) {
                x(-1).anyGlass()
                x(+0).enrichedUraniumBlock()
                x(+1).anyGlass()
            }

            y(+1) {
                x(-2).anyStairs()
                x(-1).anyGlass()
                x(+0).enrichedUraniumBlock()
                x(+1).anyGlass()
                x(+2).anyStairs()
            }

            y(+2) {
                x(-2).netheriteCasing()
                x(-1).sponge()
                x(+0).enrichedUraniumBlock()
                x(+1).sponge()
                x(+2).netheriteCasing()
            }

            y(+3) {
                x(-2).netheriteCasing()
                x(-1).sponge()
                x(+0).enrichedUraniumBlock()
                x(+1).sponge()
                x(+2).netheriteCasing()
            }

            y(+4) {
                x(-2).anyTerracotta()
                x(-1).anyStairs()
                x(+0).enrichedUraniumBlock()
                x(+1).anyStairs()
                x(+2).anyTerracotta()
            }

            y(+5) {
                x(-1).anyGlassPane()
                x(+0).lodestone()
                x(+1).anyGlassPane()
            }
        }

        z(+3) {
            y(-1) {
                x(-2).anyStairs()
                x(-1).ironBlock()
                x(+0).anyGlass()
                x(+1).ironBlock()
                x(+2).anyStairs()
            }

            y(+0) {
                x(-2).ironBlock()
                x(-1).enrichedUraniumBlock()
                x(+0).anyGlass()
                x(+1).enrichedUraniumBlock()
                x(+2).ironBlock()
            }

            y(+1) {
                x(-2).anyStairs()
                x(-1).enrichedUraniumBlock()
                x(+0).anyGlass()
                x(+1).enrichedUraniumBlock()
                x(+2).anyStairs()
            }

            y(+2) {
                x(-2).steelBlock()
                x(-1).sponge()
                x(+0).sponge()
                x(+1).sponge()
                x(+2).steelBlock()
            }

            y(+3) {
                x(-2).steelBlock()
                x(-1).sponge()
                x(+0).sponge()
                x(+1).sponge()
                x(+2).steelBlock()
            }

            y(+4) {
                x(-2).anyStairs()
                x(-1).anyTerracotta()
                x(+0).anyStairs()
                x(+1).anyTerracotta()
                x(+2).anyStairs()
            }

            y(+5) {
                x(+0).anyGlassPane()
            }
        }

        z(+4) {
            y(-1) {
                x(-1).anyStairs()
                x(+1).anyStairs()
            }

            y(+0) {
                x(-1).ironBlock()
                x(+1).ironBlock()
            }

            y(+1) {
                x(-1).anyStairs()
                x(+0).anyStairs()
                x(+1).anyStairs()
            }

            y(+2) {
                x(-1).steelBlock()
                x(+0).netheriteCasing()
                x(+1).steelBlock()
            }

            y(+3) {
                x(-1).steelBlock()
                x(+0).netheriteCasing()
                x(+1).steelBlock()
            }

            y(+4) {
                x(-1).anyStairs()
                x(+0).anyTerracotta()
                x(+1).anyStairs()
            }
        }
    }
}

object MiningLaserMultiblockTier4Bottom : MiningLaserMultiblockTier4() {
    override val displayName: Component get() = ofChildren(tierText, text(" Mining Laser (Bottom)"))
    override val side = BlockFace.DOWN

    override fun getFirePointOffset(): Vec3i = Vec3i(+0, -6, +2)
	override val outputOffset: Vec3i = Vec3i(-1, +1, 0)

    override fun MultiblockShape.buildStructure() {
        z(+0) {
            y(+1) {
                x(-1).anyPipedInventory()
                x(+0).powerInput()
                x(+1).ironBlock()
            }

            y(+0) {
                x(-1).anyStairs()
                x(+0).anyGlass()
                x(+1).anyStairs()
            }

            y(-1) {
                x(-1).anyStairs()
                x(+0).anyStairs()
                x(+1).anyStairs()
            }

            y(-2) {
                x(-1).steelBlock()
                x(+0).netheriteCasing()
                x(+1).steelBlock()
            }

            y(-3) {
                x(-1).steelBlock()
                x(+0).netheriteCasing()
                x(+1).steelBlock()
            }

            y(-4) {
                x(-1).anyStairs()
                x(+0).anyTerracotta()
                x(+1).anyStairs()
            }
        }

        z(+1) {
            y(+1) {
                x(-2).anyStairs()
                x(-1).ironBlock()
                x(+0).anyGlass()
                x(+1).ironBlock()
                x(+2).anyStairs()
            }

            y(+0) {
                x(-2).ironBlock()
                x(-1).enrichedUraniumBlock()
                x(+0).anyGlass()
                x(+1).enrichedUraniumBlock()
                x(+2).ironBlock()
            }

            y(-1) {
                x(-2).anyStairs()
                x(-1).enrichedUraniumBlock()
                x(+0).anyGlass()
                x(+1).enrichedUraniumBlock()
                x(+2).anyStairs()
            }

            y(-2) {
                x(-2).steelBlock()
                x(-1).sponge()
                x(+0).sponge()
                x(+1).sponge()
                x(+2).steelBlock()
            }

            y(-3) {
                x(-2).steelBlock()
                x(-1).sponge()
                x(+0).sponge()
                x(+1).sponge()
                x(+2).steelBlock()
            }

            y(-4) {
                x(-2).anyStairs()
                x(-1).anyTerracotta()
                x(+0).anyStairs()
                x(+1).anyTerracotta()
                x(+2).anyStairs()
            }

            y(-5) {
                x(+0).anyGlassPane()
            }
        }

        z(+2) {
            y(+1) {
                x(-1).anyGlass()
                x(+0).enrichedUraniumBlock()
                x(+1).anyGlass()
            }

            y(+0) {
                x(-1).anyGlass()
                x(+0).enrichedUraniumBlock()
                x(+1).anyGlass()
            }

            y(-1) {
                x(-2).anyStairs()
                x(-1).anyGlass()
                x(+0).enrichedUraniumBlock()
                x(+1).anyGlass()
                x(+2).anyStairs()
            }

            y(-2) {
                x(-2).netheriteCasing()
                x(-1).sponge()
                x(+0).enrichedUraniumBlock()
                x(+1).sponge()
                x(+2).netheriteCasing()
            }

            y(-3) {
                x(-2).netheriteCasing()
                x(-1).sponge()
                x(+0).enrichedUraniumBlock()
                x(+1).sponge()
                x(+2).netheriteCasing()
            }

            y(-4) {
                x(-2).anyTerracotta()
                x(-1).anyStairs()
                x(+0).enrichedUraniumBlock()
                x(+1).anyStairs()
                x(+2).anyTerracotta()
            }

            y(-5) {
                x(-1).anyGlassPane()
                x(+0).lodestone()
                x(+1).anyGlassPane()
            }
        }

        z(+3) {
            y(+1) {
                x(-2).anyStairs()
                x(-1).ironBlock()
                x(+0).anyGlass()
                x(+1).ironBlock()
                x(+2).anyStairs()
            }

            y(+0) {
                x(-2).ironBlock()
                x(-1).enrichedUraniumBlock()
                x(+0).anyGlass()
                x(+1).enrichedUraniumBlock()
                x(+2).ironBlock()
            }

            y(-1) {
                x(-2).anyStairs()
                x(-1).enrichedUraniumBlock()
                x(+0).anyGlass()
                x(+1).enrichedUraniumBlock()
                x(+2).anyStairs()
            }

            y(-2) {
                x(-2).steelBlock()
                x(-1).sponge()
                x(+0).sponge()
                x(+1).sponge()
                x(+2).steelBlock()
            }

            y(-3) {
                x(-2).steelBlock()
                x(-1).sponge()
                x(+0).sponge()
                x(+1).sponge()
                x(+2).steelBlock()
            }

            y(-4) {
                x(-2).anyStairs()
                x(-1).anyTerracotta()
                x(+0).anyStairs()
                x(+1).anyTerracotta()
                x(+2).anyStairs()
            }

            y(-5) {
                x(+0).anyGlassPane()
            }
        }

        z(+4) {
            y(+1) {
                x(-1).anyStairs()
                x(+1).anyStairs()
            }

            y(+0) {
                x(-1).ironBlock()
                x(+1).ironBlock()
            }

            y(-1) {
                x(-1).anyStairs()
                x(+0).anyStairs()
                x(+1).anyStairs()
            }

            y(-2) {
                x(-1).steelBlock()
                x(+0).netheriteCasing()
                x(+1).steelBlock()
            }

            y(-3) {
                x(-1).steelBlock()
                x(+0).netheriteCasing()
                x(+1).steelBlock()
            }

            y(-4) {
                x(-1).anyStairs()
                x(+0).anyTerracotta()
                x(+1).anyStairs()
            }
        }
    }
}

object MiningLaserMultiblockTier4BottomMirrored : MiningLaserMultiblockTier4() {
    override val displayName: Component get() = ofChildren(tierText, text(" Mining Laser (Bottom) (Mirrored)"))
    override val side = BlockFace.DOWN

    override fun getFirePointOffset(): Vec3i = Vec3i(+0, -6, -3)
	override val outputOffset: Vec3i = Vec3i(+1, +1, 0)

    override fun MultiblockShape.buildStructure() {
        z(+0) {
            y(+1) {
                x(-1).ironBlock()
                x(+0).powerInput()
                x(+1).anyPipedInventory()
            }

            y(+0) {
                x(-1).anyStairs()
                x(+0).anyGlass()
                x(+1).anyStairs()
            }

            y(-1) {
                x(-1).anyStairs()
                x(+0).anyStairs()
                x(+1).anyStairs()
            }

            y(-2) {
                x(-1).steelBlock()
                x(+0).netheriteCasing()
                x(+1).steelBlock()
            }

            y(-3) {
                x(-1).steelBlock()
                x(+0).netheriteCasing()
                x(+1).steelBlock()
            }

            y(-4) {
                x(-1).anyStairs()
                x(+0).anyTerracotta()
                x(+1).anyStairs()
            }
        }

        z(+1) {
            y(+1) {
                x(-2).anyStairs()
                x(-1).ironBlock()
                x(+0).anyGlass()
                x(+1).ironBlock()
                x(+2).anyStairs()
            }

            y(+0) {
                x(-2).ironBlock()
                x(-1).enrichedUraniumBlock()
                x(+0).anyGlass()
                x(+1).enrichedUraniumBlock()
                x(+2).ironBlock()
            }

            y(-1) {
                x(-2).anyStairs()
                x(-1).enrichedUraniumBlock()
                x(+0).anyGlass()
                x(+1).enrichedUraniumBlock()
                x(+2).anyStairs()
            }

            y(-2) {
                x(-2).steelBlock()
                x(-1).sponge()
                x(+0).sponge()
                x(+1).sponge()
                x(+2).steelBlock()
            }

            y(-3) {
                x(-2).steelBlock()
                x(-1).sponge()
                x(+0).sponge()
                x(+1).sponge()
                x(+2).steelBlock()
            }

            y(-4) {
                x(-2).anyStairs()
                x(-1).anyTerracotta()
                x(+0).anyStairs()
                x(+1).anyTerracotta()
                x(+2).anyStairs()
            }

            y(-5) {
                x(+0).anyGlassPane()
            }
        }

        z(+2) {
            y(+1) {
                x(-1).anyGlass()
                x(+0).enrichedUraniumBlock()
                x(+1).anyGlass()
            }

            y(+0) {
                x(-1).anyGlass()
                x(+0).enrichedUraniumBlock()
                x(+1).anyGlass()
            }

            y(-1) {
                x(-2).anyStairs()
                x(-1).anyGlass()
                x(+0).enrichedUraniumBlock()
                x(+1).anyGlass()
                x(+2).anyStairs()
            }

            y(-2) {
                x(-2).netheriteCasing()
                x(-1).sponge()
                x(+0).enrichedUraniumBlock()
                x(+1).sponge()
                x(+2).netheriteCasing()
            }

            y(-3) {
                x(-2).netheriteCasing()
                x(-1).sponge()
                x(+0).enrichedUraniumBlock()
                x(+1).sponge()
                x(+2).netheriteCasing()
            }

            y(-4) {
                x(-2).anyTerracotta()
                x(-1).anyStairs()
                x(+0).enrichedUraniumBlock()
                x(+1).anyStairs()
                x(+2).anyTerracotta()
            }

            y(-5) {
                x(-1).anyGlassPane()
                x(+0).lodestone()
                x(+1).anyGlassPane()
            }
        }

        z(+3) {
            y(+1) {
                x(-2).anyStairs()
                x(-1).ironBlock()
                x(+0).anyGlass()
                x(+1).ironBlock()
                x(+2).anyStairs()
            }

            y(+0) {
                x(-2).ironBlock()
                x(-1).enrichedUraniumBlock()
                x(+0).anyGlass()
                x(+1).enrichedUraniumBlock()
                x(+2).ironBlock()
            }

            y(-1) {
                x(-2).anyStairs()
                x(-1).enrichedUraniumBlock()
                x(+0).anyGlass()
                x(+1).enrichedUraniumBlock()
                x(+2).anyStairs()
            }

            y(-2) {
                x(-2).steelBlock()
                x(-1).sponge()
                x(+0).sponge()
                x(+1).sponge()
                x(+2).steelBlock()
            }

            y(-3) {
                x(-2).steelBlock()
                x(-1).sponge()
                x(+0).sponge()
                x(+1).sponge()
                x(+2).steelBlock()
            }

            y(-4) {
                x(-2).anyStairs()
                x(-1).anyTerracotta()
                x(+0).anyStairs()
                x(+1).anyTerracotta()
                x(+2).anyStairs()
            }

            y(-5) {
                x(+0).anyGlassPane()
            }
        }

        z(+4) {
            y(+1) {
                x(-1).anyStairs()
                x(+1).anyStairs()
            }

            y(+0) {
                x(-1).ironBlock()
                x(+1).ironBlock()
            }

            y(-1) {
                x(-1).anyStairs()
                x(+0).anyStairs()
                x(+1).anyStairs()
            }

            y(-2) {
                x(-1).steelBlock()
                x(+0).netheriteCasing()
                x(+1).steelBlock()
            }

            y(-3) {
                x(-1).steelBlock()
                x(+0).netheriteCasing()
                x(+1).steelBlock()
            }

            y(-4) {
                x(-1).anyStairs()
                x(+0).anyTerracotta()
                x(+1).anyStairs()
            }
        }
    }
}

object MiningLaserMultiblockTier4Side : MiningLaserMultiblockTier4() {
    override val displayName: Component get() = ofChildren(tierText, text(" Mining Laser (Side)"))
	override val side = BlockFace.UP

	override fun getFirePointOffset(): Vec3i = Vec3i(-1, +1, +8)
	override val outputOffset: Vec3i = Vec3i(-1, -1, 0)

	override fun MultiblockShape.buildStructure() {
		z(+0) {
			y(-1) {
				x(-2).anyStairs()
				x(-1).anyPipedInventory()
				x(+0).powerInput()
			}

			y(+0) {
				x(-3).anyStairs()
				x(-2).ironBlock()
				x(-1).anyGlass()
				x(+0).ironBlock()
				x(+1).anyStairs()
			}

			y(+1) {
				x(-2).anyGlass()
				x(-1).enrichedUraniumBlock()
				x(+0).anyGlass()
			}
			y(+2) {
				x(-3).anyStairs()
				x(-2).ironBlock()
				x(-1).anyGlass()
				x(+0).ironBlock()
				x(+1).anyStairs()
			}
			y(+3) {
				x(-2).anyStairs()
				x(+0).anyStairs()
			}

		}

		z(+1) {
			y(-1) {
				x(-2).ironBlock()
				x(+0).ironBlock()
			}

			y(+0) {
				x(-3).ironBlock()
				x(-2).enrichedUraniumBlock()
				x(-1).anyGlass()
				x(+0).enrichedUraniumBlock()
				x(+1).ironBlock()
			}

			y(+1) {
				x(-2).anyGlass()
				x(-1).enrichedUraniumBlock()
				x(+0).anyGlass()
			}
			y(+2) {
				x(-3).ironBlock()
				x(-2).enrichedUraniumBlock()
				x(-1).anyGlass()
				x(+0).enrichedUraniumBlock()
				x(+1).ironBlock()
			}
			y(+3) {
                x(-2).ironBlock()
                x(+0).ironBlock()
			}

		}

		z(+2) {
			y(-1) {
				x(-2).anyStairs()
				x(-1).anyStairs()
				x(+0).anyStairs()
			}

			y(+0) {
				x(-3).anyStairs()
				x(-2).enrichedUraniumBlock()
				x(-1).anyGlass()
				x(+0).enrichedUraniumBlock()
				x(+1).anyStairs()
			}

			y(+1) {
				x(-3).anyWall()
				x(-2).anyGlass()
				x(-1).enrichedUraniumBlock()
				x(+0).anyGlass()
				x(+1).anyWall()
			}
			y(+2) {
				x(-3).anyStairs()
				x(-2).enrichedUraniumBlock()
				x(-1).anyGlass()
				x(+0).enrichedUraniumBlock()
				x(+1).anyStairs()
			}
			y(+3) {
				x(-2).anyStairs()
				x(-1).anyStairs()
				x(+0).anyStairs()
			}

		}

		z(+3) {
			y(-1) {
				x(-2).steelBlock()
				x(-1).netheriteCasing()
				x(+0).steelBlock()
			}

			y(+0) {
				x(-3).steelBlock()
				x(-2).sponge()
				x(-1).sponge()
				x(+0).sponge()
				x(+1).steelBlock()
			}

			y(+1) {
				x(-3).netheriteCasing()
				x(-2).sponge()
				x(-1).enrichedUraniumBlock()
				x(+0).sponge()
				x(+1).netheriteCasing()
			}
			y(+2) {
				x(-3).steelBlock()
				x(-2).sponge()
				x(-1).sponge()
				x(+0).sponge()
				x(+1).steelBlock()
			}
			y(+3) {
				x(-2).steelBlock()
				x(-1).netheriteCasing()
				x(+0).steelBlock()
			}

		}

		z(+4) {
			y(-1) {
				x(-2).steelBlock()
				x(-1).netheriteCasing()
				x(+0).steelBlock()
			}

			y(+0) {
				x(-3).steelBlock()
				x(-2).sponge()
				x(-1).sponge()
				x(+0).sponge()
				x(+1).steelBlock()
			}

			y(+1) {
				x(-3).netheriteCasing()
				x(-2).sponge()
				x(-1).enrichedUraniumBlock()
				x(+0).sponge()
				x(+1).netheriteCasing()
			}
			y(+2) {
				x(-3).steelBlock()
				x(-2).sponge()
				x(-1).sponge()
				x(+0).sponge()
				x(+1).steelBlock()
			}
			y(+3) {
				x(-2).steelBlock()
				x(-1).netheriteCasing()
				x(+0).steelBlock()
			}

		}

		z(+5) {
			y(-1) {
				x(-2).anyStairs()
				x(-1).anyTerracotta()
				x(+0).anyStairs()
			}

			y(+0) {
				x(-3).anyStairs()
				x(-2).anyTerracotta()
				x(-1).anyWall()
				x(+0).anyTerracotta()
				x(+1).anyStairs()
			}

			y(+1) {
				x(-3).anyTerracotta()
				x(-2).anyWall()
				x(-1).enrichedUraniumBlock()
				x(+0).anyWall()
				x(+1).anyTerracotta()
			}
			y(+2) {
				x(-3).anyStairs()
				x(-2).anyTerracotta()
				x(-1).anyWall()
				x(+0).anyTerracotta()
				x(+1).anyStairs()
			}
			y(+3) {
				x(-2).anyStairs()
				x(-1).anyTerracotta()
				x(+0).anyStairs()
			}

		}

		z(+6) {

			y(+0) {
				x(-1).anyGlassPane()
			}
			y(+1) {
				x(-2).anyGlassPane()
				x(-1).lodestone()
				x(+0).anyGlassPane()
			}
			y(+2) {
				x(-1).anyGlassPane()
			}

		}
	}
}

object MiningLaserMultiblockTier4SideMirrored : MiningLaserMultiblockTier4() {
    override val displayName: Component get() = ofChildren(tierText, text(" Mining Laser (Side) (Mirrored)"))
    override val side = BlockFace.UP

    override fun getFirePointOffset(): Vec3i = Vec3i(+1, -1, -8)
	override val outputOffset: Vec3i = Vec3i(+1, -1, 0)

    override fun MultiblockShape.buildStructure() {
        z(+0) {
            y(-1) {
                x(+0).powerInput()
                x(+1).anyPipedInventory()
                x(+2).anyStairs()
            }

            y(+0) {
                x(+3).anyStairs()
                x(+2).ironBlock()
                x(+1).anyGlass()
                x(+0).ironBlock()
                x(-1).anyStairs()
            }

            y(+1) {
                x(+2).anyGlass()
                x(+1).enrichedUraniumBlock()
                x(+0).anyGlass()
            }
            y(+2) {
                x(+3).anyStairs()
                x(+2).ironBlock()
                x(+1).anyGlass()
                x(+0).ironBlock()
                x(-1).anyStairs()
            }
            y(+3) {
                x(+2).anyStairs()
                x(+0).anyStairs()
            }

        }

        z(+1) {
            y(-1) {
                x(+2).ironBlock()
                x(+0).ironBlock()
            }

            y(+0) {
                x(+3).ironBlock()
                x(+2).enrichedUraniumBlock()
                x(+1).anyGlass()
                x(+0).enrichedUraniumBlock()
                x(-1).ironBlock()
            }

            y(+1) {
                x(+2).anyGlass()
                x(+1).enrichedUraniumBlock()
                x(+0).anyGlass()
            }
            y(+2) {
                x(+3).ironBlock()
                x(+2).enrichedUraniumBlock()
                x(+1).anyGlass()
                x(+0).enrichedUraniumBlock()
                x(-1).ironBlock()
            }
            y(+3) {
                x(+2).ironBlock()
                x(+0).ironBlock()
            }

        }

        z(+2) {
            y(-1) {
                x(+2).anyStairs()
                x(+1).anyStairs()
                x(+0).anyStairs()
            }

            y(+0) {
                x(+3).anyStairs()
                x(+2).enrichedUraniumBlock()
                x(+1).anyGlass()
                x(+0).enrichedUraniumBlock()
                x(-1).anyStairs()
            }

            y(+1) {
                x(+3).anyWall()
                x(+2).anyGlass()
                x(+1).enrichedUraniumBlock()
                x(+0).anyGlass()
                x(-1).anyWall()
            }
            y(+2) {
                x(+3).anyStairs()
                x(+2).enrichedUraniumBlock()
                x(+1).anyGlass()
                x(+0).enrichedUraniumBlock()
                x(-1).anyStairs()
            }
            y(+3) {
                x(+2).anyStairs()
                x(+1).anyStairs()
                x(+0).anyStairs()
            }

        }

        z(+3) {
            y(-1) {
                x(+2).steelBlock()
                x(+1).netheriteCasing()
                x(+0).steelBlock()
            }

            y(+0) {
                x(+3).steelBlock()
                x(+2).sponge()
                x(+1).sponge()
                x(+0).sponge()
                x(-1).steelBlock()
            }

            y(+1) {
                x(+3).netheriteCasing()
                x(+2).sponge()
                x(+1).enrichedUraniumBlock()
                x(+0).sponge()
                x(-1).netheriteCasing()
            }
            y(+2) {
                x(+3).steelBlock()
                x(+2).sponge()
                x(+1).sponge()
                x(+0).sponge()
                x(-1).steelBlock()
            }
            y(+3) {
                x(+2).steelBlock()
                x(+1).netheriteCasing()
                x(+0).steelBlock()
            }

        }

        z(+4) {
            y(-1) {
                x(+2).steelBlock()
                x(+1).netheriteCasing()
                x(+0).steelBlock()
            }

            y(+0) {
                x(+3).steelBlock()
                x(+2).sponge()
                x(+1).sponge()
                x(+0).sponge()
                x(-1).steelBlock()
            }

            y(+1) {
                x(+3).netheriteCasing()
                x(+2).sponge()
                x(+1).enrichedUraniumBlock()
                x(+0).sponge()
                x(-1).netheriteCasing()
            }
            y(+2) {
                x(+3).steelBlock()
                x(+2).sponge()
                x(+1).sponge()
                x(+0).sponge()
                x(-1).steelBlock()
            }
            y(+3) {
                x(+2).steelBlock()
                x(+1).netheriteCasing()
                x(+0).steelBlock()
            }

        }

        z(+5) {
            y(-1) {
                x(+2).anyStairs()
                x(+1).anyTerracotta()
                x(+0).anyStairs()
            }

            y(+0) {
                x(+3).anyStairs()
                x(+2).anyTerracotta()
                x(+1).anyWall()
                x(+0).anyTerracotta()
                x(-1).anyStairs()
            }

            y(+1) {
                x(+3).anyTerracotta()
                x(+2).anyWall()
                x(+1).enrichedUraniumBlock()
                x(+0).anyWall()
                x(-1).anyTerracotta()
            }
            y(+2) {
                x(+3).anyStairs()
                x(+2).anyTerracotta()
                x(+1).anyWall()
                x(+0).anyTerracotta()
                x(-1).anyStairs()
            }
            y(+3) {
                x(+2).anyStairs()
                x(+1).anyTerracotta()
                x(+0).anyStairs()
            }

        }

        z(+6) {

            y(+0) {
                x(+1).anyGlassPane()
            }
            y(+1) {
                x(+2).anyGlassPane()
                x(+1).lodestone()
                x(+0).anyGlassPane()
            }
            y(+2) {
                x(+1).anyGlassPane()
            }

        }
    }
}
