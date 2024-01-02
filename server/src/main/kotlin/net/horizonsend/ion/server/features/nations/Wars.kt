package net.horizonsend.ion.server.features.nations

import net.horizonsend.ion.common.IonComponent
import net.horizonsend.ion.common.database.Oid
import net.horizonsend.ion.common.database.cache.nations.NationCache
import net.horizonsend.ion.common.database.cache.nations.WarCache
import net.horizonsend.ion.common.database.schema.misc.SLPlayer
import net.horizonsend.ion.common.database.schema.misc.SLPlayerId
import net.horizonsend.ion.common.database.schema.nations.Nation
import net.horizonsend.ion.common.database.schema.nations.war.Truce
import net.horizonsend.ion.common.database.schema.nations.war.War
import net.horizonsend.ion.common.database.schema.nations.war.WarGoal
import net.horizonsend.ion.common.database.schema.nations.war.WhitePeace
import net.horizonsend.ion.common.extensions.success
import net.horizonsend.ion.common.utils.discord.Embed
import net.horizonsend.ion.common.utils.text.colors.HEColorScheme
import net.horizonsend.ion.common.utils.text.template
import net.horizonsend.ion.server.features.cache.PlayerCache
import net.horizonsend.ion.server.features.misc.messaging.ServerDiscordMessaging
import net.horizonsend.ion.server.miscellaneous.utils.Notify
import net.horizonsend.ion.server.miscellaneous.utils.Tasks
import net.horizonsend.ion.server.miscellaneous.utils.slPlayerId
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.TextColor
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.player.PlayerJoinEvent
import org.litote.kmongo.eq
import org.litote.kmongo.or
import org.litote.kmongo.setValue
import java.util.concurrent.TimeUnit

object Wars : IonComponent() {
	private const val STALEMATE_THRESHOLD = 100

	fun warMessageTemplate(message: String, vararg params: Any?) = template(
		message = message,
		color = HEColorScheme.HE_MEDIUM_GRAY,
		paramColor = TextColor.fromHexString("#F4372A")!!,
		useQuotesAroundObjects = false,
		*params
	)

	fun importantWarMessageTemplate(message: String, vararg params: Any?) = template(
		message = message,
		color = TextColor.fromHexString("#F4372A")!!,
		paramColor = HEColorScheme.HE_MEDIUM_GRAY,
		useQuotesAroundObjects = false,
		*params
	)

	override fun onEnable() {
		Tasks.asyncRepeat(200L, 200L, ::checkExpiredRequests)
	}

	private fun checkExpiredRequests() {
		requestedStalemates.forEach { it.checkExpired() }
	}

	private fun sendMessages(message: Component, embed: Embed) {
		Notify.notifyOnlineAction(message)

		ServerDiscordMessaging.eventsEmbed(embed)
		ServerDiscordMessaging.globalEmbed(embed)
	}

	/** Starts a war between the two nations */
	fun startWar(declaring: Oid<Nation>, defender: Oid<Nation>, goal: WarGoal) {
		val name = formatName(declaring, defender)

		War.create(declaring, defender, goal, name)

		val declaringName = NationCache[declaring].name
		val defendingName = NationCache[declaring].name

		val embed = createStartEmbed(declaringName, defendingName, goal)
		sendMessages(importantWarMessageTemplate("{0} has declared war on {1}!", declaringName, defendingName), embed)
	}

	private fun createStartEmbed(declaringName: String, defendingName: String, goal: WarGoal): Embed {
		return Embed(
			title = "$declaringName has declared war on $defendingName!",
			description = "$declaringName is demanding ${goal.getVerb()} $defendingName!"
		)
	}

	/** Ends the war in a point determined state, surrenders are handled differently. */
	fun endWar(id: Oid<War>) = Tasks.async {
		val war = War.findById(id) ?: error("War $id does not exist!")

		if (war.points > STALEMATE_THRESHOLD) return@async endWarVictory(id)
		if (war.points < -STALEMATE_THRESHOLD) return@async endWarLoss(id)
		return@async endWarStalemate(id)
	}

	/** Ends the war in a victory for the attacker */
	fun endWarVictory(id: Oid<War>) = Tasks.async {
		val document = War.findById(id) ?: error("War $id not found!")

		Truce.create(
			victor = document.aggressor, // Aggressor has won
			defeated = document.defender,
			war = id,
			goal = document.aggressorGoal
		)

		War.updateById(id, setValue(War::result, War.Result.AGGRESSOR_VICTORY))

		val embed = createEndEmbed(id, War.Result.AGGRESSOR_VICTORY)
		sendMessages(importantWarMessageTemplate("The {0} has ended in a aggressor victory!!", document.name), embed)
	}

