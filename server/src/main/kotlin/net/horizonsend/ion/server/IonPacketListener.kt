package net.horizonsend.ion.server

import com.comphenix.protocol.PacketType
import com.comphenix.protocol.events.PacketAdapter
import com.comphenix.protocol.events.PacketEvent
import net.minecraft.network.protocol.game.ServerboundUseItemOnPacket
import net.minecraft.network.protocol.game.ServerboundUseItemPacket
import net.starlegacy.feature.starship.control.StarshipControl
import net.starlegacy.util.Tasks
import org.bukkit.block.BlockFace
import org.bukkit.event.block.Action
import org.bukkit.event.player.PlayerInteractEvent

class IonPacketListener(type: PacketType) : PacketAdapter(
	IonServer,
	type
) {
	override fun onPacketReceiving(e: PacketEvent) {
		if (e.isPlayerTemporary) return

		when (e.packet.handle) {
			is ServerboundUseItemOnPacket -> {
				e.player.debugRed("use item on (rclick block)")

				Tasks.sync {
					StarshipControl.onClick(
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

			is ServerboundUseItemPacket -> {
				e.player.debugRed("use item (rclick air)")

				Tasks.sync {
					StarshipControl.onClick(
						PlayerInteractEvent(
							e.player,
							Action.RIGHT_CLICK_AIR,
							null,
							null,
							BlockFace.DOWN
						)
					)
				}
			}
		}
	}

	override fun onPacketSending(event: PacketEvent?) {
	}
}
