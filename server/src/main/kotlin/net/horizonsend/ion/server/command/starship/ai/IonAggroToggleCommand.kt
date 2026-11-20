package net.horizonsend.ion.server.command.starship.ai

import co.aikar.commands.annotation.CommandAlias
import co.aikar.commands.annotation.CommandPermission
import co.aikar.commands.annotation.Default
import co.aikar.commands.annotation.Description
import net.horizonsend.ion.common.extensions.success
import net.horizonsend.ion.server.command.SLCommand
import org.bukkit.entity.Player
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

@CommandAlias("ionaggrotoggle")
@CommandPermission("group.dutymode")
object IonAggroToggleCommand : SLCommand() {
	val aiIgnoredPlayers: MutableSet<UUID> = ConcurrentHashMap.newKeySet()

	@Suppress("Unused")
	@Default
	@Description("Toggle whether AI ships ignore you while in dutymode")
	fun onIonAggroToggle(sender: Player) {
		if (aiIgnoredPlayers.remove(sender.uniqueId)) {
			sender.success("Disabled AI aggro ignore")
		} else {
			aiIgnoredPlayers.add(sender.uniqueId)
			sender.success("Enabled AI aggro ignore")
		}
	}
}
