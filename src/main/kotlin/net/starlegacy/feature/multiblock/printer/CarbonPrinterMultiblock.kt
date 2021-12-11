package net.starlegacy.feature.multiblock.printer

import net.starlegacy.feature.multiblock.MultiblockShape
import net.starlegacy.feature.progression.advancement.SLAdvancement
import net.starlegacy.util.isConcretePowder
import org.bukkit.Material
import org.bukkit.inventory.ItemStack

object CarbonPrinterMultiblock : PrinterMultiblock() {
    override val advancement = SLAdvancement.PRINTER_CARBON

    override val signText = createSignText(
        "&bCarbon",
        "&fPrinter",
        "",
        "&7-:[=]:-"
    )

    override fun getOutput(product: Material): ItemStack = ItemStack(product, 2)

    override fun MultiblockShape.RequirementBuilder.printerMachineryBlock() = sponge()
    override fun MultiblockShape.RequirementBuilder.printerCoreBlock() = type(Material.MAGMA_BLOCK)
    override fun MultiblockShape.RequirementBuilder.printerProductBlock() = filteredTypes { it.isConcretePowder }
}
