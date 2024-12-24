package net.horizonsend.ion.server.features.player

import co.aikar.commands.PaperCommandManager
import co.aikar.commands.annotation.CommandAlias
import co.aikar.commands.annotation.CommandPermission
import co.aikar.commands.annotation.Default
import co.aikar.commands.annotation.Subcommand
import net.horizonsend.ion.common.database.cache.nations.NationCache
import net.horizonsend.ion.common.database.cache.nations.SettlementCache
import net.horizonsend.ion.common.database.schema.misc.SLPlayer
import net.horizonsend.ion.common.database.schema.misc.SLPlayer
import net.horizonsend.ion.common.database.uuid
import net.horizonsend.ion.common.extensions.alertAction
import net.horizonsend.ion.common.extensions.success
import net.horizonsend.ion.common.extensions.userError
import net.horizonsend.ion.server.configuration.ConfigurationFiles
import net.horizonsend.ion.server.features.cache.PlayerCache
import net.horizonsend.ion.server.features.progression.PlayerXPLevelCache
import net.horizonsend.ion.server.features.world.IonWorld.Companion.hasFlag
import net.horizonsend.ion.server.features.world.WorldFlag
import net.horizonsend.ion.server.miscellaneous.utils.Tasks
import net.horizonsend.ion.server.miscellaneous.utils.slPlayerId
import org.bukkit.Bukkit
import org.bukkit.Statistic.PLAY_ONE_MINUTE
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.litote.kmongo.setValue
import java.time.Duration
import kotlin.math.pow

@CommandAlias("removeprotection")
object NewPlayerProtection : net.horizonsend.ion.server.command.SLCommand(), Listener {
	private val UPDATE_RATE_MINS = Duration.ofMinutes(5L)
	private val PROTECTION_DURATION_DAYS = Duration.ofDays(2L)

	/* kwazedilla 2024/12/24: rewrite to not depend on luckperms
	private val lpUserManager = luckPerms.userManager

	private val oldProtectionIndicator = SuffixNode.builder("&6★&r", 0).build()
	private val oldAlternateProtectionIndicator = SuffixNode.builder(" &6★ &r", 0).build()
	private val newerDldAlternateProtectionIndicator = SuffixNode.builder("<gold>★<reset>", 0).build()

	private val protectionIndicator = SuffixNode.builder("<gold> ★<reset>", 0).build()
	private val removeProtectionPermission = PermissionNode.builder("ion.core.protection.removed").build()
	 */

	override fun onEnable(manager: PaperCommandManager) {
		Tasks.syncRepeat(UPDATE_RATE_MINS.toSeconds() * 20, UPDATE_RATE_MINS.toSeconds() * 20) {
			for (player in Bukkit.getOnlinePlayers())
				player.updateProtection()
		}
	}

	@Default
	@Suppress("unused") // Command
	fun onRemoveProtection(sender: Player) {
		onRemoveProtection(sender, sender.name)
	}

	@CommandPermission("ion.core.protection.removeothers")
	@Subcommand("other")
	fun onRemoveProtection(sender: Player, target: String) {
		val id = SLPlayer[target]?._id ?: fail { "Unable to remove new player protection from $target, the player does not exist." }
		lpUserManager.modifyUser(id.uuid) {
			it.data().run {
				add(removeProtectionPermission)
				remove(protectionIndicator)
			}
		}.thenAccept { t ->
		SLPlayer.updateById(targetSlPlayer._id, setValue(SLPlayer::hasNewPlayerProtection, false))
		SLPlayer.updateById(targetSlPlayer._id, setValue(SLPlayer::newPlayerProtectionResetOn, 0L))

		sender.success("Removed new player protection from $target.")}
	}

	@CommandPermission("ion.core.protection.giveothers")
	@CommandAlias("giveprotection")
	fun onGiveProtection(sender: Player, target: String) {
		/*
		val lpUser = lpUserManager.getUser(target)

		if (lpUser == null) {
			sender.userError(
				"Unable to give new player protection to $target, the player does not exist."
			)
			return
		}

		lpUser.data().run {
			remove(removeProtectionPermission)
			add(protectionIndicator)
		}

		lpUserManager.saveUser(lpUser)
		 */

		val targetSlPlayer = SLPlayer[resolveOfflinePlayer(target)] // errors by itself if not found
		if (targetSlPlayer == null) {
			sender.userError("Player not found")
			return
		}
		SLPlayer.updateById(targetSlPlayer._id, setValue(SLPlayer::hasNewPlayerProtection, true))
		SLPlayer.updateById(targetSlPlayer._id, setValue(SLPlayer::newPlayerProtectionResetOn, System.currentTimeMillis()))

		sender.success("Gave new player protection to $target.")
	}

