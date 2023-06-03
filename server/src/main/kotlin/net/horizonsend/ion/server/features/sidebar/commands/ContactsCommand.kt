package net.horizonsend.ion.server.features.sidebar.commands

import co.aikar.commands.BaseCommand
import co.aikar.commands.annotation.CommandAlias
import co.aikar.commands.annotation.Optional
import co.aikar.commands.annotation.Subcommand
import net.horizonsend.ion.common.database.PlayerData
import org.bukkit.entity.Player
import org.jetbrains.exposed.sql.transactions.transaction

@CommandAlias("contacts")
class ContactsCommand : BaseCommand() {

    @Suppress("unused")
    @Subcommand("starship")
    fun onToggleStarship(
        sender: Player,
        @Optional toggle: Boolean?
    ) = transaction {
        val sidebarSettings = PlayerData[sender.name]!!.sidebarSettings
        sidebarSettings.contactsStarships = toggle ?: !sidebarSettings.contactsStarships
    }

    @Suppress("unused")
    @Subcommand("planet")
    fun onTogglePlanets(
        sender: Player,
        @Optional toggle: Boolean?
    ) = transaction {
        val sidebarSettings = PlayerData[sender.name]!!.sidebarSettings
        sidebarSettings.contactsPlanets = toggle ?: !sidebarSettings.contactsPlanets
    }

    @Suppress("unused")
    @Subcommand("star")
    fun onToggleStars(
        sender: Player,
        @Optional toggle: Boolean?
    ) = transaction {
        val sidebarSettings = PlayerData[sender.name]!!.sidebarSettings
        sidebarSettings.contactsStars = toggle ?: !sidebarSettings.contactsStars
    }

    @Suppress("unused")
    @Subcommand("beacon")
    fun onToggleBeacons(
        sender: Player,
        @Optional toggle: Boolean?
    ) = transaction {
        val sidebarSettings = PlayerData[sender.name]!!.sidebarSettings
        sidebarSettings.contactsBeacons = toggle ?: !sidebarSettings.contactsBeacons
    }
}