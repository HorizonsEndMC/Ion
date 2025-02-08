package net.horizonsend.ion.server.features.gui.custom.settings

import net.horizonsend.ion.common.database.cache.nations.AbstractPlayerCache
import net.horizonsend.ion.common.database.schema.misc.SLPlayer
import net.horizonsend.ion.server.command.misc.IonSitCommand.sitStateNode
import net.horizonsend.ion.server.features.gui.GuiItem
import net.horizonsend.ion.server.features.gui.GuiItems
import net.horizonsend.ion.server.features.gui.custom.settings.button.DBCachedBooleanToggle
import net.horizonsend.ion.server.features.gui.custom.settings.button.DBCachedEnumCycle
import net.horizonsend.ion.server.features.gui.custom.settings.button.DBCachedIntCycle
import net.horizonsend.ion.server.features.gui.custom.settings.button.DBCachedIntegerInput
import net.horizonsend.ion.server.features.gui.custom.settings.button.PermissionBooleanToggle
import net.horizonsend.ion.server.features.gui.custom.settings.commands.SoundSettingsCommand.CruiseIndicatorSounds
import net.horizonsend.ion.server.features.sidebar.MainSidebar
import net.horizonsend.ion.server.features.sidebar.tasks.ContactsSidebar.ContactsColoring
import net.horizonsend.ion.server.features.sidebar.tasks.ContactsSidebar.ContactsSorting
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.Component.text
import org.bukkit.entity.Player
import xyz.xenondevs.invui.item.Item
import xyz.xenondevs.invui.item.impl.SimpleItem

class SettingsMainMenuGui(player: Player) : SettingsPageGui(player, "Settings") {
	override fun getFirstLine(player: Player): Component {
		return Component.empty()
	}

	override fun getSecondLine(player: Player): Component {
		return Component.empty()
	}

	override fun makeButton(pageGui: SettingsPageGui): GuiItems.AbstractButtonItem {
		return GuiItem.LIST.makeButton(this, title, "") { _, _, _ -> open() }
	}

	override val backButton: Item = SimpleItem(GuiItems.blankItem)

