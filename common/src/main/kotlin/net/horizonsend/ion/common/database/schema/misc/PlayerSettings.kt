package net.horizonsend.ion.common.database.schema.misc

import com.mongodb.client.result.InsertOneResult
import net.horizonsend.ion.common.database.DbObject
import net.horizonsend.ion.common.database.DbObjectCompanion
import net.horizonsend.ion.common.database.none
import net.horizonsend.ion.common.database.trx
import org.litote.kmongo.eq
import org.litote.kmongo.id.StringId

data class PlayerSettings(
	override val _id: StringId<PlayerSettings>,

	var contactsDistance: Int = 6000,
	var contactsMaxNameLength: Int = 64,
	var contactsSort: Int = 0,
	var contactsColoring: Int = 0,
	var contactsEnabled: Boolean = true,
	var contactsStarships: Boolean = true,
	var lastStarshipEnabled: Boolean = true,
	var planetsEnabled: Boolean = true,
	var starsEnabled: Boolean = true,
	var beaconsEnabled: Boolean = true,
	var stationsEnabled: Boolean = false,
	var bookmarksEnabled: Boolean = true,
	var relationAiEnabled: Boolean = true,
	var relationNoneEnabled: Boolean = true,
	var relationEnemyEnabled: Boolean = true,
	var relationUnfriendlyEnabled: Boolean = true,
	var relationNeutralEnabled: Boolean = true,
	var relationFriendlyEnabled: Boolean = true,
	var relationAllyEnabled: Boolean = true,
	var relationNationEnabled: Boolean = true,
	var relationAiStationEnabled: Boolean = true,
	var relationNoneStationEnabled: Boolean = true,
	var relationEnemyStationEnabled: Boolean = true,
	var relationUnfriendlyStationEnabled: Boolean = true,
	var relationNeutralStationEnabled: Boolean = true,
	var relationFriendlyStationEnabled: Boolean = true,
	var relationAllyStationEnabled: Boolean = true,
	var relationNationStationEnabled: Boolean = true,
	var waypointsEnabled: Boolean = true,
	var compactWaypoints: Boolean = true,
	var starshipsEnabled: Boolean = true,
	var advancedStarshipInfo: Boolean = false,
	var rotateCompass: Boolean = false,
	var combatTimerEnabled: Boolean = true,

	var hudPlanetsImage: Boolean = true,
	var hudPlanetsSelector: Boolean = true,
	var hudIconStars: Boolean = true,
	var hudIconBeacons: Boolean = true,
	var hudIconStations: Boolean = false,
	var hudIconBookmarks: Boolean = false,

	var showItemSearchItem: Boolean = true,
	var protectionMessagesEnabled: Boolean = true,
	var useAlternateDCCruise: Boolean = false,
	var dcRefreshRate: Int = 1,
	var enableAdditionalSounds: Boolean = true,
	var soundCruiseIndicator: Int = 0,
	var enableCombatTimerAlerts: Boolean = true,
	var hitmarkerOnHull: Boolean = true,
	var hitmarkerOnShield: Boolean = true,
	var flareTime: Int = 5,
	var useAlternateShieldHitParticle : Boolean = false,

	var shortenChatChannels: Boolean = false,

	var hideUserPrefixes: Boolean = false,
	var hideGlobalPrefixes: Boolean = false,

	var defaultBazaarGroupedSort: Int = 0,
	var defaultBazaarIndividualSort: Int = 0,
	var defaultBazaarListingManagementSort: Int = 0,
	var skipBazaarSingleEntryMenus: Boolean = false,
	var listingManageDefaultListView: Boolean = false,
	var orderManageDefaultListView: Boolean = false,
	var orderBrowseSort: Int = 0,
	var orderManageSort: Int = 0,

	var bazaarSellBrowseFilters: String = "{}",
	var bazaarSellManageFilters: String = "{}",
	var bazaarOrderBrowseFilters: String = "{}",
	var bazaarOrderManageFilters: String = "{}",

	var displayEntityVisibility: Int = 0,
	var nearbyWeaponSounds: Int = 0,
	var farWeaponSounds: Int = 0,
	var floatWhileDc: Boolean = true,
	var reverseDcBoost: Boolean = false,
	var toggleDcBoost: Boolean = false,
	var alternateFireButtons: Boolean = false,
	var chestShopDisplays: Boolean = true,
	var miningLaserEffectLevel: Int = 3,
	var fleetStatus: Boolean = true,
) : DbObject {
	companion object : DbObjectCompanion<PlayerSettings, StringId<PlayerSettings>>(PlayerSettings::class, setup = {}) {
		fun create(id: StringId<PlayerSettings>): InsertOneResult = trx { session ->
			require(col.none(session, PlayerSettings::_id eq id))

			col.insertOne(session, PlayerSettings(id))
		}
	}
}
