package net.horizonsend.ion.server.features.multiblock.type.printer

import net.horizonsend.ion.server.features.multiblock.shape.MultiblockShape
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.Component.text
import org.bukkit.Material
import org.bukkit.inventory.ItemStack

abstract class AbstractGlassPrinterMultiblock : PrinterMultiblock() {
	override val signText = createSignText(
		line1 = "&8Glass",
		line2 = "&fPrinter",
		line3 = "",
		line4 = "&7|[+][+]|"
	)
	override val displayName: Component
		get() = text("Glass Printer")
	override val description: Component
		get() = text("Transforms Cobblestone into Glass.")
	override fun getOutput(product: Material): ItemStack = ItemStack(product, 2)

	override fun MultiblockShape.RequirementBuilder.printerMachineryBlock() = sponge()
	override fun MultiblockShape.RequirementBuilder.printerCoreBlock() = endRod()
	override fun MultiblockShape.RequirementBuilder.printerProductBlock() = this.anyGlass()
}

object GlassPrinterMultiblock : AbstractGlassPrinterMultiblock() {
	override val mirrored = false
}

object GlassPrinterMultiblockMirrored : AbstractGlassPrinterMultiblock() {
	override val mirrored = true
}
