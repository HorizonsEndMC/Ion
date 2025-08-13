package net.horizonsend.ion.server.features.gui.custom.settings

import net.horizonsend.ion.common.database.schema.misc.PlayerSettings
import net.horizonsend.ion.server.command.misc.IonSitCommand.sitStateNode
import net.horizonsend.ion.server.features.client.display.ClientDisplayEntities
import net.horizonsend.ion.server.features.gui.GuiItem
import net.horizonsend.ion.server.features.gui.GuiItems
import net.horizonsend.ion.server.features.gui.custom.settings.button.database.DBCachedBooleanToggle
import net.horizonsend.ion.server.features.gui.custom.settings.button.database.DBCachedEnumCycle
import net.horizonsend.ion.server.features.gui.custom.settings.button.database.DBCachedIntCycle
import net.horizonsend.ion.server.features.gui.custom.settings.button.database.DBCachedIntegerInput
import net.horizonsend.ion.server.features.gui.custom.settings.button.permission.PermissionBooleanToggle
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
	override fun makeButton(pageGui: SettingsPageGui): GuiItems.AbstractButtonItem {
		return GuiItem.LIST.makeButton(this, title, "") { _, _, _ -> openGui() }
	}

	override val backButton: Item = SimpleItem(GuiItems.blankItem)

    override val buttonsList = listOf(
		createSettingsPage(player, "Control Settings",
			DBCachedBooleanToggle(text("DC Overrides Cruise"), "", GuiItem.GUNSHIP, false, PlayerSettings::useAlternateDCCruise),
			DBCachedIntegerInput(1_000_000,-1, text("DC Refresh Rate"),
				"\"How frequently DC responds to your movement and teleports you, a value of -1 means that refresh is entirely ping driven. High values means more forging feedback but less responsive", GuiItem.GUNSHIP, -1, PlayerSettings::dcRefreshRate)
		),
		createSettingsPage(player, "Sidebar Settings",
			createSettingsPage(player, "Combat Timer Settings",
				DBCachedBooleanToggle(text("Enable Combat Timer Info"), "", GuiItem.LIST, true, PlayerSettings::combatTimerEnabled)
			),
			createSettingsPage(player, "Starships Settings",
				DBCachedBooleanToggle(text("Enable Starship Info"), "", GuiItem.LIST, true, PlayerSettings::starshipsEnabled),
				DBCachedBooleanToggle(text("Display Advanced Info"), "", GuiItem.LIST, false, PlayerSettings::advancedStarshipInfo),
				DBCachedBooleanToggle(text("Rotating Compass"), "", GuiItem.COMPASS_NEEDLE, false, PlayerSettings::rotateCompass)
			),
			createSettingsPage(player, "Contacts Settings",
				DBCachedBooleanToggle(text("Enable Contacts Info"), "", GuiItem.LIST, true, PlayerSettings::contactsEnabled),
				DBCachedIntegerInput(0, MainSidebar.CONTACTS_RANGE, text("Change Contacts Range"), "", GuiItem.ROUTE_SEGMENT, 6000, PlayerSettings::contactsDistance),
				DBCachedIntegerInput(1, MainSidebar.MAX_NAME_LENGTH, text("Change Max Name Length"), "", GuiItem.LIST, 6000, PlayerSettings::contactsMaxNameLength),
				DBCachedEnumCycle(ContactsSorting::class.java, text("Change Sort Order"), "", GuiItem.LIST, 0, PlayerSettings::contactsSort),
				DBCachedEnumCycle(ContactsColoring::class.java, text("Change Coloring"), "", GuiItem.LIST, 0, PlayerSettings::contactsColoring),
				DBCachedBooleanToggle(text("Enable Starships"), "", GuiItem.GUNSHIP, true, PlayerSettings::contactsStarships),
				DBCachedBooleanToggle(text("Enable Last Starship"), "", GuiItem.GENERIC_STARSHIP, true, PlayerSettings::lastStarshipEnabled),
				DBCachedBooleanToggle(text("Enable Planets"), "", GuiItem.PLANET, true, PlayerSettings::planetsEnabled),
				DBCachedBooleanToggle(text("Enable Stars"), "", GuiItem.STAR, true, PlayerSettings::starsEnabled),
				DBCachedBooleanToggle(text("Enable Beacons"), "", GuiItem.BEACON, true, PlayerSettings::beaconsEnabled),
				DBCachedBooleanToggle(text("Enable Stations"), "", GuiItem.STATION, true, PlayerSettings::stationsEnabled),
				DBCachedBooleanToggle(text("Enable Bookmarks"), "", GuiItem.BOOKMARK, true, PlayerSettings::bookmarksEnabled),
				DBCachedBooleanToggle(text("Enable AI Starships"), "", GuiItem.GUNSHIP, true, PlayerSettings::relationAiEnabled),
				DBCachedBooleanToggle(text("Enable No Relation Starships"), "", GuiItem.GUNSHIP, true, PlayerSettings::relationNoneEnabled),
				DBCachedBooleanToggle(text("Enable Enemy Starships"), "", GuiItem.GUNSHIP, true, PlayerSettings::relationEnemyEnabled),
				DBCachedBooleanToggle(text("Enable Unfriendly Starships"), "", GuiItem.GUNSHIP, true, PlayerSettings::relationUnfriendlyEnabled),
				DBCachedBooleanToggle(text("Enable Neutral Starships"), "", GuiItem.GUNSHIP, true, PlayerSettings::relationNeutralEnabled),
				DBCachedBooleanToggle(text("Enable Friendly Starships"), "", GuiItem.GUNSHIP, true, PlayerSettings::relationFriendlyEnabled),
				DBCachedBooleanToggle(text("Enable Ally Starships"), "", GuiItem.GUNSHIP, true, PlayerSettings::relationAllyEnabled),
				DBCachedBooleanToggle(text("Enable Nation Starships"), "", GuiItem.GUNSHIP, true, PlayerSettings::relationNationEnabled),
				DBCachedBooleanToggle(text("Enable AI Stations"), "", GuiItem.GUNSHIP, true, PlayerSettings::relationAiStationEnabled),
				DBCachedBooleanToggle(text("Enable No Relation Stations"), "", GuiItem.GUNSHIP, true, PlayerSettings::relationNoneStationEnabled),
				DBCachedBooleanToggle(text("Enable Enemy Stations"), "", GuiItem.GUNSHIP, true, PlayerSettings::relationEnemyStationEnabled),
				DBCachedBooleanToggle(text("Enable Unfriendly Stations"), "", GuiItem.GUNSHIP, true, PlayerSettings::relationUnfriendlyStationEnabled),
				DBCachedBooleanToggle(text("Enable Neutral Stations"), "", GuiItem.GUNSHIP, true, PlayerSettings::relationNeutralStationEnabled),
				DBCachedBooleanToggle(text("Enable Friendly Stations"), "", GuiItem.GUNSHIP, true, PlayerSettings::relationFriendlyStationEnabled),
				DBCachedBooleanToggle(text("Enable Ally Stations"), "", GuiItem.GUNSHIP, true, PlayerSettings::relationAllyStationEnabled),
				DBCachedBooleanToggle(text("Enable Nation Stations"), "", GuiItem.GUNSHIP, true, PlayerSettings::relationNationStationEnabled),
			),
			createSettingsPage(player, "Route Settings",
				DBCachedBooleanToggle(text("Enable Route Info"), "", GuiItem.LIST, true, PlayerSettings::waypointsEnabled),
				DBCachedBooleanToggle(text("Route Segments Enabled"), "", GuiItem.LIST, true, PlayerSettings::compactWaypoints)
			)
		),
		createSettingsPage(player, "Graphics Settings",
			createSettingsPage(player, "HUD Icon Settings",
				DBCachedBooleanToggle(text("Toggle Planet Selector"), "", GuiItem.COMPASS_NEEDLE, true, PlayerSettings::hudPlanetsSelector),
				DBCachedBooleanToggle(text("Toggle Planet Visibility"), "", GuiItem.PLANET, true, PlayerSettings::hudPlanetsImage),
				DBCachedBooleanToggle(text("Toggle Star Visibility"), "", GuiItem.STAR, true, PlayerSettings::hudIconStars),
				DBCachedBooleanToggle(text("Toggle Beacon Visibility"), "", GuiItem.BEACON, true, PlayerSettings::hudIconBeacons),
				DBCachedBooleanToggle(text("Toggle Station Visibility"), "", GuiItem.STATION, false, PlayerSettings::hudIconStations),
				DBCachedBooleanToggle(text("Toggle Bookmark Visibility"), "", GuiItem.BOOKMARK, false, PlayerSettings::hudIconBookmarks),
			),
			createSettingsPage(player, "Effects Settings",
				DBCachedEnumCycle(ClientDisplayEntities.Visibility::class.java, text("Display Entities"), "Changes the visibility of display entity effects", GuiItem.LIST, 0, PlayerSettings::displayEntityVisibility),
				DBCachedBooleanToggle(text("Toggle Alternative Shield Impact Particles"), "", GuiItem.BOOKMARK, false, PlayerSettings::useAlternateShieldHitParticle),
				DBCachedIntegerInput(1,100, text("Flare Duration"),
					"\"How long flares from hitting shields should last in ticks", GuiItem.BOOKMARK, 5, PlayerSettings::flareTime)
			),
		),
		createSettingsPage(player, "Sound Settings",
			DBCachedBooleanToggle(text("Enable Additional Sounds"), "", GuiItem.SOUND, true, PlayerSettings::enableAdditionalSounds),
			DBCachedEnumCycle(CruiseIndicatorSounds::class.java, text("Cruise Indicator Sound"), "Click to Cycle", GuiItem.SOUND, 0, PlayerSettings::soundCruiseIndicator),
			DBCachedBooleanToggle(text("Hitmarker On Hull"), "", GuiItem.SOUND, true, PlayerSettings::hitmarkerOnHull)
		),
		createSettingsPage(player, "Other Settings",
			DBCachedBooleanToggle(text("Enable Combat Timer Alerts"), "", GuiItem.LIST, true, PlayerSettings::enableCombatTimerAlerts),
			DBCachedBooleanToggle(text("Enable Protection Messages"), "", GuiItem.LIST, true, PlayerSettings::protectionMessagesEnabled),
			DBCachedBooleanToggle(text("Shorten Chat Messages"), "", GuiItem.LIST, false, PlayerSettings::shortenChatChannels),
			DBCachedBooleanToggle(text("Remove User Prefixes"), "Removes prefixes, like Helper and Mod, from non-global chats", GuiItem.LIST, false, PlayerSettings::hideUserPrefixes),
			DBCachedBooleanToggle(text("Remove Global Prefixes"), "Removes prefixes, like Helper and Mod, from global chats", GuiItem.LIST, false, PlayerSettings::hideGlobalPrefixes),
			DBCachedBooleanToggle(text("Show /itemsearch Items"), "", GuiItem.COMPASS_NEEDLE, true, PlayerSettings::showItemSearchItem),
			PermissionBooleanToggle(sitStateNode, text("Hitmarker On Hull"), "", GuiItem.BOOKMARK, true)
		)
    ).onEach { subMenu -> subMenu.setParent(this@SettingsMainMenuGui)  }

	override fun getFirstLine(player: Player): Component {
		return Component.empty()
	}

	override fun getSecondLine(player: Player): Component {
		return Component.empty()
	}
}
