package net.horizonsend.ion.server.features.misc

import com.comphenix.protocol.PacketType
import com.comphenix.protocol.ProtocolLibrary
import com.comphenix.protocol.events.PacketAdapter
import com.comphenix.protocol.events.PacketEvent
import net.horizonsend.ion.server.IonServer
import net.horizonsend.ion.server.IonServerComponent
import net.horizonsend.ion.server.command.admin.debugRed
import net.horizonsend.ion.server.features.starship.control.weaponry.PlayerStarshipWeaponry.onClick
import net.horizonsend.ion.server.features.starship.control.weaponry.PlayerStarshipWeaponry.onShiftRClick
import net.horizonsend.ion.server.miscellaneous.utils.Tasks
import org.bukkit.block.BlockFace
import org.bukkit.event.block.Action
import org.bukkit.event.player.PlayerInteractEvent

object PacketHandler : IonServerComponent() {
	override fun onEnable() {
		listOf(
			RightClickListener()
		).forEach {
			ProtocolLibrary.getProtocolManager().addPacketListener(it)
		}
	}
}

class RightClickListener : PacketAdapter(
	IonServer,
	PacketType.Play.Client.USE_ITEM,
	PacketType.Play.Client.BLOCK_PLACE,
) {
	override fun onPacketReceiving(e: PacketEvent) {
		if (e.isPlayerTemporary) return

		e.player.debugRed("use item")

		Tasks.sync {
			onClick(
				PlayerInteractEvent(
					e.player,
					Action.RIGHT_CLICK_BLOCK,
					null,
					null,
					BlockFace.DOWN
				)
			)
			onShiftRClick(
					PlayerInteractEvent(
							e.player,
							Action.RIGHT_CLICK_BLOCK,
							null,
							null,
							BlockFace.DOWN
					)
			)
		}
	}
}
