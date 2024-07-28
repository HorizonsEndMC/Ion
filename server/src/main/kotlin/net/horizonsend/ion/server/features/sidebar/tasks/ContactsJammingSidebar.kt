package net.horizonsend.ion.server.features.sidebar.tasks

import net.horizonsend.ion.common.extensions.informationAction
import net.horizonsend.ion.common.extensions.userErrorAction
import net.horizonsend.ion.server.IonServerComponent
import net.horizonsend.ion.server.miscellaneous.utils.Tasks
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import java.util.UUID

object ContactsJammingSidebar : IonServerComponent() {

    val jammedPlayers = mutableMapOf<UUID, Long>()

    fun jamPlayer(player: Player, jammerName: String) {
        jammedPlayers[player.uniqueId] = System.currentTimeMillis() + 1000L // jam for a second
        player.userErrorAction("Contacts data jammed by $jammerName!")
    }

    override fun onEnable() {
        Tasks.syncRepeat(0L, 10L) {
            for (uuid in jammedPlayers.keys) {
                val unjamTime = jammedPlayers[uuid] ?: 0
                if (unjamTime < System.currentTimeMillis()) {
                    jammedPlayers[uuid]?.let { jammedPlayers.remove(uuid) }
                    Bukkit.getPlayer(uuid)?.informationAction("Contacts data restored")
                }
            }
        }
    }
}