package net.starlegacy.feature.multiblock.particleshield

import net.starlegacy.feature.multiblock.Multiblock
import net.starlegacy.feature.progression.advancement.SLAdvancement

internal class ShieldMultiblockClass65Test : ShieldMultiblockTest() {
    override fun getMultiblock(): Multiblock = ShieldMultiblockClass65
    override fun getExpectedAdvancement(): SLAdvancement = SLAdvancement.PARTICLE_SHIELD_65
    override fun getClassText(): String = "&b6.5"
}
