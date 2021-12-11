package net.starlegacy.feature.multiblock.particleshield

import net.starlegacy.feature.multiblock.Multiblock
import net.starlegacy.feature.progression.advancement.SLAdvancement

internal class ShieldMultiblockClass08iTest : ShieldMultiblockTest() {
    override fun getMultiblock(): Multiblock = ShieldMultiblockClass08i
    override fun getExpectedAdvancement(): SLAdvancement = SLAdvancement.PARTICLE_SHIELD_08I
    override fun getClassText(): String = "&d0.8i"
}
