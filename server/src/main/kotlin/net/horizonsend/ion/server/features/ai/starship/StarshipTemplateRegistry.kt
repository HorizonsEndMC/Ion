package net.horizonsend.ion.server.features.ai.starship

import net.horizonsend.ion.common.utils.configuration.Configuration
import net.horizonsend.ion.common.utils.text.colors.ABYSSAL_DARK_RED
import net.horizonsend.ion.common.utils.text.colors.ABYSSAL_DESATURATED_RED
import net.horizonsend.ion.common.utils.text.colors.ABYSSAL_LIGHT_RED
import net.horizonsend.ion.common.utils.text.colors.EXPLORER_MEDIUM_CYAN
import net.horizonsend.ion.common.utils.text.colors.HEColorScheme.Companion.HE_LIGHT_GRAY
import net.horizonsend.ion.common.utils.text.colors.MINING_CORP_DARK_ORANGE
import net.horizonsend.ion.common.utils.text.colors.MINING_CORP_LIGHT_ORANGE
import net.horizonsend.ion.common.utils.text.colors.PIRATE_LIGHT_RED
import net.horizonsend.ion.common.utils.text.colors.PIRATE_SATURATED_RED
import net.horizonsend.ion.common.utils.text.colors.PRIVATEER_DARK_TEAL
import net.horizonsend.ion.common.utils.text.colors.PRIVATEER_LIGHT_TEAL
import net.horizonsend.ion.common.utils.text.colors.PRIVATEER_MEDIUM_TEAL
import net.horizonsend.ion.common.utils.text.colors.TSAII_DARK_ORANGE
import net.horizonsend.ion.common.utils.text.colors.TSAII_VERY_DARK_ORANGE
import net.horizonsend.ion.common.utils.text.colors.WATCHER_STANDARD
import net.horizonsend.ion.common.utils.text.colors.吃饭人_STANDARD
import net.horizonsend.ion.common.utils.text.miniMessage
import net.horizonsend.ion.common.utils.text.serialize
import net.horizonsend.ion.server.IonServerComponent
import net.horizonsend.ion.server.configuration.ConfigurationFiles
import net.horizonsend.ion.server.features.ai.configuration.AIStarshipTemplate.WeaponSet
import net.horizonsend.ion.server.features.starship.StarshipType.AI_BATTLECRUISER
import net.horizonsend.ion.server.features.starship.StarshipType.AI_CORVETTE
import net.horizonsend.ion.server.features.starship.StarshipType.AI_CORVETTE_LOGISTIC
import net.horizonsend.ion.server.features.starship.StarshipType.AI_DESTROYER
import net.horizonsend.ion.server.features.starship.StarshipType.AI_FRIGATE
import net.horizonsend.ion.server.features.starship.StarshipType.AI_GUNSHIP
import net.horizonsend.ion.server.features.starship.StarshipType.AI_LIGHT_FREIGHTER
import net.horizonsend.ion.server.features.starship.StarshipType.AI_SHUTTLE
import net.horizonsend.ion.server.features.starship.StarshipType.AI_STARFIGHTER
import net.horizonsend.ion.server.features.starship.StarshipType.AI_TRANSPORT
import net.horizonsend.ion.server.features.starship.StarshipType.UNIDENTIFIEDSHIP
import net.kyori.adventure.text.Component.text

/**
 * Predefined starship templates.
 *
 * These are not AI on their own, just enough instructions to spawn, detect, and pilot the ship.
 *
 * Some other details tied to the ship, such as its name and weapon sets are included.
 *
 * Starship templates may be used for multiple AI templates, such as reinforced or non-reinforced variants.
 *
 * All behavior will be handled by AI Templates
 * @see AITemplateRegistry
 **/
object StarshipTemplateRegistry : IonServerComponent(true) {
	private val TEMPLATE_DIRECTORY = ConfigurationFiles.configurationFolder.resolve("starship_templates").apply { mkdirs() }

