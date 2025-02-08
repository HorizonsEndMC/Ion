package net.horizonsend.ion.server.features.starship.movement

import net.horizonsend.ion.common.extensions.information
import net.horizonsend.ion.common.extensions.userError
import net.horizonsend.ion.server.IonServerComponent
import net.horizonsend.ion.server.configuration.ConfigurationFiles
import net.horizonsend.ion.server.features.player.CombatTimer
import net.horizonsend.ion.server.miscellaneous.utils.Tasks
import net.horizonsend.ion.server.miscellaneous.utils.listen
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.event.entity.PlayerDeathEvent
import java.time.Duration
import java.util.UUID

object PlanetTeleportCooldown : IonServerComponent() {

    private val ENTRY_COOLDOWN = Duration.ofMinutes(2)
    private val EXIT_COOLDOWN = Duration.ofMinutes(2)

    private val entryCooldown = mutableMapOf<UUID, Long>()
    private val exitCooldown = mutableMapOf<UUID, Long>()

    private var enabled = false

    override fun onEnable() {
        enabled = ConfigurationFiles.featureFlags().combatTimers

        if (!enabled) return

        Tasks.syncRepeat(0L, 20L) {
            // Remove planet entry/exit timers if enough time has elapsed
            for (entry in entryCooldown) {
                if (entry.value <= System.currentTimeMillis()) {
                    entryCooldown.remove(entry.key)
                    Bukkit.getPlayer(entry.key)?.information("You can now enter planets again")
                }
            }

            for (entry in exitCooldown) {
                if (entry.value <= System.currentTimeMillis()) {
                    exitCooldown.remove(entry.key)
                    Bukkit.getPlayer(entry.key)?.information("You can now exit planets again")
                }
            }
        }

        // Remove all combat tags on death
        listen<PlayerDeathEvent> { event ->
            if (entryCooldown[event.player.uniqueId] != null) {
                event.player.information("You can now enter planets again")
                entryCooldown.remove(event.player.uniqueId)
            }
            if (exitCooldown[event.player.uniqueId] != null) {
                event.player.information("You can now exit planets again")
                exitCooldown.remove(event.player.uniqueId)
            }
        }
    }

    fun cannotEnterPlanets(player: Player): Boolean {
        return entryCooldown[player.uniqueId] != null
    }

    fun cannotExitPlanets(player: Player): Boolean {
        return exitCooldown[player.uniqueId] != null
    }

    fun addEnterPlanetRestriction(player: Player) {
        if (!enabled) return

        // Only applies if the player is not in NPC combat or PVP combat
        if (!CombatTimer.isNpcCombatTagged(player) && !CombatTimer.isPvpCombatTagged(player)) return

        player.userError("You can no longer re-enter a planet for another ${ENTRY_COOLDOWN.toMinutes()} minutes")
        addEnterPlanetCooldown(player.uniqueId)
    }

    fun addExitPlanetRestriction(player: Player) {
        if (!enabled) return

        // Only applies if the player is not in NPC combat or PVP combat
        if (!CombatTimer.isNpcCombatTagged(player) && !CombatTimer.isPvpCombatTagged(player)) return

        player.userError("You can no longer re-exit a planet for another ${EXIT_COOLDOWN.toMinutes()} minutes")
        addExitPlanetCooldown(player.uniqueId)
    }

    fun addEnterPlanetCooldown(uuid: UUID) {
        entryCooldown[uuid] = System.currentTimeMillis() + ENTRY_COOLDOWN.toMillis()
    }

    fun addExitPlanetCooldown(uuid: UUID) {
        exitCooldown[uuid] = System.currentTimeMillis() + EXIT_COOLDOWN.toMillis()
    }

    fun removeEnterPlanetCooldown(uuid: UUID) {
        entryCooldown.remove(uuid)
    }

    fun removeExitPlanetCooldown(uuid: UUID) {
        exitCooldown.remove(uuid)
    }
}
