package net.starlegacy.feature.multiblock.particleshield

import net.starlegacy.feature.multiblock.Multiblock
import net.starlegacy.feature.progression.advancement.SLAdvancement

internal class ShieldMultiblockClass20Test : ShieldMultiblockTest() {
    override fun getMultiblock(): Multiblock = ShieldMultiblockClass20
    override fun getExpectedAdvancement(): SLAdvancement = SLAdvancement.PARTICLE_SHIELD_20
    override fun getClassText(): String = "&b2.0"
}
