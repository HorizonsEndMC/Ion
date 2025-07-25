package net.horizonsend.ion.server.features.progression.bounties

import net.horizonsend.ion.common.database.schema.misc.ClaimedBounty
import net.horizonsend.ion.common.database.schema.misc.SLPlayer
import net.horizonsend.ion.common.database.schema.misc.SLPlayerId
import net.horizonsend.ion.common.database.slPlayerId
import net.horizonsend.ion.common.extensions.success
import net.horizonsend.ion.common.extensions.userError
import net.horizonsend.ion.common.utils.miscellaneous.toCreditsString
import net.horizonsend.ion.common.utils.text.template
import net.horizonsend.ion.common.utils.text.toCreditComponent
import net.horizonsend.ion.server.IonServerComponent
import net.horizonsend.ion.server.configuration.ConfigurationFiles
import net.horizonsend.ion.server.features.cache.BountyCache
import net.horizonsend.ion.server.features.cache.PlayerCache
import net.horizonsend.ion.server.features.starship.TypeCategory
import net.horizonsend.ion.server.features.progression.Levels
import net.horizonsend.ion.server.features.starship.control.controllers.player.PlayerController
import net.horizonsend.ion.server.features.starship.damager.PlayerDamager
import net.horizonsend.ion.server.features.starship.event.StarshipExplodeEvent
import net.horizonsend.ion.server.miscellaneous.utils.Notify
import net.horizonsend.ion.server.miscellaneous.utils.Tasks
import net.horizonsend.ion.server.miscellaneous.utils.VAULT_ECO
import net.horizonsend.ion.server.miscellaneous.utils.slPlayerId
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.NamedTextColor.DARK_RED
import net.kyori.adventure.text.format.NamedTextColor.RED
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.entity.PlayerDeathEvent
import org.bukkit.inventory.ItemStack
import org.litote.kmongo.and
import org.litote.kmongo.eq
import org.litote.kmongo.inc
import org.litote.kmongo.setValue
import java.util.Date
import java.util.UUID
import java.util.concurrent.TimeUnit

object Bounties : IonServerComponent() {
	private const val MIN_BOUNTY_LEVEL = 10

	/** Time between claiming bounties on a player **/
	val coolDown get() = Date(System.currentTimeMillis() - TimeUnit.DAYS.toMillis(2))

	/** Time the next bounty would be claimed **/
	val nextClaim get() = Date(System.currentTimeMillis() + TimeUnit.DAYS.toMillis(2))

	/** Time when a claimed bounty would expire **/
	val lastActive get() = Date(System.currentTimeMillis() - TimeUnit.DAYS.toMillis(1))

	data class BountyPlayer(
		val name: String,
		val uniqueId: UUID,
		val skull: ItemStack,
		val bounty: Double
	)

	fun claimBounty(hunter: Player, target: BountyPlayer) {
		claimBounty(hunter, target.uniqueId.slPlayerId, target.name, target.bounty)
	}

	fun claimBounty(hunter: Player, targetId: SLPlayerId, targetName: String, targetBounty: Double) = Tasks.async {
		if (Levels[hunter] < MIN_BOUNTY_LEVEL) return@async hunter.userError("You must be level $MIN_BOUNTY_LEVEL before you can claim a bounty!")
		if (hasActive(hunter.slPlayerId, targetId)) return@async hunter.userError("You already have an active bounty on $targetName!")
		if (!canClaim(hunter.slPlayerId, targetId)) return@async hunter.userError("You already claimed a bounty on $targetName in the last two days!")

		ClaimedBounty.claim(hunter.slPlayerId, targetId)

		hunter.success("You have claimed a bounty on $targetName for $targetBounty. \nYou have 1 day to collect this bounty before it expires.")
	}

	/** Checks if the hunter has claimed a bounty on the target in the previous two days **/
	fun canClaim(hunter: SLPlayerId, target: SLPlayerId): Boolean {
		val mostRecent = BountyCache[hunter, target] ?: return true

		return mostRecent.claimTime < coolDown
	}

