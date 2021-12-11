package net.starlegacy.feature.multiblock.particleshield

import net.starlegacy.feature.multiblock.Multiblock
import net.starlegacy.feature.progression.advancement.SLAdvancement

internal class ShieldMultiblockClass08LeftTest : ShieldMultiblockTest() {
    override fun getMultiblock(): Multiblock = ShieldMultiblockClass08Left
    override fun getExpectedAdvancement(): SLAdvancement = SLAdvancement.PARTICLE_SHIELD_08
    override fun getClassText(): String = "&b0.8"
}
