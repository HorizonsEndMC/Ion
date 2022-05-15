package net.horizonsend.ion.core

import co.aikar.commands.BaseCommand
import co.aikar.commands.annotation.CommandAlias
import co.aikar.commands.annotation.CommandPermission
import co.aikar.commands.annotation.Default
import kotlin.math.pow
import net.luckperms.api.LuckPermsProvider
import net.luckperms.api.node.types.PermissionNode
import net.luckperms.api.node.types.SuffixNode
import net.starlegacy.SETTINGS
import net.starlegacy.database.schema.misc.SLPlayer
import net.starlegacy.database.schema.nations.Nation
import net.starlegacy.database.schema.nations.Settlement
import net.starlegacy.database.slPlayerId
import org.bukkit.Statistic.PLAY_ONE_MINUTE
import org.bukkit.entity.Player

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
	fun onRemoveProtection(sender: Player, target: String) {
		val lpUser = lpUserManager.getUser(target)

		if (lpUser == null) {
			sender.sendFeedbackMessage(FeedbackType.USER_ERROR, "Unable to remove new player protection from {0}, the player does not exist.", target)
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
			if (hasProtection())
				add(protectionIndicator)

			else
				remove(protectionIndicator)
		}

		lpUserManager.saveUser(lpUser)
	}

	fun Player.hasProtection(): Boolean {
		if (hasPermission("ion.core.protection.removed")) return false // If protection has been removed by staff.
		if (SLPlayer[this].nation?.let { Settlement.findById(Nation.findById(it)!!.capital)!!.leader == slPlayerId } == true) return false // If owns nation
		return getStatistic(PLAY_ONE_MINUTE) / 72000.0 <= 48.0.pow((100.0 - SLPlayer[this].level) * 0.01) // If playtime is less then 48^((100-x)*0.001) hours
	}
}