	fun Player.updateProtection() {
		if (!ConfigurationFiles.legacySettings().master) return // server must be the master server for protection to work (don't want creative affecting protection status)

		/*
		val lpUser = lpUserManager.getUser(uniqueId)!!

		lpUser.data().run {
			if (contains(oldProtectionIndicator, NodeEqualityPredicate.IGNORE_EXPIRY_TIME) == Tristate.TRUE) {
				remove(oldProtectionIndicator)
			}

			if (contains(oldAlternateProtectionIndicator, NodeEqualityPredicate.IGNORE_EXPIRY_TIME) == Tristate.TRUE) {
				remove(oldAlternateProtectionIndicator)
			}

			if (contains(newerDldAlternateProtectionIndicator, NodeEqualityPredicate.IGNORE_EXPIRY_TIME) == Tristate.TRUE) {
				remove(newerDldAlternateProtectionIndicator)
			}

			if (hasProtection()) {
				add(protectionIndicator)
			} else {
				remove(protectionIndicator)
			}
		}

		lpUserManager.saveUser(lpUser)
		 */

		if (this.hasProtection()) {
			SLPlayer.updateById(this.slPlayerId, setValue(SLPlayer::hasNewPlayerProtection, true))
		} else {
			SLPlayer.updateById(this.slPlayerId, setValue(SLPlayer::hasNewPlayerProtection, false))
		}
	}

	fun Player.hasProtection(): Boolean {
		if (hasMetadata("NPC")) return false

		val player = PlayerCache[this]
		val playerLevel = PlayerXPLevelCache[this]

		//if (hasPermission("ion.core.protection.removed")) return false // If protection has been removed by staff.
		if (!player.hasNewPlayerProtection) return false
		if (player.nationOid?.let { SettlementCache[NationCache[it].capital].leader == slPlayerId } == true) return false // If player owns a nation
		return if (player.newPlayerProtectionResetOn == 0L)
			// convert from ticks to hours played
			getStatistic(PLAY_ONE_MINUTE) / (Duration.ofHours(1L).toSeconds() * 20).toDouble() <=
					PROTECTION_DURATION_DAYS.toHours().toDouble().pow((100.0 - playerLevel.level) * 0.01) // If playtime is less than 48^((100-x)*0.01) hours
		else {
			val remainingTimeAsDouble = PROTECTION_DURATION_DAYS.toHours().toDouble().pow((100.0 - playerLevel.level) * 0.01)
			val remainingHours = remainingTimeAsDouble.toLong()
			val remainingFractional = (Duration.ofHours(1L).toMillis() * (remainingTimeAsDouble - remainingHours)).toLong()

			System.currentTimeMillis() <= player.newPlayerProtectionResetOn + Duration.ofHours(remainingHours).toMillis() + remainingFractional
		}
	}

//	fun UUID.hasProtection(): CompletableFuture<Boolean?> {
//		val future = CompletableFuture<Boolean?>()
//
//		Tasks.async {
//			val player = SLPlayer[this]
//
//			if (player == null) {
//				future.complete(null)
//				return@async
//			}
//
//			val playerLevel = player.level
//
//			val protectionRemoved = luckPerms.userManager.loadUser(this).get().distinctNodes.filterIsInstance<PermissionNode>().any {
//				it.permission == "ion.core.protection.removed"
//			}
//
//			if (protectionRemoved) {
//				future.complete(false)
//				return@async
//			}
//
//			if (player.nation?.let { SettlementCache[NationCache[it].capital].leader == this.slPlayerId } == true) {
//				future.complete(false)
//				return@async
//			}
//
//			val offlinePlayer = findOfflinePlayer(this)
//
//			if (offlinePlayer == null) {
//				future.complete(null)
//				return@async
//			}
//
//			val playTime = offlinePlayer.getStatistic(PLAY_ONE_MINUTE) / 72000.0 <= 48.0.pow((100.0 - playerLevel) * 0.01) // If playtime is less then 48^((100-x)*0.001) hours
//			future.complete(playTime)
//		}
//
//		return future
//	}

	@EventHandler
	fun onPlayerHurtNoob(event: EntityDamageByEntityEvent) {
		if (event.entity !is Player || event.damager !is Player) return

		if ((event.entity as Player).hasProtection() && !event.entity.world.hasFlag(WorldFlag.ARENA)) event.damager.alertAction(
			"The player you are attacking has new player protection!\n" +
				"Attacking them for any reason other than self defense is against the rules"
		)
	}
}
