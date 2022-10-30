package net.horizonsend.ion.common.database.enums

enum class Ranktrack(val displayName: String, val colour: String, val description: String, val ranks: List<Rank>) {
	REFUGEE("Refugee", "<white>", "Placeholder", Refugee.values().toList()),
	OUTLAW("Outlaw", "<red>", "Placeholder", Outlaw.values().toList()),
	PRIVATEER("Privateer", "<blue>", "Placeholder", Privateer.values().toList()),
	INDUSTRIALIST("Industrialist", "<green>", "Placeholder", Industrialist.values().toList());

	interface Rank {
		val displayName: String
		val colour: String
		val experienceRequirement: Int
		val parentRanktrack: Ranktrack
		val maxWarShipLevel: Int
		val maxTradeShipLevel: Int
		val maxBlueprints: Int
		val levelPriority: Int
		val icon: Int
	}

	enum class Refugee(
		override val colour: String,
		override val displayName: String,
		override val experienceRequirement: Int,
		override val maxWarShipLevel: Int,
		override val maxTradeShipLevel: Int,
		override val maxBlueprints: Int,
		override val levelPriority: Int,
		override val icon: Int
	) : Rank {
		REFUGEE("<white>", "Refugee", 0, 0, 1, 10, 0, 501);

		override val parentRanktrack = Ranktrack.REFUGEE
	}

	enum class Outlaw(
		override val colour: String,
		override val displayName: String,
		override val experienceRequirement: Int,
		override val maxWarShipLevel: Int,
		override val maxTradeShipLevel: Int,
		override val maxBlueprints: Int,
		override val levelPriority: Int,
		override val icon: Int
	) : Rank {
		PIRATE("<red>", "Pirate", 1, 1, 1, 20, 1, 502),
		RAIDER("<red>", "Raider", 7000, 2, 1, 40, 2, 503),
		CORSAIR("<dark_red>", "Corsair", 25000, 3, 2, 60, 3, 504),
		MARAUDER("<dark_red>", "Marauder", 65000, 4, 2, 80, 4, 505),
		WARLORD("<purple>", "Warlord", 150000, 5, 3, 100, 5, 506);

		override val parentRanktrack = Ranktrack.OUTLAW
	}

	enum class Privateer(
		override val colour: String,
		override val displayName: String,
		override val experienceRequirement: Int,
		override val maxWarShipLevel: Int,
		override val maxTradeShipLevel: Int,
		override val maxBlueprints: Int,
		override val levelPriority: Int,
		override val icon: Int
	) : Rank {
		PRIVATEER("<color:#07cadb>", "Privateer", 1, 1, 1, 20, 1, 507),
		CAPTAIN("<color:#07cadb>", "Lieutenant", 7000, 2, 1, 40, 2, 508),
		COMMANDER("<color:#0f99f5>", "Captain", 25000, 3, 2, 60, 3, 509),
		BATTLEMASTER("<color:#0f99f5>", "Commander", 65000, 4, 2, 80, 4, 510),
		ADMIRAL("<blue>", "Admiral", 150000, 5, 3, 100, 5, 511);

		override val parentRanktrack: Ranktrack = Ranktrack.PRIVATEER
	}

	enum class Industrialist(
		override val colour: String,
		override val displayName: String,
		override val experienceRequirement: Int,
		override val maxWarShipLevel: Int,
		override val maxTradeShipLevel: Int,
		override val maxBlueprints: Int,
		override val levelPriority: Int,
		override val icon: Int
	) : Rank {
		COLONIST("<lime>", "Colonist", 1, 1, 1, 20, 1, 512),
		BARON("<lime>", "Baron", 18000, 1, 2, 40, 2, 513),
		VIZIER("<green>", "Vizier", 34375, 2, 3, 60, 3, 514),
		VICEROY("<dark_green>", "Viceroy", 230625, 2, 4, 80, 4, 515),
		MAGISTRATE("<gold>", "Magistrate", 1_808_750, 3, 5, 100, 5, 516);

		override val parentRanktrack: Ranktrack = Ranktrack.INDUSTRIALIST
	}
}
