package net.horizonsend.ion.server.miscellaneous.commands

import co.aikar.commands.BaseCommand
import co.aikar.commands.annotation.CommandAlias
import co.aikar.commands.annotation.CommandPermission
import co.aikar.commands.annotation.Default
import net.starlegacy.database.schema.misc.SLPlayer
import org.bukkit.entity.Player
import kotlin.math.floor

@CommandAlias("calcxp")
@CommandPermission("ion.calcxp")
class CalcExpCommand : BaseCommand() {
	@Default
	@Suppress("Unused")
	fun onCustomItemCommand(
		sender: Player,
		targetLevel: Int
	) {
		val xp = f2(targetLevel, SLPlayer[sender].level, SLPlayer[sender].xp)
		val creds = 2.5 * xp

		sender.sendRichMessage(
			"""

				<#b8e0d4><bold>Level ${SLPlayer[sender].level} -> Level $targetLevel</bold>

				<dark_gray>▪ <#eac4d5>Target XP<gray>: <#d6eadf>$xp
				<dark_gray>▪ <#eac4d5>Price<gray>: <#d6eadf>${creds}C

			""".trimIndent()
		)
	}

	private fun f(x: Int): Double {
		return 50 * x * maxOf(1.0, floor(x / 10.0)) + 500
	}

	private fun f2(targetLevel: Int, level: Int, xp: Int) = (level..targetLevel).reduce { acc, i -> (acc + f(i)).toInt() } - xp
}
