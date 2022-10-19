package net.starlegacy.feature.multiblock.misc

import net.starlegacy.feature.multiblock.MultiblockShape
import net.starlegacy.feature.multiblock.PowerStoringMultiblock
import org.bukkit.block.BlockFace
import org.bukkit.block.Sign
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.InventoryHolder

object DecomposerMultiblock : PowerStoringMultiblock() {
	override val maxPower: Int = 75_000
	override val name: String = "decomposer"
	override val signText = createSignText(
		"&cDecomposer",
		null,
		null,
		null
	)

	override fun MultiblockShape.buildStructure() {
		at(0, 0, 0).ironBlock()
		at(0, -1, -1).anyPipedInventory()
	}

	fun getStorage(sign: Sign): Inventory {
		return (sign.block.getRelative(BlockFace.DOWN).state as InventoryHolder).inventory
	}
}