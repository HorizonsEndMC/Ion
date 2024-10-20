package net.horizonsend.ion.server.command.misc

import co.aikar.commands.annotation.CommandAlias
import co.aikar.commands.annotation.CommandCompletion
import co.aikar.commands.annotation.CommandPermission
import co.aikar.commands.annotation.Description
import co.aikar.commands.annotation.Optional
import co.aikar.commands.annotation.Subcommand
import net.horizonsend.ion.common.database.schema.misc.SLPlayer
import net.horizonsend.ion.common.extensions.success
import net.horizonsend.ion.server.command.SLCommand
import net.horizonsend.ion.server.features.cache.PlayerCache
import net.horizonsend.ion.server.features.player.CombatTimer
import net.horizonsend.ion.server.miscellaneous.utils.slPlayerId
import org.bukkit.entity.Player
import org.litote.kmongo.setValue

@CommandAlias("combattimer")
object CombatTimerCommand : SLCommand() {

    @Subcommand("toggle")
    @Suppress("unused")
    @CommandCompletion("true|false")
    @Description("Toggle combat timer alerts")
    fun onToggle(sender: Player, @Optional toggle: Boolean?) {
        val enableCombatTimerAlerts = toggle ?: !PlayerCache[sender].enableCombatTimerAlerts
        SLPlayer.updateById(sender.slPlayerId, setValue(SLPlayer::enableCombatTimerAlerts, enableCombatTimerAlerts))
        PlayerCache[sender.uniqueId].enableCombatTimerAlerts = enableCombatTimerAlerts
        sender.success("Changed enabled combat timer alerts to $enableCombatTimerAlerts")
        if (enableCombatTimerAlerts) {
            sender.success("You will be notified when you gain a combat timer")
        } else {
            sender.success("You will no longer be notified when you gain a combat timer")
        }
    }

    @Subcommand("npc give")
    @Suppress("unused")
    @CommandPermission("ion.combattimer")
    @CommandCompletion("@players")
    @Description("Apply an NPC combat tag to a player")
    fun onGiveNpcCombatTag(sender: Player, target: String) {
        val player = resolveOfflinePlayer(target)

        CombatTimer.addNpcCombatTag(player)
        sender.success("Gave $target an NPC combat tag")
    }

    @Subcommand("npc remove")
    @Suppress("unused")
    @CommandPermission("ion.combattimer")
    @CommandCompletion("@players")
    @Description("Remove an NPC combat tag from a player")
    fun onRemoveNpcCombatTag(sender: Player, target: String) {
        val player = resolveOfflinePlayer(target)

        CombatTimer.removeNpcCombatTag(player)
        sender.success("Removed NPC combat tag from $target")
    }

    @Subcommand("pvp give")
    @Suppress("unused")
    @CommandPermission("ion.combattimer")
    @CommandCompletion("@players")
    @Description("Apply a PvP combat tag to a player")
    fun onGivePvpCombatTag(sender: Player, target: String) {
        val player = resolveOfflinePlayer(target)

        CombatTimer.addPvpCombatTag(player)
        sender.success("Gave $target a PvP combat tag")
    }

    @Subcommand("pvp remove")
    @Suppress("unused")
    @CommandPermission("ion.combattimer")
    @CommandCompletion("@players")
    @Description("Remove a PvP combat tag from a player")
    fun onRemovePvpCombatTag(sender: Player, target: String) {
        val player = resolveOfflinePlayer(target)

        CombatTimer.removePvpCombatTag(player)
        sender.success("Removed PvP combat tag from $target")
    }
}