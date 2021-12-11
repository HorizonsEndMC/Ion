package net.starlegacy.feature.multiblock.particleshield

import net.starlegacy.feature.multiblock.Multiblock
import net.starlegacy.feature.progression.advancement.SLAdvancement

internal class ShieldMultiblockClass85Test : ShieldMultiblockTest() {
    override fun getMultiblock(): Multiblock = ShieldMultiblockClass85
    override fun getExpectedAdvancement(): SLAdvancement = SLAdvancement.PARTICLE_SHIELD_85
    override fun getClassText(): String = "&b8.5"
}
