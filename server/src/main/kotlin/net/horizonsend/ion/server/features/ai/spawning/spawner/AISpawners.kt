package net.horizonsend.ion.server.features.ai.spawning.spawner

import net.horizonsend.ion.common.utils.text.colors.HEColorScheme.Companion.HE_MEDIUM_GRAY
import net.horizonsend.ion.common.utils.text.miniMessage
import net.horizonsend.ion.server.IonServerComponent
import net.horizonsend.ion.server.features.ai.configuration.WorldSettings
import net.horizonsend.ion.server.features.ai.faction.AIFaction
import net.horizonsend.ion.server.features.ai.faction.AIFaction.Companion.EXPLORER_LIGHT_CYAN
import net.horizonsend.ion.server.features.ai.faction.AIFaction.Companion.MINING_GUILD
import net.horizonsend.ion.server.features.ai.faction.AIFaction.Companion.PERSEUS_EXPLORERS
import net.horizonsend.ion.server.features.ai.faction.AIFaction.Companion.PIRATES
import net.horizonsend.ion.server.features.ai.faction.AIFaction.Companion.PIRATE_SATURATED_RED
import net.horizonsend.ion.server.features.ai.faction.AIFaction.Companion.PRIVATEER_LIGHT_TEAL
import net.horizonsend.ion.server.features.ai.faction.AIFaction.Companion.SYSTEM_DEFENSE_FORCES
import net.horizonsend.ion.server.features.ai.faction.AIFaction.Companion.TSAII_DARK_ORANGE
import net.horizonsend.ion.server.features.ai.faction.AIFaction.Companion.TSAII_MEDIUM_ORANGE
import net.horizonsend.ion.server.features.ai.faction.AIFaction.Companion.TSAII_RAIDERS
import net.horizonsend.ion.server.features.ai.faction.AIFaction.Companion.WATCHER_ACCENT
import net.horizonsend.ion.server.features.ai.faction.AIFaction.Companion.miningGuildMini
import net.horizonsend.ion.server.features.ai.faction.AIFaction.Companion.吃饭人_STANDARD
import net.horizonsend.ion.server.features.ai.spawning.AISpawningManager
import net.horizonsend.ion.server.features.ai.starship.AITemplateRegistry
import net.horizonsend.ion.server.features.ai.starship.AITemplateRegistry.MALINGSHU_REINFORCED
import net.horizonsend.ion.server.features.ai.starship.AITemplateRegistry.MIANBAO_REINFORCED
import net.horizonsend.ion.server.features.ai.starship.AITemplateRegistry.TERALITH
import net.horizonsend.ion.server.features.ai.starship.AITemplateRegistry.VERDOLITH_REINFORCED
import net.horizonsend.ion.server.features.ai.starship.AITemplateRegistry.spawnChance

object AISpawners : IonServerComponent() {
	val WATCHER_BASIC = registerSpawner(StandardFactionSpawner(
		faction = AIFaction.WATCHERS,
//		spawnMessage = ofChildren(
//			text("[", HE_LIGHT_GRAY),
//			text("{3} System Alert", WATCHER_ACCENT),
//			text("]", HE_LIGHT_GRAY),
//			text(" An unknown starship signature is being broadcast, proceed with extreme caution.", WATCHER_STANDARD)
//		),
		spawnMessage = "<$WATCHER_ACCENT>An unknown starship signature is being broadcast in {4} at {1}, {3}".miniMessage(),
		pointChance = 0.5,
		pointThreshold = 20 * 60 * 7,
		worlds = listOf(
			WorldSettings(
				worldName = "Trench",
				probability = 0.5,
				minDistanceFromPlayer = 2500.0,
				maxDistanceFromPlayer = 4500.0,
				templates = listOf(
					spawnChance(VERDOLITH_REINFORCED, 0.75),
					spawnChance(TERALITH, 0.25)
				)
			),
			WorldSettings(
				worldName = "AU-0821",
				probability = 0.5,
				minDistanceFromPlayer = 2500.0,
				maxDistanceFromPlayer = 4500.0,
				templates = listOf(
					spawnChance(VERDOLITH_REINFORCED, 0.75),
					spawnChance(TERALITH, 0.25)
				)
			)
		)
	))

