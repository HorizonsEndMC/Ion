package net.starlegacy.feature.multiblock.particleshield

import net.starlegacy.feature.multiblock.Multiblock
import net.starlegacy.feature.progression.advancement.SLAdvancement

internal class ShieldMultiblockClass30Test : ShieldMultiblockTest() {
    override fun getMultiblock(): Multiblock = ShieldMultiblockClass30
    override fun getExpectedAdvancement(): SLAdvancement = SLAdvancement.PARTICLE_SHIELD_30
    override fun getClassText(): String = "&b3.0"
}
