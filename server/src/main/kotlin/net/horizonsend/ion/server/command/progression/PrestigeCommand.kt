package net.horizonsend.ion.server.command.progression

import co.aikar.commands.annotation.CommandAlias
import co.aikar.commands.annotation.Default
import co.aikar.commands.annotation.Optional
import co.aikar.commands.annotation.Subcommand
import net.horizonsend.ion.common.extensions.information
import net.horizonsend.ion.common.extensions.success
import net.horizonsend.ion.common.extensions.userError
import net.horizonsend.ion.common.utils.miscellaneous.toCreditsString
import net.horizonsend.ion.common.utils.text.template
import net.horizonsend.ion.server.command.SLCommand
import net.horizonsend.ion.server.features.progression.MAX_LEVEL
import net.horizonsend.ion.server.features.progression.PlayerXPLevelCache
import net.horizonsend.ion.server.features.starship.active.ActiveStarships
import net.horizonsend.ion.server.miscellaneous.utils.Notify
import net.horizonsend.ion.server.miscellaneous.utils.VAULT_ECO
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.format.NamedTextColor.GOLD
import net.kyori.adventure.text.format.NamedTextColor.GREEN
import net.kyori.adventure.text.format.TextDecoration.BOLD
import org.bukkit.Particle
import org.bukkit.Sound
import org.bukkit.entity.Player

@CommandAlias("prestige")
object PrestigeCommand : SLCommand() {
	private const val COST = 250000.0

	@Default
	fun onPrestige(sender: Player) {
		sender.information("Your prestige is ${PlayerXPLevelCache[sender].prestige}. At level $MAX_LEVEL, use /prestige advance to prestige.")
	}

	@Subcommand("advance")
	fun onConfirm(sender: Player, @Optional cost: Double?) {
		requireEconomyEnabled()
		requireNotInCombat(sender)
		failIf(ActiveStarships.findByPilot(sender) != null) { "You can't prestige while piloting a starship!" }
		requireMoney(sender, COST, "prestige")

		val playerData = PlayerXPLevelCache[sender]
		val level = playerData.level
		val prestige = playerData.prestige
		failIf(level != MAX_LEVEL) { "You must be level $MAX_LEVEL to prestige." }
		failIf(prestige == Int.MAX_VALUE) { "Your prestige cannot be increased further. Touch grass." }

		if (cost != COST) {
			sender.userError(
				"Prestiging costs ${COST.toCreditsString()} and resets your level to 1 and overcapped XP to 0. This cannot be undone.\n" +
					"To confirm, do <u><click:run_command:/prestige advance $COST>/prestige advance $COST</click>"
			)
			return
		}

		val withdrawal = VAULT_ECO.withdrawPlayer(sender, COST)
		failIf(!withdrawal.transactionSuccess()) { "Could not withdraw ${COST.toCreditsString()}. Your prestige was not changed. If you believe this is an error, please contact staff." }

		PlayerXPLevelCache.async {
			setPrestige(sender.uniqueId, prestige + 1)
			setLevel(sender.uniqueId, 1)
			setSLXP(sender.uniqueId, 0)
		}
		val newPrestige = prestige + 1

		sender.success("You reached prestige $newPrestige! Your level is now 1. ${COST.toCreditsString()} credits have been removed from your balance.")

		val announcement = template(
			text("{0} reached prestige {1}{2}", GREEN),
			text(sender.name, GOLD),
			text(newPrestige, GOLD),
			text("!".repeat(newPrestige.coerceIn(0, 40)), GREEN)
		).decorate(BOLD)

		Notify.chatAndGlobal(Component.newline().append(announcement).append(Component.newline()))

		val origin = sender.location
		val world = sender.world
		for ((offsetX, offsetZ) in listOf(-1.5 to -1.0, 1.5 to -1.0, 0.0 to 1.5)) {
			val location = origin.clone().add(offsetX, 2.0, offsetZ)
			world.spawnParticle(Particle.FIREWORK, location, 30, 0.35, 0.35, 0.35, 0.1)
		}
		world.playSound(origin, Sound.ENTITY_FIREWORK_ROCKET_TWINKLE, 1.0f, 1.0f)
	}
}