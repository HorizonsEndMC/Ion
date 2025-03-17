package net.horizonsend.ion.server.features.multiblock.type.printer

import net.horizonsend.ion.server.features.multiblock.shape.MultiblockShape
import net.horizonsend.ion.server.miscellaneous.utils.isConcretePowder
import org.bukkit.Material
import org.bukkit.inventory.ItemStack

abstract class AbstractCarbonPrinterMultiblock : PrinterMultiblock() {
	override val signText = createSignText(
		"&bCarbon",
		"&fPrinter",
		"",
		"&7-:[=]:-"
	)
	override fun getOutput(product: Material): ItemStack = ItemStack(product, 2)

	override fun MultiblockShape.RequirementBuilder.printerMachineryBlock() = sponge()
	override fun MultiblockShape.RequirementBuilder.printerCoreBlock() = type(Material.MAGMA_BLOCK)
	override fun MultiblockShape.RequirementBuilder.printerProductBlock() = filteredTypes("any concrete powder") { it.isConcretePowder }
}

object CarbonPrinterMultiblock : AbstractCarbonPrinterMultiblock() {
	override val mirrored = false
}

object CarbonPrinterMultiblockMirrored : AbstractCarbonPrinterMultiblock() {
	override val mirrored = true
}