	val 吃饭人_BASIC = registerSpawner(StandardFactionSpawner(
		faction = AIFaction.吃饭人,
//		spawnMessage = ofChildren(
//			text("[", HE_LIGHT_GRAY),
//			text("{3} System Alert", WATCHER_ACCENT),
//			text("]", HE_LIGHT_GRAY),
//			text(" An unknown starship signature is being broadcast, proceed with extreme caution.", 吃饭人_STANDARD)
//		),
		spawnMessage = "<${吃饭人_STANDARD}>An unknown starship signature is being broadcast in {4} at {1}, {3}".miniMessage(),
		pointChance = 0.5,
		pointThreshold = 20 * 60 * 7,
		worlds = listOf(
			WorldSettings(
				worldName = "Trench",
				probability = 0.5,
				minDistanceFromPlayer = 2500.0,
				maxDistanceFromPlayer = 4500.0,
				templates = listOf(
					spawnChance(MIANBAO_REINFORCED, 0.5),
					spawnChance(MALINGSHU_REINFORCED, 0.5)
				)
			),
			WorldSettings(
				worldName = "AU-0821",
				probability = 0.5,
				minDistanceFromPlayer = 2500.0,
				maxDistanceFromPlayer = 4500.0,
				templates = listOf(
					spawnChance(MIANBAO_REINFORCED, 0.5),
					spawnChance(MALINGSHU_REINFORCED, 0.5)
				)
			)
		)
	))

