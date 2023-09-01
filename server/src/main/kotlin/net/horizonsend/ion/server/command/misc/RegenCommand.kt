package net.horizonsend.ion.server.command.misc

import co.aikar.commands.annotation.CommandAlias
import co.aikar.commands.annotation.CommandPermission
import co.aikar.commands.annotation.Default
import net.horizonsend.ion.common.extensions.success
import net.horizonsend.ion.server.command.SLCommand
import net.horizonsend.ion.server.features.explosion.reversal.Regeneration.regenerateBlocks
import org.bukkit.command.CommandSender
import java.io.IOException
import java.math.BigDecimal
import java.math.RoundingMode

@CommandAlias("regen")
@CommandPermission("ion.regen")
object RegenCommand : SLCommand() {
	@Default
	@Suppress("unused")
	fun onRegen(sender: CommandSender) {
		val start = System.nanoTime()
		var regeneratedBlocks = 0

		try {
			regeneratedBlocks = regenerateBlocks()
		} catch (e: IOException) {
			e.printStackTrace()
		}

		val elapsed = System.nanoTime() - start

		val seconds: String = BigDecimal(elapsed / 1000000000.0)
			.setScale(6, RoundingMode.HALF_UP)
			.toPlainString()

		sender.success("Regenerated $regeneratedBlocks blocks in $seconds seconds.")
	}
}
