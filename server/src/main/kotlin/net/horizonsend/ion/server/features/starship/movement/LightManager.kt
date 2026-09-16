package net.horizonsend.ion.server.features.starship.movement

import com.comphenix.protocol.PacketType
import com.comphenix.protocol.events.ListenerPriority
import com.comphenix.protocol.events.PacketAdapter
import com.comphenix.protocol.events.PacketEvent
import com.comphenix.protocol.wrappers.WrappedBlockData
import net.horizonsend.ion.common.IonComponent
import net.horizonsend.ion.common.database.schema.misc.PlayerSettings
import net.horizonsend.ion.server.IonServer
import net.horizonsend.ion.server.IonServer.protocolManager
import net.horizonsend.ion.server.features.cache.PlayerSettingsCache.getSetting
import net.minecraft.network.protocol.game.ClientboundLevelChunkWithLightPacket
import net.minecraft.network.protocol.game.ClientboundLightUpdatePacketData
import org.bukkit.Material
import org.bukkit.entity.Player
import java.lang.reflect.Field


object LightManager : IonComponent() {

	private var lightDataField: Field? = null
	override fun onEnable() {
		lightDataField = resolveLightDataField()
		registerListeners()
	}

	fun registerListeners() {
		protocolManager?.addPacketListener(object : PacketAdapter(
			IonServer, ListenerPriority.HIGHEST,
			PacketType.Play.Server.LIGHT_UPDATE
		) {
			override fun onPacketSending(event: PacketEvent) {
				if(event.player.getSetting(PlayerSettings::doLightUpdates) == true) return
				event.setCancelled(true)
			}
		})


		// Light data embedded in the initial chunk-send packet.
		protocolManager?.addPacketListener(object : PacketAdapter(
			IonServer, ListenerPriority.HIGHEST,
			PacketType.Play.Server.MAP_CHUNK
		) {
			override fun onPacketSending(event: PacketEvent?) {
				stripLights(event ?: return, event.player ?: return)
			}
		})

	}

	private fun resolveLightDataField(): Field? {
		try {
			for (f in ClientboundLevelChunkWithLightPacket::class.java.getDeclaredFields()) {
				if (f.type == ClientboundLightUpdatePacketData::class.java) {
					f.setAccessible(true)
					return f
				}
			}
		} catch (e: java.lang.Exception) {
			IonServer.logger.severe("LightPacketManager: could not locate lightData field: $e")
		}
		return null
	}

	fun stripLights(event: PacketEvent, player: Player) {
		if (player.getSetting(PlayerSettings::doLightUpdates) == true) return

		val packet = event.packet
		val handler = packet.handle as? ClientboundLevelChunkWithLightPacket ?: return
		val lightData = handler.lightData

		lightData.skyYMask.clear()
		lightData.blockYMask.clear()
		lightData.emptySkyYMask.clear()
		lightData.emptyBlockYMask.clear()
		lightData.skyUpdates.clear()
		lightData.blockUpdates.clear()
	}
}
