package net.horizonsend.ion.server.listeners.luckperms

import java.util.UUID
import net.horizonsend.ion.common.listeners.luckperms.AbstractUserDataRecalculateListener
import net.kyori.adventure.text.Component
import org.bukkit.Bukkit

class UserDataRecalculateListener : AbstractUserDataRecalculateListener() {
	override fun onUserDataRecalculateEvent(uuid: UUID, playerListName: Component) {
		Bukkit.getPlayer(uuid)?.playerListName(playerListName)
	}
}