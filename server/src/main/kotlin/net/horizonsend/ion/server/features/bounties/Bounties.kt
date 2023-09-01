package net.horizonsend.ion.server.features.bounties

import net.horizonsend.ion.common.database.schema.misc.ClaimedBounty
import net.horizonsend.ion.common.database.schema.misc.SLPlayer
import net.horizonsend.ion.common.database.schema.misc.SLPlayerId
import net.horizonsend.ion.common.database.slPlayerId
import net.horizonsend.ion.common.extensions.success
import net.horizonsend.ion.common.extensions.userError
import net.horizonsend.ion.server.IonServer
import net.horizonsend.ion.server.IonServerComponent
import net.horizonsend.ion.server.features.cache.PlayerCache
import net.horizonsend.ion.server.features.starship.controllers.PlayerController
import net.horizonsend.ion.server.features.starship.event.StarshipExplodeEvent
import net.horizonsend.ion.server.miscellaneous.utils.Notify
import net.horizonsend.ion.server.miscellaneous.utils.Tasks
import net.horizonsend.ion.server.miscellaneous.utils.VAULT_ECO
import net.horizonsend.ion.server.miscellaneous.utils.slPlayerId
import net.horizonsend.ion.server.miscellaneous.utils.toCreditsString
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.entity.PlayerDeathEvent
import org.bukkit.inventory.ItemStack
import org.litote.kmongo.and
import org.litote.kmongo.eq
import org.litote.kmongo.gte
import org.litote.kmongo.inc
import org.litote.kmongo.setValue
import java.util.Date
import java.util.UUID
import java.util.concurrent.CompletableFuture
import java.util.concurrent.TimeUnit

object Bounties : IonServerComponent() {
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
		if (hasActive(hunter.slPlayerId, targetId).get()) return@async hunter.userError("You already have an active bounty on $targetName!")
		if (!canClaim(hunter.slPlayerId, targetId)) return@async hunter.userError("You already claimed a bounty on $targetName in the last two days!")

		ClaimedBounty.claim(hunter.slPlayerId, targetId)

		hunter.success("You have claimed a bounty on $targetName for $targetBounty. \nYou have 1 day to collect this bounty before it expires.")
	}

	/** Checks if the hunter has claimed a bounty on the target in the previous two days **/
	fun canClaim(hunter: SLPlayerId, target: SLPlayerId): Boolean {
		val query = and(
			ClaimedBounty::target eq target,
			ClaimedBounty::hunter eq hunter,
			ClaimedBounty::claimTime gte coolDown
		)

		return ClaimedBounty.none(query)
	}

	/** Checks if the hunter has an active bounty on the target **/
	private fun hasActive(hunter: SLPlayerId, target: SLPlayerId): CompletableFuture<Boolean> {
		val future = CompletableFuture<Boolean>()

		Tasks.async {
			val query = and(
				ClaimedBounty::target eq target,
				ClaimedBounty::hunter eq hunter,
				ClaimedBounty::claimTime gte lastActive,
				ClaimedBounty::completed eq false
			)

			future.complete(ClaimedBounty.any(query))
		}

		return future
	}

	@EventHandler(priority = EventPriority.HIGH)
	fun onPlayerKill(event: PlayerDeathEvent) = Tasks.async {
		if (isNotSurvival()) return@async
		val victim = event.player
		val killer = event.entity.killer ?: return@async

		if (hasActive(killer.slPlayerId, victim.slPlayerId).get()) {
			collectBounty(killer, victim)
		} else {
			val amount = 2500.0

			val killerBounty = PlayerCache[killer].bounty

			val increaseReason = text()
				.append(text("For killing ", NamedTextColor.RED))
				.append(text(victim.name, NamedTextColor.DARK_RED))
				.append(text(", ", NamedTextColor.RED))
				.append(text(killer.name, NamedTextColor.DARK_RED))
				.append(text("'s bounty was increased by ", NamedTextColor.RED))
				.append(text((amount).toCreditsString(), NamedTextColor.GOLD))
				.append(text("! It is now ", NamedTextColor.RED))
				.append(text((killerBounty + amount).toCreditsString(), NamedTextColor.GOLD))
				.build()

			increaseBounty(killer, amount, increaseReason)
		}
	}

	@EventHandler
	fun onShipSink(event: StarshipExplodeEvent) {
		if (isNotSurvival()) return
		val victim = (event.starship.controller as? PlayerController)?.player ?: return
		if (event.starship.type.isWarship) return

		val blockCountMultipler = 1.5

		val damagers = event.starship.damagers.filterKeys { damager ->
			// require they be online to get xp
			// if they have this perm, e.g. someone in dutymode or on creative, they don't get xp
			Bukkit.getPlayer(damager.id)?.hasPermission("starships.noxp") == false
		}.mapValues { it.value.get() }

		val sum = damagers.values.sum().toDouble()
		val totalMoney = event.starship.initialBlockCount.toDouble() * blockCountMultipler

		for ((damager, points) in damagers) {
			val killer = Bukkit.getPlayer(damager.id) ?: continue
			val percent = points / sum
			val money = totalMoney * percent

			val killerBounty = PlayerCache[killer].bounty

			val reason = text()
				.append(text("For killing ", NamedTextColor.RED))
				.append(text(victim.name, NamedTextColor.DARK_RED))
				.append(text(", ", NamedTextColor.RED))
				.append(text(killer.name, NamedTextColor.DARK_RED))
				.append(text("'s bounty was increased by $money! It is now ", NamedTextColor.RED))
				.append(text((killerBounty + money).toCreditsString(), NamedTextColor.GOLD))
				.build()

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
			.append(text(hunter.name, NamedTextColor.DARK_RED))
			.append(text(" has collected a boutny of ", NamedTextColor.RED))
			.append(text((bounty).toCreditsString(), NamedTextColor.GOLD))
			.append(text(" for killing ", NamedTextColor.RED))
			.append(text(target.name, NamedTextColor.DARK_RED))
			.append(text(".", NamedTextColor.RED))
			.build()

		Notify.online(message)
		VAULT_ECO.depositPlayer(hunter, bounty)
		// Maybe play a sound?
		hunter.sendMessage(
			text()
				.append(text((bounty).toCreditsString(), NamedTextColor.GOLD))
				.append(text(" has been added to your account.", NamedTextColor.RED))
				.build()
		)
	}

	private fun increaseBounty(target: Player, amount: Double, reason: Component? = null) {
		if (isNotSurvival()) return

		val cached = PlayerCache[target]
		SLPlayer.updateById(target.slPlayerId, inc(SLPlayer::bounty, amount))

		val defaultReason = text()
			.append(text(target.name, NamedTextColor.DARK_RED))
			.append(text("'s bounty was increased by $amount! It is now ", NamedTextColor.RED))
			.append(text((cached.bounty + amount).toCreditsString(), NamedTextColor.GOLD))
			.build()

		Tasks.sync {
			Notify.online(reason ?: defaultReason)
		}
	}

	fun isNotSurvival(): Boolean = !IonServer.configuration.serverName.equals("Survival", ignoreCase = true)
}
