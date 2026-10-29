package net.horizonsend.ion.server.command.progression

import co.aikar.commands.annotation.CommandAlias
import co.aikar.commands.annotation.CommandCompletion
import co.aikar.commands.annotation.Default
import co.aikar.commands.annotation.Optional
import co.aikar.commands.annotation.Subcommand
import net.horizonsend.ion.common.extensions.information
import net.horizonsend.ion.common.extensions.userError
import net.horizonsend.ion.common.utils.miscellaneous.toCreditsString
import net.horizonsend.ion.common.utils.text.template
import net.horizonsend.ion.server.command.SLCommand
import net.horizonsend.ion.server.features.progression.MAX_LEVEL
import net.horizonsend.ion.server.features.progression.PrestigeCongratulations
import net.horizonsend.ion.server.features.progression.PlayerXPLevelCache
import net.horizonsend.ion.server.features.starship.active.ActiveStarships
import net.horizonsend.ion.server.miscellaneous.utils.Notify
import net.horizonsend.ion.server.miscellaneous.utils.Tasks
import net.horizonsend.ion.server.miscellaneous.utils.VAULT_ECO
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.format.NamedTextColor.GOLD
import net.kyori.adventure.text.format.NamedTextColor.DARK_PURPLE
import net.kyori.adventure.text.format.NamedTextColor.GRAY
import net.kyori.adventure.text.format.NamedTextColor.GREEN
import net.kyori.adventure.text.format.TextDecoration.BOLD
import net.kyori.adventure.text.format.TextDecoration.UNDERLINED
import net.kyori.adventure.text.event.ClickEvent
import org.bukkit.Bukkit
import org.bukkit.Color
import org.bukkit.FireworkEffect
import org.bukkit.FireworkEffect.Type
import org.bukkit.Location
import org.bukkit.Particle
import org.bukkit.Sound
import org.bukkit.entity.Firework
import org.bukkit.entity.Player

@CommandAlias("prestige")
object PrestigeCommand : SLCommand() {
	private const val COST = 250000.0
	private const val FIREWORK_INTERVAL_TICKS = 5L
	private val FIREWORK_COLORS = listOf(
		Color.RED,
		Color.BLUE,
		Color.GREEN,
		Color.YELLOW,
		Color.WHITE,
		Color.AQUA,
		Color.fromRGB(0xFFA500),
		Color.fromRGB(0xFFC0CB)
	)

	@Default
	fun onPrestige(sender: Player) {
		sender.information("Your prestige is ${PlayerXPLevelCache[sender].prestige}. At level $MAX_LEVEL, use /prestige advance to prestige.")
	}

	@Subcommand("advance")
	fun onConfirm(sender: Player, @Optional cost: Double?) {
		requireEconomyEnabled()
		requireNotInCombat(sender)
        requireMoney(sender, COST, "prestige")

        // Just to make sure nobody is piloting a ship they'd then be ineligible for.
		failIf(ActiveStarships.findByPilot(sender) != null) { "You can't prestige while piloting a starship!" }

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
		PrestigeCongratulations.startWindow(sender.uniqueId, newPrestige)

		val senderMessage = template(
			text("You reached {0} {1}! Your level is now {2}. Good luck!", GREEN),
			text("prestige", DARK_PURPLE),
			text(newPrestige, GOLD),
			text("1", GOLD)
		)
		.append(Component.newline())
		.append(text("${COST.toCreditsString()} credits have been removed from your balance.", GREEN))
		
		sender.sendMessage(senderMessage)

		val announcement = template(
			text("{0} reached prestige {1}{2}", DARK_PURPLE),
			text(sender.name, GOLD),
			text(newPrestige, GOLD),
			text("!".repeat(newPrestige.coerceIn(0, 40)), GREEN)
		).decorate(BOLD)

		Notify.chatAndGlobal(Component.newline().append(announcement))
		Bukkit.broadcast(
			text("[Click here to congratulate them]", GRAY)
				.decorate(UNDERLINED)
				.clickEvent(ClickEvent.runCommand("/prestige congratulate ${sender.name}"))
		)

		val origin = sender.location
		val world = sender.world
		for ((offsetX, offsetZ) in listOf(-1.5 to -1.0, 1.5 to -1.0, 0.0 to 1.5)) {
			val location = origin.clone().add(offsetX, 2.0, offsetZ)
			world.spawnParticle(Particle.FIREWORK, location, 30, 0.35, 0.35, 0.35, 0.1)
		}
		world.playSound(origin, Sound.ENTITY_FIREWORK_ROCKET_TWINKLE, 1.0f, 1.0f)
	}

	@Subcommand("congratulate")
	@CommandCompletion("@players")
	fun onCongratulate(sender: Player, targetName: String) {
		val target = Bukkit.getOfflinePlayerIfCached(targetName) ?: fail { "Player $targetName not found." }
		val entry = PrestigeCongratulations[target.uniqueId] ?: fail { "$targetName has not prestiged on this server recently." }

		if (entry.isExpired()) {
			PrestigeCongratulations.remove(target.uniqueId, entry)
			fail { "The congratulation window for $targetName has expired." }
		}

		failIf(!entry.congratulatedBy.add(sender.uniqueId)) { "You have already congratulated $targetName." }
		val fireworksPerSpot = (entry.prestige.coerceAtMost(14) + 1)

		val displayName = target.name ?: targetName
		Notify.chatAndGlobal(
			template(
				text("{0} congratulated {1} on {2}!", GREEN),
				text(sender.name, GOLD),
				text(displayName, GOLD),
				text("prestiging", DARK_PURPLE)
			)
		)

		for (offset in listOf(-1.5 to -1.0, 1.5 to -1.0, 0.0 to 1.5)) {
			spawnFireworks(sender.location, offset, fireworksPerSpot)
		}
		sender.world.playSound(sender.location, Sound.ENTITY_FIREWORK_ROCKET_TWINKLE, 1.0f, 1.0f)

		Bukkit.getPlayer(target.uniqueId)?.let { onlineTarget ->
			spawnFireworks(onlineTarget.location, listOf(-1.5 to -1.0, 1.5 to -1.0, 0.0 to 1.5).random(), fireworksPerSpot)
			onlineTarget.world.playSound(onlineTarget.location, Sound.ENTITY_FIREWORK_ROCKET_TWINKLE, 1.0f, 1.0f)
		}
	}

	private fun spawnFireworks(origin: Location, offset: Pair<Double, Double>, count: Int) {
		val location = origin.clone().add(offset.first, 2.0, offset.second)
		repeat(count.coerceIn(1, 15)) { index ->
			val launch = { 
				val color = FIREWORK_COLORS.random()
				val firework = location.world.spawn(location, Firework::class.java)
				firework.fireworkMeta = firework.fireworkMeta.apply {
					power = 0
					addEffect(FireworkEffect.builder().with(Type.BURST).withColor(color).build())
				}
				
				firework.detonate()
			}

			if (index == 0) launch()
			else Tasks.syncDelay(index * FIREWORK_INTERVAL_TICKS, launch)
		}
	}
}