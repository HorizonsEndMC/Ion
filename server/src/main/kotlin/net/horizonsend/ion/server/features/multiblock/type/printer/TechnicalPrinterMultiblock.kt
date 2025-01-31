package net.horizonsend.ion.server.features.multiblock.type.printer

import net.horizonsend.ion.server.features.multiblock.shape.MultiblockShape
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.Component.text
import org.bukkit.Material
import org.bukkit.inventory.ItemStack

abstract class AbstractTechnicalPrinterMultiblock : PrinterMultiblock() {
	override val signText = createSignText(
		"&cTechnical",
		"&fPrinter",
		"",
		"&7+?[-|],+"
	)
	override val displayName: Component
		get() = text("Technical Printer")
	override val description: Component
		get() = text("Transforms Cobblestone into Sponge.")
	override fun getOutput(product: Material) = ItemStack(product, 1)

	override fun MultiblockShape.RequirementBuilder.printerMachineryBlock() = redstoneLamp()
	override fun MultiblockShape.RequirementBuilder.printerCoreBlock() = daylightSensor()
	override fun MultiblockShape.RequirementBuilder.printerProductBlock() = sponge()
}

object TechnicalPrinterMultiblock : AbstractTechnicalPrinterMultiblock() {
	override val mirrored = false
}

object TechnicalPrinterMultiblockMirrored : AbstractTechnicalPrinterMultiblock() {
	override val mirrored = true
}
