package net.horizonsend.ion.server.features.bounties

import net.horizonsend.ion.common.database.schema.misc.SLPlayer
import net.horizonsend.ion.server.IonServer
import net.horizonsend.ion.server.IonServerComponent
import net.horizonsend.ion.server.features.cache.PlayerCache
import net.horizonsend.ion.server.miscellaneous.utils.Notify
import net.horizonsend.ion.server.miscellaneous.utils.VAULT_ECO
import net.horizonsend.ion.server.miscellaneous.utils.slPlayerId
import net.horizonsend.ion.server.miscellaneous.utils.toCreditsString
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.entity.PlayerDeathEvent
import org.litote.kmongo.inc
import org.litote.kmongo.setValue

class Bounties : IonServerComponent() {
	override fun onEnable() {

	}

	override fun onDisable() {

	}

	@EventHandler
	fun onPlayerKill(event: PlayerDeathEvent) {
		if (isNotSurvival()) return

		val victim = event.player
		val killer = event.entity.killer ?: return

		collectBounty(victim, killer)
	}

	fun collectBounty(killer: Player, killed: Player) {
		val cached = PlayerCache[killed]
		val bounty = cached.bounty

		SLPlayer.updateById(killed.slPlayerId, setValue(SLPlayer::bounty, 0.0))

		VAULT_ECO.depositPlayer(killer, bounty)
	}

	fun increaseBounty(bountied: Player, amount: Int) {
		if (isNotSurvival()) return

		val cached = PlayerCache[bountied]
		SLPlayer.updateById(bountied.slPlayerId, inc(SLPlayer::bounty, amount))

		val text = text()
			.append(text(bountied.name, NamedTextColor.DARK_RED))
			.append(text("'s bounty was increased by $amount! It is now ", NamedTextColor.RED))
			.append(text((cached.bounty + amount).toCreditsString(), NamedTextColor.GOLD))
			.build()

		Notify.online(text)
	}

	fun isNotSurvival(): Boolean = IonServer.configuration.serverName.equals("survival", ignoreCase = true)
}
