package net.starlegacy.feature.multiblock.particleshield

import net.starlegacy.feature.multiblock.MultiblockShape
import net.starlegacy.feature.progression.advancement.SLAdvancement

object ShieldMultiblockClass30 : SphereShieldMultiblock() {
    override val advancement = SLAdvancement.PARTICLE_SHIELD_30
    override val maxRange = 22
    override val signText = createSignText(
        line1 = "&3Particle Shield",
        line2 = "&7Generator",
        line3 = null,
        line4 = "&8Class &b3.0"
    )

    override fun MultiblockShape.buildStructure() {
        z(-1) {
            y(-1) {
                x(-1).anyStairs()
                x(+0).ironBlock()
                x(+1).anyStairs()
            }

            y(+0) {
                x(-1).anyGlassPane()
                x(+1).anyGlassPane()
            }
        }

        z(+0) {
            y(-1) {
                x(-1).ironBlock()
                x(+0).sponge()
                x(+1).ironBlock()
            }

            y(+0) {
                x(-1).anyGlassPane()
                x(+0).anyGlass()
                x(+1).anyGlassPane()
            }
        }

        z(+1) {
            y(-1) {
                x(-1).anyStairs()
                x(+0).ironBlock()
                x(+1).anyStairs()
            }

            y(+0) {
                x(-1).anyGlassPane()
                x(+0).anyStairs()
                x(+1).anyGlassPane()
            }
        }
    }
}
