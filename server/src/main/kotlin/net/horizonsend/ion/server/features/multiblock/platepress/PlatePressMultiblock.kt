package net.horizonsend.ion.server.features.multiblock.platepress

import net.horizonsend.ion.server.features.multiblock.FurnaceMultiblock
import net.horizonsend.ion.server.features.multiblock.Multiblock
import net.horizonsend.ion.server.features.multiblock.PowerStoringMultiblock

class PlatePressMultiblock : Multiblock(), PowerStoringMultiblock, FurnaceMultiblock {

    override val name = "platepress"

    override val signText = createSignText(
        line1 = "&7Plate &4Press",
        line2 = null,
        line3 = null,
        line4 = null
    )

}