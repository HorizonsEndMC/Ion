package net.horizonsend.ion.server.features.ai.starship

import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import net.horizonsend.ion.common.utils.text.miniMessage
import net.horizonsend.ion.server.features.ai.configuration.AIEmities
import net.horizonsend.ion.server.features.ai.configuration.AITemplate
import net.horizonsend.ion.server.features.ai.module.AIModule
import net.horizonsend.ion.server.features.ai.module.misc.EnmityMessageModule
import net.horizonsend.ion.server.features.ai.module.misc.EnmityTriggerMessage
import net.horizonsend.ion.server.features.ai.module.misc.FleeMessageModule
import net.horizonsend.ion.server.features.ai.module.misc.FleeTriggerMessage
import net.horizonsend.ion.server.features.ai.module.misc.RadiusMessageModule
import net.horizonsend.ion.server.features.ai.module.misc.ReinforcementSpawnerModule
import net.horizonsend.ion.server.features.ai.module.misc.SmackTalkModule
import net.horizonsend.ion.server.features.ai.module.targeting.EnmityModule
import net.horizonsend.ion.server.features.ai.spawning.spawner.ReinforcementSpawner
import net.horizonsend.ion.server.features.ai.spawning.spawner.mechanics.SpawnerMechanic
import net.horizonsend.ion.server.features.starship.control.controllers.ai.AIController
import net.kyori.adventure.text.minimessage.MiniMessage
import java.util.function.Consumer

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

	/**
	 * Replacement params: 0: world, 1: x, 2: y, z: 3
	 **/
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


	data class EnmityMessageInformation(
		val prefix: String,
		val compiled: List<EnmityTriggerMessage>
	) : AdditionalModule {
		override val name: String = "enmityMessage"

		override fun createModule(controller: AIController): EnmityMessageModule {
			val prefixComponent = MiniMessage.miniMessage().deserialize(prefix)

			// ⬇️ Safely resolve config from the AIController at runtime
			val configSupplier: () -> AIEmities.AIEmityConfiguration = {
				controller.getCoreModuleByType<EnmityModule>()?.config
					?: AIEmities().defaultAIEmityConfiguration
			}

			return EnmityMessageModule(controller, prefixComponent, compiled, configSupplier)
		}
	}

	data class FleeMessageInformation(
		val prefix: String,
		val compiled: List<FleeTriggerMessage>
	) : AdditionalModule {
		override val name: String = "fleeMessage"

		override fun createModule(controller: AIController): FleeMessageModule {
			val prefixComponent = MiniMessage.miniMessage().deserialize(prefix)

			return FleeMessageModule(controller, prefixComponent, compiled)
		}
	}

	@Serializable
	data class BasicReinforcementInformation(
		val activationThreshold: Double,
		val delay: Long,
		val broadcastMessage: String?,
		val reinforcementShips: List<AITemplate.SpawningInformationHolder>
	) : AdditionalModule {
		@Contextual
		override val name: String = "reinforcement"

		override fun createModule(controller: AIController): ReinforcementSpawnerModule {
			val list = mutableListOf<Consumer<AIController>>()

			val spawner = ReinforcementSpawner(controller, reinforcementShips, list)

			return ReinforcementSpawnerModule(
				controller = controller,
				spawner = spawner,
				activationAverageShieldHealth = activationThreshold,
				spawnBroadCastMessage = broadcastMessage?.let { message -> MiniMessage.miniMessage().deserialize(message) },
				delay = delay,
				controllerModifiers = list
			)
		}
	}

	data class AdvancedReinforcementInformation(
		val activationThreshold: Double,
		val delay: Long,
		val broadcastMessage: String?,
		val providedSpawner: (AIController) -> SpawnerMechanic
	) : AdditionalModule {
		override val name: String = "reinforcement"

		override fun createModule(controller: AIController): ReinforcementSpawnerModule {
			val list = mutableListOf<Consumer<AIController>>()

			val spawner = ReinforcementSpawner(controller, providedSpawner.invoke(controller), list)

			return ReinforcementSpawnerModule(
				controller = controller,
				spawner = spawner,
				activationAverageShieldHealth = activationThreshold,
				spawnBroadCastMessage = broadcastMessage?.let { message -> MiniMessage.miniMessage().deserialize(message) },
				delay = delay,
				controllerModifiers = list
			)
		}
	}
}
