package net.horizonsend.ion.server.features.ai.faction

import net.horizonsend.ion.common.utils.text.colors.HEColorScheme
import net.horizonsend.ion.common.utils.text.miniMessage
import net.horizonsend.ion.common.utils.text.ofChildren
import net.horizonsend.ion.common.utils.text.toComponent
import net.horizonsend.ion.server.features.ai.module.misc.FactionManagerModule
import net.horizonsend.ion.server.features.ai.spawning.AISpawningManager.allAIStarships
import net.horizonsend.ion.server.features.ai.starship.AITemplateRegistry
import net.horizonsend.ion.server.features.ai.starship.BehaviorConfiguration
import net.horizonsend.ion.server.features.starship.active.ActiveStarship
import net.horizonsend.ion.server.features.starship.control.controllers.ai.AIController
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.format.TextColor
import net.kyori.adventure.text.format.TextDecoration

/**
 * Responsible for providing ships, colors, and managing names of AI ships
 **/
class AIFaction private constructor(
	val identifier: String,
	val color: Int = Integer.parseInt("ff0000", 16),
	private val nameList: List<Component>
) {
	private var templateProcess: AITemplateRegistry.Builder.() -> Unit = {}

	fun getNameList() = nameList

	fun getFactionStarships(): List<ActiveStarship> = allAIStarships().filter { ship ->
		val factionManager = (ship.controller as AIController).modules["faction"] as? FactionManagerModule ?: return@filter false
		return@filter factionManager.faction == this
	}

	fun getAvailableName(): Component {
		return nameList.shuffled().firstOrNull { name ->
			getFactionStarships().none { (it.controller as AIController).getPilotName() == name }
		} ?: nameList.random()
	}

	fun processTemplate(template: AITemplateRegistry.Builder) {
		templateProcess(template)
	}

	class Builder(private val identifier: String, val color: Int) {
		private val names: MutableList<Component> = mutableListOf()

		private val templateProcessing: MutableList<AITemplateRegistry.Builder.() -> Unit> = mutableListOf()

		/**
		 * Prefix used for smack talk and radius messages
		 **/
		private var	messagePrefix = "<${HEColorScheme.HE_MEDIUM_GRAY}>Receiving transmission from <${TextColor.color(color)}>unknown</${TextColor.color(color)}> vessel."

		fun setMessagePrefix(new: String): Builder {
			messagePrefix = new
			return this
		}

		fun addName(name: Component): Builder {
			names += name
			return this
		}

		fun addNames(vararg names: Component): Builder {
			this.names += names
			return this
		}

		fun addNames(names: Collection<Component>): Builder {
			this.names += names
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

		fun build(): AIFaction {
			val faction = AIFaction(identifier, color, names)

			factions += faction

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

		val WATCHER_STANDARD = TextColor.fromHexString("#6DCCA3")!!
		val WATCHER_ACCENT = TextColor.fromHexString("#16A124")!!

		val WATCHERS = builder("WATCHERS", WATCHER_ACCENT.value())
			.addNames(listOf(
				"Dimidium Hivecraft", "Dimidium Swarm", "Dimidium Nest", "Dimidium Cluster", "Dimidium Commune", "Dimidium Brood", "Harriot Hivecraft", "Harriot Swarm", "Harriot Nest", "Harriot Cluster", "Harriot Commune", "Harriot Brood", "Dagon Hivecraft", "Dagon Swarm", "Dagon Nest", "Dagon Cluster", "Dagon Commune", "Dagon Brood", "Tadmor Hivecraft", "Tadmor Swarm",
				"Tadmor Nest", "Tadmor Cluster", "Tadmor Commune", "Tadmor Brood", "Hypatia Hivecraft", "Hypatia Swarm", "Hypatia Nest", "Hypatia Cluster", "Hypatia Commune", "Hypatia Brood", "Dulcinea Hivecraft", "Dulcinea Swarm", "Dulcinea Nest", "Dulcinea Cluster", "Dulcinea Commune", "Dulcinea Brood", "Fortitudo Hivecraft", "Fortitudo Swarm", "Fortitudo Nest", "Fortitudo Cluster",
				"Fortitudo Commune", "Fortitudo Brood", "Poltergeist Hivecraft", "Poltergeist Swarm", "Poltergeist Nest", "Poltergeist Cluster", "Poltergeist Commune", "Poltergeist Brood", "Yvaga Hivecraft", "Yvaga Swarm", "Yvaga Nest", "Yvaga Cluster", "Yvaga Commune", "Yvaga Brood", "Naron Hivecraft", "Naron Swarm", "Naron Nest", "Naron Cluster", "Naron Commune", "Naron Brood",
				"Levantes Hivecraft", "Levantes Swarm", "Levantes Nest", "Levantes Cluster", "Levantes Commune", "Levantes Brood", "Tylos Hivecraft", "Tylos Swarm", "Tylos Nest", "Tylos Cluster", "Tylos Commune", "Tylos Brood"
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
			.addRadiusMessages(2500.0 to "<$WATCHER_STANDARD>Hostile vessel subsystem lock-on confirmed. Engaging.")
			.build()

		val 吃饭人_STANDARD = TextColor.fromHexString("#FFC11A")!!

		val 吃饭人 = builder("吃饭人", 吃饭人_STANDARD.value())
			.addNames(
				text("飞行员", 吃饭人_STANDARD),
				text("面包", 吃饭人_STANDARD),
				text("蛋糕", 吃饭人_STANDARD),
				text("面条", 吃饭人_STANDARD),
				text("米饭", 吃饭人_STANDARD),
				text("土豆", 吃饭人_STANDARD),
				text("马铃薯", 吃饭人_STANDARD),
				text("薯叶", 吃饭人_STANDARD),
			)
			.build()

		val MINING_CORP_LIGHT_ORANGE = HEColorScheme.HE_LIGHT_ORANGE
		val MINING_CORP_DARK_ORANGE = TextColor.fromHexString("#D98507")!!

		val miningGuild = ofChildren(text("Mining ", MINING_CORP_LIGHT_ORANGE), text("Guild", MINING_CORP_DARK_ORANGE))
		val miningGuildMini = "<$MINING_CORP_LIGHT_ORANGE>Mining <$MINING_CORP_DARK_ORANGE>Guild"

		val reinforcementMiniMessage = "$miningGuildMini <${HEColorScheme.HE_MEDIUM_GRAY}>vessel {5} in distress, requesting immediate reinforcements."

		val MINING_GUILD = builder("MINING_GUILD", MINING_CORP_LIGHT_ORANGE.value())
			.setMessagePrefix("<${HEColorScheme.HE_MEDIUM_GRAY}>Receiving transmission from $miningGuildMini <${HEColorScheme.HE_MEDIUM_GRAY}>vessel")
			.addNames(
				text("Nil Noralgratin", MINING_CORP_LIGHT_ORANGE),
				text("Alpi Artion", MINING_CORP_LIGHT_ORANGE),
				text("Sisko Sargred", MINING_CORP_LIGHT_ORANGE),
				text("Heimo Hourrog", MINING_CORP_LIGHT_ORANGE),
				text("Gann Grulgrorlim", MINING_CORP_LIGHT_ORANGE),
				text("Lempi Lassnia", MINING_CORP_LIGHT_ORANGE),
				text("Sighebyrn Strenkann", MINING_CORP_LIGHT_ORANGE),
				text("Rik Rihre", MINING_CORP_LIGHT_ORANGE),
				text("Alanury Addar", MINING_CORP_LIGHT_ORANGE),
				text("Kyllikki Kukock", MINING_CORP_LIGHT_ORANGE),
				text("Sighebyrn Strenkann", MINING_CORP_LIGHT_ORANGE)
			)
			.addRadiusMessages(
				550.0 * 1.5 to "<#FFA500>You are entering restricted airspace. If you hear this transmission, turn away immediately or you will be fired upon.",
				550.0 to "<RED>You have violated restricted airspace. Your vessel will be fired upon."
			)
			.build()

		val EXPLORER_LIGHT_CYAN = TextColor.fromHexString("#59E3D7")!!
		val EXPLORER_MEDIUM_CYAN = TextColor.fromHexString("#3AA198")!!
		val EXPLORER_DARK_CYAN = TextColor.fromHexString("#1F5651")!!

		val PERSEUS_EXPLORERS = builder("PERSEUS_EXPLORERS", EXPLORER_LIGHT_CYAN.value())
			.setMessagePrefix("<$EXPLORER_LIGHT_CYAN>Receiving transmission from civilian vessel")
			.addNames(
				"<$EXPLORER_LIGHT_CYAN>Rookie <$EXPLORER_MEDIUM_CYAN>Pilot".miniMessage(),
				"<$EXPLORER_LIGHT_CYAN>Seasoned Explorer".miniMessage(),
				"<$EXPLORER_LIGHT_CYAN>Rookie Captain".miniMessage(),
				"<$EXPLORER_LIGHT_CYAN>Rookie <$EXPLORER_MEDIUM_CYAN>Pilot".miniMessage(),
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
			.build()

		val PRIVATEER_LIGHTER_TEAL = TextColor.fromHexString("#48e596")!!
		val PRIVATEER_LIGHT_TEAL = TextColor.fromHexString("#5DD097")!!
		val PRIVATEER_MEDIUM_TEAL = TextColor.fromHexString("#79B698")!!
		val PRIVATEER_DARK_TEAL = TextColor.fromHexString("#639f77")!!

		val SYSTEM_DEFENSE_FORCES = builder("SYSTEM_DEFENSE_FORCES", PRIVATEER_LIGHT_TEAL.value())
			.setMessagePrefix("<${HEColorScheme.HE_MEDIUM_GRAY}>Receiving transmission from <$PRIVATEER_LIGHT_TEAL>privateer</$PRIVATEER_LIGHT_TEAL> vessel")
			.addNames(
				"<$PRIVATEER_MEDIUM_TEAL>System Defense <$PRIVATEER_LIGHT_TEAL>Rookie".miniMessage(),
				"<$PRIVATEER_MEDIUM_TEAL>System Defense <$PRIVATEER_LIGHT_TEAL>Trainee".miniMessage(),
				"<$PRIVATEER_MEDIUM_TEAL>System Defense <$PRIVATEER_LIGHT_TEAL>Pilot".miniMessage(),
				"<$PRIVATEER_MEDIUM_TEAL>System Defense <$PRIVATEER_LIGHT_TEAL>Veteran".miniMessage(),
				"<$PRIVATEER_MEDIUM_TEAL>System Defense <$PRIVATEER_LIGHT_TEAL>Ace".miniMessage(),
			)
			.addRadiusMessages(
				650.0 * 1.5 to "<#FFA500>You are entering restricted airspace. If you hear this transmission, turn away immediately or you will be fired upon.",
				650.0 to "<RED>You have violated restricted airspace. Your vessel will be fired upon."
			)
			.addSmackMessages(
				"<white>Stand down, we have you outmatched!",
				"<white>Once I breach your shields, there's no going back.",
				"<white>Ha, you call those weapons?",
				"<white>System command, hostile contact is taking severe shield damage.",
				"<white>Flanking right!",
				"<white>Flanking left!"
			)
			.build()

		val TSAII_LIGHT_ORANGE = TextColor.fromHexString("#F37F58")!!
		val TSAII_MEDIUM_ORANGE = TextColor.fromHexString("#E56034")!!
		val TSAII_DARK_ORANGE = TextColor.fromHexString("#A1543A")!!
		val TSAII_VERY_DARK_ORANGE = TextColor.fromHexString("#9C3614")!!

		val TSAII_RAIDERS = builder("TSAII_RAIDERS", TSAII_MEDIUM_ORANGE.value())
			.addNames(
				text("Dhagdagar", TSAII_DARK_ORANGE),
				text("Zazgrord", TSAII_DARK_ORANGE),
				text("Dhagzuzz", TSAII_DARK_ORANGE),
				text("Hrorgrum", TSAII_DARK_ORANGE),
				text("Rabidstompa", TSAII_DARK_ORANGE),
				text("Godcooka", TSAII_DARK_ORANGE),
				text("Skarcrushah", TSAII_DARK_ORANGE),
				text("Big Bozz",TSAII_DARK_ORANGE, TextDecoration.BOLD)
			)
			.addSmackMessages(
				"I'll leave nothing but scrap",
				"I'll cut you to bacon",
				"When I'm done with you, I'll mantle your skull!"
			)
			.addRadiusMessages(
				1500.0 to "Get off our turf!"
			)
			.build()

		val PIRATE_LIGHT_RED = TextColor.fromHexString("#A06363")!!
		val PIRATE_SATURATED_RED = TextColor.fromHexString("#C63F3F")!!
		val PIRATE_DARK_RED = TextColor.fromHexString("#732525")!!

		val PIRATES = builder("PIRATES", PIRATE_SATURATED_RED.value())
			.setMessagePrefix("<$PIRATE_SATURATED_RED>Receiving transmission from pirate vessel")
			.addNames(listOf(
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
			).map { it.toComponent(PIRATE_LIGHT_RED) })
			.addSmackMessages(
				"Nice day, Nice Ship. I think ill take it!",
				"I'll plunder your booty!",
				"Scram or we'll blow you to pieces!",
				"Someones too curious for their own good.",
				"Don't say I didn't warn ya, mate."
			)
			.addRadiusMessages(
				750.0 * 1.5 to "<#FFA500>Back off!.",
				750.0 to "<RED>I'll make an example of you!."
			)
			.build()
	}
}
