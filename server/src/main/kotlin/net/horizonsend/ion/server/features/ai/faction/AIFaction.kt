package net.horizonsend.ion.server.features.ai.faction

import net.horizonsend.ion.common.utils.text.colors.ABYSSAL_DARK_RED
import net.horizonsend.ion.common.utils.text.colors.ABYSSAL_DESATURATED_RED
import net.horizonsend.ion.common.utils.text.colors.ABYSSAL_LIGHT_RED
import net.horizonsend.ion.common.utils.text.colors.EXPLORER_LIGHT_CYAN
import net.horizonsend.ion.common.utils.text.colors.EXPLORER_MEDIUM_CYAN
import net.horizonsend.ion.common.utils.text.colors.HEColorScheme
import net.horizonsend.ion.common.utils.text.colors.MINING_CORP_DARK_ORANGE
import net.horizonsend.ion.common.utils.text.colors.MINING_CORP_LIGHT_ORANGE
import net.horizonsend.ion.common.utils.text.colors.PIRATE_LIGHT_RED
import net.horizonsend.ion.common.utils.text.colors.PIRATE_SATURATED_RED
import net.horizonsend.ion.common.utils.text.colors.PRIVATEER_LIGHT_TEAL
import net.horizonsend.ion.common.utils.text.colors.PRIVATEER_MEDIUM_TEAL
import net.horizonsend.ion.common.utils.text.colors.TSAII_DARK_ORANGE
import net.horizonsend.ion.common.utils.text.colors.TSAII_MEDIUM_ORANGE
import net.horizonsend.ion.common.utils.text.colors.WATCHER_ACCENT
import net.horizonsend.ion.common.utils.text.colors.WATCHER_STANDARD
import net.horizonsend.ion.common.utils.text.colors.吃饭人_STANDARD
import net.horizonsend.ion.common.utils.text.miniMessage
import net.horizonsend.ion.common.utils.text.toComponent
import net.horizonsend.ion.server.features.ai.configuration.AIEmities
import net.horizonsend.ion.server.features.ai.configuration.AITemplate
import net.horizonsend.ion.server.features.ai.module.misc.EnmityMessageModule.Companion.betrayalAggro
import net.horizonsend.ion.server.features.ai.module.misc.EnmityMessageModule.Companion.escalatedFriendlyFire
import net.horizonsend.ion.server.features.ai.module.misc.EnmityMessageModule.Companion.triggeredByFriendlyFire
import net.horizonsend.ion.server.features.ai.module.misc.EnmityTriggerMessage
import net.horizonsend.ion.server.features.ai.module.misc.FactionManagerModule
import net.horizonsend.ion.server.features.ai.module.targeting.EnmityModule
import net.horizonsend.ion.server.features.ai.spawning.AISpawningManager.allAIStarships
import net.horizonsend.ion.server.features.ai.spawning.ships.FactionShip
import net.horizonsend.ion.server.features.ai.spawning.ships.SpawnedShip
import net.horizonsend.ion.server.features.ai.starship.AITemplateRegistry
import net.horizonsend.ion.server.features.ai.starship.BehaviorConfiguration
import net.horizonsend.ion.server.features.starship.active.ActiveStarship
import net.horizonsend.ion.server.features.starship.control.controllers.ai.AIController
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.format.NamedTextColor.DARK_RED
import net.kyori.adventure.text.format.TextColor
import net.kyori.adventure.text.format.TextDecoration
import org.bukkit.Color

/**
 * Responsible for providing ships, colors, and managing names of AI ships
 **/
