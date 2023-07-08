package net.horizonsend.ion.server.miscellaneous

import com.comphenix.protocol.PacketType
import com.comphenix.protocol.events.PacketAdapter
import com.comphenix.protocol.events.PacketEvent
import net.horizonsend.ion.server.IonServer
import net.horizonsend.ion.server.miscellaneous.commands.debugRed
import net.minecraft.network.protocol.game.ClientboundSoundPacket
import net.minecraft.network.protocol.game.ServerboundUseItemOnPacket
import net.minecraft.network.protocol.game.ServerboundUseItemPacket
import net.minecraft.resources.ResourceLocation
import net.starlegacy.feature.starship.control.StarshipControl
import net.starlegacy.feature.starship.subsystem.weapon.projectile.SimpleProjectile
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

		when (val packet = e.packet.handle) {
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

			is ClientboundSoundPacket -> {
				if (
					packet.sound.`is`(ResourceLocation("entity.generic.explode")) &&
					SimpleProjectile.noSoundList.any {
						it.x == packet.x && it.y == packet.y && it.z == packet.z
					}
				) {
					e.isCancelled = true
					SimpleProjectile.noSoundList.removeIf {
						it.x == packet.x && it.y == packet.y && it.z == packet.z
					}
				}
			}
		}
	}

	override fun onPacketSending(event: PacketEvent?) {
	}
}
