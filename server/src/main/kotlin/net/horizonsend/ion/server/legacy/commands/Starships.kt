package net.horizonsend.ion.server.legacy.commands

import co.aikar.commands.annotation.CommandAlias
import co.aikar.commands.annotation.Default
import net.horizonsend.ion.server.IonServer
import net.horizonsend.ion.common.database.schema.misc.SLPlayer
import net.horizonsend.ion.common.database.schema.starships.PlayerStarshipData
import net.horizonsend.ion.server.miscellaneous.utils.slPlayerId
import net.starlegacy.command.SLCommand
import net.starlegacy.util.blockKeyX
import net.starlegacy.util.blockKeyY
import net.starlegacy.util.blockKeyZ
import org.bukkit.entity.Player
import org.litote.kmongo.and
import org.litote.kmongo.eq
import org.litote.kmongo.or

@CommandAlias("starships")
object Starships : SLCommand() {
	@Default
	fun starships(sender: Player) {
		sender.sendRichMessage(
			PlayerStarshipData.find(
				and(
					PlayerStarshipData::captain eq sender.slPlayerId,
					or(
						PlayerStarshipData::serverName eq null,
						PlayerStarshipData::serverName eq IonServer.configuration.serverName
					)
				)
			).joinToString("\n", "<bold>Starships:</bold><gray>\n") {
				val x = blockKeyX(it.blockKey)
				val y = blockKeyY(it.blockKey)
				val z = blockKeyZ(it.blockKey)

				val ownedBy =
					if (it.captain != sender.slPlayerId) " owned by <aqua>${SLPlayer[it.captain]?.lastKnownName}</aqua>" else ""
				val serverUnknown = if (it.serverName == null) " <red>(Unspecified Server)</red>" else ""

				"${it.starshipType.formatted()} at <green>$x</green>, <green>$y</green>, <green>$z</green> @ <gold>${it.levelName}</gold>$ownedBy$serverUnknown"
			}
		)
	}
}