	// START_TEST
	val TEST_JAMMER = registerTemplate(StarshipTemplate(
		schematicName = "test_jammer",
		type = AI_GUNSHIP,
		miniMessageName = text("Test Jammer", WATCHER_STANDARD).serialize(miniMessage)
	))

	val TEST_LOGISTIC = registerTemplate(StarshipTemplate(
		schematicName = "test_logistic",
		type = AI_CORVETTE_LOGISTIC,
		miniMessageName = text("Test Logistic", WATCHER_STANDARD).serialize(miniMessage)
	))

	val TEST_BATTLECRUISER = registerTemplate(StarshipTemplate(
		schematicName = "test_battlecruiser",
		type = AI_BATTLECRUISER,
		miniMessageName = text("Test Battlecruiser", WATCHER_STANDARD).serialize(miniMessage)
	))

	val TEST_DISINTEGRATOR = registerTemplate(StarshipTemplate(
		schematicName = "test_disintegrator",
		type = AI_DESTROYER,
		miniMessageName = text("Test Disintegrator", WATCHER_STANDARD).serialize(miniMessage)
	))

	val TEST_CYCLE = registerTemplate(StarshipTemplate(
		schematicName = "test_cycle",
		type = AI_FRIGATE,
		miniMessageName = text("Test Cycle", WATCHER_STANDARD).serialize(miniMessage)
	))
	// END_TEST

	// START_WATCHERS

	val VERDOLITH = registerTemplate(StarshipTemplate(
		schematicName = "Verdolith",
		type = AI_FRIGATE,
		miniMessageName = text("Verdolith", WATCHER_STANDARD).serialize(miniMessage),
		manualWeaponSets = mutableSetOf(
			WeaponSet(
				name = "phaser",
				engagementRangeMin = 0.0,
				engagementRangeMax = 220.0
			),
			WeaponSet(
				name = "manual",
				engagementRangeMin = 220.0,
				engagementRangeMax = 550.0
			),
		),
		autoWeaponSets = mutableSetOf(
			WeaponSet(
			name = "auto",
			engagementRangeMin = 250.0,
			engagementRangeMax = 550.0
		)
		),
	))

	val TERALITH = registerTemplate(StarshipTemplate(
		schematicName = "Teralith",
		type = AI_FRIGATE,
		miniMessageName = text("Teralith", WATCHER_STANDARD).serialize(miniMessage),
		manualWeaponSets = mutableSetOf(
			WeaponSet(
				name = "phaser",
				engagementRangeMin = 0.0,
				engagementRangeMax = 220.0
			),
			WeaponSet(
				name = "manual",
				engagementRangeMin = 220.0,
				engagementRangeMax = 550.0
			),
		),
		autoWeaponSets = mutableSetOf(
			WeaponSet(
			name = "auto",
			engagementRangeMin = 250.0,
			engagementRangeMax = 550.0
		))
	))

	val ARBOREALITH = registerTemplate(StarshipTemplate(
		schematicName = "Arborealith",
		type = AI_DESTROYER,
		miniMessageName = text("Arborealith", WATCHER_STANDARD).serialize(miniMessage),
		manualWeaponSets = mutableSetOf(
			WeaponSet(
				name = "phaser",
				engagementRangeMin = 0.0,
				engagementRangeMax = 220.0
			),
			WeaponSet(
				name = "manual",
				engagementRangeMin = 220.0,
				engagementRangeMax = 550.0
			),
		),
		autoWeaponSets = mutableSetOf(
			WeaponSet(
				name = "auto",
				engagementRangeMin = 250.0,
				engagementRangeMax = 550.0
			))

	))

	// END_WATCHERS
	//START 吃饭人

