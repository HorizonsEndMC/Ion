package net.horizonsend.ion.server.legacy

import co.aikar.commands.BaseCommand
import co.aikar.commands.annotation.CommandAlias
import co.aikar.commands.annotation.CommandPermission
import co.aikar.commands.annotation.Default
import co.aikar.commands.annotation.Subcommand
import net.horizonsend.ion.common.database.cache.nations.NationCache
import net.horizonsend.ion.common.database.cache.nations.SettlementCache
import net.horizonsend.ion.common.extensions.alert
import net.horizonsend.ion.common.extensions.alertAction
import net.horizonsend.ion.common.extensions.success
import net.horizonsend.ion.common.extensions.userError
import net.horizonsend.ion.server.features.cache.PlayerCache
import net.horizonsend.ion.server.miscellaneous.slPlayerId
import net.luckperms.api.LuckPermsProvider
import net.luckperms.api.node.types.PermissionNode
import net.luckperms.api.node.types.SuffixNode
import net.starlegacy.SETTINGS
import net.starlegacy.feature.progression.PlayerXPLevelCache
import net.starlegacy.util.Tasks
import org.bukkit.Bukkit
import org.bukkit.Statistic.PLAY_ONE_MINUTE
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityDamageByEntityEvent
import kotlin.math.pow

@CommandAlias("removeprotection")
object NewPlayerProtection : BaseCommand(), Listener {
	private val lpUserManager = LuckPermsProvider.get().userManager

	private val protectionIndicator = SuffixNode.builder(" &6★ &r", 0).build()
	private val removeProtectionPermission = PermissionNode.builder("ion.core.protection.removed").build()

	fun onEnable() {
		Tasks.syncRepeat(5 * 20 * 60, 5 * 20 * 60) {
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
		val lpUser = lpUserManager.getUser(target)

		if (lpUser == null) {
			sender.userError(
				"Unable to remove new player protection from $target, the player does not exist."
			)
			return
		}

		lpUser.data().run {
			add(removeProtectionPermission)
			remove(protectionIndicator)
		}

		lpUserManager.saveUser(lpUser)

		sender.success("Removed new player protection from $target.")
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
		if (player.nationOid?.let { SettlementCache[NationCache[it].capital].leader == slPlayerId } == true) return false // If owns
		return getStatistic(PLAY_ONE_MINUTE) / 72000.0 <= 48.0.pow((100.0 - playerLevel.level) * 0.01) // If playtime is less then 48^((100-x)*0.001) hours
	}

	@EventHandler
	fun onPlayerHurtNoob(event: EntityDamageByEntityEvent) {
		if (event.entity !is Player || event.damager !is Player) return

		if ((event.entity as Player).hasProtection()) event.damager.alertAction(
				"The player you are attacking has new player protection!\n" +
					"Attacking them for any reason other than self defense is against the rules"
			)
	}
}
