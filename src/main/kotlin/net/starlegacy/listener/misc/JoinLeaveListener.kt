package net.starlegacy.listener.misc

import net.starlegacy.database.schema.misc.SLPlayer
import net.starlegacy.database.schema.misc.SLPlayerId
import net.starlegacy.database.slPlayerId
import net.starlegacy.listener.SLEventListener
import net.starlegacy.util.Tasks
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.player.AsyncPlayerPreLoginEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.litote.kmongo.combine
import org.litote.kmongo.updateOneById
import java.util.Date
import java.util.UUID

object JoinLeaveListener : SLEventListener() {
    override fun supportsVanilla(): Boolean {
        return true
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    fun onPlayerJoin(event: AsyncPlayerPreLoginEvent) {
        updateOrCreatePlayer(event.uniqueId, event.name)
    }

    @EventHandler
    fun onPlayerQuit(event: PlayerQuitEvent) = Tasks.async {
        updateOrCreatePlayer(event.player.uniqueId, event.player.name)
    }

    private fun updateOrCreatePlayer(uuid: UUID, name: String) {
        val id: SLPlayerId = uuid.slPlayerId
        val data: SLPlayer? = SLPlayer.findById(id)

        val now = Date(System.currentTimeMillis())

        when {
            // new person
            data == null -> {
                SLPlayer.col.insertOne(
                    SLPlayer(
                        id,
                        name,
                        now
                    )
                )
                log.info("Registered $name in the database for the first time, join time $now")
                return
            }

            // only need to update last seen
            data.lastKnownName == name -> {
                SLPlayer.col.updateOneById(id, org.litote.kmongo.setValue(SLPlayer::lastSeen, now))
            }

            // set both last seen, and username
            else -> SLPlayer.col.updateOneById(
                id,
                combine(
                    org.litote.kmongo.setValue(SLPlayer::lastSeen, now),
                    org.litote.kmongo.setValue(SLPlayer::lastKnownName, name)
                )
            )
        }
    }
}
