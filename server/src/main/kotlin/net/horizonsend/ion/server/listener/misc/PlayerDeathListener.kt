package net.horizonsend.ion.server.listener.misc

import net.horizonsend.ion.server.command.admin.SequenceAdminCommand.end
import net.horizonsend.ion.server.configuration.ConfigurationFiles
import net.horizonsend.ion.server.features.sequences.SequenceKeys
import net.horizonsend.ion.server.features.sequences.SequenceManager
import net.horizonsend.ion.server.features.world.IonWorld.Companion.hasFlag
import net.horizonsend.ion.server.features.world.WorldFlag
import net.horizonsend.ion.server.listener.SLEventListener
import net.horizonsend.ion.server.miscellaneous.utils.Notify
import net.kyori.adventure.text.Component
import org.bukkit.damage.DamageType
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.entity.PlayerDeathEvent

object PlayerDeathListener : SLEventListener() {
	@EventHandler(ignoreCancelled = false, priority = EventPriority.MONITOR)
	fun onPlayerDeath(event: PlayerDeathEvent) {
		if (event.isCancelled) return

		if (!ConfigurationFiles.serverConfiguration().crossServerDeathMessages) return

		val message = event.deathMessage()
		message?.let {
			Notify.chatAndGlobal(message)
			event.deathMessage(null)
		}
	}

	@EventHandler
	fun onPlayerDeathAboveMaxHeight(event: PlayerDeathEvent) {
		if (event.isCancelled) return

		if (event.damageSource.damageType != DamageType.OUT_OF_WORLD) return

		val player = event.player
		if (player.location.y < player.location.world.maxHeight + 64) return

		event.deathMessage(Component.text("${player.name} ascended above the world"))
	}

	@EventHandler
	fun onPlayerDeathInTutorialWorld(event: PlayerDeathEvent) {
		if (event.isCancelled) return

		if (!event.player.world.hasFlag(WorldFlag.TUTORIAL_WORLD)) return

		event.drops.clear()
		event.keepInventory = true

		// Restart tutorial on death
		SequenceManager.endSequence(event.player, SequenceKeys.TUTORIAL.getValue())
		SequenceManager.endSequence(event.player, SequenceKeys.TUTORIAL_TRANSIT_HUB.getValue())
		SequenceManager.clearSequenceData(event.player)
		SequenceManager.startPhase(event.player, SequenceKeys.TUTORIAL, SequenceKeys.TUTORIAL.getValue().firstPhase)
	}
}
