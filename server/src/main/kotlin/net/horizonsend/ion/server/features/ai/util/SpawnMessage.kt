package net.horizonsend.ion.server.features.ai.util

import net.horizonsend.ion.common.utils.text.colors.HEColorScheme
import net.horizonsend.ion.common.utils.text.template
import net.horizonsend.ion.server.IonServer
import net.horizonsend.ion.server.features.ai.configuration.AITemplate
import net.horizonsend.ion.server.miscellaneous.utils.Notify
import net.kyori.adventure.audience.ForwardingAudience
import net.kyori.adventure.text.Component
import org.bukkit.Location
import org.bukkit.entity.Player

interface SpawnMessage {
	fun broadcast(location: Location, template: AITemplate)

	fun format(base: Component, location: Location, template: AITemplate): Component {
		return template(
			message = base,
			paramColor = HEColorScheme.HE_LIGHT_GRAY,
			useQuotesAroundObjects = false,
			template.starshipInfo.componentName(),
			location.blockX,
			location.blockY,
			location.blockZ,
			location.world.name
		)
	}

	class GlobalMessage(val message: Component) : SpawnMessage {
		override fun broadcast(location: Location, template: AITemplate) {
			Notify.chatAndGlobal(format(message, location, template))
		}
	}

	class ChatMessage(val message: Component) : SpawnMessage {
		override fun broadcast(location: Location, template: AITemplate) {
			IonServer.server.sendMessage(format(message, location, template))
		}
	}

	class RadiusMessage(val message: Component, val radius: Double) : SpawnMessage {
		override fun broadcast(location: Location, template: AITemplate) {
			ForwardingAudience { location.getNearbyPlayers(radius) }.sendMessage(format(message, location, template))
		}
	}

	class WorldMessage(val message: Component) : SpawnMessage {
		override fun broadcast(location: Location, template: AITemplate) {
			location.world.sendMessage(format(message, location, template))
		}
	}

	class SelectorMessage(val message: Component, private val selector: (Player) -> Boolean) : SpawnMessage {
		override fun broadcast(location: Location, template: AITemplate) {
			ForwardingAudience { IonServer.server.onlinePlayers.filter(selector) }.sendMessage(format(message, location, template))
		}
	}
}
