package net.horizonsend.ion.server.command.qol

import co.aikar.commands.annotation.CommandAlias
import co.aikar.commands.annotation.Default
import net.horizonsend.ion.common.extensions.success
import net.horizonsend.ion.common.extensions.userError
import net.horizonsend.ion.server.command.SLCommand
import net.horizonsend.ion.server.features.multiblock.MultiblockAccess
import net.horizonsend.ion.server.features.multiblock.type.particleshield.ShieldMultiblock
import net.horizonsend.ion.server.miscellaneous.utils.getBlockTypeSafe
import net.horizonsend.ion.server.miscellaneous.utils.getSelection
import net.horizonsend.ion.server.miscellaneous.utils.isWallSign
import org.bukkit.block.Sign
import org.bukkit.entity.Player

@CommandAlias("displayshields")
object DisplayShieldsCommand : SLCommand() {
    @Default
    fun onDisplayShields(sender: Player) {
        val maxSelectionVolume = 200000
        val selection = sender.getSelection() ?: return
        if (selection.volume > maxSelectionVolume) {
            sender.userError("Selection too large! The maximum volume is $maxSelectionVolume.")
            return
        }

        if (sender.world.name != selection.world?.name) return

        var count = 0

        for (blockPosition in selection) {
            val x = blockPosition.x()
            val y = blockPosition.y()
            val z = blockPosition.z()

            val block = sender.world.getBlockAt(x, y, z)

            if (getBlockTypeSafe(sender.world, x, y, z)?.isWallSign == true) {
                val sign = block.state as Sign
                val multiblock = MultiblockAccess.getStored(sign)
                if (multiblock == null || multiblock !is ShieldMultiblock) continue

                multiblock.displayShieldCoverage(sign)
            }

            count++
        }

        sender.success("Displaying $count shields")
    }
}