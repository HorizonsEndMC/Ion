package net.horizonsend.ion.common.database.enums

import java.awt.Color

enum class Ranktrack(val displayName: String, val colour: Color, val description: String, val ranks: List<Rank>) {
	REFUGEE("Refugee", Color.WHITE, "Placeholder", Refugee.values().toList()),
	OUTLAW("Outlaw", Color.WHITE, "Placeholder", Outlaw.values().toList()),
	PRIVATEER("Privateer", Color.WHITE, "Placeholder", Privateer.values().toList()),
	INDUSTRIALIST("Industrialist", Color.WHITE, "Placeholder", Industrialist.values().toList());

	interface Rank {
		val displayName: String
		val colour: Color
		val experienceRequirement: Int
		val parentRanktrack: Ranktrack
	}

	enum class Refugee(
		override val colour: Color,
		override val displayName: String,
		override val experienceRequirement: Int
	) : Rank {
		REFUGEE(Color.WHITE, "Refugee", 0);

		override val parentRanktrack = Ranktrack.REFUGEE
	}

	enum class Outlaw(
		override val colour: Color,
		override val displayName: String,
		override val experienceRequirement: Int
	) : Rank {
		PIRATE(Color.WHITE, "Pirate", 1),
		RAIDER(Color.WHITE, "Raider", 7000),
		CORSAIR(Color.WHITE, "Corsair", 25000),
		MARAUDER(Color.WHITE, "Marauder", 65000),
		WARLORD(Color.WHITE, "Warlord", 150000);

		override val parentRanktrack = Ranktrack.OUTLAW
	}

	enum class Privateer(
		override val colour: Color,
		override val displayName: String,
		override val experienceRequirement: Int
	) : Rank {
		PRIVATEER(Color.WHITE, "Privateer", 1),
		CAPTAIN(Color.WHITE, "Lieutenant", 7000),
		COMMANDER(Color.WHITE, "Captain", 25000),
		BATTLEMASTER(Color.WHITE, "Commander", 65000),
		ADMIRAL(Color.WHITE, "Admiral", 150000);

		override val parentRanktrack: Ranktrack = Ranktrack.PRIVATEER
	}

	enum class Industrialist(
		override val colour: Color,
		override val displayName: String,
		override val experienceRequirement: Int
	) : Rank {
		COLONIST(Color.WHITE, "Colonist", 1),
		BARON(Color.WHITE, "Baron", 18000),
		VIZIER(Color.WHITE, "Vizier", 34375),
		VICEROY(Color.WHITE, "Viceroy", 230625),
		MAGISTRATE(Color.WHITE, "Magistrate", 1_808_750);

		override val parentRanktrack: Ranktrack = Ranktrack.INDUSTRIALIST
	}
}