	val PIRATE_BASIC = registerSpawner(StandardFactionSpawner(
		faction = PIRATES,
//		spawnMessage = ofChildren(
//			text("[", HE_LIGHT_GRAY),
//			text("{3} System Alert", PIRATE_DARK_RED),
//			text("]", HE_LIGHT_GRAY),
//			text(" Pirate activity detected!", PIRATE_LIGHT_RED)
//		),
		spawnMessage = "<${HE_MEDIUM_GRAY}>A pirate {0} has been identified in the area of {1}, {3}, in {4}. <$PIRATE_SATURATED_RED>Please avoid the sector until the threat has been cleared.".miniMessage(),
		pointChance = 0.5,
		pointThreshold = 10000,
		worlds = listOf(
			WorldSettings(
				worldName = "Asteri",
				probability = 0.15,
				minDistanceFromPlayer = 2000.0,
				maxDistanceFromPlayer = 4000.0,
				templates = listOf(
					spawnChance(AITemplateRegistry.ISKAT, 0.2),
					spawnChance(AITemplateRegistry.VOSS, 0.2),
					spawnChance(AITemplateRegistry.HECTOR, 0.4),
					spawnChance(AITemplateRegistry.HIRO, 0.4),
					spawnChance(AITemplateRegistry.WASP, 0.4)
				)
			),
			WorldSettings(
				worldName = "Regulus",
				probability = 0.25,
				minDistanceFromPlayer = 2000.0,
				maxDistanceFromPlayer = 4000.0,
				templates = listOf(
					spawnChance(AITemplateRegistry.ISKAT, 0.2),
					spawnChance(AITemplateRegistry.VOSS, 0.2),
					spawnChance(AITemplateRegistry.HECTOR, 0.4),
					spawnChance(AITemplateRegistry.HIRO, 0.4),
					spawnChance(AITemplateRegistry.WASP, 0.4),
					spawnChance(AITemplateRegistry.FRENZ, 0.4),
					spawnChance(AITemplateRegistry.TEMPEST, 0.2),
					spawnChance(AITemplateRegistry.VELASCO, 0.2),
					spawnChance(AITemplateRegistry.VENDETTA, 0.2),
					spawnChance(AITemplateRegistry.ANAAN, 0.2),
					spawnChance(AITemplateRegistry.CORMORANT, 0.2),
				)
			),
			WorldSettings(
				worldName = "Sirius",
				probability = 0.15,
				minDistanceFromPlayer = 2000.0,
				maxDistanceFromPlayer = 4000.0,
				templates = listOf(
					spawnChance(AITemplateRegistry.ISKAT, 0.2),
					spawnChance(AITemplateRegistry.VOSS, 0.2),
					spawnChance(AITemplateRegistry.HECTOR, 0.4),
					spawnChance(AITemplateRegistry.HIRO, 0.4),
					spawnChance(AITemplateRegistry.WASP, 0.4),
					spawnChance(AITemplateRegistry.FRENZ, 0.4),
					spawnChance(AITemplateRegistry.TEMPEST, 0.2),
					spawnChance(AITemplateRegistry.VELASCO, 0.2)
				)
			),
			WorldSettings(
				worldName = "Ilios",
				probability = 0.15,
				minDistanceFromPlayer = 2000.0,
				maxDistanceFromPlayer = 4000.0,
				templates = listOf(
					spawnChance(AITemplateRegistry.ISKAT, 0.2),
					spawnChance(AITemplateRegistry.VOSS, 0.2),
					spawnChance(AITemplateRegistry.HECTOR, 0.4),
					spawnChance(AITemplateRegistry.HIRO, 0.4),
					spawnChance(AITemplateRegistry.WASP, 0.4),
					spawnChance(AITemplateRegistry.FRENZ, 0.4),
					spawnChance(AITemplateRegistry.TEMPEST, 0.2),
					spawnChance(AITemplateRegistry.VELASCO, 0.2),
					spawnChance(AITemplateRegistry.VENDETTA, 0.2),
					spawnChance(AITemplateRegistry.ANAAN, 0.2),
					spawnChance(AITemplateRegistry.CORMORANT, 0.2)
				)
			),
			WorldSettings(
				worldName = "Horizon",
				probability = 0.15,
				minDistanceFromPlayer = 2000.0,
				maxDistanceFromPlayer = 4000.0,
				templates = listOf(
					spawnChance(AITemplateRegistry.VENDETTA, 0.2),
					spawnChance(AITemplateRegistry.ANAAN, 0.2),
					spawnChance(AITemplateRegistry.CORMORANT, 0.2),
					spawnChance(AITemplateRegistry.MANTIS, 0.2),
					spawnChance(AITemplateRegistry.HERNSTEIN, 0.2),
					spawnChance(AITemplateRegistry.FYR, 0.2),
					spawnChance(AITemplateRegistry.BLOODSTAR, 0.2)
				)
			),
			WorldSettings(
				worldName = "Trench",
				probability = 0.15,
				minDistanceFromPlayer = 2000.0,
				maxDistanceFromPlayer = 4000.0,
				templates = listOf(
					spawnChance(AITemplateRegistry.VENDETTA, 0.2),
					spawnChance(AITemplateRegistry.ANAAN, 0.2),
					spawnChance(AITemplateRegistry.CORMORANT, 0.2),
					spawnChance(AITemplateRegistry.MANTIS, 0.2),
					spawnChance(AITemplateRegistry.HERNSTEIN, 0.2),
					spawnChance(AITemplateRegistry.FYR, 0.2),
					spawnChance(AITemplateRegistry.BLOODSTAR, 0.2)
				)
			),
			WorldSettings(
				worldName = "AU-0821",
				probability = 0.15,
				minDistanceFromPlayer = 2000.0,
				maxDistanceFromPlayer = 4000.0,
				templates = listOf(
					spawnChance(AITemplateRegistry.VENDETTA, 0.2),
					spawnChance(AITemplateRegistry.ANAAN, 0.2),
					spawnChance(AITemplateRegistry.CORMORANT, 0.2),
					spawnChance(AITemplateRegistry.MANTIS, 0.2),
					spawnChance(AITemplateRegistry.HERNSTEIN, 0.2),
					spawnChance(AITemplateRegistry.FYR, 0.2),
					spawnChance(AITemplateRegistry.BLOODSTAR, 0.2)
				)
			)
		)
	))

