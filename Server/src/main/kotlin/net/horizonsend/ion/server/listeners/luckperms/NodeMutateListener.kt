package net.horizonsend.ion.server.listeners.luckperms

import net.horizonsend.ion.common.listeners.luckperms.AbstractNodeMutateListener
import net.horizonsend.ion.common.utilities.constructPlayerListName
import org.bukkit.Bukkit

class NodeMutateListener : AbstractNodeMutateListener() {
	override fun onNodeMutateEvent() {
		// TODO: Really we should only be updating the one player, but that's effort.
		Bukkit.getOnlinePlayers().forEach { player ->
			constructPlayerListName(player.name, player.uniqueId).thenAcceptAsync {
				player.playerListName(it)
			}
		}
	}
}