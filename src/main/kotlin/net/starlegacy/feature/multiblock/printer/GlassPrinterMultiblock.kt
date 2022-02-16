package net.starlegacy.feature.multiblock.printer

import net.starlegacy.feature.multiblock.MultiblockShape
import net.starlegacy.feature.progression.advancement.SLAdvancement
import org.bukkit.Material
import org.bukkit.inventory.ItemStack

object GlassPrinterMultiblock : PrinterMultiblock() {
	override val advancement = SLAdvancement.PRINTER_GLASS

	override val signText = createSignText(
		line1 = "&8Glass",
		line2 = "&fPrinter",
		line3 = "",
		line4 = "&7|[+][+]|"
	)

	override fun getOutput(product: Material): ItemStack = ItemStack(product, 2)

	override fun MultiblockShape.RequirementBuilder.printerMachineryBlock() = sponge()
	override fun MultiblockShape.RequirementBuilder.printerCoreBlock() = endRod()
	override fun MultiblockShape.RequirementBuilder.printerProductBlock() = anyGlass()
}
