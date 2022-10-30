package net.horizonsend.ion.common.database.enums

enum class RanktrackTraits(val traits: List<Traits>) {
	REFUGEE(RefugeeTraits.values().toList()),
	OUTLAW(OutlawTraits.values().toList());
	interface Traits {
		val displayName: String
		val parentRanktrack: Ranktrack
		val ranktrackRank: Ranktrack.Rank
		val description: String
		val icon: Int
		val colour: Int
	}
	enum class RefugeeTraits(
		override val displayName: String,
		override val parentRanktrack: Ranktrack = Ranktrack.REFUGEE,
		override val ranktrackRank: Ranktrack.Rank,
		override val description: String,
		override val icon: Int,
		override val colour: Int = 10660003
		) : Traits {
			REFUGEE("Refugee", Ranktrack.REFUGEE, Ranktrack.Refugee.REFUGEE, "You are a Refugee from the war-torn 7b, here to make a home among the stars of the perseus sector", 520)
		}
	enum class OutlawTraits(
		override val displayName: String,
		override val parentRanktrack: Ranktrack = Ranktrack.OUTLAW,
		override val ranktrackRank: Ranktrack.Rank,
		override val description: String,
		override val icon: Int,
		override val colour: Int = 16711680
	) : Traits {
		CRATE_STEALING("Crate Stealing", Ranktrack.OUTLAW, Ranktrack.Outlaw.PIRATE, "After stealing crates from colonists, you can trade them in for money", 521),
		MICRO_DRIVE("Micro Drive", Ranktrack.OUTLAW, Ranktrack.Outlaw.RAIDER, "A micro Warp drive capable of skipping your ship forward in space.", 522),
	}
}