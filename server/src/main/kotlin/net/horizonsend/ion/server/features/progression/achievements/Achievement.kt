package net.horizonsend.ion.server.features.progression.achievements

import net.horizonsend.ion.server.miscellaneous.registrations.persistence.NamespacedKeys
import net.kyori.adventure.key.Key

enum class Achievement(
	val title: String,
	val description: String,
	val creditReward: Int,
	val experienceReward: Int,
	val chetheriteReward: Int,
	val icon: Key?
) {
	PLANET_AERACH("Aerach really rocks!", "Visit: Aerach", 750, 125, 0, NamespacedKeys.packKey("planet_icon/aerach")), // Kwazedilla
	PLANET_ARET("I hate sand", "Visit: Aret", 750, 125, 0, NamespacedKeys.packKey("planet_icon/aret")), // Wither
	PLANET_CHANDRA("One small step", "Visit: Chandra", 750, 125, 0, NamespacedKeys.packKey("planet_icon/chandra")), // Wither
	PLANET_CHIMGARA("Dont get trypophobia", "Visit: Chimgara", 750, 125, 0, NamespacedKeys.packKey("planet_icon/chimgara")), // Gutin
	PLANET_DAMKOTH("The cheth must flow", "Visit: Damkoth", 750, 125, 0, NamespacedKeys.packKey("planet_icon/damkoth")), // Wither
	PLANET_GAHARA("Ice Ice baby", "Visit: Gahara", 750, 125, 0, NamespacedKeys.packKey("planet_icon/gahara")), // Sciath
	PLANET_HERDOLI("The red planet", "Visit: Herdoli", 750, 125, 0, NamespacedKeys.packKey("planet_icon/herdoli")), // Wither
	PLANET_ILIUS("Welcome to HE!", "Visit: Ilius", 750, 125, 0, NamespacedKeys.packKey("planet_icon/ilius")), // Vandrayk
	PLANET_ISIK("Ashes to Ashes..", "Visit: Isik", 750, 125, 0, NamespacedKeys.packKey("planet_icon/isik")), // Kwazedilla
	PLANET_KOVFEFE("It's not chocolate", "Visit: Kovfefe", 750, 125, 0, NamespacedKeys.packKey("planet_icon/kovfefe")), // Astral
	PLANET_KRIO("Snowball fight", "Visit: Krio", 750, 125, 0, NamespacedKeys.packKey("planet_icon/krio")), // Vandrayk
	PLANET_LIODA("Caves and Cliffs", "Visit: Lioda", 750, 125, 0, NamespacedKeys.packKey("planet_icon/lioda")), // Vandrayk
	PLANET_LUXITERNA("Supercritical", "Visit: Luxiterna", 750, 125, 0, NamespacedKeys.packKey("planet_icon/luxiterna")), // Gutin
	PLANET_QATRA("What happened here", "Visit: Qatra", 750, 125, 0, NamespacedKeys.packKey("planet_icon/qatra")), // GenBukkit
	PLANET_RUBACIEA("Icy-Hot", "Visit: Rubaciea", 750, 125, 0, NamespacedKeys.packKey("planet_icon/rubaciea")), // Liluzivirt
	PLANET_TURMS("Turms and Conditions", "Visit: Turms", 750, 125, 0, NamespacedKeys.packKey("planet_icon/turms")), // Kwazedilla
	PLANET_VASK("Ooh! A penny!", "Visit: Vask", 750, 125, 0, NamespacedKeys.packKey("planet_icon/vask")), // Vandrayk
	SYSTEM_ASTERI("New beginnings", "Visit: Asteri System", 2500, 400, 0, NamespacedKeys.packKey("achievement_icon/asteri")), // Gutin
	SYSTEM_ILIOS("Not the planet", "Visit: Ilios System", 2500, 400, 0, NamespacedKeys.packKey("achievement_icon/ilios")), // Kwazedilla
	SYSTEM_REGULUS("Middle Ground", "Visit: Regulus System", 2500, 400, 0, NamespacedKeys.packKey("achievement_icon/regulus")), // Sciath
	SYSTEM_SIRIUS("Why So Sirius?", "Visit: Sirius System", 2500, 400, 0, NamespacedKeys.packKey("achievement_icon/sirius")), // Sciath
	ACQUIRE_ALUMINIUM("Pronounced Aluminium", "Obtain Aluminium", 250, 100, 0, NamespacedKeys.packKey("mineral/aluminum")), // Gutin
	ACQUIRE_CHETHERITE("Unleaded", "Obtain Chetherite", 250, 100, 0, NamespacedKeys.packKey("mineral/chetherite")), // Gutin
	ACQUIRE_TITANIUM("Future's Material", "Obtain Titanium", 250, 100, 0, NamespacedKeys.packKey("mineral/titanium")), // Gutin + Astral
	ACQUIRE_URANIUM("Split the atom", "Obtain Uranium", 250, 100, 0, NamespacedKeys.packKey("mineral/uranium")), // Astral
	BUY_SPAWN_SHUTTLE("Space and beyond", "Buy a spawn shuttle", 250, 100, 0, NamespacedKeys.packKey("achievement_icon/shuttle_buy")), // Sciath + Astral
	CAPTURE_STATION("Bear eliminated", "Capture a station", 5000, 750, 0, NamespacedKeys.packKey("achievement_icon/station_capture")), // Liluzivirt
	COMPLETE_CARGO_RUN("Space trucking", "Complete a shipment", 500, 125, 0, null), // Astral
	COMPLETE_TUTORIAL("Space Cadet", "Complete the Tutorial", 1000, 250, 0, NamespacedKeys.packKey("achievement_icon/complete_tutorial")), // Wither
	CREATE_NATION("Galactic Power", "Found a nation", 5000, 500, 0, NamespacedKeys.packKey("achievement_icon/create_nation")), // Vandrayk
	CREATE_OUTPOST("Manifest Destiny", "Create a nation claim", 2500, 250, 0, NamespacedKeys.packKey("achievement_icon/create_outpost")), // Vandrayk
	CREATE_SETTLEMENT("Breaking ground", "Found a settlement", 1000, 250, 0, NamespacedKeys.packKey("achievement_icon/create_settlement")), // Astral
	DETECT_MULTIBLOCK("Industrial Revolution", "Detect a multiblock", 500, 125, 0, null), // Wither + Astral
	DETECT_SHIP("All in working order", "Detect a starship", 100, 50, 0, null), // Vandrayk
	KILL_PLAYER("Carried away", "Kill a player", 250, 100, 0, NamespacedKeys.packKey("achievement_icon/player_kill")), // Astral
	KILL_SHIP("Tango Down", "Shoot down a ship", 1000, 250, 0, NamespacedKeys.packKey("achievement_icon/kill_ship")), // Vandrayk
	LEVEL_10("What do we do now?", "Reach level 10", 1000, 250, 0, NamespacedKeys.packKey("achievement_icon/level_10")), // Vandrayk
	LEVEL_20("Where it begins", "Reach level 20", 2000, 350, 0, NamespacedKeys.packKey("achievement_icon/level_20")), // Kwazedilla + Astral
	LEVEL_40("Sorry for the pain", "Reach level 40", 5000, 500, 0, NamespacedKeys.packKey("achievement_icon/level_40")), // Sciath
	LEVEL_80("Overwhelming power", "Reach level 80", 10000, 1000, 0, NamespacedKeys.packKey("achievement_icon/level_80")), // Astral
	SIEGE_STATION("Poking the bear", "Participate in a siege", 500, 125, 0, NamespacedKeys.packKey("achievement_icon/station_siege")), // Gutin
	USE_HYPERSPACE("Ludicrous Speed!", "Use hyperspace", 250, 75, 8, NamespacedKeys.packKey("achievement_icon/hyperspace")); // Liluzivirt
}