	val MIANBAO = registerTemplate(StarshipTemplate(
		schematicName = "Mianbao",
		type = AI_CORVETTE,
		miniMessageName = text("Mianbao", 吃饭人_STANDARD).serialize(miniMessage),
		manualWeaponSets = mutableSetOf(
			WeaponSet(
			name = "manual",
			engagementRangeMin = 0.0,
			engagementRangeMax = 550.0
		)
		),
		autoWeaponSets = mutableSetOf(
			WeaponSet(
			name = "auto",
			engagementRangeMin = 250.0,
			engagementRangeMax = 550.0
		)
		)
	))

	val MALINGSHU = registerTemplate(StarshipTemplate(
		schematicName = "Malingshu",
		type = AI_FRIGATE,
		miniMessageName = text("Malingshu", 吃饭人_STANDARD).serialize(miniMessage),
		manualWeaponSets = mutableSetOf(
			WeaponSet(
			name = "manual",
			engagementRangeMin = 0.0,
			engagementRangeMax = 550.0
		)
		),
		autoWeaponSets = mutableSetOf(
			WeaponSet(
				name = "auto",
				engagementRangeMin = 0.0,
				engagementRangeMax = 250.0
			),
			WeaponSet(
				name = "tt",
				engagementRangeMin = 250.0,
				engagementRangeMax = 550.0
			)
		)
	))

	// END_吃饭人
	// START_PIRATE

	val ISKAT = registerTemplate(StarshipTemplate(
		schematicName = "Iskat",
		type = AI_STARFIGHTER,
		miniMessageName = "<${PIRATE_LIGHT_RED.asHexString()}>Iskat"
	))

	val VOSS = registerTemplate(StarshipTemplate(
		schematicName = "Voss",
		type = AI_STARFIGHTER,
		miniMessageName = "<${PIRATE_LIGHT_RED.asHexString()}>Voss"
	))

	val HECTOR = registerTemplate(StarshipTemplate(
		schematicName = "Hector",
		type = AI_STARFIGHTER,
		miniMessageName = "<${PIRATE_LIGHT_RED.asHexString()}>Hector"
	))

	val HIRO = registerTemplate(StarshipTemplate(
		schematicName = "Hiro",
		type = AI_STARFIGHTER,
		miniMessageName = "<${PIRATE_LIGHT_RED.asHexString()}>Hiro"
	))

	val WASP = registerTemplate(StarshipTemplate(
		schematicName = "Wasp",
		type = AI_STARFIGHTER,
		miniMessageName = "<${PIRATE_LIGHT_RED.asHexString()}>Wasp"
	))

	val FRENZ = registerTemplate(StarshipTemplate(
		schematicName = "Frenz",
		type = AI_STARFIGHTER,
		miniMessageName = "<${PIRATE_LIGHT_RED.asHexString()}>Frenz"
	))

	val TEMPEST = registerTemplate(StarshipTemplate(
		schematicName = "Tempest",
		type = AI_STARFIGHTER,
		miniMessageName = "<${PIRATE_LIGHT_RED.asHexString()}>Tempest"
	))

	val VELASCO = registerTemplate(StarshipTemplate(
		schematicName = "Velasco",
		type = AI_STARFIGHTER,
		miniMessageName = "<${PIRATE_LIGHT_RED.asHexString()}>Velasco"
	))

	val ANAAN = registerTemplate(StarshipTemplate(
		schematicName = "Anaan",
		type = AI_GUNSHIP,
		miniMessageName = "<${PIRATE_SATURATED_RED.asHexString()}>Anaan",
		autoWeaponSets = mutableSetOf(WeaponSet(name = "auto", engagementRangeMin = 0.0, engagementRangeMax = 500.0))
	))

	val VENDETTA = registerTemplate(StarshipTemplate(
		schematicName = "Vendetta",
		type = AI_GUNSHIP,
		miniMessageName = "<${PIRATE_SATURATED_RED.asHexString()}>Vendetta",
		manualWeaponSets = mutableSetOf(WeaponSet(name = "main", engagementRangeMin = 0.0, engagementRangeMax = 500.0)),
		autoWeaponSets = mutableSetOf(WeaponSet(name = "auto", engagementRangeMin = 0.0, engagementRangeMax = 500.0))
	))

