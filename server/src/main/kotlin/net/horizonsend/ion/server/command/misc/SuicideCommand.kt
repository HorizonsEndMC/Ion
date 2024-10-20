package net.horizonsend.ion.server.command.misc

import co.aikar.commands.annotation.CommandAlias
import co.aikar.commands.annotation.Default
import net.horizonsend.ion.common.extensions.information
import net.horizonsend.ion.common.extensions.userError
import net.horizonsend.ion.server.command.SLCommand
import net.horizonsend.ion.server.features.player.CombatTimer
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.event.entity.EntityDamageEvent

@CommandAlias("suicide|unalive")
object SuicideCommand : SLCommand() {
    @Default
    @Suppress("unused")
    fun onSuicide(sender: Player) {
        // Prevent combat timer players from wimping out
        if (CombatTimer.isNpcCombatTagged(sender) || CombatTimer.isPvpCombatTagged(sender)) {
            sender.userError("Your iron will prevents you from giving up so soon. Fight with honor!")
            return
        }

        // Force kill if this event is cancelled
        EntityDamageEvent(sender, EntityDamageEvent.DamageCause.SUICIDE, Double.MAX_VALUE).callEvent()
        sender.health = 0.0

        // Notify everyone of this player's untimely demise
        sender.information("Goodbye cruel world...")
        for (otherPlayer in Bukkit.getOnlinePlayers()) {
            otherPlayer.information("Player ${sender.name} took their own life")
        }
    }
}