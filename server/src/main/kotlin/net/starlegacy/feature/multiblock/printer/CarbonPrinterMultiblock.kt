package net.starlegacy.feature.multiblock.printer

import net.starlegacy.feature.multiblock.LegacyMultiblockShape
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

	override fun LegacyMultiblockShape.RequirementBuilder.printerMachineryBlock() = sponge()
	override fun LegacyMultiblockShape.RequirementBuilder.printerCoreBlock() = type(Material.MAGMA_BLOCK)
	override fun LegacyMultiblockShape.RequirementBuilder.printerProductBlock() = filteredTypes { it.isConcretePowder }
}
