package net.horizonsend.ion.server.features.sidebar.command

import co.aikar.commands.annotation.CommandAlias
import co.aikar.commands.annotation.Optional
import co.aikar.commands.annotation.Subcommand
import net.horizonsend.ion.common.database.schema.misc.PlayerSettings
import net.horizonsend.ion.common.extensions.success
import net.horizonsend.ion.common.extensions.userError
import net.horizonsend.ion.server.command.SLCommand
import net.horizonsend.ion.server.features.cache.PlayerSettingsCache.getSettingOrThrow
import net.horizonsend.ion.server.features.cache.PlayerSettingsCache.setSetting
import net.horizonsend.ion.server.features.sidebar.MainSidebar
import net.horizonsend.ion.server.features.sidebar.tasks.ContactsSidebar
import org.bukkit.entity.Player

@CommandAlias("sidebar")
object SidebarContactsCommand : SLCommand() {
	@Subcommand("contacts")
    fun defaultCase(
		sender: Player
	) {
		sender.userError("Usage: /sidebar contacts <option> [toggle]")
	}

	@Subcommand("contacts enable")
	fun onEnableContacts(
		sender: Player
	) {
		sender.setSetting(PlayerSettings::contactsEnabled, true)
		sender.success("Enabled contacts on sidebar")
	}

	@Subcommand("contacts disable")
	fun onDisableContacts(
		sender: Player
	) {

		sender.setSetting(PlayerSettings::contactsEnabled, false)
		sender.success("Disabled contacts on sidebar")
	}

	@Subcommand("contacts distance")
	fun onSetContactsDistance(
		sender: Player,
		distance: Int?
	) {
		val newDistance = distance?.coerceIn(0, MainSidebar.CONTACTS_RANGE) ?: MainSidebar.CONTACTS_RANGE

		sender.setSetting(PlayerSettings::contactsDistance, newDistance)
		sender.success("Changed contacts distance to $newDistance")
	}

	@Subcommand("contacts maxNameLength")
	fun onSetContactsMaxNameLength(
		sender: Player,
		maxLength: Int?
	) {
		val newLength = maxLength?.coerceIn(1, MainSidebar.MAX_NAME_LENGTH) ?: MainSidebar.MAX_NAME_LENGTH

		sender.setSetting(PlayerSettings::contactsMaxNameLength, newLength)
		sender.success("Changed contacts max name length to $newLength")
	}

	@Subcommand("contacts sortOrder")
	fun onChangeContactsSortOrder(sender: Player) {
		val currentSetting = sender.getSettingOrThrow(PlayerSettings::contactsSort)

		// Keep newSetting in the range of the number of sort options
		val newSetting = if (currentSetting < ContactsSidebar.ContactsSorting.entries.size - 1) currentSetting + 1 else 0

		sender.setSetting(PlayerSettings::contactsSort, newSetting)
		sender.success("Changed contacts sorting method to ${ContactsSidebar.ContactsSorting.entries[newSetting]}")
	}

	@Subcommand("contacts coloring")
	fun onChangeContactsColoring(sender: Player) {
		val currentSetting = sender.getSettingOrThrow(PlayerSettings::contactsColoring)

		// Keep newSetting in the range of the number of sort options
		val newSetting = if (currentSetting < ContactsSidebar.ContactsColoring.entries.size - 1) currentSetting + 1 else 0

		sender.setSetting(PlayerSettings::contactsColoring, newSetting)
		sender.success("Changed contacts coloring to ${ContactsSidebar.ContactsColoring.entries[newSetting]}")
	}

	@Subcommand("contacts starship")
	fun onToggleStarship(
		sender: Player,
		@Optional toggle: Boolean?
	) {
		val contactsStarships = toggle ?: !sender.getSettingOrThrow(PlayerSettings::contactsStarships)

		sender.setSetting(PlayerSettings::contactsStarships, contactsStarships)
		sender.success("Changed starship visibility to $contactsStarships")
	}

	@Subcommand("contacts lastStarship")
	fun onToggleLastStarship(
		sender: Player,
		@Optional toggle: Boolean?
	) {
		val contactsLastStarship = toggle ?: !sender.getSettingOrThrow(PlayerSettings::lastStarshipEnabled)

		sender.setSetting(PlayerSettings::lastStarshipEnabled, contactsLastStarship)
		sender.success("Changed last starship visibility to $contactsLastStarship")
	}

