package net.horizonsend.ion.server.features.client.whereisit

import co.aikar.commands.annotation.CommandAlias
import co.aikar.commands.annotation.CommandCompletion
import co.aikar.commands.annotation.CommandPermission
import co.aikar.commands.annotation.Default
import co.aikar.commands.annotation.Optional
import co.aikar.commands.annotation.Subcommand
import net.horizonsend.ion.common.extensions.userError
import net.horizonsend.ion.server.features.client.whereisit.mod.Searcher
import net.horizonsend.ion.server.features.customitems.CustomItems
import net.horizonsend.ion.server.miscellaneous.utils.Tasks
import net.horizonsend.ion.server.miscellaneous.utils.highlightBlock
import net.horizonsend.ion.server.miscellaneous.utils.toVec3i
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

@CommandAlias("itemsearch")
@CommandPermission("ion.search")
object SearchCommand : net.horizonsend.ion.server.command.SLCommand() {
	@Default
	fun default(
		player: Player,
		@Optional material: Material?
	) = searchAndSend(player, if (material != null) ItemStack(material) else player.inventory.itemInMainHand)

	@Subcommand("custom")
	@CommandCompletion("@customItem")
	fun custom(
		player: Player,
		customItem: String
	) {
		val itemStack = CustomItems.getByIdentifier(customItem)?.constructItemStack() ?: run {
			player.userError("Can't find item $customItem!")
			return
		}

		searchAndSend(player, itemStack)
	}

	@Subcommand("legacy")
	@CommandCompletion("@customitems")
	fun legacy(
		player: Player,
		customItem: String
	) {
		val itemStack = net.horizonsend.ion.server.miscellaneous.registrations.legacy.CustomItems[customItem]?.itemStack(1) ?: run {
			player.userError("Can't find item $customItem!")
			return
		}

		searchAndSend(player, itemStack)
	}

	private fun searchAndSend(player: Player, stack: ItemStack) = Tasks.async {
		val res = Searcher.searchItemStack(
			player,
			stack
		)

		if (res.isEmpty()) {
			player.userError("Couldn't find any item in a 20 block range.")
			return@async
		}

		for (pos in res.keys) {
			highlightBlock(player, pos.toVec3i(), 10 * 20)
		}

		player.sendRichMessage(
			"<hover:show_text:\"${res.keys.joinToString("\n") { "<gray>X: ${it.x} Y: ${it.y} Z: ${it.z}" }}\">" +
				"<#7f7fff>Found ${res.size} containers. [Hover]"
		)
	}
}
