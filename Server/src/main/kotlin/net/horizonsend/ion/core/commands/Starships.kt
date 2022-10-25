package net.horizonsend.ion.core.commands

import co.aikar.commands.BaseCommand
import co.aikar.commands.annotation.CommandAlias
import co.aikar.commands.annotation.Default
import net.starlegacy.database.schema.misc.SLPlayer
import net.starlegacy.database.schema.starships.PlayerStarshipData.Companion.findByPilot
import net.starlegacy.database.slPlayerId
import net.starlegacy.util.blockKeyX
import net.starlegacy.util.blockKeyY
import net.starlegacy.util.blockKeyZ
import org.bukkit.entity.Player

@CommandAlias("starships")
internal class Starships : BaseCommand() {
	@Default
	fun starships(sender: Player) {
		sender.sendRichMessage(
			findByPilot(sender.slPlayerId).map {
				val x = blockKeyX(it.blockKey)
				val y = blockKeyY(it.blockKey)
				val z = blockKeyZ(it.blockKey)

				"${it.starshipType.formatted} in world <gold>${it.levelName}</gold> at <green>$x</green>, <green>$y</green>, <green>$z</green> owned by <aqua>${SLPlayer[it.captain]?.lastKnownName}</aqua>"

			}.joinToString("\n", "<bold>Starships:</bold><gray>")
		)
	}
}