	@Subcommand("contacts planet")
	fun onTogglePlanets(
		sender: Player,
		@Optional toggle: Boolean?
	) {
		val contactsPlanets = toggle ?: !sender.getSettingOrThrow(PlayerSettings::planetsEnabled)

		sender.setSetting(PlayerSettings::planetsEnabled, contactsPlanets)
		sender.success("Changed planet visibility to $contactsPlanets")
	}

	@Subcommand("contacts star")
	fun onToggleStars(
		sender: Player,
		@Optional toggle: Boolean?
	) {
		val starsEnabled = toggle ?: !sender.getSettingOrThrow(PlayerSettings::starsEnabled)

		sender.setSetting(PlayerSettings::starsEnabled, starsEnabled)
		sender.success("Changed star visibility to $starsEnabled")
	}

	@Subcommand("contacts beacon")
	fun onToggleBeacons(
		sender: Player,
		@Optional toggle: Boolean?
	) {
		val beaconsEnabled = toggle ?: !sender.getSettingOrThrow(PlayerSettings::beaconsEnabled)

		sender.setSetting(PlayerSettings::beaconsEnabled, beaconsEnabled)
		sender.success("Changed beacon visibility to $beaconsEnabled")
	}

	@Subcommand("contacts station")
	fun onToggleStations(
		sender: Player,
		@Optional toggle: Boolean?
	) {
		val stationsEnabled = toggle ?: !sender.getSettingOrThrow(PlayerSettings::stationsEnabled)

		sender.setSetting(PlayerSettings::stationsEnabled, stationsEnabled)
		sender.success("Changed station visibility to $stationsEnabled")
	}

	@Subcommand("contacts bookmark")
	fun onToggleBookmarks(
		sender: Player,
		@Optional toggle: Boolean?
	) {
		val bookmarksEnabled = toggle ?: !sender.getSettingOrThrow(PlayerSettings::bookmarksEnabled)

		sender.setSetting(PlayerSettings::bookmarksEnabled, bookmarksEnabled)
		sender.success("Changed bookmark visibility to $bookmarksEnabled")
	}

	@Subcommand("contacts ai")
	fun onToggleAi(
		sender: Player,
		@Optional toggle: Boolean?
	) {
		val aiEnabled = toggle ?: !sender.getSettingOrThrow(PlayerSettings::relationAiEnabled)

		sender.setSetting(PlayerSettings::relationAiEnabled, aiEnabled)
		sender.success("Changed AI starship visibility to $aiEnabled")
	}

	@Subcommand("contacts none")
	fun onToggleNone(
		sender: Player,
		@Optional toggle: Boolean?
	) {
		val noneEnabled = toggle ?: !sender.getSettingOrThrow(PlayerSettings::relationNoneEnabled)

		sender.setSetting(PlayerSettings::relationNoneEnabled, noneEnabled)
		sender.success("Changed no relation starship visibility to $noneEnabled")
	}

	@Subcommand("contacts enemy")
	fun onToggleEnemy(
		sender: Player,
		@Optional toggle: Boolean?
	) {
		val enemyEnabled = toggle ?: !sender.getSettingOrThrow(PlayerSettings::relationEnemyEnabled)

		sender.setSetting(PlayerSettings::relationEnemyEnabled, enemyEnabled)
		sender.success("Changed enemy starship visibility to $enemyEnabled")
	}

	@Subcommand("contacts unfriendly")
	fun onToggleUnfriendly(
		sender: Player,
		@Optional toggle: Boolean?
	) {
		val unfriendlyEnabled = toggle ?: !sender.getSettingOrThrow(PlayerSettings::relationUnfriendlyEnabled)

		sender.setSetting(PlayerSettings::relationUnfriendlyEnabled, unfriendlyEnabled)
		sender.success("Changed unfriendly starship visibility to $unfriendlyEnabled")
	}

	@Subcommand("contacts neutral")
	fun onToggleNeutral(
		sender: Player,
		@Optional toggle: Boolean?
	) {
		val neutralEnabled = toggle ?: !sender.getSettingOrThrow(PlayerSettings::relationNeutralEnabled)

		sender.setSetting(PlayerSettings::relationNeutralEnabled, neutralEnabled)
		sender.success("Changed neutral starship visibility to $neutralEnabled")
	}

	@Subcommand("contacts friendly")
	fun onToggleFriendly(
		sender: Player,
		@Optional toggle: Boolean?
	) {
		val friendlyEnabled = toggle ?: !sender.getSettingOrThrow(PlayerSettings::relationFriendlyEnabled)

		sender.setSetting(PlayerSettings::relationFriendlyEnabled, friendlyEnabled)
		sender.success("Changed friendly starship visibility to $friendlyEnabled")
	}

