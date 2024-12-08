package net.horizonsend.ion.server.features.sidebar.command

import co.aikar.commands.annotation.CommandAlias
import co.aikar.commands.annotation.Optional
import co.aikar.commands.annotation.Subcommand
import net.horizonsend.ion.common.database.schema.misc.SLPlayer
import net.horizonsend.ion.common.extensions.success
import net.horizonsend.ion.common.extensions.userError
import net.horizonsend.ion.server.features.cache.PlayerCache
import net.horizonsend.ion.server.miscellaneous.utils.slPlayerId
import net.horizonsend.ion.server.command.SLCommand
import net.horizonsend.ion.server.features.sidebar.MainSidebar
import net.horizonsend.ion.server.features.sidebar.tasks.ContactsSidebar
import org.bukkit.entity.Player
import org.litote.kmongo.setValue

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
		SLPlayer.updateById(sender.slPlayerId, setValue(SLPlayer::contactsEnabled, true))
		PlayerCache[sender].contactsEnabled = true
		sender.success("Enabled contacts on sidebar")
	}

	@Subcommand("contacts disable")
	fun onDisableContacts(
		sender: Player
	) {
		SLPlayer.updateById(sender.slPlayerId, setValue(SLPlayer::contactsEnabled, false))
		PlayerCache[sender].contactsEnabled = false
		sender.success("Disabled contacts on sidebar")
	}

	@Subcommand("contacts distance")
	fun onSetContactsDistance(
		sender: Player,
		distance: Int?
	) {
		val newDistance = distance?.coerceIn(0, MainSidebar.CONTACTS_RANGE) ?: MainSidebar.CONTACTS_RANGE
		SLPlayer.updateById(sender.slPlayerId, setValue(SLPlayer::contactsDistance, newDistance))
		PlayerCache[sender].contactsDistance = newDistance
		sender.success("Changed contacts distance to $newDistance")
	}

	@Subcommand("contacts maxNameLength")
	fun onSetContactsMaxNameLength(
		sender: Player,
		maxLength: Int?
	) {
		val newLength = maxLength?.coerceIn(1, MainSidebar.MAX_NAME_LENGTH) ?: MainSidebar.MAX_NAME_LENGTH
		SLPlayer.updateById(sender.slPlayerId, setValue(SLPlayer::contactsMaxNameLength, newLength))
		PlayerCache[sender].contactsMaxNameLength = newLength
		sender.success("Changed contacts max name length to $newLength")
	}

	@Subcommand("contacts sortOrder")
	fun onChangeContactsSortOrder(sender: Player) {
		val currentSetting = PlayerCache[sender.uniqueId].contactsSort

		// Keep newSetting in the range of the number of sort options
		val newSetting = if (currentSetting < ContactsSidebar.ContactsSorting.entries.size - 1) currentSetting + 1 else 0
		SLPlayer.updateById(sender.slPlayerId, setValue(SLPlayer::contactsSort, newSetting))
		PlayerCache[sender].contactsSort = newSetting
		sender.success("Changed contacts sorting method to ${ContactsSidebar.ContactsSorting.entries[newSetting]}")
	}

	@Subcommand("contacts coloring")
	fun onChangeContactsColoring(sender: Player) {
		val currentSetting = PlayerCache[sender.uniqueId].contactsColoring

		// Keep newSetting in the range of the number of sort options
		val newSetting = if (currentSetting < ContactsSidebar.ContactsColoring.entries.size - 1) currentSetting + 1 else 0
		SLPlayer.updateById(sender.slPlayerId, setValue(SLPlayer::contactsColoring, newSetting))
		PlayerCache[sender].contactsColoring = newSetting
		sender.success("Changed contacts coloring to ${ContactsSidebar.ContactsColoring.entries[newSetting]}")
	}

	@Subcommand("contacts starship")
	fun onToggleStarship(
		sender: Player,
		@Optional toggle: Boolean?
	) {
		val contactsStarships = toggle ?: !PlayerCache[sender].contactsStarships
		SLPlayer.updateById(sender.slPlayerId, setValue(SLPlayer::contactsStarships, contactsStarships))
		PlayerCache[sender].contactsStarships = contactsStarships
		sender.success("Changed starship visibility to $contactsStarships")
	}

	@Subcommand("contacts lastStarship")
	fun onToggleLastStarship(
		sender: Player,
		@Optional toggle: Boolean?
	) {
		val contactsLastStarship = toggle ?: !PlayerCache[sender].lastStarshipEnabled
		SLPlayer.updateById(sender.slPlayerId, setValue(SLPlayer::lastStarshipEnabled, contactsLastStarship))
		PlayerCache[sender].lastStarshipEnabled = contactsLastStarship
		sender.success("Changed last starship visibility to $contactsLastStarship")
	}

	@Subcommand("contacts planet")
	fun onTogglePlanets(
		sender: Player,
		@Optional toggle: Boolean?
	) {
		val contactsPlanets = toggle ?: !PlayerCache[sender].planetsEnabled
		SLPlayer.updateById(sender.slPlayerId, setValue(SLPlayer::planetsEnabled, contactsPlanets))
		PlayerCache[sender].planetsEnabled = contactsPlanets
		sender.success("Changed planet visibility to $contactsPlanets")
	}

	@Subcommand("contacts star")
	fun onToggleStars(
		sender: Player,
		@Optional toggle: Boolean?
	) {
		val starsEnabled = toggle ?: !PlayerCache[sender].starsEnabled
		SLPlayer.updateById(sender.slPlayerId, setValue(SLPlayer::starsEnabled, starsEnabled))
		PlayerCache[sender].starsEnabled = starsEnabled
		sender.success("Changed star visibility to $starsEnabled")
	}

	@Subcommand("contacts beacon")
	fun onToggleBeacons(
		sender: Player,
		@Optional toggle: Boolean?
	) {
		val beaconsEnabled = toggle ?: !PlayerCache[sender].beaconsEnabled
		SLPlayer.updateById(sender.slPlayerId, setValue(SLPlayer::beaconsEnabled, beaconsEnabled))
		PlayerCache[sender].beaconsEnabled = beaconsEnabled
		sender.success("Changed beacon visibility to $beaconsEnabled")
	}

	@Subcommand("contacts station")
	fun onToggleStations(
		sender: Player,
		@Optional toggle: Boolean?
	) {
		val stationsEnabled = toggle ?: !PlayerCache[sender].stationsEnabled
		SLPlayer.updateById(sender.slPlayerId, setValue(SLPlayer::stationsEnabled, stationsEnabled))
		PlayerCache[sender].stationsEnabled = stationsEnabled
		sender.success("Changed station visibility to $stationsEnabled")
	}

	@Subcommand("contacts bookmark")
	fun onToggleBookmarks(
		sender: Player,
		@Optional toggle: Boolean?
	) {
		val bookmarksEnabled = toggle ?: !PlayerCache[sender].bookmarksEnabled
		SLPlayer.updateById(sender.slPlayerId, setValue(SLPlayer::bookmarksEnabled, bookmarksEnabled))
		PlayerCache[sender].bookmarksEnabled = bookmarksEnabled
		sender.success("Changed bookmark visibility to $bookmarksEnabled")
	}

	@Subcommand("contacts ai")
	fun onToggleAi(
		sender: Player,
		@Optional toggle: Boolean?
	) {
		val aiEnabled = toggle ?: !PlayerCache[sender].relationAiEnabled
		SLPlayer.updateById(sender.slPlayerId, setValue(SLPlayer::relationAiEnabled, aiEnabled))
		PlayerCache[sender].relationAiEnabled = aiEnabled
		sender.success("Changed AI starship visibility to $aiEnabled")
	}

	@Subcommand("contacts none")
	fun onToggleNone(
		sender: Player,
		@Optional toggle: Boolean?
	) {
		val noneEnabled = toggle ?: !PlayerCache[sender].relationNoneEnabled
		SLPlayer.updateById(sender.slPlayerId, setValue(SLPlayer::relationNoneEnabled, noneEnabled))
		PlayerCache[sender].relationNoneEnabled = noneEnabled
		sender.success("Changed no relation starship visibility to $noneEnabled")
	}

	@Subcommand("contacts enemy")
	fun onToggleEnemy(
		sender: Player,
		@Optional toggle: Boolean?
	) {
		val enemyEnabled = toggle ?: !PlayerCache[sender].relationEnemyEnabled
		SLPlayer.updateById(sender.slPlayerId, setValue(SLPlayer::relationEnemyEnabled, enemyEnabled))
		PlayerCache[sender].relationEnemyEnabled = enemyEnabled
		sender.success("Changed enemy starship visibility to $enemyEnabled")
	}

	@Subcommand("contacts unfriendly")
	fun onToggleUnfriendly(
		sender: Player,
		@Optional toggle: Boolean?
	) {
		val unfriendlyEnabled = toggle ?: !PlayerCache[sender].relationUnfriendlyEnabled
		SLPlayer.updateById(sender.slPlayerId, setValue(SLPlayer::relationUnfriendlyEnabled, unfriendlyEnabled))
		PlayerCache[sender].relationUnfriendlyEnabled = unfriendlyEnabled
		sender.success("Changed unfriendly starship visibility to $unfriendlyEnabled")
	}

	@Subcommand("contacts neutral")
	fun onToggleNeutral(
		sender: Player,
		@Optional toggle: Boolean?
	) {
		val neutralEnabled = toggle ?: !PlayerCache[sender].relationNeutralEnabled
		SLPlayer.updateById(sender.slPlayerId, setValue(SLPlayer::relationNeutralEnabled, neutralEnabled))
		PlayerCache[sender].relationNeutralEnabled = neutralEnabled
		sender.success("Changed neutral starship visibility to $neutralEnabled")
	}

	@Subcommand("contacts friendly")
	fun onToggleFriendly(
		sender: Player,
		@Optional toggle: Boolean?
	) {
		val friendlyEnabled = toggle ?: !PlayerCache[sender].relationFriendlyEnabled
		SLPlayer.updateById(sender.slPlayerId, setValue(SLPlayer::relationFriendlyEnabled, friendlyEnabled))
		PlayerCache[sender].relationFriendlyEnabled = friendlyEnabled
		sender.success("Changed friendly starship visibility to $friendlyEnabled")
	}

	@Subcommand("contacts ally")
	fun onToggleAlly(
		sender: Player,
		@Optional toggle: Boolean?
	) {
		val allyEnabled = toggle ?: !PlayerCache[sender].relationAllyEnabled
		SLPlayer.updateById(sender.slPlayerId, setValue(SLPlayer::relationAllyEnabled, allyEnabled))
		PlayerCache[sender].relationAllyEnabled = allyEnabled
		sender.success("Changed ally starship visibility to $allyEnabled")
	}

	@Subcommand("contacts nation")
	fun onToggleNation(
		sender: Player,
		@Optional toggle: Boolean?
	) {
		val nationEnabled = toggle ?: !PlayerCache[sender].relationNationEnabled
		SLPlayer.updateById(sender.slPlayerId, setValue(SLPlayer::relationNationEnabled, nationEnabled))
		PlayerCache[sender].relationNationEnabled = nationEnabled
		sender.success("Changed nation starship visibility to $nationEnabled")
	}

	@Subcommand("contacts aiStation")
	fun onToggleAiStation(
		sender: Player,
		@Optional toggle: Boolean?
	) {
		val aiStationEnabled = toggle ?: !PlayerCache[sender].relationAiEnabled
		SLPlayer.updateById(sender.slPlayerId, setValue(SLPlayer::relationAiStationEnabled, aiStationEnabled))
		PlayerCache[sender].relationAiStationEnabled = aiStationEnabled
		sender.success("Changed AI station visibility to $aiStationEnabled")
	}

	@Subcommand("contacts noneStation")
	fun onToggleNoneStation(
		sender: Player,
		@Optional toggle: Boolean?
	) {
		val noneStationEnabled = toggle ?: !PlayerCache[sender].relationNoneEnabled
		SLPlayer.updateById(sender.slPlayerId, setValue(SLPlayer::relationNoneStationEnabled, noneStationEnabled))
		PlayerCache[sender].relationNoneStationEnabled = noneStationEnabled
		sender.success("Changed no relation station visibility to $noneStationEnabled")
	}

	@Subcommand("contacts enemyStation")
	fun onToggleEnemyStation(
		sender: Player,
		@Optional toggle: Boolean?
	) {
		val enemyStationEnabled = toggle ?: !PlayerCache[sender].relationEnemyEnabled
		SLPlayer.updateById(sender.slPlayerId, setValue(SLPlayer::relationEnemyStationEnabled, enemyStationEnabled))
		PlayerCache[sender].relationEnemyStationEnabled = enemyStationEnabled
		sender.success("Changed enemy station visibility to $enemyStationEnabled")
	}

	@Subcommand("contacts unfriendlyStation")
	fun onToggleUnfriendlyStation(
		sender: Player,
		@Optional toggle: Boolean?
	) {
		val unfriendlyStationEnabled = toggle ?: !PlayerCache[sender].relationUnfriendlyEnabled
		SLPlayer.updateById(sender.slPlayerId, setValue(SLPlayer::relationUnfriendlyStationEnabled, unfriendlyStationEnabled))
		PlayerCache[sender].relationUnfriendlyStationEnabled = unfriendlyStationEnabled
		sender.success("Changed unfriendly station visibility to $unfriendlyStationEnabled")
	}

	@Subcommand("contacts neutralStation")
	fun onToggleNeutralStation(
		sender: Player,
		@Optional toggle: Boolean?
	) {
		val neutralStationEnabled = toggle ?: !PlayerCache[sender].relationNeutralEnabled
		SLPlayer.updateById(sender.slPlayerId, setValue(SLPlayer::relationNeutralStationEnabled, neutralStationEnabled))
		PlayerCache[sender].relationNeutralStationEnabled = neutralStationEnabled
		sender.success("Changed neutral station visibility to $neutralStationEnabled")
	}

	@Subcommand("contacts friendlyStation")
	fun onToggleFriendlyStation(
		sender: Player,
		@Optional toggle: Boolean?
	) {
		val friendlyStationEnabled = toggle ?: !PlayerCache[sender].relationFriendlyEnabled
		SLPlayer.updateById(sender.slPlayerId, setValue(SLPlayer::relationFriendlyStationEnabled, friendlyStationEnabled))
		PlayerCache[sender].relationFriendlyStationEnabled = friendlyStationEnabled
		sender.success("Changed friendly station visibility to $friendlyStationEnabled")
	}

	@Subcommand("contacts allyStation")
	fun onToggleAllyStation(
		sender: Player,
		@Optional toggle: Boolean?
	) {
		val allyStationEnabled = toggle ?: !PlayerCache[sender].relationAllyEnabled
		SLPlayer.updateById(sender.slPlayerId, setValue(SLPlayer::relationAllyStationEnabled, allyStationEnabled))
		PlayerCache[sender].relationAllyStationEnabled = allyStationEnabled
		sender.success("Changed ally station visibility to $allyStationEnabled")
	}

	@Subcommand("contacts nationStation")
	fun onToggleNationStation(
		sender: Player,
		@Optional toggle: Boolean?
	) {
		val nationStationEnabled = toggle ?: !PlayerCache[sender].relationNationEnabled
		SLPlayer.updateById(sender.slPlayerId, setValue(SLPlayer::relationNationStationEnabled, nationStationEnabled))
		PlayerCache[sender].relationNationStationEnabled = nationStationEnabled
		sender.success("Changed nation station visibility to $nationStationEnabled")
	}
}