	val CORMORANT = registerTemplate(StarshipTemplate(
		schematicName = "Cormorant",
		type = AI_GUNSHIP,
		miniMessageName = "<${PIRATE_SATURATED_RED.asHexString()}>Cormorant",
		manualWeaponSets = mutableSetOf(WeaponSet(name = "main", engagementRangeMin = 0.0, engagementRangeMax = 500.0)),
		autoWeaponSets = mutableSetOf(WeaponSet(name = "auto", engagementRangeMin = 0.0, engagementRangeMax = 500.0))
	))

	val MANTIS = registerTemplate(StarshipTemplate(
		schematicName = "Mantis",
		type = AI_GUNSHIP,
		miniMessageName = "<${PIRATE_SATURATED_RED.asHexString()}>Mantis",
		manualWeaponSets = mutableSetOf(WeaponSet(name = "main", engagementRangeMin = 0.0, engagementRangeMax = 500.0)),
		autoWeaponSets = mutableSetOf(WeaponSet(name = "auto", engagementRangeMin = 0.0, engagementRangeMax = 500.0))
	))

	val HERNSTEIN = registerTemplate(StarshipTemplate(
		schematicName = "Hernstein",
		type = AI_GUNSHIP,
		miniMessageName = "<${PIRATE_SATURATED_RED.asHexString()}>Hernstein",
		manualWeaponSets = mutableSetOf(WeaponSet(name = "main", engagementRangeMin = 0.0, engagementRangeMax = 500.0)),
		autoWeaponSets = mutableSetOf(WeaponSet(name = "auto", engagementRangeMin = 0.0, engagementRangeMax = 500.0))
	))

	val FYR = registerTemplate(StarshipTemplate(
		schematicName = "Fyr",
		type = AI_GUNSHIP,
		miniMessageName = "<${PIRATE_SATURATED_RED.asHexString()}>Fyr",
		manualWeaponSets = mutableSetOf(WeaponSet(name = "main", engagementRangeMin = 0.0, engagementRangeMax = 500.0)),
		autoWeaponSets = mutableSetOf(WeaponSet(name = "auto", engagementRangeMin = 0.0, engagementRangeMax = 500.0))
	))

	val BLOODSTAR = registerTemplate(StarshipTemplate(
		schematicName = "Bloodstar",
		type = AI_CORVETTE,
		miniMessageName = "<${PIRATE_SATURATED_RED.asHexString()}>Bloodstar",
		manualWeaponSets = mutableSetOf(WeaponSet(name = "main", engagementRangeMin = 0.0, engagementRangeMax = 500.0)),
		autoWeaponSets = mutableSetOf(WeaponSet(name = "auto", engagementRangeMin = 0.0, engagementRangeMax = 500.0))
	))

	// END_PIRATE
	// START_EXPLORER

	val WAYFINDER = registerTemplate(StarshipTemplate(
		schematicName = "Wayfinder",
		type = AI_TRANSPORT,
		miniMessageName = "<${EXPLORER_MEDIUM_CYAN.asHexString()}>Wayfinder",
	))

	val STRIKER = registerTemplate(StarshipTemplate(
		schematicName = "Striker",
		type = AI_SHUTTLE,
		miniMessageName = "<${EXPLORER_MEDIUM_CYAN.asHexString()}>Striker",
	))

	val NIMBLE = registerTemplate(StarshipTemplate(
		schematicName = "Nimble",
		type = AI_SHUTTLE,
		miniMessageName = "<${EXPLORER_MEDIUM_CYAN.asHexString()}>Nimble",
	))

