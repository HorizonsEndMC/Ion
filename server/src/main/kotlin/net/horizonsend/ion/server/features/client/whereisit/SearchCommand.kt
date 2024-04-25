package net.horizonsend.ion.server.features.client.whereisit

import co.aikar.commands.annotation.CommandAlias
import co.aikar.commands.annotation.CommandCompletion
import co.aikar.commands.annotation.CommandPermission
import co.aikar.commands.annotation.Default
import net.horizonsend.ion.common.extensions.userError
import net.horizonsend.ion.server.command.GlobalCompletions
import net.horizonsend.ion.server.command.SLCommand
import net.horizonsend.ion.server.features.client.display.ClientDisplayEntities.highlightBlock
import net.horizonsend.ion.server.features.client.display.ClientDisplayEntities.sendEntityPacket
import net.horizonsend.ion.server.features.client.whereisit.mod.Searcher
import net.horizonsend.ion.server.miscellaneous.utils.Tasks
import net.horizonsend.ion.server.miscellaneous.utils.minecraft
import net.horizonsend.ion.server.miscellaneous.utils.toVec3i
import net.minecraft.core.BlockPos
import org.bukkit.entity.Player

@CommandAlias("itemsearch")
@CommandPermission("ion.search")
object SearchCommand : SLCommand() {
	@Default
	@CommandCompletion("@anyItem")
	fun default( player: Player, vararg itemStrList: String) = Tasks.async {
		val strList = itemStrList.toList()
		val locationSet = mutableSetOf<BlockPos>()
		for(str in strList){
			val item = GlobalCompletions.stringToItem(str) ?: player.inventory.itemInMainHand
			val res = Searcher.searchItemStack(player, item)
			for (pos in res.keys) {
				sendEntityPacket(player, highlightBlock(player.world.minecraft, pos.toVec3i()), 10 * 20)
				res.mapTo(locationSet) { it.key }
			}
		}
		if(itemStrList.isEmpty()){
			player.userError("Specify which item to search.")
		}else if (locationSet.size == 0) {
			player.userError("Couldn't find any item in a 20 block range.")
			return@async
		} else {
			player.sendRichMessage(
				"<hover:show_text:\"${locationSet.joinToString("\n") { "<gray>X: ${it.x} Y: ${it.y} Z: ${it.z}" }}\">" +
				"<#7f7fff>Found ${locationSet.size} containers. [Hover]"
			)
		}
	}
}
