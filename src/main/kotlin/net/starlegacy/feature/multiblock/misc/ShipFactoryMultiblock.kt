package net.starlegacy.feature.multiblock.misc

import net.starlegacy.feature.multiblock.MultiblockShape
import net.starlegacy.feature.multiblock.PowerStoringMultiblock
import net.starlegacy.feature.progression.advancement.SLAdvancement
import net.starlegacy.util.getFacing
import net.starlegacy.util.rightFace
import org.bukkit.block.Sign
import org.bukkit.entity.Player
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.InventoryHolder

object ShipFactoryMultiblock : PowerStoringMultiblock() {
    override val advancement = SLAdvancement.SHIP_FACTORY
    override val name = "shipfactory"

    override val signText = createSignText(
        line1 = "&1Ship Factory",
        line2 = null,
        line3 = null,
        line4 = null
    )

    override val maxPower: Int = 1_000_000

    override fun onTransformSign(player: Player, sign: Sign) {
        sign.setLine(2, sign.getLine(1))
        sign.setLine(1, player.uniqueId.toString())
    }

    override fun MultiblockShape.buildStructure() {
        z(+0) {
            y(-1) {
                x(+0).ironBlock()
                x(+1).ironBlock()
            }

            y(+0) {
                x(+0).furnace()
                x(+1).anyPipedInventory()
            }
        }
    }

    fun getStorage(sign: Sign): Inventory {
        val direction = sign.getFacing().oppositeFace
        return (sign.block.getRelative(direction).getRelative(direction.rightFace).state as InventoryHolder).inventory
    }
}