	@Subcommand("contacts ally")
	fun onToggleAlly(
		sender: Player,
		@Optional toggle: Boolean?
	) {
		val allyEnabled = toggle ?: !sender.getSettingOrThrow(PlayerSettings::relationAllyEnabled)

		sender.setSetting(PlayerSettings::relationAllyEnabled, allyEnabled)
		sender.success("Changed ally starship visibility to $allyEnabled")
	}

	@Subcommand("contacts nation")
	fun onToggleNation(
		sender: Player,
		@Optional toggle: Boolean?
	) {
		val nationEnabled = toggle ?: !sender.getSettingOrThrow(PlayerSettings::relationNationEnabled)

		sender.setSetting(PlayerSettings::relationNationEnabled, nationEnabled)
		sender.success("Changed nation starship visibility to $nationEnabled")
	}

	@Subcommand("contacts aiStation")
	fun onToggleAiStation(
		sender: Player,
		@Optional toggle: Boolean?
	) {
		val aiStationEnabled = toggle ?: !sender.getSettingOrThrow(PlayerSettings::relationAiEnabled)

		sender.setSetting(PlayerSettings::relationAiStationEnabled, aiStationEnabled)
		sender.success("Changed AI station visibility to $aiStationEnabled")
	}

	@Subcommand("contacts noneStation")
	fun onToggleNoneStation(
		sender: Player,
		@Optional toggle: Boolean?
	) {
		val noneStationEnabled = toggle ?: !sender.getSettingOrThrow(PlayerSettings::relationNoneEnabled)

		sender.setSetting(PlayerSettings::relationNoneStationEnabled, noneStationEnabled)
		sender.success("Changed no relation station visibility to $noneStationEnabled")
	}

	@Subcommand("contacts enemyStation")
	fun onToggleEnemyStation(
		sender: Player,
		@Optional toggle: Boolean?
	) {
		val enemyStationEnabled = toggle ?: !sender.getSettingOrThrow(PlayerSettings::relationEnemyEnabled)

		sender.setSetting(PlayerSettings::relationEnemyStationEnabled, enemyStationEnabled)
		sender.success("Changed enemy station visibility to $enemyStationEnabled")
	}

	@Subcommand("contacts unfriendlyStation")
	fun onToggleUnfriendlyStation(
		sender: Player,
		@Optional toggle: Boolean?
	) {
		val unfriendlyStationEnabled = toggle ?: !sender.getSettingOrThrow(PlayerSettings::relationUnfriendlyEnabled)

		sender.setSetting(PlayerSettings::relationUnfriendlyStationEnabled, unfriendlyStationEnabled)
		sender.success("Changed unfriendly station visibility to $unfriendlyStationEnabled")
	}

	@Subcommand("contacts neutralStation")
	fun onToggleNeutralStation(
		sender: Player,
		@Optional toggle: Boolean?
	) {
		val neutralStationEnabled = toggle ?: !sender.getSettingOrThrow(PlayerSettings::relationNeutralEnabled)

		sender.setSetting(PlayerSettings::relationNeutralStationEnabled, neutralStationEnabled)
		sender.success("Changed neutral station visibility to $neutralStationEnabled")
	}

	@Subcommand("contacts friendlyStation")
	fun onToggleFriendlyStation(
		sender: Player,
		@Optional toggle: Boolean?
	) {
		val friendlyStationEnabled = toggle ?: !sender.getSettingOrThrow(PlayerSettings::relationFriendlyEnabled)

		sender.setSetting(PlayerSettings::relationFriendlyStationEnabled, friendlyStationEnabled)
		sender.success("Changed friendly station visibility to $friendlyStationEnabled")
	}

	@Subcommand("contacts allyStation")
	fun onToggleAllyStation(
		sender: Player,
		@Optional toggle: Boolean?
	) {
		val allyStationEnabled = toggle ?: !sender.getSettingOrThrow(PlayerSettings::relationAllyEnabled)

		sender.setSetting(PlayerSettings::relationAllyStationEnabled, allyStationEnabled)
		sender.success("Changed ally station visibility to $allyStationEnabled")
	}

	@Subcommand("contacts nationStation")
	fun onToggleNationStation(
		sender: Player,
		@Optional toggle: Boolean?
	) {
		val nationStationEnabled = toggle ?: !sender.getSettingOrThrow(PlayerSettings::relationNationEnabled)

		sender.setSetting(PlayerSettings::relationNationStationEnabled, nationStationEnabled)
		sender.success("Changed nation station visibility to $nationStationEnabled")
	}
}
