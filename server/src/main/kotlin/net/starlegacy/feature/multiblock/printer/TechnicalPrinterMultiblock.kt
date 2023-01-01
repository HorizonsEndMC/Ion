package net.starlegacy.feature.multiblock.printer

import net.starlegacy.feature.multiblock.LegacyMultiblockShape
import org.bukkit.Material
import org.bukkit.inventory.ItemStack

object TechnicalPrinterMultiblock : PrinterMultiblock() {
	override val signText = createSignText(
		"&cTechnical",
		"&fPrinter",
		"",
		"&7+?[-|],+"
	)

	override fun getOutput(product: Material) = ItemStack(product, 1)

	override fun LegacyMultiblockShape.RequirementBuilder.printerMachineryBlock() = redstoneLamp()
	override fun LegacyMultiblockShape.RequirementBuilder.printerCoreBlock() = daylightSensor()
	override fun LegacyMultiblockShape.RequirementBuilder.printerProductBlock() = sponge()
}
