package net.horizonsend.ion.server.features.multiblock.type.printer

import net.horizonsend.ion.server.features.multiblock.shape.MultiblockShape
import net.horizonsend.ion.server.miscellaneous.utils.isStainedTerracotta
import org.bukkit.Material
import org.bukkit.inventory.ItemStack

abstract class AbstractArmorPrinterMultiblock : PrinterMultiblock() {
	override val signText = createSignText(
		line1 = "&6Armor",
		line2 = "&fPrinter",
		line3 = null,
		line4 = "&7!??_+!0"
	)

	override fun getOutput(product: Material): ItemStack = ItemStack(product, 1)

	override fun MultiblockShape.RequirementBuilder.printerMachineryBlock() = sponge()
	override fun MultiblockShape.RequirementBuilder.printerCoreBlock() = type(Material.ANVIL)
	override fun MultiblockShape.RequirementBuilder.printerProductBlock() =
		filteredTypes("any terracotta") { it == Material.TERRACOTTA || it.isStainedTerracotta }
}

object ArmorPrinterMultiblock : AbstractArmorPrinterMultiblock() {
	override val mirrored = false
}

object ArmorPrinterMultiblockMirrored : AbstractArmorPrinterMultiblock() {
	override val mirrored = true
}