class AIFaction private constructor(
	val identifier: String,
	val color: Int = Integer.parseInt("ff0000", 16),
	val nameList: Map<Int,List<Component>>,
	val suffixes: Map<Int,String>
) {
	private var templateProcess: AITemplateRegistry.Builder.() -> Unit = {}

	fun getFactionStarships(): List<ActiveStarship> = allAIStarships().filter { ship ->
		val controller = ship.controller
		if (controller !is AIController) return@filter false
		val factionManager = controller.getUtilModule(FactionManagerModule::class.java) ?: return@filter false

		return@filter factionManager.faction == this
	}

	fun getAvailableName(difficulty : Int): Component {
		val list = nameList[difficulty]!!
		return list.shuffled().firstOrNull { name ->
			getFactionStarships().none { (it.controller as AIController).pilotName == name }
		} ?: list.random()
	}

	fun processTemplate(template: AITemplateRegistry.Builder) {
		templateProcess(template)
	}

	val controllerModifier: (AIController) -> Unit = { controller ->
		controller.setColor(Color.fromRGB(color))
		controller.addUtilModule(FactionManagerModule(controller, this))
	}

	fun asSpawnedShip(template: AITemplate): SpawnedShip {
		return FactionShip(template, this)
	}

	class Builder(private val identifier: String, val color: Int) {
		private val names: MutableMap<Int,MutableList<Component>> = mutableMapOf()

		private val templateProcessing: MutableList<AITemplateRegistry.Builder.() -> Unit> = mutableListOf()

		private val suffixes: MutableMap<Int,String> = mutableMapOf()

		private val enmityMessages = mutableListOf<EnmityTriggerMessage>()

		/**
		 * Prefix used for smack talk and radius messages
		 **/
		private var	messagePrefix = "<${HEColorScheme.HE_MEDIUM_GRAY}>Receiving transmission from <${TextColor.color(color)}>unknown</${TextColor.color(color)}> vessel."

		fun setMessagePrefix(new: String): Builder {
			messagePrefix = new
			return this
		}

		fun addName(difficulty: Int,name: Component): Builder {
			if (names[difficulty] == null) names[difficulty] = mutableListOf()
			names[difficulty]!! += name
			return this
		}

		fun addNames(difficulty: Int,vararg names: Component): Builder {
			if (this.names[difficulty] == null) this.names[difficulty] = mutableListOf()
			this.names[difficulty]!! += names
			return this
		}

		fun addNames(difficulty: Int,names: Collection<Component>): Builder {
			if (this.names[difficulty] == null) this.names[difficulty] = mutableListOf()
			this.names[difficulty]!! += names
			return this
		}

		fun addDifficultySuffix(difficulty: Int, suffix : String): Builder {
			this.suffixes[difficulty] = suffix
			return this
		}

		fun addSmackMessages(vararg messages: String): Builder {
			this.templateProcessing += {
				addAdditionalModule(BehaviorConfiguration.SmackInformation(prefix = this@Builder.messagePrefix, messages = messages.toList()))
			}
			return this
		}

		fun addRadiusMessages(vararg messages: Pair<Double, String>): Builder {
			this.templateProcessing += {
				addAdditionalModule(BehaviorConfiguration.RadiusMessageInformation(prefix = this@Builder.messagePrefix, messages = mapOf(*messages)))
			}
			return this
		}

		fun addEnmityMessages(vararg messages: Pair<String, Pair<Double, String>>): Builder {
			messages.forEach { (id, pair) ->
				val (multiplier, msg) = pair
				enmityMessages += EnmityTriggerMessage(
					id = id,
					message = msg.miniMessage(),
					shouldTrigger = { opponent, config ->
						opponent.baseWeight >= (config.initialAggroThreshold * multiplier) && !opponent.aggroed
					}
				)
			}
			return this
		}

		fun addAggroMessage(idAndMsg: Pair<String, String>): Builder {
			val (id, msg) = idAndMsg
			enmityMessages += EnmityTriggerMessage(
				id = id,
				message = msg.miniMessage(),
				shouldTrigger = { opponent, _ -> opponent.aggroed }
			)
			return this
		}

		fun addFriendlyFireMessages(
			suspicion: Pair<String, String>,
			warning: Pair<String, String>,
			betrayal: Pair<String, String>
		): Builder {
			val (id1, msg1) = suspicion
			val (id2, msg2) = warning
			val (id3, msg3) = betrayal

			enmityMessages += EnmityTriggerMessage(id1, msg1.miniMessage(), triggeredByFriendlyFire)
			enmityMessages += EnmityTriggerMessage(id2, msg2.miniMessage(), escalatedFriendlyFire)
			enmityMessages += EnmityTriggerMessage(id3, msg3.miniMessage(), betrayalAggro)
			return this
		}



		fun build(): AIFaction {
			val faction = AIFaction(identifier, color, names, suffixes)

			factions += faction

			this@Builder.templateProcessing += {
				if (enmityMessages.isNotEmpty()) {
					addAdditionalModule(
						BehaviorConfiguration.EnmityMessageInformation(messagePrefix, enmityMessages)
					)
				}
			}

			faction.templateProcess = {
				this@Builder.templateProcessing.forEach { it(this) }
			}



			return faction
		}
	}

	companion object {
		val factions = mutableSetOf<AIFaction>()

		fun builder(identifier: String, color: Int): Builder = Builder(identifier, color)
		fun builder(identifier: String, color: String): Builder = Builder(identifier, Integer.parseInt(color.removePrefix("#"), 16))

		val WATCHERS = builder("WATCHERS", WATCHER_ACCENT.value())
			.addNames(0, listOf(
				"Dimidium Hivecraft", "Harriot Hivecraft", "Dagon Hivecraft", "Tadmor Hivecraft", "Hypatia Hivecraft",
				"Dulcinea Hivecraft", "Fortitudo Hivecraft", "Poltergeist Hivecraft", "Yvaga Hivecraft", "Naron Hivecraft",
				"Levantes Hivecraft", "Tylos Hivecraft"
			).map { it.toComponent(WATCHER_STANDARD) })
			.addNames(1, listOf(
				"Dimidium Swarm", "Harriot Swarm", "Dagon Swarm", "Tadmor Swarm","Hypatia Swarm", "Dulcinea Swarm", "Fortitudo Swarm",
				"Poltergeist Swarm", "Yvaga Swarm", "Naron Swarm", "Levantes Swarm", "Tylos Swarm"
			).map { it.toComponent(WATCHER_STANDARD) })
			.addNames(2, listOf(
				"Dimidium Cluster", "Harriot Cluster", "Dagon Cluster", "Tadmor Cluster", "Hypatia Cluster","Dulcinea Cluster",
				"Fortitudo Cluster", "Poltergeist Cluster", "Yvaga Cluster", "Naron Cluster", "Levantes Cluster", "Tylos Cluster"
			).map { it.toComponent(WATCHER_STANDARD) })
			.addNames(3, listOf(
				"Dimidium Nest", "Harriot Nest", "Dagon Nest", "Tadmor Nest", "Hypatia Nest", "Dulcinea Nest", "Fortitudo Nest",
				"Poltergeist Nest", "Yvaga Nest", "Naron Nest", "Levantes Nest", "Tylos Nest"
			).map { it.toComponent(WATCHER_STANDARD) })
			.addNames(4, listOf(
				"Dimidium Commune", "Dimidium Brood", "Harriot Commune", "Harriot Brood", "Dagon Commune", "Dagon Brood", "Tadmor Commune", "Tadmor Brood",
				"Hypatia Commune", "Hypatia Brood", "Dulcinea Commune", "Dulcinea Brood", "Fortitudo Commune", "Fortitudo Brood",
				"Poltergeist Commune", "Poltergeist Brood", "Yvaga Commune", "Yvaga Brood", "Naron Commune", "Naron Brood",
				"Levantes Commune", "Levantes Brood", "Tylos Commune", "Tylos Brood"
			).map { it.toComponent(WATCHER_STANDARD) })
			.setMessagePrefix("<${HEColorScheme.HE_MEDIUM_GRAY}>Receiving transmission from <$WATCHER_ACCENT>unknown</$WATCHER_ACCENT> vessel. <italic>Translating:</italic>")
			.addSmackMessages(
				"<$WATCHER_STANDARD>Intercepting hostile transmissions. Adapting swarm behavior to disrupt enemy communications.",
				"<$WATCHER_STANDARD>Evasive maneuvers engaged; navigating hostile terrain.",
				"<$WATCHER_STANDARD>Near-field barrier corroded under hostile fire. Re-routing aortal flow to priority organs.",
				"<$WATCHER_STANDARD>Deploying attack swarm.",
				"<$WATCHER_STANDARD>Hostile vessel subsystem lock-on confirmed. Firing.",
				"<$WATCHER_STANDARD>Combat pattern analysis transmitted to nearest Hive.",
				"<$WATCHER_STANDARD>Hostile vessel damaged.",
				"<$WATCHER_STANDARD>Hive directive received, switching designation: Hunter-Seeker.",
				"<$WATCHER_STANDARD>Releasing attack swarm.",
				"<$WATCHER_STANDARD>Attack vector plotted.",
				"<$WATCHER_STANDARD>Engaging defensive maneuvers.",
				"<$WATCHER_STANDARD>Re-routing aortal flow to drone locomotion systems."
			)
			.addEnmityMessages(
				"warn" to (0.1 to "<gray>..."),
				"threat" to (0.5 to "<$WATCHER_STANDARD>Unknown heat signature detected")
			)
			.addAggroMessage("aggro" to "<$WATCHER_STANDARD>Hostile vessel subsystem lock-on confirmed. Engaging.")
			.addFriendlyFireMessages(
				"scan" to "<gray>Cross-signature event logged.",
				"anomaly" to "<gray>Unit violating observation protocols.",
				"engage" to "<$WATCHER_STANDARD>Hostile signal confirmed. Locking subsystems."
			)
			.addDifficultySuffix(0,"✦")
			.addDifficultySuffix(1,"✦✦")
			.addDifficultySuffix(2,"👁️")
			.addDifficultySuffix(3,"👁️👁️")
			.addDifficultySuffix(4,"🌀")
			.build()

		val 吃饭人 = builder("吃饭人", 吃饭人_STANDARD.value())
			.addNames(0,
				text("✦飞行员✦", 吃饭人_STANDARD),
				text("✦面包✦", 吃饭人_STANDARD),
				text("✦蛋糕✦", 吃饭人_STANDARD),
				text("✦面条✦", 吃饭人_STANDARD),
				text("✦米饭✦", 吃饭人_STANDARD),
				text("✦土豆✦", 吃饭人_STANDARD),
				text("✦马铃薯✦", 吃饭人_STANDARD),
				text("✦薯叶✦", 吃饭人_STANDARD),
			)
			.addNames(1,
				text("✦✦飞行员✦✦", 吃饭人_STANDARD),
				text("✦✦面包✦✦", 吃饭人_STANDARD),
				text("✦✦蛋糕✦✦", 吃饭人_STANDARD),
				text("✦✦面条✦✦", 吃饭人_STANDARD),
				text("✦✦米饭✦✦", 吃饭人_STANDARD),
				text("✦✦土豆✦✦", 吃饭人_STANDARD),
				text("✦✦马铃薯✦✦", 吃饭人_STANDARD),
				text("✦✦薯叶✦✦", 吃饭人_STANDARD),
			)
			.addNames(2,
				text("✨飞行员✨", 吃饭人_STANDARD),
				text("✨面包✨", 吃饭人_STANDARD),
				text("✨蛋糕✨", 吃饭人_STANDARD),
				text("✨面条✨", 吃饭人_STANDARD),
				text("✨米饭✨", 吃饭人_STANDARD),
				text("✨土豆✨", 吃饭人_STANDARD),
				text("✨马铃薯✨", 吃饭人_STANDARD),
				text("✨薯叶✨", 吃饭人_STANDARD),
			)
			.addNames(3,
				text("✨✨飞行员✨✨", 吃饭人_STANDARD),
				text("✨✨面包✨✨", 吃饭人_STANDARD),
				text("✨✨蛋糕✨✨", 吃饭人_STANDARD),
				text("✨✨面条✨✨", 吃饭人_STANDARD),
				text("✨✨米饭✨✨", 吃饭人_STANDARD),
				text("✨✨土豆✨✨", 吃饭人_STANDARD),
				text("✨✨马铃薯✨✨", 吃饭人_STANDARD),
				text("✨✨薯叶✨✨", 吃饭人_STANDARD),
			)
			.addNames(4,
				text("\uD83C\uDF5E飞行员\uD83C\uDF5E", 吃饭人_STANDARD),
				text("\uD83C\uDF5E 面包\uD83C\uDF5E", 吃饭人_STANDARD),
				text("\uD83E\uDD50蛋糕\uD83E\uDD50", 吃饭人_STANDARD),
				text("\uD83E\uDD50面条\uD83E\uDD50", 吃饭人_STANDARD),
				text("\uD83E\uDD56米饭\uD83E\uDD56", 吃饭人_STANDARD),
				text("\uD83E\uDD56土豆\uD83E\uDD56", 吃饭人_STANDARD),
				text("\uD83E\uDD68马铃薯\uD83E\uDD68", 吃饭人_STANDARD),
				text("\uD83E\uDD68薯叶\uD83E\uDD68", 吃饭人_STANDARD),
			)
			.addDifficultySuffix(0,"✦")
			.addDifficultySuffix(1,"✦✦")
			.addDifficultySuffix(2,"🥖")
			.addDifficultySuffix(3,"🥖🥖")
			.addDifficultySuffix(4,"🥐")
			.build()

		val miningGuildMini = "<$MINING_CORP_LIGHT_ORANGE>Mining <$MINING_CORP_DARK_ORANGE>Guild"

		val MINING_GUILD = builder("MINING_GUILD", MINING_CORP_LIGHT_ORANGE.value())
			.setMessagePrefix("<${HEColorScheme.HE_MEDIUM_GRAY}>Receiving transmission from $miningGuildMini <${HEColorScheme.HE_MEDIUM_GRAY}>vessel")
			.addNames(0,
				text("Intern Nil Noralgratin", MINING_CORP_LIGHT_ORANGE),
				text("Intern Alpi Artion", MINING_CORP_LIGHT_ORANGE),
				text("Intern Sisko Sargred", MINING_CORP_LIGHT_ORANGE),
				text("Intern Heimo Hourrog", MINING_CORP_LIGHT_ORANGE),
				text("Intern Gann Grulgrorlim", MINING_CORP_LIGHT_ORANGE),
				text("Intern Lempi Lassnia", MINING_CORP_LIGHT_ORANGE),
				text("Intern Sighebyrn Strenkann", MINING_CORP_LIGHT_ORANGE),
				text("Intern Rik Rihre", MINING_CORP_LIGHT_ORANGE),
				text("Intern Alanury Addar", MINING_CORP_LIGHT_ORANGE),
				text("Intern Kyllikki Kukock", MINING_CORP_LIGHT_ORANGE),
				text("Intern Sighebyrn Strenkann", MINING_CORP_LIGHT_ORANGE)
			)
			.addNames(1,
				text("Employee Nil Noralgratin", MINING_CORP_LIGHT_ORANGE),
				text("Employee Alpi Artion", MINING_CORP_LIGHT_ORANGE),
				text("Employee Sisko Sargred", MINING_CORP_LIGHT_ORANGE),
				text("Employee Heimo Hourrog", MINING_CORP_LIGHT_ORANGE),
				text("Employee Gann Grulgrorlim", MINING_CORP_LIGHT_ORANGE),
				text("Employee Lempi Lassnia", MINING_CORP_LIGHT_ORANGE),
				text("Employee Sighebyrn Strenkann", MINING_CORP_LIGHT_ORANGE),
				text("Employee Rik Rihre", MINING_CORP_LIGHT_ORANGE),
				text("Employee Alanury Addar", MINING_CORP_LIGHT_ORANGE),
				text("Employee Kyllikki Kukock", MINING_CORP_LIGHT_ORANGE),
				text("Employee Sighebyrn Strenkann", MINING_CORP_LIGHT_ORANGE)
			)
			.addNames(2,
				text("Manager Nil Noralgratin", MINING_CORP_LIGHT_ORANGE),
				text("Manager Alpi Artion", MINING_CORP_LIGHT_ORANGE),
				text("Manager Sisko Sargred", MINING_CORP_LIGHT_ORANGE),
				text("Manager Heimo Hourrog", MINING_CORP_LIGHT_ORANGE),
				text("Manager Gann Grulgrorlim", MINING_CORP_LIGHT_ORANGE),
				text("Manager Lempi Lassnia", MINING_CORP_LIGHT_ORANGE),
				text("Manager Sighebyrn Strenkann", MINING_CORP_LIGHT_ORANGE),
				text("Manager Rik Rihre", MINING_CORP_LIGHT_ORANGE),
				text("Manager Alanury Addar", MINING_CORP_LIGHT_ORANGE),
				text("Manager Kyllikki Kukock", MINING_CORP_LIGHT_ORANGE),
				text("Manager Sighebyrn Strenkann", MINING_CORP_LIGHT_ORANGE)
			)
			.addNames(3,
				text("Director Nil Noralgratin", MINING_CORP_LIGHT_ORANGE),
				text("Director Alpi Artion", MINING_CORP_LIGHT_ORANGE),
				text("Director Sisko Sargred", MINING_CORP_LIGHT_ORANGE),
				text("Director Heimo Hourrog", MINING_CORP_LIGHT_ORANGE),
				text("Director Gann Grulgrorlim", MINING_CORP_LIGHT_ORANGE),
				text("Director Lempi Lassnia", MINING_CORP_LIGHT_ORANGE),
				text("Director Sighebyrn Strenkann", MINING_CORP_LIGHT_ORANGE),
				text("Director Rik Rihre", MINING_CORP_LIGHT_ORANGE),
				text("Director Alanury Addar", MINING_CORP_LIGHT_ORANGE),
				text("Director Kyllikki Kukock", MINING_CORP_LIGHT_ORANGE),
				text("Director Sighebyrn Strenkann", MINING_CORP_LIGHT_ORANGE)
			)
			.addNames(4,
				text("Executive Nil Noralgratin", MINING_CORP_LIGHT_ORANGE),
				text("Executive Alpi Artion", MINING_CORP_LIGHT_ORANGE),
				text("Executive Sisko Sargred", MINING_CORP_LIGHT_ORANGE),
				text("Executive Heimo Hourrog", MINING_CORP_LIGHT_ORANGE),
				text("Executive Gann Grulgrorlim", MINING_CORP_LIGHT_ORANGE),
				text("Executive Lempi Lassnia", MINING_CORP_LIGHT_ORANGE),
				text("Executive Sighebyrn Strenkann", MINING_CORP_LIGHT_ORANGE),
				text("Executive Rik Rihre", MINING_CORP_LIGHT_ORANGE),
				text("Executive Alanury Addar", MINING_CORP_LIGHT_ORANGE),
				text("Executive Kyllikki Kukock", MINING_CORP_LIGHT_ORANGE),
				text("Executive Sighebyrn Strenkann", MINING_CORP_LIGHT_ORANGE)
			)
			.addEnmityMessages(
				"notice" to (0.1 to "<gray>Unregistered vessel detected near Guild claim."),
				"warn" to (0.5 to "<#FFA500>Warning: You are trespassing on Mining Guild property.")
			)
			.addAggroMessage("aggro" to "<red>Defense protocols active. You will be removed.")
			.addFriendlyFireMessages(
				"suspect" to "<gray>Watch where your aiming! We have expensive equipment.",
				"warn" to "<#FFA500>Manager is gonna tear me a new one if you keep this up.",
				"betrayal" to "<red>That's it! For the trouble im selling your ship for scrap!"
			)
			.addDifficultySuffix(0,"✦")
			.addDifficultySuffix(1,"✦✦")
			.addDifficultySuffix(2,"⛏️")
			.addDifficultySuffix(3,"⛏️⛏️")
			.addDifficultySuffix(4,"💰")
			.build()

		val PERSEUS_EXPLORERS = builder("PERSEUS_EXPLORERS", EXPLORER_LIGHT_CYAN.value())
			.setMessagePrefix("<$EXPLORER_LIGHT_CYAN>Receiving transmission from civilian vessel")
			.addNames(0,
				"<$EXPLORER_LIGHT_CYAN>Rookie <$EXPLORER_MEDIUM_CYAN>Pilot".miniMessage(),
				"<$EXPLORER_LIGHT_CYAN>Novice <$EXPLORER_MEDIUM_CYAN>Pilot".miniMessage(),
				"<$EXPLORER_LIGHT_CYAN>New Explorer".miniMessage(),
				"<$EXPLORER_LIGHT_CYAN>Rookie Captain".miniMessage(),
			)
			.addNames(1,
				"<$EXPLORER_LIGHT_CYAN>Regular <$EXPLORER_MEDIUM_CYAN>Pilot".miniMessage(),
				"<$EXPLORER_LIGHT_CYAN>Trained <$EXPLORER_MEDIUM_CYAN>Pilot".miniMessage(),
				"<$EXPLORER_LIGHT_CYAN>Seasoned Explorer".miniMessage(),
				"<$EXPLORER_LIGHT_CYAN>Regular Captain".miniMessage(),
			)
			.addNames(2,
				"<$EXPLORER_LIGHT_CYAN>Veteran <$EXPLORER_MEDIUM_CYAN>Pilot".miniMessage(),
				"<$EXPLORER_LIGHT_CYAN>Keen <$EXPLORER_MEDIUM_CYAN>Pilot".miniMessage(),
				"<$EXPLORER_LIGHT_CYAN>Master Explorer".miniMessage(),
				"<$EXPLORER_LIGHT_CYAN>Veteran Captain".miniMessage(),
			)
			.addNames(3,
				"<$EXPLORER_LIGHT_CYAN>Senior Veteran <$EXPLORER_MEDIUM_CYAN>Pilot".miniMessage(),
				"<$EXPLORER_LIGHT_CYAN>Eagle Eye <$EXPLORER_MEDIUM_CYAN>Pilot".miniMessage(),
				"<$EXPLORER_LIGHT_CYAN>Epic Explorer".miniMessage(),
				"<$EXPLORER_LIGHT_CYAN>Senior Veteran Captain".miniMessage(),
			)
			.addNames(4,
				"<$EXPLORER_LIGHT_CYAN>Legendary <$EXPLORER_MEDIUM_CYAN>Pilot".miniMessage(),
				"<$EXPLORER_LIGHT_CYAN>Super Eye <$EXPLORER_MEDIUM_CYAN>Pilot".miniMessage(),
				"<$EXPLORER_LIGHT_CYAN>Trailblazer".miniMessage(),
				"<$EXPLORER_LIGHT_CYAN>Ultimate Veteran Captain".miniMessage(),
			)
			.addFriendlyFireMessages(
				"suspect" to "<gray>You okay buddy?",
				"warn" to "<#FFA500>Hey stop that",
				"betrayal" to "<red>Dammit, I knew I shouldn't have trusted you "
			)
			.addSmackMessages(
				"<white>Please no, I've done nothing wrong!",
				"<white>Spare me; this ship is all I have!",
				"<white>My friends will avenge me!",
				"<white>I'm calling the [current system name] Defense Patrol! ",
				"<white>Mayday, mayday, going down!",
				"<white>Shields are down!",
				"<white>Hull integrity critical!",
				"<white>Engines compromised!"
			)
			.addDifficultySuffix(0,"✦")
			.addDifficultySuffix(1,"✦✦")
			.addDifficultySuffix(2,"🪶")
			.addDifficultySuffix(3,"🪶🪶")
			.addDifficultySuffix(4,"🌍")
			.build()

		val SYSTEM_DEFENSE_FORCES = builder("SYSTEM_DEFENSE_FORCES", PRIVATEER_LIGHT_TEAL.value())
			.setMessagePrefix("<${HEColorScheme.HE_MEDIUM_GRAY}>Receiving transmission from <$PRIVATEER_LIGHT_TEAL>privateer</$PRIVATEER_LIGHT_TEAL> vessel")
			.addNames(0,
				"<$PRIVATEER_MEDIUM_TEAL>System Defense <$PRIVATEER_LIGHT_TEAL>Rookie Sanders".miniMessage(),
				"<$PRIVATEER_MEDIUM_TEAL>System Defense <$PRIVATEER_LIGHT_TEAL>Trainee Fed".miniMessage(),
				"<$PRIVATEER_MEDIUM_TEAL>System Defense <$PRIVATEER_LIGHT_TEAL>Rookie Smith".miniMessage(),
				"<$PRIVATEER_MEDIUM_TEAL>System Defense <$PRIVATEER_LIGHT_TEAL>Trainee Cop".miniMessage(),
			)
			.addNames(1,
				"<$PRIVATEER_MEDIUM_TEAL>System Defense <$PRIVATEER_LIGHT_TEAL>Pilot Wesley".miniMessage(),
				"<$PRIVATEER_MEDIUM_TEAL>System Defense <$PRIVATEER_LIGHT_TEAL>Private John".miniMessage(),
				"<$PRIVATEER_MEDIUM_TEAL>System Defense <$PRIVATEER_LIGHT_TEAL>Pilot Hale".miniMessage(),
				"<$PRIVATEER_MEDIUM_TEAL>System Defense <$PRIVATEER_LIGHT_TEAL>Private Haren".miniMessage(),
			)
			.addNames(2,
				"<$PRIVATEER_MEDIUM_TEAL>System Defense <$PRIVATEER_LIGHT_TEAL>Veteran Cotte".miniMessage(),
				"<$PRIVATEER_MEDIUM_TEAL>System Defense <$PRIVATEER_LIGHT_TEAL>Ace Russon".miniMessage(),
				"<$PRIVATEER_MEDIUM_TEAL>System Defense <$PRIVATEER_LIGHT_TEAL>Veteran Paine".miniMessage(),
				"<$PRIVATEER_MEDIUM_TEAL>System Defense <$PRIVATEER_LIGHT_TEAL>Ace Wilsimm".miniMessage(),
			)
			.addNames(3,
				"<$PRIVATEER_MEDIUM_TEAL>System Defense <$PRIVATEER_LIGHT_TEAL>Master Cotte".miniMessage(),
				"<$PRIVATEER_MEDIUM_TEAL>System Defense <$PRIVATEER_LIGHT_TEAL>Super Ace Russon".miniMessage(),
				"<$PRIVATEER_MEDIUM_TEAL>System Defense <$PRIVATEER_LIGHT_TEAL>Master Paine".miniMessage(),
				"<$PRIVATEER_MEDIUM_TEAL>System Defense <$PRIVATEER_LIGHT_TEAL>Super Ace Wilsimm".miniMessage(),
			)
			.addNames(4,
				"<$PRIVATEER_MEDIUM_TEAL>System Defense <$PRIVATEER_LIGHT_TEAL>Legendary Cotte".miniMessage(),
				"<$PRIVATEER_MEDIUM_TEAL>System Defense <$PRIVATEER_LIGHT_TEAL>Hero Russon".miniMessage(),
				"<$PRIVATEER_MEDIUM_TEAL>System Defense <$PRIVATEER_LIGHT_TEAL>Legendary Paine".miniMessage(),
				"<$PRIVATEER_MEDIUM_TEAL>System Defense <$PRIVATEER_LIGHT_TEAL>Hero Wilsimm".miniMessage(),
			)
			.addEnmityMessages(
				"notice" to (0.1 to "<gray>Monitoring unidentified contact."),
				"warn" to (0.5 to "<#FFA500>You are entering restricted airspace. Turn back immediately.")
			)
			.addAggroMessage("aggro" to "<red>You have violated restricted airspace. Weapons lock in progress.")
			.addFriendlyFireMessages(
				"suspect" to "<gray>Watch your fire, pilot.",
				"warn" to "<#FFA500>That last hit wasn't an accident.",
				"betrayal" to "<red>You’ve turned on your squadron. Consider yourself expelled."
			)
			.addSmackMessages(
				"<white>Stand down, we have you outmatched!",
				"<white>Once I breach your shields, there's no going back.",
				"<white>Ha, you call those weapons?",
				"<white>System command, hostile contact is taking severe shield damage.",
				"<white>Flanking right!",
				"<white>Flanking left!"
			)
			.addDifficultySuffix(0,"✦")
			.addDifficultySuffix(1,"✦✦")
			.addDifficultySuffix(2,"🛡️")
			.addDifficultySuffix(3,"🛡️🛡️")
			.addDifficultySuffix(4,"♠️")
			.build()

		val TSAII_RAIDERS = builder("TSAII_RAIDERS", TSAII_MEDIUM_ORANGE.value())
			.addNames(0,
				text("pup Dhagdagar", TSAII_DARK_ORANGE),
				text("wimp Zazgrord", TSAII_DARK_ORANGE),
				text("stooge Furriebruh", TSAII_DARK_ORANGE),
			)
			.addNames(1,
				text("pup Dhagdagar", TSAII_DARK_ORANGE),
				text("wimp Zazgrord", TSAII_DARK_ORANGE),
				text("stooge Furriebruh", TSAII_DARK_ORANGE),
			)
			.addNames(2,
				text("Hrorgrum", TSAII_DARK_ORANGE),
				text("Rabidstompa", TSAII_DARK_ORANGE),
				text("Godcooka", TSAII_DARK_ORANGE),
				text("Skarcrushah", TSAII_DARK_ORANGE),
			)
			.addNames(3,
				text("Hrorgrum", TSAII_DARK_ORANGE),
				text("Rabidstompa", TSAII_DARK_ORANGE),
				text("Godcooka", TSAII_DARK_ORANGE),
				text("Skarcrushah", TSAII_DARK_ORANGE),
			)
			.addNames(4,
				text("Big Bozz",TSAII_DARK_ORANGE, TextDecoration.BOLD),
				text("Rizz Master",TSAII_DARK_ORANGE, TextDecoration.BOLD),
				text("GOATaider",TSAII_DARK_ORANGE, TextDecoration.BOLD),
			)
			.addSmackMessages(
				"I'll leave nothing but scrap",
				"I'll cut you to bacon",
				"When I'm done with you, I'll mantle your skull!"
			)
			.addEnmityMessages(
				"notice" to (0.1 to "<gray>Heh. What's this then?"),
				"warn" to (0.4 to "<#FFA500>Is the prey approaching the hunter?"),
			)
			.addAggroMessage("aggro" to "<red>You're my dinner!")
			.addFriendlyFireMessages(
				"laugh" to "<gray>Someone is fiesty today.",
				"growl" to "<#FFA500>Better shut it before the beat down.",
				"rage" to "<red>Thanks for the knife backstabber!"
			)
			.addDifficultySuffix(0,"✦")
			.addDifficultySuffix(1,"✦✦")
			.addDifficultySuffix(2,"🐷")
			.addDifficultySuffix(3,"🐷🐷")
			.addDifficultySuffix(4,"😈")
			.build()

		private val pirateNames = listOf(
			"Lord Monty", "Kaptin Jakk", "Mr. D", "Fugitive 862", "Vex", "Dapper Dan", "Dan \"The Man\"", "Vekel \"The Man\"", "Link \"Invincible\" Burton", "\"Fearless\" Dave", "The Reaper", "\"Golden-Eye\" Sue Withers", "Fat Fredd", "Greasy Jill", "The Toof Fari", "King Crabbe", "Redcap Reid", "Bloodbeard", "Long Johnson", "\"Ripper\" Jack",
			"Big Boris", "Styles Blackmane", "Lil' Tim", "\"Grandpa\" Marty", "Eric The Slayer", "\"Big Brain\" Simmons", "\"Salty\" Swailes", "Eclipse", "Mistress Celina", "Mistress Vera", "Hubert \"Moneybags\" McGee", "Huntly \"Hunter\" Whittaker", "Red Deth", "\"Shady\" Bill Williams", "Oswald One-Eye", "Lil' Peter", "Swordfingers", "Screwhead", "Evelynn \"The Evil\" Myers", "Bearclaw",
			"Capn' Stefan", "Fugitive 604", "Filthy Frank", "Billy \"The Kid\" Smith", "Russel \"The Boar\" Pert", "Bearclaw Bill", "Wesley \"The Crusher\"", "Jean \"Picker\" Ardluc", "Gru \"The Redeemer\"", "Smelly Schneider", "Little Lilly", "Little Mouse", "Master O. O. Gwae", "Derek \"Pyro\" Martin", "The Headtaker", "Mr. BoomBoom", "Big Harold", "Malinda \"The Hawk\" Carlyle", "Cameron \"Cougar\" Embre", "\"Princess\" Libby Hayley",
			"Mitch \"Turtle\" Black", "Harrison \"The Executioner\"", "King Jakka", "Fugitive 241", "Fugitive 667", "Seth \"Crazy Hands\" Hartwell", "Selina \"Panther\" Black", "\"Shady\" Sophia Turner", "Sherman The Mad", "Beebe \"The Bear\" Barton", "Annette \"The Unseen\" Fyr", "Lord Far Quaad", "Harold \"The Thresher\"", "Whitman \"The Bull\" Clemons", "Fugitive 404", "Radley \"Stardog\" Arlin", "Grandma Lucille", "The Rizzler", "Jerry \"Killer\" Clarkson", "Big Gus",
			"Mama Lia", "Lady Antonia Tack", "Captain Vor", "The Khan", "Lucky Larry", "Bruisin' Betty", "Ugluk \"Maneater\" Johnson", "Happy Hayden", "Man-Ray", "The Shadow", "Powell \"Iron-Belly\" Chatham", "Enigma", "The Dragon", "Kader \"Wolf\" Gray", "Big Hands", "Nightowl", "Killjoy", "Sapphire", "Rabid Randy", "Echo",
			"Stanton Derane", "Stanton Smithers", "Ulrus", "Reid Sladek", "Denyse Cadler", "Hrongar Meton", "Trent Jamesson", "Toma Nombur", "Doni Drew", "Heinrich Wickel", "Vilhelm Lindon", "Tamir Mashon", "Malon Redal", "Alvar Julen", "Ember Camus", "Keyon Coombs", "Bailey Zain", "Carmen Reeves", "Little Fingers", "Lydia Lester",
			"Aurora Salvadore", "Eva Longia", "Nia Payne", "Elvera Jett", "Claxton Hale", "Larsa Merton", "Xander Sheffield", "Amber Fark", "Radley Wright", "Lynley Paine", "Micah Caldera", "Garrison DuCote", "Urien Ralers", "Seth Vangelos", "Lucy Loretta IV", "Sam Gueniverre", "Meg", "Honda Ohna", "Harri Mudd", "Kreef Garga",
			"Django Bett", "Emeri Jas", "Gendar", "Mebo Teeja", "Wam Zesek", "Bad Cane", "Pumi Raramita", "Wade Weiss", "Adam Sander", "Zayn Foster", "Enir Boreh", "Bristol Fleming", "Sadyhe Wahl", "Ben Dover", "Foba Bett", "Blanche Darkwalker", "Rosella Daniesh", "Rosalia Daniesh", "Rosilla Daniesh", "Ham Swolo",
			"Luk Star-Runner", "Brock Hayes", "Studs Shearman", "Sham Corrend", "Varlo Daraay", "Deng Pelles", "Luca Dara", "Lon Avand", "Grego Grenko", "Leys Kilis", "Tonor Donnall", "Jaa Kiles", "Guy Fawkes", "Nica Rezal", "Juda Grossand", "Mildra Scolly", "Bine Theson", "Renda Leson", "Mildra Wardson", "Arbann Clore",
			"Mara Hilly", "Jacquel Pere", "Loise Kinson", "Jerry Homart", "Keithy Hompson", "Enner Nera", "Joshua Manett", "Jonio Reson", "Billie Colley", "Jesse Hayeson", "Pauly Hardson", "Arlon Scarte", "Johne Guezal", "Willy Hernett", "Sara Ancim", "Magent Tille", "Amabe Tille", "Rana Avik", "Aitan Corrik", "Tala Haren",
			"Lysa Nalle", "Vital Kilian", "Jaina Harik", "Jet Severt", "Warrick Burcham", "Preston Jammer", "Arik Llewellyn", "Glen Lockley", "Damien Hyland", "Thaddeus Engstrom", "Darius Calder", "Fae Helsing", "Elsy Carrick", "Ariana Rackham", "Fae Arleth", "Joie Jann", "Kerilyn Woldt", "Gwen Vangelos", "Morgana Stasny", "Maia Morgan",
			"Allyson Byrn", "Kadi Kovane", "Antid Buchkina", "Vlukar Zadenko", "Zori Atyev", "Redi Kinova", "Alen Kinova", "Adil Sova", "Lana Igomov", "Unarya Bova", "Valaya Serova", "Vilma Khoteva", "Aleno Aponov", "Leva Montova", "Amila Grenau", "Jysell Halcyon", "Jaina Antis", "Vital Baize", "Sanne Korraay", "Mara Kale",
			"Elabe Enkows", "Ierran Haren", "Lysa Prenda", "Myla Ajinn", "Cadan Keggle", "Hoola Bane", "Caden Vamma", "Elar Stazi", "Hoola Madak", "Lana Trehalt", "Linor Pragant", "Garm Thalcorr", "Kuna Vene", "Val Hamne", "Hugo Minne", "Maro Kesyk", "Garm Horne", "Jaa Harand", "Jafan Dolphe", "Jery Reson",
			"Anier Wilsimm", "Jone Jackson", "Phily Hingte", "Jeffry Rezal", "Rianio Cotte", "Russe Russon", "Alteth Homes", "Donna Coopow", "Chera Hayeson", "Jase Hilly", "Kara Tinels", "Fryna Coxand", "Ricy Henders", "Nety Hernand", "Rachia Russon", "Amen Bertson", "Masa Take", "Utan Moro", "Nishi Yosun", "Inon Boro",
			"Ekoh Hideo", "Yakan Miko", "Sumi Tomi", "Matsu Chiko", "Natse Kuko", "Akuk Yuikoshi", "Kino Nami", "Mota Euikoki", "Hira Machi", "Kano Niko", "Kawa Sako", "Kagi Chito", "Kynon Graydon", "Xeno Severt", "Yukon Centrich", "Galen Lockley", "Nicol Jaenke", "Bjorn Wynn", "Garrison Burcham", "Zebulon Leath"
		)
		val PIRATES = builder("PIRATES", PIRATE_SATURATED_RED.value())
			.setMessagePrefix("<$PIRATE_SATURATED_RED>Receiving transmission from pirate vessel")
			.addNames(0, pirateNames.map { ("Rowdy "+it).toComponent(PIRATE_LIGHT_RED) })
			.addNames(0, pirateNames.map { ("Deliquent "+it).toComponent(PIRATE_LIGHT_RED) })
			.addNames(1, pirateNames.map { ("Wanted "+it).toComponent(PIRATE_LIGHT_RED) })
			.addNames(1, pirateNames.map { ("Criminal "+it).toComponent(PIRATE_LIGHT_RED) })
			.addNames(2, pirateNames.map { ("Capo "+it).toComponent(PIRATE_LIGHT_RED) })
			.addNames(2, pirateNames.map { ("Vicious "+it).toComponent(PIRATE_LIGHT_RED) })
			.addNames(3, pirateNames.map { ("Cutthorat "+it).toComponent(PIRATE_LIGHT_RED) })
			.addNames(3, pirateNames.map { ("Devil "+it).toComponent(PIRATE_LIGHT_RED) })
			.addNames(4, pirateNames.map { ("Calamity "+it).toComponent(PIRATE_LIGHT_RED) })
			.addNames(4, pirateNames.map { ("Woe "+it).toComponent(PIRATE_LIGHT_RED) })
			.addSmackMessages(
				"Nice day, Nice Ship. I think ill take it!",
				"I'll plunder your booty!",
				"Scram or we'll blow you to pieces!",
				"Someones too curious for their own good.",
				"Don't say I didn't warn ya, mate."
			)
			.addEnmityMessages(
				"notice" to (0.1 to "<gray>They're watching us..."),
				"warn" to (0.4 to "<#FFA500>Back off!")
			)
			.addAggroMessage("aggro" to "<RED>I'll make an example of you!")
			.addFriendlyFireMessages(
				"laugh" to "<gray>Oi! That better be a mistake.",
				"growl" to "<#FFA500>You're asking for it.",
				"rage" to "<red>You just bought yourself a death warrant!"
			)
			.addDifficultySuffix(0,"✦")
			.addDifficultySuffix(1,"✦✦")
			.addDifficultySuffix(2,"🔥")
			.addDifficultySuffix(3,"🔥🔥")
			.addDifficultySuffix(4,"💥")
			.build()

		val ABYSSAL = builder("ABYSALL", ABYSSAL_LIGHT_RED.value())
			.addNames( 0, listOf("Spectre", "Nebuchadnezzar").map { it.toComponent(ABYSSAL_DARK_RED) })
			.addNames( 1, listOf("Balthazar", "Salmanazar").map { it.toComponent(ABYSSAL_DARK_RED) })
			.addNames( 2, listOf("Jeroboam", "The Pale One").map { it.toComponent(ABYSSAL_DARK_RED) })
			.addNames( 3, listOf("Silent Screm", "Final Woe").map { it.toComponent(ABYSSAL_DARK_RED) })
			.addNames( 4, listOf("Lucifer", "The Last Dusk").map { it.toComponent(ABYSSAL_DARK_RED) })
			.setMessagePrefix("")
			.addSmackMessages(
				"<$ABYSSAL_DESATURATED_RED>Why do you hide your bones?",
				"<$ABYSSAL_DESATURATED_RED>The sand and the bones.",
				"<$ABYSSAL_DESATURATED_RED>Oh you fool.",
				"<$ABYSSAL_DESATURATED_RED>Do you see the wings?",
				"<$ABYSSAL_DESATURATED_RED>Only in darkness does the flower take hold.",
				"<$ABYSSAL_DESATURATED_RED>Your fear is branded deep.",
				"<$ABYSSAL_DESATURATED_RED>Its a shame.",
				"<$ABYSSAL_DESATURATED_RED>We are sorry.",
				"<$ABYSSAL_DESATURATED_RED>Under a starless sky.",
				"<$ABYSSAL_DESATURATED_RED>Godspeed.",
				"<$ABYSSAL_DESATURATED_RED>Do you know your worth?.",
				"<$ABYSSAL_DESATURATED_RED>Purposeless.",
				"<$ABYSSAL_DESATURATED_RED>Do you know what's really out there?.",
			)
			.addDifficultySuffix(0,"✦")
			.addDifficultySuffix(1,"✦✦")
			.addDifficultySuffix(2,"🖤")
			.addDifficultySuffix(3,"🖤🖤")
			.addDifficultySuffix(4,"🕳️")
			.build()

		val PUMPKINS = builder("PUMPKINS", TextColor.fromHexString("#FFA500")!!.value())
			.addNames(0,listOf("Kin!", "Matriarch!").map { it.toComponent(TextColor.fromHexString("#FFA500")!!) })
			.addNames(1,listOf("Kin!!", "Matriarch!!").map { it.toComponent(TextColor.fromHexString("#FFA500")!!) })
			.addNames(2,listOf("Kin!!!", "Matriarch!!!").map { it.toComponent(TextColor.fromHexString("#FFA500")!!) })
			.addNames(3,listOf("Kin!!!!", "Matriarch!!!!").map { it.toComponent(TextColor.fromHexString("#FFA500")!!) })
			.addNames(4,listOf("Kin!!!!!", "Matriarch!!!!!").map { it.toComponent(TextColor.fromHexString("#FFA500")!!) })
			.setMessagePrefix("<#FFA500>OY! Hey!")
			.addDifficultySuffix(0,"✦")
			.addDifficultySuffix(1,"✦✦")
			.addDifficultySuffix(2,"🎃")
			.addDifficultySuffix(3,"🎃🎃")
			.addDifficultySuffix(4,"🎃🎃🎃")
			.build()

		val SKELETONS = builder("SKELETONS", DARK_RED.value())
			.addNames(0,listOf("Lost Soul").map { it.toComponent(DARK_RED) })
			.addNames(1,listOf("Hungry Ghoul").map { it.toComponent(DARK_RED) })
			.addNames(2,listOf("Frenzied Wreath").map { it.toComponent(DARK_RED) })
			.addNames(3,listOf("Death").map { it.toComponent(DARK_RED) })
			.addNames(4,listOf("Old Bones").map { it.toComponent(DARK_RED) })
			.setMessagePrefix("")
			.addSmackMessages(
				"YOU WILL SOON JOIN THE DEAD, MORTAL!",
				"FEAR THE COMING OF THE DEADNOUGHT!",
				"COWER IN FEAR, MEATSACK!",
				"{0}, PREPARE TO MEET YOUR DOOM!",
				"TODAY IS A GOOD DAY FOR YOU TO DIE!",
				"YOUR BONES WILL BE OURS!",
				"HE HE HE HAW!",
				"MWA HA HA HA!",
				"FOOLISH MORTAL!",
				"YOU CANNOT KILL THE DEAD!",
				"<i>ominous rattling",
				"BURN, MORTAL!",
				"<i>incomprehensible gibberish"
			)
			.addDifficultySuffix(0,"✦")
			.addDifficultySuffix(1,"✦✦")
			.addDifficultySuffix(2,"💀")
			.addDifficultySuffix(3,"💀💀")
			.addDifficultySuffix(4,"☠️")
			.build()
	}
}