	val DESSLE = registerTemplate(StarshipTemplate(
		schematicName = "Dessle",
		type = AI_LIGHT_FREIGHTER,
		miniMessageName = "<${EXPLORER_MEDIUM_CYAN.asHexString()}>Dessle <${HE_LIGHT_GRAY.asHexString()}>OldOreData Transporter",
	))

	val MINHAUL_CHETHERITE = registerTemplate(StarshipTemplate(
		schematicName = "Minhaul_chetherite",
		type = AI_SHUTTLE,
		miniMessageName = "<${EXPLORER_MEDIUM_CYAN.asHexString()}>Minhaul <${HE_LIGHT_GRAY.asHexString()}>[<light_purple>Chetherite<${HE_LIGHT_GRAY.asHexString()}>]",
	))

	val MINHAUL_REDSTONE = registerTemplate(StarshipTemplate(
		schematicName = "Minhaul_redstone",
		type = AI_SHUTTLE,
		miniMessageName = "<${EXPLORER_MEDIUM_CYAN.asHexString()}>Minhaul <${HE_LIGHT_GRAY.asHexString()}>[<red>Redstone<${HE_LIGHT_GRAY.asHexString()}>]",
	))

	val MINHAUL_TITANIUM = registerTemplate(StarshipTemplate(
		schematicName = "Minhaul_chetherite",
		type = AI_SHUTTLE,
		miniMessageName = "<${EXPLORER_MEDIUM_CYAN.asHexString()}>Minhaul <${HE_LIGHT_GRAY.asHexString()}>[<gray>Titanium<${HE_LIGHT_GRAY.asHexString()}>]",
	))

	val EXOTRAN_TITANIUM = registerTemplate(StarshipTemplate(
		schematicName = "Exotran_titanium",
		type = AI_LIGHT_FREIGHTER,
		miniMessageName = "<${EXPLORER_MEDIUM_CYAN.asHexString()}>Exotran <${HE_LIGHT_GRAY.asHexString()}>[<gray>Titanium<${HE_LIGHT_GRAY.asHexString()}>]",
	))

	val EXOTRAN_CHETHERITE = registerTemplate(StarshipTemplate(
		schematicName = "Exotran_chetherite",
		type = AI_LIGHT_FREIGHTER,
		miniMessageName = "<${EXPLORER_MEDIUM_CYAN.asHexString()}>Exotran <${HE_LIGHT_GRAY.asHexString()}>[<light_purple>Chetherite<${HE_LIGHT_GRAY.asHexString()}>]",
	))

	val EXOTRAN_REDSTONE = registerTemplate(StarshipTemplate(
		schematicName = "Exotran_redstone",
		type = AI_LIGHT_FREIGHTER,
		miniMessageName = "<${EXPLORER_MEDIUM_CYAN.asHexString()}>Exotran <${HE_LIGHT_GRAY.asHexString()}>[<red>Redstone<${HE_LIGHT_GRAY.asHexString()}>]",
	))

	val AMPH = registerTemplate(StarshipTemplate(
		schematicName = "Amph",
		type = AI_TRANSPORT,
		miniMessageName = "<${EXPLORER_MEDIUM_CYAN.asHexString()}>Amph",
	))

	// END_EXPLORER
	// START_PRIVATEER

	val BULWARK = registerTemplate(StarshipTemplate(
		schematicName = "Bulwark",
		type = AI_CORVETTE,
		miniMessageName = "<$PRIVATEER_DARK_TEAL>Bulwark",
		manualWeaponSets = mutableSetOf(
			WeaponSet(name = "main", engagementRangeMin = 0.0, engagementRangeMax = 550.0)
		),
		autoWeaponSets = mutableSetOf(
			WeaponSet(name = "auto", engagementRangeMin = 0.0, engagementRangeMax = 550.0)
		)
	))

