package net.starlegacy.feature.multiblock.areashield

import net.starlegacy.feature.progression.advancement.SLAdvancement
import net.starlegacy.util.Vec3i

internal class AreaShield5Test : AreaShieldTest() {
    override fun getMultiblock() = AreaShield5
    override fun getExpectedAdvancement() = SLAdvancement.AREA_SHIELD_5
    override fun getExpectedNoteBlockOffset(): Vec3i = Vec3i(0, -1, 0)
    override fun getExpectedRadius() = 5
}
