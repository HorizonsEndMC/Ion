package net.horizonsend.ion.server.features.multiblock.printer

import net.horizonsend.ion.server.features.multiblock.MultiblockShape
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

	override fun MultiblockShape.RequirementBuilder.printerMachineryBlock() = redstoneLamp()
	override fun MultiblockShape.RequirementBuilder.printerCoreBlock() = daylightSensor()
	override fun MultiblockShape.RequirementBuilder.printerProductBlock() = sponge()
}
