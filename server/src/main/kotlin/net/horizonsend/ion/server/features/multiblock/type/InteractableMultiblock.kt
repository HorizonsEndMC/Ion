package net.horizonsend.ion.server.features.multiblock.type

import kotlinx.coroutines.launch
import net.horizonsend.ion.common.extensions.userError
import net.horizonsend.ion.server.features.multiblock.Multiblocks
import net.horizonsend.ion.server.features.multiblock.newer.MultiblockAccess
import net.horizonsend.ion.server.features.multiblock.util.getBukkitBlockState
import net.horizonsend.ion.server.listener.SLEventListener
import org.bukkit.block.Sign
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.block.Action
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.EquipmentSlot

interface InteractableMultiblock {
	fun onSignInteract(sign: Sign, player: Player, event: PlayerInteractEvent)

	companion object : SLEventListener() {
		@EventHandler
		fun onPlayerInteract(event: PlayerInteractEvent) = Multiblocks.multiblockCoroutineScope.launch {
			if (event.hand != EquipmentSlot.HAND) return@launch
			if (event.action != Action.RIGHT_CLICK_BLOCK) return@launch
			val player = event.player

			val clickedBlock = event.clickedBlock ?: return@launch
			val sign = getBukkitBlockState(clickedBlock, false) as? Sign ?: return@launch

			val multiblock = Multiblocks.getFromSignPosition(
				sign.world,
				sign.x,
				sign.y,
				sign.z,
				checkStructure = true,
				loadChunks = false
			) ?: MultiblockAccess.getMultiblock(sign, checkStructure = true, loadChunks = false)

			if (multiblock !is InteractableMultiblock) return@launch

			multiblock.requiredPermission?.let { permission ->
				if (!player.hasPermission(permission)) {
					player.userError("You don't have permission to use that multiblock!")
					return@launch
				}
			}

			multiblock.onSignInteract(sign, player, event)
		}
	}
}
