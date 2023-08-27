package net.horizonsend.ion.server.features.sidebar.command

import co.aikar.commands.annotation.CommandAlias
import co.aikar.commands.annotation.Default
import co.aikar.commands.annotation.Optional
import co.aikar.commands.annotation.Subcommand
import net.horizonsend.ion.common.database.schema.misc.SLPlayer
import net.horizonsend.ion.common.extensions.success
import net.horizonsend.ion.common.extensions.userError
import net.horizonsend.ion.server.features.cache.PlayerCache
import net.horizonsend.ion.server.miscellaneous.utils.slPlayerId
import net.horizonsend.ion.server.command.SLCommand
import org.bukkit.entity.Player
import org.litote.kmongo.set
import org.litote.kmongo.setTo

@CommandAlias("sidebar contacts")
object ContactsCommand : SLCommand() {
	@Default
	@Suppress("unused")
	fun defaultCase(
		sender: Player
	) {
		sender.userError("Usage: /contacts <contact> [toggle]")
	}

	@Suppress("unused")
	@Subcommand("starship")
	fun onToggleStarship(
		sender: Player,
		@Optional toggle: Boolean?
	) {
		val contactsStarships = toggle ?: !PlayerCache[sender].contactsStarships
		SLPlayer.updateById(sender.slPlayerId, set(SLPlayer::contactsStarships setTo contactsStarships))
		sender.success("Changed starship visibility to $contactsStarships")
	}

	@Suppress("unused")
	@Subcommand("lastStarship")
	fun onToggleLastStarship(
		sender: Player,
		@Optional toggle: Boolean?
	) {
		val contactsLastStarship = toggle ?: !PlayerCache[sender].lastStarshipEnabled
		SLPlayer.updateById(sender.slPlayerId, set(SLPlayer::lastStarshipEnabled setTo contactsLastStarship))
		sender.success("Changed last starship visibility to $contactsLastStarship")
	}

	@Suppress("unused")
	@Subcommand("planet")
	fun onTogglePlanets(
		sender: Player,
		@Optional toggle: Boolean?
	) {
		val contactsPlanets = toggle ?: !PlayerCache[sender].planetsEnabled
		SLPlayer.updateById(sender.slPlayerId, set(SLPlayer::planetsEnabled setTo contactsPlanets))
		sender.success("Changed planet visibility to $contactsPlanets")
	}

	@Suppress("unused")
	@Subcommand("star")
	fun onToggleStars(
		sender: Player,
		@Optional toggle: Boolean?
	) {
		val starsEnabled = toggle ?: !PlayerCache[sender].starsEnabled
		SLPlayer.updateById(sender.slPlayerId, set(SLPlayer::starsEnabled setTo starsEnabled))
		sender.success("Changed star visibility to $starsEnabled")
	}

	@Suppress("unused")
	@Subcommand("beacon")
	fun onToggleBeacons(
		sender: Player,
		@Optional toggle: Boolean?
	) {
		val beaconsEnabled = toggle ?: !PlayerCache[sender].beaconsEnabled
		SLPlayer.updateById(sender.slPlayerId, set(SLPlayer::beaconsEnabled setTo beaconsEnabled))
		sender.success("Changed beacon visibility to $beaconsEnabled")
	}
}
