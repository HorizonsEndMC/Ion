package net.starlegacy.feature.multiblock.printer

import net.starlegacy.feature.multiblock.MultiblockShape
import net.starlegacy.feature.progression.advancement.SLAdvancement
import org.bukkit.Material
import org.bukkit.inventory.ItemStack

object TechnicalPrinterMultiblock : PrinterMultiblock() {
	override val advancement = SLAdvancement.PRINTER_TECHNICAL

	override val signText = createSignText(
		"&cTechnical",
		"&fPrinter",
		"",
		"&7+?[-|],+"
	)

	override fun getOutput(product: Material) = ItemStack(product, 1)

	override fun MultiblockShape.RequirementBuilder.printerMachineryBlock() = redstoneLamp()
	override fun MultiblockShape.RequirementBuilder.printerCoreBlock() = daylightSensor()
	override fun MultiblockShape.RequirementBuilder.printerProductBlock() = sponge()
}