	val CONTRACTOR = registerTemplate(StarshipTemplate(
		schematicName = "Contractor",
		type = AI_GUNSHIP,
		miniMessageName = "<$PRIVATEER_MEDIUM_TEAL>Contractor",
		manualWeaponSets = mutableSetOf(
			WeaponSet(name = "manual", engagementRangeMin = 0.0, engagementRangeMax = 550.0)
		),
		autoWeaponSets = mutableSetOf(
			WeaponSet(name = "auto", engagementRangeMin = 0.0, engagementRangeMax = 550.0)
		)
	))

	val DAGGER = registerTemplate(StarshipTemplate(
		schematicName = "Dagger",
		type = AI_STARFIGHTER,
		miniMessageName = "<$PRIVATEER_LIGHT_TEAL>Dagger",
	))

	val DAYBREAK = registerTemplate(StarshipTemplate(
		schematicName = "Daybreak",
		type = AI_CORVETTE,
		miniMessageName = "<$PRIVATEER_LIGHT_TEAL>Daybreak",
	))

	val PATROLLER = registerTemplate(StarshipTemplate(
		schematicName = "Patroller",
		type = AI_GUNSHIP,
		miniMessageName = "<$PRIVATEER_LIGHT_TEAL>Patroller",
	))

	val PROTECTOR = registerTemplate(StarshipTemplate(
		schematicName = "Protector",
		type = AI_GUNSHIP,
		miniMessageName = "<$PRIVATEER_LIGHT_TEAL>Patroller",
	))

	val VETERAN = registerTemplate(StarshipTemplate(
		schematicName = "Veteran",
		type = AI_GUNSHIP,
		miniMessageName = "<$PRIVATEER_LIGHT_TEAL>Veteran",
	))

	val TENETA = registerTemplate(StarshipTemplate(
		schematicName = "Teneta",
		type = AI_STARFIGHTER,
		miniMessageName = "<$PRIVATEER_LIGHT_TEAL>Teneta",
	))

	val FURIOUS = registerTemplate(StarshipTemplate(
		schematicName = "Furious",
		type = AI_STARFIGHTER,
		miniMessageName = "<$PRIVATEER_LIGHT_TEAL>Furious",
	))

	val INFLICT = registerTemplate(StarshipTemplate(
		schematicName = "Inflict",
		type = AI_STARFIGHTER,
		miniMessageName = "<$PRIVATEER_LIGHT_TEAL>Inflict",
	))

	// END_PRIVATEER
	// START_MINING_GUILD

	val OSTRICH = registerTemplate(StarshipTemplate(
		schematicName = "Ostrich",
		type = AI_LIGHT_FREIGHTER,
		miniMessageName = "<$MINING_CORP_DARK_ORANGE>Ostrich",
	))

	val WOODPECKER = registerTemplate(StarshipTemplate(
		schematicName = "Ostrich",
		type = AI_SHUTTLE,
		miniMessageName = "<$MINING_CORP_LIGHT_ORANGE>Woodpecker",
	))

	val BEAVER = registerTemplate(StarshipTemplate(
		schematicName = "Beaver",
		type = AI_TRANSPORT,
		miniMessageName = "<$MINING_CORP_LIGHT_ORANGE>Beaver",
	))

	val BADGER = registerTemplate(StarshipTemplate(
		schematicName = "Badger",
		type = AI_LIGHT_FREIGHTER,
		miniMessageName = "<$MINING_CORP_DARK_ORANGE>Badger",
	))

	val TYPE_V11 = registerTemplate(StarshipTemplate(
		schematicName = "typeV11",
		type = AI_STARFIGHTER,
		miniMessageName = "<$MINING_CORP_LIGHT_ORANGE>Type <$HE_LIGHT_GRAY>V-11",
	))

	val TYPEA21B = registerTemplate(StarshipTemplate(
		schematicName = "typeA21b",
		type = AI_SHUTTLE,
		miniMessageName = "<$MINING_CORP_LIGHT_ORANGE>Type <$HE_LIGHT_GRAY>A-21b",
	))

