package net.horizonsend.ion.server.listener.packets

import com.comphenix.protocol.PacketType
import com.comphenix.protocol.ProtocolLibrary
import com.comphenix.protocol.ProtocolManager
import com.comphenix.protocol.events.PacketAdapter
import com.comphenix.protocol.events.PacketEvent
import net.horizonsend.ion.server.IonServer
import net.horizonsend.ion.server.core.IonServerComponent
import net.horizonsend.ion.server.features.custom.items.type.armor.AscendingMode
import net.horizonsend.ion.server.features.custom.items.type.armor.StrafingMode
import net.horizonsend.ion.server.features.custom.items.type.tool.mods.armor.RocketBoostingMod
import net.minecraft.network.protocol.game.ServerboundPlayerInputPacket
import org.bukkit.Bukkit

object PacketListeners : IonServerComponent() {
	private lateinit var protocolManager: ProtocolManager

	override fun onEnable() {
		if (!Bukkit.getPluginManager().isPluginEnabled("ProtocolLib")) return

		protocolManager = ProtocolLibrary.getProtocolManager()

		protocolManager.addPacketListener(object: PacketAdapter(IonServer, PacketType.Play.Client.STEER_VEHICLE) {
			override fun onPacketReceiving(event: PacketEvent) {
				val player = event.player
				val input = (event.packet.handle as ServerboundPlayerInputPacket).input

				if ((input.left && input.right) || (!input.left && !input.right)) { // both or neither pressed
					RocketBoostingMod.strafingMode.remove(player.uniqueId)
				} else {
					if(input.left) RocketBoostingMod.strafingMode[player.uniqueId] = StrafingMode.LEFT
					if(input.right) RocketBoostingMod.strafingMode[player.uniqueId] = StrafingMode.RIGHT
				}
				if((input.forward && input.backward) || (!input.forward && !input.backward)) { // both or neither pressed
					RocketBoostingMod.ascendingMode.remove(player.uniqueId)
				} else {
					if(input.forward) RocketBoostingMod.ascendingMode[player.uniqueId] = AscendingMode.DESCENDING
					if(input.backward) RocketBoostingMod.ascendingMode[player.uniqueId] = AscendingMode.ASCENDING
				}

			}
		})
	}
}