	private fun explorerWorld(worldName: String, probability: Double): WorldSettings = WorldSettings(
		worldName = worldName,
		probability = probability,
		minDistanceFromPlayer = 1500.0,
		maxDistanceFromPlayer = 3500.0,
		templates = listOf(
			spawnChance(AITemplateRegistry.WAYFINDER, 0.35),
			spawnChance(AITemplateRegistry.STRIKER, 0.3),
			spawnChance(AITemplateRegistry.NIMBLE, 0.2),
			spawnChance(AITemplateRegistry.DESSLE, 0.2),
			spawnChance(AITemplateRegistry.MINHAUL_CHETHERITE, 0.15),
			spawnChance(AITemplateRegistry.MINHAUL_REDSTONE, 0.1),
			spawnChance(AITemplateRegistry.MINHAUL_TITANIUM, 0.1),
			spawnChance(AITemplateRegistry.EXOTRAN_CHETHERITE, 0.15),
			spawnChance(AITemplateRegistry.EXOTRAN_REDSTONE, 0.1),
			spawnChance(AITemplateRegistry.EXOTRAN_TITANIUM, 0.1),
			spawnChance(AITemplateRegistry.AMPH, 0.35)
		)
	)

	val EXPLORER_BASIC = registerSpawner(StandardFactionSpawner(
		PERSEUS_EXPLORERS,
//		spawnMessage = ofChildren(
//			text("[", HE_LIGHT_GRAY),
//			text("{3} System Alert", EXPLORER_LIGHT_CYAN),
//			text("]", HE_LIGHT_GRAY),
//			text(" A Horizon Transit Lines vessel will be passing through the system.", EXPLORER_MEDIUM_CYAN)
//		),
		spawnMessage = "<$EXPLORER_LIGHT_CYAN>Horizon Transit Lines<${HE_MEDIUM_GRAY}> {0} spawned at {1}, {3}, in {4}".miniMessage(),
		pointChance = 0.75,
		pointThreshold = 20 * 60 * 10,
		worlds = listOf(
			explorerWorld("Asteri", 0.2),
			explorerWorld("Sirius", 0.11),
			explorerWorld("Regulus", 0.2),
			explorerWorld("Ilios", 0.135),
			explorerWorld("Horizon", 0.27),
			explorerWorld("Trench", 0.055),
			explorerWorld("AU-0821", 0.055),
		)
	))