    override val buttonsList = listOf<SettingsPageGui>(
		createSettingsPage(player, "Control Settings",
			DBCachedBooleanToggle(text("DC Overrides Cruise"), "", GuiItem.GUNSHIP, false, SLPlayer::useAlternateDCCruise, AbstractPlayerCache.PlayerData::useAlternateDCCruise),
			DBCachedIntCycle(5, 1, text("DC Speed Modifier"), "", GuiItem.GUNSHIP, 1, SLPlayer::dcSpeedModifier, AbstractPlayerCache.PlayerData::dcSpeedModifier)
		),
		createSettingsPage(player, "Sidebar Settings",
			createSettingsPage(player, "Combat Timer Settings",
				DBCachedBooleanToggle(text("Enable Combat Timer Info"), "", GuiItem.LIST, true, SLPlayer::combatTimerEnabled, AbstractPlayerCache.PlayerData::combatTimerEnabled)
			),
			createSettingsPage(player, "Starships Settings",
				DBCachedBooleanToggle(text("Enable Starship Info"), "", GuiItem.LIST, true, SLPlayer::starshipsEnabled, AbstractPlayerCache.PlayerData::starshipsEnabled),
				DBCachedBooleanToggle(text("Display Advanced Info"), "", GuiItem.LIST, false, SLPlayer::advancedStarshipInfo, AbstractPlayerCache.PlayerData::advancedStarshipInfo),
				DBCachedBooleanToggle(text("Rotating Compass"), "", GuiItem.COMPASS_NEEDLE, false, SLPlayer::rotateCompass, AbstractPlayerCache.PlayerData::rotateCompass)
			),
			createSettingsPage(player, "Contacts Settings",
				DBCachedBooleanToggle(text("Enable Contacts Info"), "", GuiItem.LIST, true, SLPlayer::contactsEnabled, AbstractPlayerCache.PlayerData::contactsEnabled),
				DBCachedIntegerInput(0, MainSidebar.CONTACTS_RANGE, text("Change Contacts Range"), "", GuiItem.ROUTE_SEGMENT, 6000, SLPlayer::contactsDistance, AbstractPlayerCache.PlayerData::contactsDistance),
				DBCachedIntegerInput(1, MainSidebar.MAX_NAME_LENGTH, text("Change Max Name Length"), "", GuiItem.LIST, 6000, SLPlayer::contactsMaxNameLength, AbstractPlayerCache.PlayerData::contactsMaxNameLength),
				DBCachedEnumCycle(ContactsSorting::class.java, text("Change Sort Order"), "", GuiItem.LIST, 0, SLPlayer::contactsSort, AbstractPlayerCache.PlayerData::contactsSort),
				DBCachedEnumCycle(ContactsColoring::class.java, text("Change Coloring"), "", GuiItem.LIST, 0, SLPlayer::contactsColoring, AbstractPlayerCache.PlayerData::contactsColoring),
				DBCachedBooleanToggle(text("Enable Starships"), "", GuiItem.GUNSHIP, true, SLPlayer::contactsStarships, AbstractPlayerCache.PlayerData::contactsStarships),
				DBCachedBooleanToggle(text("Enable Last Starship"), "", GuiItem.GENERIC_STARSHIP, true, SLPlayer::lastStarshipEnabled, AbstractPlayerCache.PlayerData::lastStarshipEnabled),
				DBCachedBooleanToggle(text("Enable Planets"), "", GuiItem.PLANET, true, SLPlayer::planetsEnabled, AbstractPlayerCache.PlayerData::planetsEnabled),
				DBCachedBooleanToggle(text("Enable Stars"), "", GuiItem.STAR, true, SLPlayer::starsEnabled, AbstractPlayerCache.PlayerData::starsEnabled),
				DBCachedBooleanToggle(text("Enable Beacons"), "", GuiItem.BEACON, true, SLPlayer::beaconsEnabled, AbstractPlayerCache.PlayerData::beaconsEnabled),
				DBCachedBooleanToggle(text("Enable Stations"), "", GuiItem.STATION, true, SLPlayer::stationsEnabled, AbstractPlayerCache.PlayerData::stationsEnabled),
				DBCachedBooleanToggle(text("Enable Bookmarks"), "", GuiItem.BOOKMARK, true, SLPlayer::bookmarksEnabled, AbstractPlayerCache.PlayerData::bookmarksEnabled),
				DBCachedBooleanToggle(text("Enable AI Starships"), "", GuiItem.GUNSHIP, true, SLPlayer::relationAiEnabled, AbstractPlayerCache.PlayerData::relationAiEnabled),
				DBCachedBooleanToggle(text("Enable No Relation Starships"), "", GuiItem.GUNSHIP, true, SLPlayer::relationNoneEnabled, AbstractPlayerCache.PlayerData::relationNoneEnabled),
				DBCachedBooleanToggle(text("Enable Enemy Starships"), "", GuiItem.GUNSHIP, true, SLPlayer::relationEnemyEnabled, AbstractPlayerCache.PlayerData::relationEnemyEnabled),
				DBCachedBooleanToggle(text("Enable Unfriendly Starships"), "", GuiItem.GUNSHIP, true, SLPlayer::relationUnfriendlyEnabled, AbstractPlayerCache.PlayerData::relationUnfriendlyEnabled),
				DBCachedBooleanToggle(text("Enable Neutral Starships"), "", GuiItem.GUNSHIP, true, SLPlayer::relationNeutralEnabled, AbstractPlayerCache.PlayerData::relationNeutralEnabled),
				DBCachedBooleanToggle(text("Enable Friendly Starships"), "", GuiItem.GUNSHIP, true, SLPlayer::relationFriendlyEnabled, AbstractPlayerCache.PlayerData::relationFriendlyEnabled),
				DBCachedBooleanToggle(text("Enable Ally Starships"), "", GuiItem.GUNSHIP, true, SLPlayer::relationAllyEnabled, AbstractPlayerCache.PlayerData::relationAllyEnabled),
				DBCachedBooleanToggle(text("Enable Nation Starships"), "", GuiItem.GUNSHIP, true, SLPlayer::relationNationEnabled, AbstractPlayerCache.PlayerData::relationNationEnabled),
				DBCachedBooleanToggle(text("Enable AI Stations"), "", GuiItem.GUNSHIP, true, SLPlayer::relationAiStationEnabled, AbstractPlayerCache.PlayerData::relationAiStationEnabled),
				DBCachedBooleanToggle(text("Enable No Relation Stations"), "", GuiItem.GUNSHIP, true, SLPlayer::relationNoneStationEnabled, AbstractPlayerCache.PlayerData::relationNoneStationEnabled),
				DBCachedBooleanToggle(text("Enable Enemy Stations"), "", GuiItem.GUNSHIP, true, SLPlayer::relationEnemyStationEnabled, AbstractPlayerCache.PlayerData::relationEnemyStationEnabled),
				DBCachedBooleanToggle(text("Enable Unfriendly Stations"), "", GuiItem.GUNSHIP, true, SLPlayer::relationUnfriendlyStationEnabled, AbstractPlayerCache.PlayerData::relationUnfriendlyStationEnabled),
				DBCachedBooleanToggle(text("Enable Neutral Stations"), "", GuiItem.GUNSHIP, true, SLPlayer::relationNeutralStationEnabled, AbstractPlayerCache.PlayerData::relationNeutralStationEnabled),
				DBCachedBooleanToggle(text("Enable Friendly Stations"), "", GuiItem.GUNSHIP, true, SLPlayer::relationFriendlyStationEnabled, AbstractPlayerCache.PlayerData::relationFriendlyStationEnabled),
				DBCachedBooleanToggle(text("Enable Ally Stations"), "", GuiItem.GUNSHIP, true, SLPlayer::relationAllyStationEnabled, AbstractPlayerCache.PlayerData::relationAllyStationEnabled),
				DBCachedBooleanToggle(text("Enable Nation Stations"), "", GuiItem.GUNSHIP, true, SLPlayer::relationNationStationEnabled, AbstractPlayerCache.PlayerData::relationNationStationEnabled),
			),
			createSettingsPage(player, "Route Settings",
				DBCachedBooleanToggle(text("Enable Route Info"), "", GuiItem.LIST, true, SLPlayer::waypointsEnabled, AbstractPlayerCache.PlayerData::waypointsEnabled),
				DBCachedBooleanToggle(text("Route Segments Enabled"), "", GuiItem.LIST, true, SLPlayer::compactWaypoints, AbstractPlayerCache.PlayerData::compactWaypoints)
			)
		),
		createSettingsPage(player, "HUD Settings",
			createSettingsPage(player, "HUD Icon Settings",
				DBCachedBooleanToggle(text("Toggle Planet Selector"), "", GuiItem.COMPASS_NEEDLE, true, SLPlayer::hudPlanetsSelector, AbstractPlayerCache.PlayerData::hudPlanetsSelector),
				DBCachedBooleanToggle(text("Toggle Planet Visibility"), "", GuiItem.PLANET, true, SLPlayer::hudPlanetsImage, AbstractPlayerCache.PlayerData::hudPlanetsImage),
				DBCachedBooleanToggle(text("Toggle Star Visibility"), "", GuiItem.STAR, true, SLPlayer::hudIconStars, AbstractPlayerCache.PlayerData::hudIconStars),
				DBCachedBooleanToggle(text("Toggle Beacon Visibility"), "", GuiItem.BEACON, true, SLPlayer::hudIconBeacons, AbstractPlayerCache.PlayerData::hudIconBeacons),
				DBCachedBooleanToggle(text("Toggle Station Visibility"), "", GuiItem.STATION, false, SLPlayer::hudIconStations, AbstractPlayerCache.PlayerData::hudIconStations),
				DBCachedBooleanToggle(text("Toggle Bookmark Visibility"), "", GuiItem.BOOKMARK, false, SLPlayer::hudIconBookmarks, AbstractPlayerCache.PlayerData::hudIconBookmarks),
			),
		),
		createSettingsPage(player, "Sound Settings",
			DBCachedBooleanToggle(text("Enable Additional Sounds"), "", GuiItem.SOUND, true, SLPlayer::enableAdditionalSounds, AbstractPlayerCache.PlayerData::enableAdditionalSounds),
			DBCachedEnumCycle(CruiseIndicatorSounds::class.java, text("Cruise Indicator Sound"), "Click to Cycle", GuiItem.SOUND, 0, SLPlayer::soundCruiseIndicator, AbstractPlayerCache.PlayerData::soundCruiseIndicator),
			DBCachedBooleanToggle(text("Hitmarker On Hull"), "", GuiItem.SOUND, true, SLPlayer::hitmarkerOnHull, AbstractPlayerCache.PlayerData::hitmarkerOnHull)
		),
		createSettingsPage(player, "Other Settings",
			DBCachedBooleanToggle(text("Enable Combat Timer Alerts"), "", GuiItem.LIST, true, SLPlayer::enableCombatTimerAlerts, AbstractPlayerCache.PlayerData::enableCombatTimerAlerts),
			DBCachedBooleanToggle(text("Enable Protection Messages"), "", GuiItem.LIST, true, SLPlayer::protectionMessagesEnabled, AbstractPlayerCache.PlayerData::protectionMessagesEnabled),
			DBCachedBooleanToggle(text("Shorten Chat Messages"), "", GuiItem.LIST, false, SLPlayer::shortenChatChannels, AbstractPlayerCache.PlayerData::shortenChatChannels),
			DBCachedBooleanToggle(text("Remove User Prefixes"), "Removes prefixes, like Helper and Mod, from non-global chats", GuiItem.LIST, false, SLPlayer::hideUserPrefixes, AbstractPlayerCache.PlayerData::hideUserPrefixes),
			DBCachedBooleanToggle(text("Remove Global Prefixes"), "Removes prefixes, like Helper and Mod, from global chats", GuiItem.LIST, false, SLPlayer::hideGlobalPrefixes, AbstractPlayerCache.PlayerData::hideGlobalPrefixes),
			DBCachedBooleanToggle(text("Show /itemsearch Items"), "", GuiItem.COMPASS_NEEDLE, true, SLPlayer::showItemSearchItem, AbstractPlayerCache.PlayerData::showItemSearchItem),
			PermissionBooleanToggle(sitStateNode, text("Hitmarker On Hull"), "", GuiItem.BOOKMARK, true)
		)
    ).apply { forEach { subMenu -> subMenu.parent = this@SettingsMainMenuGui } }

    fun openMainWindow() {
        currentWindow = buildWindow(player).apply { open() }
    }
}
