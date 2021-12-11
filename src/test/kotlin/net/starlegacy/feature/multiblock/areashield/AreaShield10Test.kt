package net.starlegacy.feature.multiblock.areashield

import net.starlegacy.feature.progression.advancement.SLAdvancement
import net.starlegacy.util.Vec3i

internal class AreaShield10Test : AreaShieldTest() {
    override fun getMultiblock() = AreaShield10
    override fun getExpectedAdvancement() = SLAdvancement.AREA_SHIELD_10
    override fun getExpectedNoteBlockOffset(): Vec3i = Vec3i(0, -1, 0)
    override fun getExpectedRadius() = 10
}
