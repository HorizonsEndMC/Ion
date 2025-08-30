package net.horizonsend.ion.server.features.starship.modules

import net.horizonsend.ion.common.utils.discord.Embed
import net.horizonsend.ion.common.utils.discord.Embed.Field
import net.horizonsend.ion.common.utils.text.MessageFactory
import net.horizonsend.ion.common.utils.text.bracketed
import net.horizonsend.ion.common.utils.text.colors.HEColorScheme.Companion.HE_LIGHT_GRAY
import net.horizonsend.ion.common.utils.text.ofChildren
import net.horizonsend.ion.common.utils.text.orEmpty
import net.horizonsend.ion.common.utils.text.template
import net.horizonsend.ion.server.IonServer
import net.horizonsend.ion.server.configuration.ConfigurationFiles
import net.horizonsend.ion.server.features.ai.module.misc.CaravanModule
import net.horizonsend.ion.server.features.chat.Discord
import net.horizonsend.ion.server.features.chat.Discord.asDiscord
import net.horizonsend.ion.server.features.progression.ShipKillXP
import net.horizonsend.ion.server.features.starship.active.ActiveStarship
import net.horizonsend.ion.server.features.starship.control.controllers.NoOpController
import net.horizonsend.ion.server.features.starship.control.controllers.ai.AIController
import net.horizonsend.ion.server.features.starship.control.controllers.player.PlayerController
import net.horizonsend.ion.server.features.starship.damager.AIShipDamager
import net.horizonsend.ion.server.features.starship.damager.Damager
import net.horizonsend.ion.server.features.world.IonWorld.Companion.hasFlag
import net.horizonsend.ion.server.features.world.WorldFlag
import net.horizonsend.ion.server.miscellaneous.utils.Notify
import net.horizonsend.ion.server.miscellaneous.utils.getArenaPrefix
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.Component.newline
import net.kyori.adventure.text.Component.space
import net.kyori.adventure.text.Component.text

class AISinkMessageFactory(private val sunkShip: ActiveStarship) : MessageFactory {
	override fun execute() {
		val data = sunkShip.damagers

		// First person got the final blow
		val sortedByTime = data.entries.filter { (damager, data) ->
				if (damager is AIShipDamager && damager.starship.controller is NoOpController) {
					IonServer.slF4JLogger.warn("Removed AI damager $damager")
					return@filter false
				}

				if (data.lastDamaged < ShipKillXP.damagerExpiration) {
					IonServer.slF4JLogger.warn("Removed expired damager $damager")
					return@filter false
				}

				true
			}
			.sortedByDescending { it.value.lastDamaged }

		if (sortedByTime.isEmpty()) return IonServer.slF4JLogger.warn("Starship sunk with no damagers")

		val (killerDamager, _) = sortedByTime.first()
		val killerShip = killerDamager.starship

		val inArena = sunkShip.world.hasFlag(WorldFlag.ARENA)

		val sinkMessage = if (killerShip != null) template(
			text("{0}{1} was sunk by {2} piloting {3}"),
			useQuotesAroundObjects = false,
			getArenaPrefix(sunkShip.world),
			sunkShip.getDisplayName(),
			killerDamager.getDisplayName(),
			killerShip.getDisplayName()
		) else template(
			text("{0}{1} was sunk by {2}"),
			useQuotesAroundObjects = false,
			getArenaPrefix(sunkShip.world),
			sunkShip.getDisplayName(),
			killerDamager.getDisplayName(),
		)

		val assists = getAssists(sortedByTime.map { it.key })

		if ((sunkShip.controller as? AIController)?.getUtilModule(CaravanModule::class.java) != null && !inArena) {
			Notify.chatAndGlobal(ofChildren(sinkMessage, assists.orEmpty()))
		} else {
			IonServer.server.sendMessage(ofChildren(sinkMessage, assists.orEmpty()))
		}
		if (sunkShip.initialBlockCount < 12000) return // super capitals after this point
		if (inArena) return

		//todo: replace with another url.
		val headURL = (sunkShip.controller as? PlayerController)?.player?.name?.let { "https://minotar.net/avatar/$it" }
		val killedNationColor = sunkShip.controller.damager.color.asRGB()

		val fields = mutableListOf(Field(name = asDiscord(sinkMessage), value = "", inline = false))
		if (assists != null) fields.add(Field("Assisted By:", asDiscord(assists)))

		val embed = Embed(
			title = "Starship Kill",
			timestamp = System.currentTimeMillis(),
			color = killedNationColor,
			thumbnail = headURL,
			fields = fields
		)

		Discord.sendEmbed(ConfigurationFiles.discordSettings().eventsChannel, embed)
		Discord.sendEmbed(ConfigurationFiles.discordSettings().globalChannel, embed)
	}

	private fun getAssists(damagers: Iterable<Damager>) : Component? {
		val sortedByTime = damagers.iterator()
		if (!sortedByTime.hasNext()) return null

		val assistsMessage = text().append(ofChildren(space(), bracketed(text("Assists", HE_LIGHT_GRAY))))
		val hoverBuilder = text()

		// Take 5 damagers
		while (sortedByTime.hasNext()) {
			val next = sortedByTime.next()
			hoverBuilder.append(next.getDisplayName())
			if (sortedByTime.hasNext()) hoverBuilder.append(newline())
		}

		assistsMessage.hoverEvent(hoverBuilder.build())

		return assistsMessage.build()
	}
}
