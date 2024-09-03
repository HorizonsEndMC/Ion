package net.horizonsend.ion.server.features.multiblock.type.misc

import net.horizonsend.ion.server.features.machine.decomposer.Decomposers
import net.horizonsend.ion.server.features.multiblock.Multiblock
import net.horizonsend.ion.server.features.multiblock.shape.MultiblockShape
import net.horizonsend.ion.server.features.multiblock.type.InteractableMultiblock
import net.horizonsend.ion.server.features.multiblock.type.PowerStoringMultiblock
import org.bukkit.block.BlockFace
import org.bukkit.block.Sign
import org.bukkit.entity.Player
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.InventoryHolder

object DecomposerMultiblock : Multiblock(), PowerStoringMultiblock, InteractableMultiblock {
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

	override fun onSignInteract(sign: Sign, player: Player, event: PlayerInteractEvent) {
		Decomposers.onClick(event)
	}
}
