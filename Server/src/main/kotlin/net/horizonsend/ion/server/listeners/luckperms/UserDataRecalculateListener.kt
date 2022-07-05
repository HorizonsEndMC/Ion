package net.horizonsend.ion.server.listeners.luckperms

import java.util.UUID
import net.horizonsend.ion.common.listeners.luckperms.AbstractUserDataRecalculateListener
import net.horizonsend.ion.common.utilities.constructPlayerListName
import org.bukkit.Bukkit

class UserDataRecalculateListener : AbstractUserDataRecalculateListener() {
	override fun onUserDataRecalculateEvent(uuid: UUID, prefix: String?, suffix: String?) {
		val player = Bukkit.getPlayer(uuid) ?: return

		player.playerListName(constructPlayerListName(player.name, prefix, suffix))
	}
}