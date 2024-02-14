package net.horizonsend.ion.server.features.client.networking.serverbound

import com.comphenix.protocol.ProtocolLibrary
import net.horizonsend.ion.server.IonServerComponent

object PacketHandler : IonServerComponent() {
	override fun onEnable() {
		listOf(
			RightClickListener()
		).forEach {
			ProtocolLibrary.getProtocolManager().addPacketListener(it)
		}
	}
}