	val MINING_GUILD_BASIC = registerSpawner(StandardFactionSpawner(
		MINING_GUILD,
//		spawnMessage = ofChildren(
//			text("The ", HEColorScheme.HE_MEDIUM_GRAY),
//			text("Mining ", MINING_CORP_LIGHT_ORANGE),
//			text("Guild ", MINING_CORP_DARK_ORANGE),
//			text("branch of {3} requests non-violence during extraction operations.", HEColorScheme.HE_MEDIUM_GRAY)
//		),
		spawnMessage = "$miningGuildMini <${HE_MEDIUM_GRAY}>extraction vessel {0} spawned at {1}, {3}, in {4}".miniMessage(),
		pointChance = 0.8,
		pointThreshold = 8400,
		worlds = listOf(
			WorldSettings(
				worldName = "Asteri",
				probability = 0.2,
				minDistanceFromPlayer = 1000.0,
				maxDistanceFromPlayer = 2500.0,
				templates = listOf(
					spawnChance(AITemplateRegistry.WOODPECKER, 0.22),
					spawnChance(AITemplateRegistry.TYPE_V11, 0.22),
					spawnChance(AITemplateRegistry.TYPEA21B, 0.22),
					spawnChance(AITemplateRegistry.TYPEI41, 0.22),
					spawnChance(AITemplateRegistry.BEAVER, 0.12)
				)
			),
			WorldSettings(
				worldName = "Sirius",
				probability = 0.11,
				minDistanceFromPlayer = 1000.0,
				maxDistanceFromPlayer = 2500.0,
				templates = listOf(
					spawnChance(AITemplateRegistry.WOODPECKER, 0.22),
					spawnChance(AITemplateRegistry.TYPE_V11, 0.22),
					spawnChance(AITemplateRegistry.TYPEA21B, 0.22),
					spawnChance(AITemplateRegistry.TYPEI41, 0.22),
					spawnChance(AITemplateRegistry.BEAVER, 0.12)
				)
			),
			WorldSettings(
				worldName = "Regulus",
				probability = 0.2,
				minDistanceFromPlayer = 1000.0,
				maxDistanceFromPlayer = 2500.0,
				templates = listOf(
					spawnChance(AITemplateRegistry.WOODPECKER, 0.22),
					spawnChance(AITemplateRegistry.TYPE_V11, 0.22),
					spawnChance(AITemplateRegistry.TYPEA21B, 0.22),
					spawnChance(AITemplateRegistry.TYPEI41, 0.22),
					spawnChance(AITemplateRegistry.BEAVER, 0.12),
					spawnChance(AITemplateRegistry.OSTRICH, 0.05),
					spawnChance(AITemplateRegistry.BADGER, 0.05),
				)
			),
			WorldSettings(
				worldName = "Ilios",
				probability = 0.13,
				minDistanceFromPlayer = 1000.0,
				maxDistanceFromPlayer = 2500.0,
				templates = listOf(
					spawnChance(AITemplateRegistry.WOODPECKER, 0.22),
					spawnChance(AITemplateRegistry.TYPE_V11, 0.22),
					spawnChance(AITemplateRegistry.TYPEA21B, 0.22),
					spawnChance(AITemplateRegistry.TYPEI41, 0.22),
					spawnChance(AITemplateRegistry.BEAVER, 0.12)
				)
			),
			WorldSettings(
				worldName = "Horizon",
				probability = 0.27,
				minDistanceFromPlayer = 1000.0,
				maxDistanceFromPlayer = 2500.0,
				templates = listOf(
					spawnChance(AITemplateRegistry.WOODPECKER, 0.12),
					spawnChance(AITemplateRegistry.TYPE_V11, 0.12),
					spawnChance(AITemplateRegistry.TYPEA21B, 0.12),
					spawnChance(AITemplateRegistry.TYPEI41, 0.12),
					spawnChance(AITemplateRegistry.BEAVER, 0.22),
					spawnChance(AITemplateRegistry.OSTRICH, 0.15),
					spawnChance(AITemplateRegistry.BADGER, 0.15),
				)
			),
			WorldSettings(worldName = "Trench",
				probability = 0.75,
				minDistanceFromPlayer = 1000.0,
				maxDistanceFromPlayer = 2500.0,
				templates = listOf(
					spawnChance(AITemplateRegistry.WOODPECKER, 0.12),
					spawnChance(AITemplateRegistry.TYPE_V11, 0.12),
					spawnChance(AITemplateRegistry.TYPEA21B, 0.12),
					spawnChance(AITemplateRegistry.TYPEI41, 0.12),
					spawnChance(AITemplateRegistry.BEAVER, 0.22),
					spawnChance(AITemplateRegistry.OSTRICH, 0.15),
					spawnChance(AITemplateRegistry.BADGER, 0.15),
				)
			),
			WorldSettings(
				worldName = "AU-0821",
				probability = 0.05,
				minDistanceFromPlayer = 1000.0,
				maxDistanceFromPlayer = 2500.0,
				templates = listOf(
					spawnChance(AITemplateRegistry.WOODPECKER, 0.12),
					spawnChance(AITemplateRegistry.TYPE_V11, 0.12),
					spawnChance(AITemplateRegistry.TYPEA21B, 0.12),
					spawnChance(AITemplateRegistry.TYPEI41, 0.12),
					spawnChance(AITemplateRegistry.BEAVER, 0.22),
					spawnChance(AITemplateRegistry.OSTRICH, 0.15),
					spawnChance(AITemplateRegistry.BADGER, 0.15),
				)
			)
		)
	))

