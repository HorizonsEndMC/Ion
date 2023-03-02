package net.starlegacy.feature.multiblock.printer

import net.starlegacy.feature.multiblock.LegacyMultiblockShape
import org.bukkit.Material
import org.bukkit.inventory.ItemStack

object GlassPrinterMultiblock : PrinterMultiblock() {
	override val signText = createSignText(
		line1 = "&8Glass",
		line2 = "&fPrinter",
		line3 = "",
		line4 = "&7|[+][+]|"
	)
	override fun getOutput(product: Material): ItemStack = ItemStack(product, 2)

	override fun LegacyMultiblockShape.RequirementBuilder.printerMachineryBlock() = sponge()
	override fun LegacyMultiblockShape.RequirementBuilder.printerCoreBlock() = endRod()
	override fun LegacyMultiblockShape.RequirementBuilder.printerProductBlock() = anyGlass()
}
