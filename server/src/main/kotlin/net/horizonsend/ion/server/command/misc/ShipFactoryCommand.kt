package net.horizonsend.ion.server.command.misc

import co.aikar.commands.annotation.CommandAlias
import co.aikar.commands.annotation.Optional
import co.aikar.commands.annotation.Subcommand
import net.horizonsend.ion.common.extensions.userError
import net.horizonsend.ion.common.utils.text.formatPaginatedMenu
import net.horizonsend.ion.common.utils.text.ofChildren
import net.horizonsend.ion.common.utils.text.toComponent
import net.horizonsend.ion.server.command.SLCommand
import net.horizonsend.ion.server.features.starship.factory.StarshipFactories
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.entity.Player

@CommandAlias("shipfactory")
object ShipFactoryCommand : SLCommand() {
	@Subcommand("listmissing")
	fun listMaterialsPaginated(sender: Player, @Optional page: Int?) {
		val missing = StarshipFactories.missingMaterialsCache[sender.uniqueId]

		if (missing == null) {
			sender.userError("You have no missing materials on record. Try running the ship factory again. (5 minute expiration)")
			return
		}

		val items = missing.entries.toList().sortedBy { it.value }

		sender.sendMessage(formatPaginatedMenu(
			entries = items.size,
			command = "/shipfactory listmissing",
			currentPage = page ?: 1,
		) { index ->
			val (item, count) = items[index]

			return@formatPaginatedMenu ofChildren(
				item.toComponent(color = NamedTextColor.RED),
				text(": ", NamedTextColor.DARK_GRAY),
				text(count, NamedTextColor.LIGHT_PURPLE)
			)
		})
	}

	@Subcommand("listmissing all")
	fun listMaterialsAll(sender: Player, @Optional page: Int?) {
		val missing = StarshipFactories.missingMaterialsCache[sender.uniqueId]

		if (missing == null) {
			sender.userError("You have no missing materials on record. Try running the ship factory again. (5 minute expiration)")
			return
		}

		sender.sendMessage(StarshipFactories.getPrintItemCountString(missing))
	}
}
