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
import org.bukkit.entity.Player
import org.litote.kmongo.setValue

@CommandAlias("sidebar")
object SidebarContactsCommand : SLCommand() {
	@Subcommand("contacts")
	@Suppress("unused")
	fun defaultCase(
		sender: Player
	) {
		sender.userError("Usage: /sidebar contacts <option> [toggle]")
	}

	@Suppress("unused")
	@Subcommand("contacts enable")
	fun onEnableContacts(
		sender: Player
	) {
		SLPlayer.updateById(sender.slPlayerId, setValue(SLPlayer::contactsEnabled, true))
		sender.success("Enabled contacts on sidebar")
	}

	@Suppress("unused")
	@Subcommand("contacts disable")
	fun onDisableContacts(
		sender: Player
	) {
		SLPlayer.updateById(sender.slPlayerId, setValue(SLPlayer::contactsEnabled, false))
		sender.success("Disabled contacts on sidebar")
	}

	@Suppress("unused")
	@Subcommand("contacts distance")
	fun onSetContactsDistance(
		sender: Player,
		distance: Int?
	) {
		val newDistance = distance?.coerceIn(0, MainSidebar.CONTACTS_RANGE) ?: MainSidebar.CONTACTS_RANGE
		SLPlayer.updateById(sender.slPlayerId, setValue(SLPlayer::contactsDistance, newDistance))
		sender.success(("Changed contacts distance to $newDistance"))
	}

	@Suppress("unused")
	@Subcommand("contacts starship")
	fun onToggleStarship(
		sender: Player,
		@Optional toggle: Boolean?
	) {
		val contactsStarships = toggle ?: !PlayerCache[sender].contactsStarships
		SLPlayer.updateById(sender.slPlayerId, setValue(SLPlayer::contactsStarships, contactsStarships))
		sender.success("Changed starship visibility to $contactsStarships")
	}

	@Suppress("unused")
	@Subcommand("contacts lastStarship")
	fun onToggleLastStarship(
		sender: Player,
		@Optional toggle: Boolean?
	) {
		val contactsLastStarship = toggle ?: !PlayerCache[sender].lastStarshipEnabled
		SLPlayer.updateById(sender.slPlayerId, setValue(SLPlayer::lastStarshipEnabled, contactsLastStarship))
		sender.success("Changed last starship visibility to $contactsLastStarship")
	}

	@Suppress("unused")
	@Subcommand("contacts planet")
	fun onTogglePlanets(
		sender: Player,
		@Optional toggle: Boolean?
	) {
		val contactsPlanets = toggle ?: !PlayerCache[sender].planetsEnabled
		SLPlayer.updateById(sender.slPlayerId, setValue(SLPlayer::planetsEnabled, contactsPlanets))
		sender.success("Changed planet visibility to $contactsPlanets")
	}

	@Suppress("unused")
	@Subcommand("contacts star")
	fun onToggleStars(
		sender: Player,
		@Optional toggle: Boolean?
	) {
		val starsEnabled = toggle ?: !PlayerCache[sender].starsEnabled
		SLPlayer.updateById(sender.slPlayerId, setValue(SLPlayer::starsEnabled, starsEnabled))
		sender.success("Changed star visibility to $starsEnabled")
	}

	@Suppress("unused")
	@Subcommand("contacts beacon")
	fun onToggleBeacons(
		sender: Player,
		@Optional toggle: Boolean?
	) {
		val beaconsEnabled = toggle ?: !PlayerCache[sender].beaconsEnabled
		SLPlayer.updateById(sender.slPlayerId, setValue(SLPlayer::beaconsEnabled, beaconsEnabled))
		sender.success("Changed beacon visibility to $beaconsEnabled")
	}

	@Suppress("unused")
	@Subcommand("contacts station")
	fun onToggleStations(
		sender: Player,
		@Optional toggle: Boolean?
	) {
		val stationsEnabled = toggle ?: !PlayerCache[sender].stationsEnabled
		SLPlayer.updateById(sender.slPlayerId, setValue(SLPlayer::stationsEnabled, stationsEnabled))
		sender.success("Changed station visibility to $stationsEnabled")
	}

	@Suppress("unused")
	@Subcommand("contacts bookmark")
	fun onToggleBookmarks(
		sender: Player,
		@Optional toggle: Boolean?
	) {
		val bookmarksEnabled = toggle ?: !PlayerCache[sender].bookmarksEnabled
		SLPlayer.updateById(sender.slPlayerId, setValue(SLPlayer::bookmarksEnabled, bookmarksEnabled))
		sender.success("Changed bookmark visibility to $bookmarksEnabled")
	}
}