	val PRIVATEER_BASIC = registerSpawner(StandardFactionSpawner(
		faction = SYSTEM_DEFENSE_FORCES,
//		spawnMessage = ofChildren(
//			text("{3} ", HE_LIGHT_GRAY),
//			text("System Defense Forces ", PRIVATEER_LIGHT_TEAL),
//			text("have started a patrol.", HE_LIGHT_GRAY)
//		),
		spawnMessage = "<$PRIVATEER_LIGHT_TEAL>Privateer patrol <${HE_MEDIUM_GRAY}>operation vessel {0} spawned at {1}, {3}, in {4}".miniMessage(),
		pointChance = 0.5,
		pointThreshold = 12000,
		worlds = listOf(
			WorldSettings(
				worldName = "Asteri",
				probability = 0.15,
				minDistanceFromPlayer = 1000.0,
				maxDistanceFromPlayer = 2500.0,
				templates = listOf(
					spawnChance(AITemplateRegistry.PROTECTOR, 0.12),
					spawnChance(AITemplateRegistry.FURIOUS, 0.12),
					spawnChance(AITemplateRegistry.INFLICT, 0.12),
					spawnChance(AITemplateRegistry.VETERAN, 0.12),
					spawnChance(AITemplateRegistry.PATROLLER, 0.12),
					spawnChance(AITemplateRegistry.TENETA, 0.12)
				)
			),
			WorldSettings(
				worldName = "Sirius",
				probability = 0.2,
				minDistanceFromPlayer = 1000.0,
				maxDistanceFromPlayer = 2500.0,
				templates = listOf(
					spawnChance(AITemplateRegistry.PROTECTOR, 0.12),
					spawnChance(AITemplateRegistry.FURIOUS, 0.12),
					spawnChance(AITemplateRegistry.INFLICT, 0.12),
					spawnChance(AITemplateRegistry.VETERAN, 0.12),
					spawnChance(AITemplateRegistry.PATROLLER, 0.12),
					spawnChance(AITemplateRegistry.DAYBREAK, 0.12),
					spawnChance(AITemplateRegistry.TENETA, 0.12),
					spawnChance(AITemplateRegistry.CONTRACTOR, 0.05)
				)
			),
			WorldSettings(
				worldName = "Regulus",
				probability = 0.3,
				minDistanceFromPlayer = 1000.0,
				maxDistanceFromPlayer = 2500.0,
				templates = listOf(
					spawnChance(AITemplateRegistry.INFLICT, 0.12),
					spawnChance(AITemplateRegistry.VETERAN, 0.12),
					spawnChance(AITemplateRegistry.PATROLLER, 0.12),
					spawnChance(AITemplateRegistry.DAYBREAK, 0.12),
					spawnChance(AITemplateRegistry.TENETA, 0.12),
					spawnChance(AITemplateRegistry.BULWARK, 0.12),
					spawnChance(AITemplateRegistry.CONTRACTOR, 0.12),
					spawnChance(AITemplateRegistry.DAGGER, 0.12)
				)
			),
			WorldSettings(
				worldName = "Ilios",
				probability = 0.1,
				minDistanceFromPlayer = 1000.0,
				maxDistanceFromPlayer = 2500.0,
				templates = listOf(
					spawnChance(AITemplateRegistry.INFLICT, 0.12),
					spawnChance(AITemplateRegistry.VETERAN, 0.12),
					spawnChance(AITemplateRegistry.PATROLLER, 0.12),
					spawnChance(AITemplateRegistry.TENETA, 0.12),
					spawnChance(AITemplateRegistry.CONTRACTOR, 0.12),
					spawnChance(AITemplateRegistry.DAYBREAK, 0.12)
				)
			),
			WorldSettings(
				worldName = "Horizon",
				probability = 0.1,
				minDistanceFromPlayer = 1000.0,
				maxDistanceFromPlayer = 2500.0,
				templates = listOf(
					spawnChance(AITemplateRegistry.VETERAN, 0.12),
					spawnChance(AITemplateRegistry.PATROLLER, 0.12),
					spawnChance(AITemplateRegistry.TENETA, 0.12),
					spawnChance(AITemplateRegistry.BULWARK, 0.12),
					spawnChance(AITemplateRegistry.CONTRACTOR, 0.12),
					spawnChance(AITemplateRegistry.DAGGER, 0.12),
					spawnChance(AITemplateRegistry.DAYBREAK, 0.12)
				)
			),
			WorldSettings(worldName = "Trench",
				probability = 0.05,
				minDistanceFromPlayer = 1000.0,
				maxDistanceFromPlayer = 2500.0,
				templates = listOf(
					spawnChance(AITemplateRegistry.VETERAN, 0.12),
					spawnChance(AITemplateRegistry.PATROLLER, 0.12),
					spawnChance(AITemplateRegistry.TENETA, 0.12),
					spawnChance(AITemplateRegistry.BULWARK, 0.12),
					spawnChance(AITemplateRegistry.CONTRACTOR, 0.12),
					spawnChance(AITemplateRegistry.DAGGER, 0.12),
					spawnChance(AITemplateRegistry.DAYBREAK, 0.12)
				)
			),
			WorldSettings(
				worldName = "AU-0821",
				probability = 0.05,
				minDistanceFromPlayer = 1000.0,
				maxDistanceFromPlayer = 2500.0,
				templates = listOf(
					spawnChance(AITemplateRegistry.VETERAN, 0.10),
					spawnChance(AITemplateRegistry.PATROLLER, 0.10),
					spawnChance(AITemplateRegistry.TENETA, 0.10),
					spawnChance(AITemplateRegistry.BULWARK, 0.12),
					spawnChance(AITemplateRegistry.CONTRACTOR, 0.12),
					spawnChance(AITemplateRegistry.DAGGER, 0.12),
					spawnChance(AITemplateRegistry.DAYBREAK, 0.12)
				)
			)
		)
	))

