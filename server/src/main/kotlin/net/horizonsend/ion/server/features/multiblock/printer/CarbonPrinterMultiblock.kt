package net.horizonsend.ion.server.features.multiblock.printer

import net.horizonsend.ion.server.features.multiblock.MultiblockShape
import net.starlegacy.util.isConcretePowder
import org.bukkit.Material
import org.bukkit.inventory.ItemStack

object CarbonPrinterMultiblock : PrinterMultiblock() {
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