	/** Checks if the hunter has an active bounty on the target **/
	fun hasActive(hunter: SLPlayerId, target: SLPlayerId): Boolean {
		val mostRecent = BountyCache[hunter, target] ?: run {
			log.info("${PlayerCache[hunter]} did not have a cached bounty")
			return false
		}

		if (mostRecent.claimTime < lastActive) {
			log.info("${PlayerCache[hunter]}'s last bounty was expired")
			return false
		}

		log.info("Bounty: $mostRecent")

		return !mostRecent.completed
	}

	@Suppress("unused")
	@EventHandler(priority = EventPriority.HIGH)
	fun onPlayerKill(event: PlayerDeathEvent) = Tasks.async {
		if (isNotSurvival()) return@async
		val victim = event.player
		val killer = event.entity.killer ?: return@async

		// Check names because of combat NPCs
		// (was this added when you could kill your own combat npc?)
		if (killer.name == victim.name) return@async

		// gets the victim's slPlayerId, even if they are offline (in the case of combat NPCs)
		val victimSlPlayerId = SLPlayer[victim.name]?._id ?: return@async

		if (hasActive(killer.slPlayerId, victimSlPlayerId)) {
			collectBounty(killer, victim)
		} else {
			val amount = 2500.0

			val killerBounty = PlayerCache[killer].bounty

			val increaseReason = text()
				.append(text("For killing ", RED))
				.append(text(victim.name, DARK_RED))
				.append(text(", ", RED))
				.append(text(killer.name, DARK_RED))
				.append(text("'s bounty was increased by ", RED))
				.append(text((amount).toCreditsString(), NamedTextColor.GOLD))
				.append(text("! It is now ", RED))
				.append(text((killerBounty + amount).toCreditsString(), NamedTextColor.GOLD))
				.build()

			increaseBounty(killer, amount, increaseReason)
		}
	}

	@Suppress("unused")
	@EventHandler
	fun onShipSink(event: StarshipExplodeEvent) {
		if (isNotSurvival()) return
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

		for ((damager : PlayerDamager, points) in damagers) {
			val killer = damager.player
			val percent = points / sum
			val money = totalMoney * percent

			val killerBounty = PlayerCache[killer].bounty

			val reason = template(
				"For sinking {0}'s {1}, {2}'s bounty was increased by {3}! It is now {4}.",
				color = RED,
				paramColor = DARK_RED,
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

	fun collectBounty(hunter: Player, target: Player) {
		val cached = PlayerCache[target]
		val bounty = cached.bounty

		val query = and(ClaimedBounty::hunter eq hunter.slPlayerId, ClaimedBounty::target eq target.slPlayerId, ClaimedBounty::completed eq false)
		val claimedBounty = ClaimedBounty.findOne(query) ?: return hunter.userError("Bounty not found! Please contact a mod.")

		SLPlayer.updateById(target.slPlayerId, setValue(SLPlayer::bounty, 0.0))
		ClaimedBounty.updateById(claimedBounty._id, setValue(ClaimedBounty::completed, true))

		val message = text()
			.append(text(hunter.name, DARK_RED))
			.append(text(" has collected a bounty of ", RED))
			.append(text((bounty).toCreditsString(), NamedTextColor.GOLD))
			.append(text(" for killing ", RED))
			.append(text(target.name, DARK_RED))
			.append(text(".", RED))
			.build()

		Notify.chatAndGlobal(message)
		VAULT_ECO.depositPlayer(hunter, bounty)
		// Maybe play a sound?
		hunter.sendMessage(
			text()
				.append(text((bounty).toCreditsString(), NamedTextColor.GOLD))
				.append(text(" has been added to your account.", RED))
				.build()
		)
	}

	private fun increaseBounty(target: Player, amount: Double, reason: Component? = null) {
		if (isNotSurvival()) return

		val cached = PlayerCache[target]
		SLPlayer.updateById(target.slPlayerId, inc(SLPlayer::bounty, amount))

		val defaultReason = text()
			.append(text(target.name, DARK_RED))
			.append(text("'s bounty was increased by $amount! It is now ", RED))
			.append(text((cached.bounty + amount).toCreditsString(), NamedTextColor.GOLD))
			.build()

		Tasks.sync {
			Notify.chatAndGlobal(reason ?: defaultReason)
		}
	}

	fun isNotSurvival(): Boolean = !ConfigurationFiles.serverConfiguration().serverName.equals("Survival", ignoreCase = true)
}