	val TYPEI41 = registerTemplate(StarshipTemplate(
		schematicName = "typeI41",
		type = AI_SHUTTLE,
		miniMessageName = "<$MINING_CORP_LIGHT_ORANGE>Type <$HE_LIGHT_GRAY>I-41",
	))

	// END_MINING_GUILD
	// START_TSAII

	val SWARMER = registerTemplate(StarshipTemplate(
		schematicName = "Swarmer",
		type = AI_STARFIGHTER,
		miniMessageName = "<$TSAII_DARK_ORANGE>Swarmer",
	))

	val SCYTHE = registerTemplate(StarshipTemplate(
		schematicName = "Swarmer",
		type = AI_STARFIGHTER,
		miniMessageName = "<$TSAII_DARK_ORANGE>Scythe",
	))

	val RAIDER = registerTemplate(StarshipTemplate(
		schematicName = "Raider",
		type = AI_GUNSHIP,
		miniMessageName = "<$TSAII_VERY_DARK_ORANGE>Raider",
	))

	val REAVER = registerTemplate(StarshipTemplate(
		schematicName = "Reaver",
		type = AI_FRIGATE,
		miniMessageName = "<$TSAII_VERY_DARK_ORANGE>Reaver",
		manualWeaponSets = mutableSetOf(WeaponSet(
			name = "manual",
			engagementRangeMin = 0.0,
			engagementRangeMax = 220.0
		)),
		autoWeaponSets = mutableSetOf(WeaponSet(
			name = "auto",
			engagementRangeMin = 250.0,
			engagementRangeMax = 550.0
		))
	))

	val BASTION = registerTemplate(StarshipTemplate(
		schematicName = "Swarmer",
		type = AI_BATTLECRUISER,
		miniMessageName = "<$TSAII_VERY_DARK_ORANGE>Bastion",
	))

	// END_TSAII
	val SKUTTLE = registerTemplate(StarshipTemplate(
		schematicName = "Skuttle",
		type = AI_STARFIGHTER,
		miniMessageName = "<dark_red>Skuttle",
	))

	val PUMPKIN_DEVOURER = registerTemplate(StarshipTemplate(
		schematicName = "pumpkin_devourer",
		type = UNIDENTIFIEDSHIP,
		miniMessageName = "<#FFA500>Pumpkin Devourer",
	))

	val PUMPKIN_KIN = registerTemplate(StarshipTemplate(
		schematicName = "small_pumpkin",
		type = UNIDENTIFIEDSHIP,
		miniMessageName = "<#FFA500>Pumpkin Kin",
	))

	val DREDGE = registerTemplate(StarshipTemplate(
		schematicName = "Dredge",
		type = UNIDENTIFIEDSHIP,
		miniMessageName = "<$ABYSSAL_DESATURATED_RED>Dredge",
	))

	val EMPEROR = registerTemplate(StarshipTemplate(
		schematicName = "Emperor",
		type = UNIDENTIFIEDSHIP,
		miniMessageName = "<$ABYSSAL_LIGHT_RED>Emperor",
	))

	val HIGH_PRIESTESS = registerTemplate(StarshipTemplate(
		schematicName = "HighPriestess",
		type = UNIDENTIFIEDSHIP,
		miniMessageName = "<$ABYSSAL_DARK_RED>High Priestess",
	))

	val GRAFT = registerTemplate(StarshipTemplate(
		schematicName = "Graft",
		type = UNIDENTIFIEDSHIP,
		miniMessageName = "<$ABYSSAL_DARK_RED>Graft",
	))

	val CHARM = registerTemplate(StarshipTemplate(
		schematicName = "Charm",
		type = UNIDENTIFIEDSHIP,
		miniMessageName = "<$ABYSSAL_DARK_RED>Charm",
	))

	private fun registerTemplate(default: StarshipTemplate): StarshipTemplate {
		return Configuration.loadOrDefault(TEMPLATE_DIRECTORY, "${default.schematicName}.json", default)
	}
}