	/** Ends the war in a loss for the attacker */
	fun endWarLoss(id: Oid<War>) = Tasks.async {
		val document = War.findById(id) ?: error("War $id not found!")

		Truce.create(
			victor = document.defender, // Aggressor has lost
			defeated = document.aggressor,
			war = id,
			goal = document.defenderGoal
		)

		War.updateById(id, setValue(War::result, War.Result.DEFENDER_VICTORY))

		val embed = createEndEmbed(id, War.Result.DEFENDER_VICTORY)
		sendMessages(importantWarMessageTemplate("The {0} has ended in a defensive victory!!", document.name), embed)
	}

	/** Ends the war in a stalemate / white peace */
	fun endWarStalemate(id: Oid<War>) = Tasks.async {
		val document = War.findById(id) ?: error("War $id not found!")

		Truce.create(
			victor = document.defender, // Order shouldn't matter
			defeated = document.aggressor,
			war = id,
			goal = WhitePeace
		)

		War.updateById(id, setValue(War::result, War.Result.WHITE_PEACE))

		val embed = createEndEmbed(id, War.Result.WHITE_PEACE)
		sendMessages(importantWarMessageTemplate("The {0} has ended in a stalemate!", document.name), embed)
	}

	private fun createEndEmbed(id: Oid<War>, result: War.Result): Embed {
		val document = War.findById(id) ?: error("War $id not found!")

		return Embed(
			title = "War Ended!",
			description = "The ${document.name} has ended in a ${result.displayName}!"
		)
	}

	// Bad solution, if wars ever get
	private val numbers = listOf("First", "Second", "Third", "Fourth", "Fifth", "Sixth", "Seventh", "Eighth", "Ninth", "Tenth")

	/** Formats a standard name for the war */
	private fun formatName(aggressor: Oid<Nation>, defender: Oid<Nation>): String {
		val previousCount = War.count(War.participantQuery(aggressor, defender)).toInt()
		val prefix = numbers.getOrNull(previousCount) ?: "Next"

		val aggressorName = NationCache[aggressor].name
		val defenderName = NationCache[defender].name

		return "$prefix $aggressorName - $defenderName War"
	}

	data class RequestedStalemate(
		val warId: Oid<War>,
		val requestingNation: Oid<Nation>,
		val otherNation: Oid<Nation>,
		val requestingPlayer: SLPlayerId,
		val time: Long
	) {
		fun format(): Component {
			val playerName = SLPlayer.getName(requestingPlayer)
			val nationName = NationCache[requestingNation].name
			val warName = WarCache[warId].name

			return warMessageTemplate(
				"{0} of {1} has requested that the {2} war end in a white peace. Use /war to accept or decline this offer.",
				playerName,
				nationName,
				warName
			)
		}

		fun checkExpired(): Boolean {
			val cached = WarCache[warId]

			if (cached.result != null) return true

			// If before the time in which it'd be expired
			if (time < expirationTime) {
				Notify.nationCrossServer(requestingNation, warMessageTemplate("Your requested white peace in {0} has expired.", cached.name))

				return true
			}

			return false
		}

		companion object {
			val REQUEST_EXPIRATION_TIME = TimeUnit.DAYS.toMillis(1)
			val expirationTime get() = System.currentTimeMillis() - REQUEST_EXPIRATION_TIME
		}
	}

	private val requestedStalemates = mutableListOf<RequestedStalemate>()

	fun requestStalemate(player: Player, otherNation: Oid<Nation>, war: Oid<War>) {
		val cached = PlayerCache[player]
		val nation = cached.nationOid ?: error("Nationless players should not be able to request a stalemate")

		val cachedWar = WarCache[war]
		val cachedNation = NationCache[nation]

		requestedStalemates.add(
			RequestedStalemate(
				warId = war,
				requestingNation = nation,
				requestingPlayer = player.slPlayerId,
				otherNation = otherNation,
				time = System.currentTimeMillis()
			)
		)

		Notify.nationCrossServer(otherNation, warMessageTemplate(
			"{0} of {1} has requested that the {2} war end in a white peace. Use /war to accept or decline this offer.",
			player.name,
			cachedNation.name,
			cachedWar.name
		))

		player.success("The request has been sent. ")
	}

	private fun checkStalemateRequests(playerId: SLPlayerId): Collection<RequestedStalemate>? {
		val slPlayer = SLPlayer[playerId] ?: return null

		val nation = slPlayer.nation ?: return null

		// Get active wars that the nation is participating in
		val wars = War.find(or(War::aggressor eq nation, War::defender eq nation, War::result eq null))

		if (wars.none()) return null

		val requests = mutableListOf<RequestedStalemate>()

		wars.forEach { activeWar ->
			val request = requestedStalemates.firstOrNull { it.warId == activeWar._id } ?: return@forEach

			if (request.checkExpired()) return@forEach

			requests += request
		}

		return requests
	}

	/** Messages a player on login if a stalemate has been requested, and they have permission to act on it */
	@EventHandler
	fun onPlayerJoin(event: PlayerJoinEvent) = Tasks.async {
		val requested = checkStalemateRequests(event.player.slPlayerId) ?: return@async

		for (request in requested) {
			event.player.sendMessage(request.format())
		}
	}


}
