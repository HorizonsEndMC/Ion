package net.horizonsend.ion.server.features.ai.starship

import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import net.horizonsend.ion.common.utils.text.miniMessage
import net.horizonsend.ion.server.IonServer
import net.horizonsend.ion.server.features.ai.configuration.AITemplate
import net.horizonsend.ion.server.features.ai.module.AIModule
import net.horizonsend.ion.server.features.ai.module.misc.RadiusMessageModule
import net.horizonsend.ion.server.features.ai.module.misc.ReinforcementSpawnerModule
import net.horizonsend.ion.server.features.ai.module.misc.SmackTalkModule
import net.horizonsend.ion.server.features.ai.spawning.spawner.ReinforcementSpawner
import net.horizonsend.ion.server.features.starship.control.controllers.ai.AIController
import net.kyori.adventure.text.minimessage.MiniMessage

@Serializable
class BehaviorConfiguration(
	val controllerFactory: String = "STARFIGHTER",
	val maxSpeed: Int = -1,
	val engagementRange: Double,

	val additionalModules: List<AdditionalModule>
) {
	@Serializable
	sealed interface AdditionalModule {
		val name: String

		fun createModule(controller: AIController): AIModule
	}

	@Serializable
	data class SmackInformation(
		val prefix: String,
		val messages: List<String>
	) : AdditionalModule {
		@Contextual
		override val name: String = "smackTalk"

		override fun createModule(controller: AIController): SmackTalkModule {
			if (messages.isEmpty()) throw NoSuchElementException()
			val prefix = MiniMessage.miniMessage().deserialize(prefix)

			val messages = messages.map { it.miniMessage() }.toTypedArray()

			return SmackTalkModule(controller, prefix, *messages)
		}
	}

	@Serializable
	data class RadiusMessageInformation(
		val prefix: String,
		val messages: Map<Double, String>
	) : AdditionalModule {
		@Contextual
		override val name: String = "radiusMessage"

		override fun createModule(controller: AIController): RadiusMessageModule {
			if (messages.isEmpty()) throw NoSuchElementException()
			val prefix = MiniMessage.miniMessage().deserialize(prefix)

			val messages = messages.mapValues { it.value.miniMessage() }

			return RadiusMessageModule(controller, prefix, messages)
		}
	}

	@Serializable
	data class ReinforcementInformation(
		val activationThreshold: Double,
		val delay: Long,
		val broadcastMessage: String?,
		val reinforcementShips: List<AITemplate.SpawningInformationHolder>
	) : AdditionalModule {
		@Contextual
		override val name: String = "reinforcement"

		override fun createModule(controller: AIController): ReinforcementSpawnerModule {
			val spawner = ReinforcementSpawner(IonServer.slF4JLogger, controller, reinforcementShips)

			return ReinforcementSpawnerModule(
				controller,
				spawner,
				activationThreshold,
				broadcastMessage?.let { message -> MiniMessage.miniMessage().deserialize(message) },
				delay = delay,
			)
		}
	}
}