	val TSAII_BASIC = registerSpawner(StandardFactionSpawner(
		faction = TSAII_RAIDERS,
//		spawnMessage = ofChildren(
//			text("[", HE_LIGHT_GRAY),
//			text("{3} System Alert", TSAII_LIGHT_ORANGE),
//			text("]", HE_LIGHT_GRAY),
//			text(" Tsaii Raider activity detected!", TSAII_MEDIUM_ORANGE)
//		),
		spawnMessage = "<${TSAII_DARK_ORANGE}>Dangerous Tsaii Raiders {0} has been reported in the area of {1}, {3}, in {4}. <$TSAII_MEDIUM_ORANGE>Please avoid the sector until the threat has been cleared!".miniMessage(),
		pointThreshold = 30 * 20 * 60,
		pointChance = 0.5,
		worlds = listOf(
			WorldSettings(
				worldName = "Horizon",
				minDistanceFromPlayer = 2500.0,
				maxDistanceFromPlayer = 4500.0,
				probability = 0.4,
				templates = listOf(
					spawnChance(AITemplateRegistry.RAIDER, 0.25),
					spawnChance(AITemplateRegistry.SCYTHE, 0.25),
					spawnChance(AITemplateRegistry.SWARMER, 0.25),
					spawnChance(AITemplateRegistry.REAVER, 0.25)
				)
			),
			WorldSettings(
				worldName = "Trench",
				minDistanceFromPlayer = 2500.0,
				maxDistanceFromPlayer = 4500.0,
				probability = 0.3,
				templates = listOf(
					spawnChance(AITemplateRegistry.RAIDER, 0.25),
					spawnChance(AITemplateRegistry.SCYTHE, 0.25),
					spawnChance(AITemplateRegistry.SWARMER, 0.25),
					spawnChance(AITemplateRegistry.REAVER, 0.25)
				)
			),
			WorldSettings(
				worldName = "AU-0821",
				minDistanceFromPlayer = 2500.0,
				maxDistanceFromPlayer = 4500.0,
				probability = 0.3,
				templates = listOf(
					spawnChance(AITemplateRegistry.RAIDER, 0.25),
					spawnChance(AITemplateRegistry.SCYTHE, 0.25),
					spawnChance(AITemplateRegistry.SWARMER, 0.25),
					spawnChance(AITemplateRegistry.REAVER, 0.25)
				)
			)
		)
	))

	private fun <T: AISpawner> registerSpawner(spawner: T): T {
		AISpawningManager.spawners += spawner

		return spawner
	}
}
