package net.horizonsend.ion.server.legacy

import co.aikar.commands.BaseCommand
import co.aikar.commands.annotation.CommandAlias
import co.aikar.commands.annotation.CommandPermission
import co.aikar.commands.annotation.Default
import co.aikar.commands.annotation.Subcommand
import net.horizonsend.ion.server.extensions.FeedbackType
import net.horizonsend.ion.server.extensions.sendFeedbackMessage
import net.luckperms.api.LuckPermsProvider
import net.luckperms.api.node.types.PermissionNode
import net.luckperms.api.node.types.SuffixNode
import net.starlegacy.SETTINGS
import net.starlegacy.cache.nations.NationCache
import net.starlegacy.cache.nations.PlayerCache
import net.starlegacy.cache.nations.SettlementCache
import net.starlegacy.database.slPlayerId
import net.starlegacy.feature.progression.PlayerXPLevelCache
import org.bukkit.Statistic.PLAY_ONE_MINUTE
import org.bukkit.entity.Player
import kotlin.math.pow

@CommandAlias("removeprotection")
object NewPlayerProtection : BaseCommand() {
	private val lpUserManager = LuckPermsProvider.get().userManager

	private val protectionIndicator = SuffixNode.builder(" &6★ &r", 0).build()
	private val removeProtectionPermission = PermissionNode.builder("ion.core.protection.removed").build()

	@Default
	@Suppress("unused") // Command
	fun onRemoveProtection(sender: Player) {
		onRemoveProtection(sender, sender.name)
	}

	@CommandPermission("ion.core.protection.removeothers")
	@Subcommand("other")
	fun onRemoveProtection(sender: Player, target: String) {
		val lpUser = lpUserManager.getUser(target)

		if (lpUser == null) {
			sender.sendFeedbackMessage(
				FeedbackType.USER_ERROR,
				"Unable to remove new player protection from {0}, the player does not exist.",
				target
			)
			return
		}

		lpUser.data().run {
			add(removeProtectionPermission)
			remove(protectionIndicator)
		}

		lpUserManager.saveUser(lpUser)

		sender.sendFeedbackMessage(FeedbackType.SUCCESS, "Removed new player protection from {0}.", target)
	}

	fun Player.updateProtection() {
		if (!SETTINGS.master) return

		val lpUser = lpUserManager.getUser(uniqueId)!!

		lpUser.data().run {
			if (hasProtection()) {
				add(protectionIndicator)
			} else {
				remove(protectionIndicator)
			}
		}

		lpUserManager.saveUser(lpUser)
	}

	fun Player.hasProtection(): Boolean {
		val player = PlayerCache[this]
		val playerLevel = PlayerXPLevelCache[this]

		if (hasPermission("ion.core.protection.removed")) return false // If protection has been removed by staff.
		if (player.nation?.let { SettlementCache[NationCache[it].capital].leader == slPlayerId } == true) return false // If owns nation
		return getStatistic(PLAY_ONE_MINUTE) / 72000.0 <= 48.0.pow((100.0 - playerLevel.level) * 0.01) // If playtime is less then 48^((100-x)*0.001) hours
	}
}
