package net.horizonsend.ion.server.features.sidebar.commands

import co.aikar.commands.BaseCommand
import co.aikar.commands.annotation.CommandAlias
import co.aikar.commands.annotation.Optional
import co.aikar.commands.annotation.Subcommand
import net.horizonsend.ion.common.database.PlayerData
import net.horizonsend.ion.common.extensions.success
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
        val sidebarSettings = transaction { PlayerData[sender.name]!!.sidebarSettings.find { it.playerData.uuid == sender } }
        if (sidebarSettings != null) sidebarSettings.contactsStarships = toggle ?: !sidebarSettings.contactsStarships
        sender.success("Changed Starship visibility to ${sidebarSettings?.contactsStarships}")
    }

    @Suppress("unused")
    @Subcommand("planet")
    fun onTogglePlanets(
        sender: Player,
        @Optional toggle: Boolean?
    ) = transaction {
        val sidebarSettings = transaction { PlayerData[sender.name]!!.sidebarSettings.find { it.playerData.uuid == sender } }
        if (sidebarSettings != null) sidebarSettings.contactsPlanets = toggle ?: !sidebarSettings.contactsPlanets
        sender.success("Changed Starship visibility to ${sidebarSettings?.contactsPlanets}")
    }

    @Suppress("unused")
    @Subcommand("star")
    fun onToggleStars(
        sender: Player,
        @Optional toggle: Boolean?
    ) = transaction {
        val sidebarSettings = transaction { PlayerData[sender.name]!!.sidebarSettings.find { it.playerData.uuid == sender } }
        if (sidebarSettings != null) sidebarSettings.contactsStars = toggle ?: !sidebarSettings.contactsStars
        sender.success("Changed Starship visibility to ${sidebarSettings?.contactsStars}")
    }

    @Suppress("unused")
    @Subcommand("beacon")
    fun onToggleBeacons(
        sender: Player,
        @Optional toggle: Boolean?
    ) = transaction {
        val sidebarSettings = transaction { PlayerData[sender.name]!!.sidebarSettings.find { it.playerData.uuid == sender } }
        if (sidebarSettings != null) sidebarSettings.contactsBeacons = toggle ?: !sidebarSettings.contactsBeacons
        sender.success("Changed Starship visibility to ${sidebarSettings?.contactsBeacons}")
    }
}