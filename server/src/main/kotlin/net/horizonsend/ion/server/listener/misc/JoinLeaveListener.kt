package net.horizonsend.ion.server.listener.misc

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.horizonsend.ion.common.database.schema.misc.SLPlayer
import net.horizonsend.ion.common.database.schema.misc.SLPlayerId
import net.horizonsend.ion.common.database.slPlayerId
import net.horizonsend.ion.server.listener.SLEventListener
import net.horizonsend.ion.server.miscellaneous.utils.Notify
import net.horizonsend.ion.server.miscellaneous.utils.Tasks
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

				val welcomeMessage = Component.text().color(NamedTextColor.GOLD)
					.append(Component.text("Welcome "))
					.append(Component.text(name, NamedTextColor.WHITE))
					.append(Component.text(" to the server!"))
					.build()

				Notify online welcomeMessage
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
