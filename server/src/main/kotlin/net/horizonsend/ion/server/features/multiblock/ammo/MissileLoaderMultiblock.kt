package net.horizonsend.ion.server.features.multiblock.ammo

import net.horizonsend.ion.server.features.multiblock.FurnaceMultiblock
import net.horizonsend.ion.server.features.multiblock.Multiblock
import net.horizonsend.ion.server.features.multiblock.MultiblockShape
import net.horizonsend.ion.server.features.multiblock.PowerStoringMultiblock
import net.kyori.adventure.text.Component
import org.bukkit.Material
import org.bukkit.block.Furnace
import org.bukkit.block.Sign
import org.bukkit.event.inventory.FurnaceBurnEvent

object MissileLoaderMultiblock : Multiblock(), PowerStoringMultiblock, FurnaceMultiblock {

    override fun MultiblockShape.buildStructure() {
        z(+0) {
            y(-1) {
                x(-2).anyStairs()
                x(-1).ironBlock()
                x(+0).wireInputComputer()
                x(+1).ironBlock()
                x(+2).anyStairs()
            }

            y(0) {
                x(-1).anyStairs()
                x(+0).machineFurnace()
                x(+1).anyStairs()
            }
        }
        z(+1) {
            y(-1) {
                x(-2).netheriteBlock()
                x(-1).type(Material.GRINDSTONE)
                x(+0).type(Material.OBSERVER)
                x(+1).type(Material.GRINDSTONE)
                x(+2).netheriteBlock()
            }
            y(0) {
                x(-2).anyGlassPane()
                x(-1).anyGlass()
                x(+0).anyGlass()
                x(+1).anyGlass()
                x(+2).anyGlassPane()
            }
        }
        z(+2) {
            y(-1) {
                x(-2).netheriteBlock()
                x(-1).type(Material.GRINDSTONE)
                x(+0).type(Material.OBSERVER)
                x(+1).type(Material.GRINDSTONE)
                x(+2).netheriteBlock()
            }
            y(0) {
                x(-2).anyGlass()
                x(-1).endRod()
                x(+0).anyGlass()
                x(+1).endRod()
                x(+2).anyGlass()
            }
        }
        z(+3) {
            y(-1) {
                x(-2).anyGlass()
                x(-1).type(Material.SMITHING_TABLE)
                x(+0).lodestone()
                x(+1).type(Material.SMITHING_TABLE)
                x(+2).anyGlass()
            }
            y(0) {
                x(-2).anyGlass()
                x(-1).type(Material.DROPPER)
                x(+0).anyGlass()
                x(+1).type(Material.DROPPER)
                x(+2).anyGlass()
            }
        }
        z(+4) {
            y(-1) {
                x(-2).netheriteBlock()
                x(-1).type(Material.GRINDSTONE)
                x(+0).type(Material.OBSERVER)
                x(+1).type(Material.GRINDSTONE)
                x(+2).netheriteBlock()
            }
            y(0) {
                x(-2).anyGlass()
                x(-1).endRod()
                x(+0).anyGlass()
                x(+1).endRod()
                x(+2).anyGlass()
            }
        }
        z(+5) {
            y(-1) {
                x(-2).netheriteBlock()
                x(-1).type(Material.GRINDSTONE)
                x(+0).type(Material.OBSERVER)
                x(+1).type(Material.GRINDSTONE)
                x(+2).netheriteBlock()
            }
            y(0) {
                x(-2).anyGlassPane()
                x(-1).anyGlass()
                x(+0).anyGlass()
                x(+1).anyGlass()
                x(+2).anyGlassPane()
            }
        }
        z(+6) {
            y(-1) {
                x(-2).anyStairs()
                x(-1).ironBlock()
                x(+0).craftingTable()
                x(+1).ironBlock()
                x(+2).anyStairs()
            }

            y(0) {
                x(-1).anyStairs()
                x(+0).anyPipedInventory()
                x(+1).anyStairs()
            }
        }
    }

    override fun onFurnaceTick(event: FurnaceBurnEvent, furnace: Furnace, sign: Sign) {
    }

    override val name: String
        get() = TODO("Not yet implemented")
    override val signText = createSignText(
        line1 = "&6Missile",
        line2 = "&8Loader",
        line3 = null,
        line4 = null
    )

    override val maxPower = 250_000


}