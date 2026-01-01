package net.horizonsend.ion.server.features.progression

import net.horizonsend.ion.common.database.cache.nations.RelationCache
import net.horizonsend.ion.common.database.schema.misc.SLPlayer
import net.horizonsend.ion.common.database.schema.misc.SLPlayerId
import net.horizonsend.ion.common.database.schema.nations.NationRelation
import net.horizonsend.ion.common.database.uuid
import net.horizonsend.ion.common.utils.text.colors.HEColorScheme.Companion.HE_MEDIUM_GRAY
import net.horizonsend.ion.common.utils.text.colors.PIRATE_SATURATED_RED
import net.horizonsend.ion.common.utils.text.template
import net.horizonsend.ion.common.utils.text.toCreditComponent
import net.horizonsend.ion.server.configuration.ConfigurationFiles
import net.horizonsend.ion.server.core.IonServerComponent
import net.horizonsend.ion.server.features.cache.PlayerCache
import net.horizonsend.ion.server.features.starship.TypeCategory
import net.horizonsend.ion.server.features.starship.control.controllers.player.PlayerController
import net.horizonsend.ion.server.features.starship.damager.PlayerDamager
import net.horizonsend.ion.server.features.starship.event.StarshipExplodeEvent
import net.horizonsend.ion.server.miscellaneous.utils.Notify
import net.horizonsend.ion.server.miscellaneous.utils.Tasks
import net.horizonsend.ion.server.miscellaneous.utils.VAULT_ECO
import net.horizonsend.ion.server.miscellaneous.utils.slPlayerId
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.Component.text
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.entity.PlayerDeathEvent
import org.litote.kmongo.inc
import org.litote.kmongo.setValue

object Bounties : IonServerComponent() {
	@Suppress("unused")
	@EventHandler(priority = EventPriority.HIGH)
	fun onPlayerKill(event: PlayerDeathEvent) = Tasks.async {
		if (!ConfigurationFiles.featureFlags().bounties) return@async
		val victim = event.player
		val killer = event.entity.killer ?: return@async

		// Check names because of combat NPCs
		if (killer.name == victim.name) return@async

		// gets the victim's slPlayerId, even if they are offline (in the case of combat NPCs)
		val victimSlPlayerId = if (Bukkit.getPlayer(victim.name) != null) victim.slPlayerId
		// Combat NPC entities are the "Player" class, but attempting to resolve their slPlayerId directly won't work
		else Bukkit.getOfflinePlayerIfCached(victim.name)?.let { PlayerCache[it.uniqueId].id } ?: return@async

		collectBounty(killer, victimSlPlayerId, victim.name)

		val amount = 2500.0

		val killerBounty = PlayerCache[killer].bounty

		val reason = template(
			text("For killing {0}, {1}'s bounty was increased by {2}! It is now {3}", HE_MEDIUM_GRAY),
			paramColor = PIRATE_SATURATED_RED,
			victim.name,
			killer.name,
			amount.toCreditComponent(),
			(killerBounty + amount).toCreditComponent()
		)

		increaseBounty(killer, amount, reason)
	}

	@Suppress("unused")
	@EventHandler
	fun onShipSink(event: StarshipExplodeEvent) {
		if (!ConfigurationFiles.featureFlags().bounties) return
		val victim = (event.starship.controller as? PlayerController)?.player ?: return
		if (event.starship.type.typeCategory == TypeCategory.WAR_SHIP) return

		val blockCountMultipler = 1.5

		val damagers = event.starship.damagers
			.filterKeys { it is PlayerDamager }
			.mapKeys { it.key as PlayerDamager }
			.filterKeys { damager ->
			// require they be online to get xp
			// if they have this perm, e.g. someone in dutymode or on creative, they don't get xp
			!damager.player.hasPermission("starships.noxp")
		}.mapValues { it.value.points.get() }

		val sum = damagers.values.sum().toDouble()
		val totalMoney = event.starship.initialBlockCount.toDouble() * blockCountMultipler

		val victimNation = PlayerCache[victim].nationOid

		for ((damager : PlayerDamager, points) in damagers) {
			val killer = damager.player

			val damagerNation = PlayerCache[killer].nationOid
			if (damagerNation != null && victimNation != null) {
				if (RelationCache[victimNation, damagerNation] >= NationRelation.Level.ALLY) continue
			}

			val percent = points / sum
			val money = totalMoney * percent

			val killerBounty = PlayerCache[killer].bounty

			val reason = template(
				text("For sinking {0}'s {1}, {2}'s bounty was increased by {3}! It is now {4}.", HE_MEDIUM_GRAY),
				paramColor = PIRATE_SATURATED_RED,
				useQuotesAroundObjects = true,
				victim.displayName(),
				event.starship.type.displayNameComponent,
				killer.displayName(),
				money.toCreditComponent(),
				(killerBounty + money).toCreditComponent()
			)

			increaseBounty(killer, money, reason)
		}
	}

	fun collectBounty(hunter: Player, target: SLPlayerId, targetName: String) {
		val cached = PlayerCache[target]
		val bounty = cached.bounty

		if (bounty <= 0.0) return

		val hunterNation = PlayerCache.getIfOnline(hunter)?.nationOid
		val targetNation = PlayerCache.getIfOnline(target.uuid)?.nationOid

		val relation = if (hunterNation != null && targetNation != null) RelationCache[hunterNation, targetNation] else NationRelation.Level.NONE
		if (relation >= NationRelation.Level.ALLY) return

		SLPlayer.updateById(target, setValue(SLPlayer::bounty, 0.0))

		val collectionMessage = template(
			text("{0} has collected a bounty of {1} for killing {2}.", HE_MEDIUM_GRAY),
			paramColor = PIRATE_SATURATED_RED,
			hunter.name,
			bounty.toCreditComponent(),
			targetName
		)

		Notify.chatAndGlobal(collectionMessage)
		VAULT_ECO.depositPlayer(hunter, bounty)
		// Maybe play a sound?

		hunter.sendMessage(template(text("{0} has been added to your account.", HE_MEDIUM_GRAY), bounty.toCreditComponent()))

		return
	}

	private fun increaseBounty(target: Player, amount: Double, reason: Component) {
		if (!ConfigurationFiles.featureFlags().bounties) return

		SLPlayer.updateById(target.slPlayerId, inc(SLPlayer::bounty, amount))

		Tasks.sync {
			Notify.chatAndGlobal(reason)
		}
	}
}
