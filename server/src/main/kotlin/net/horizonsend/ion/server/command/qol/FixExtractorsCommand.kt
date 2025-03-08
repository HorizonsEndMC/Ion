package net.horizonsend.ion.server.command.qol

import co.aikar.commands.annotation.CommandAlias
import co.aikar.commands.annotation.CommandPermission
import co.aikar.commands.annotation.Default
import net.horizonsend.ion.common.extensions.success
import net.horizonsend.ion.common.extensions.userError
import net.horizonsend.ion.server.command.SLCommand
import net.horizonsend.ion.server.features.transport.NewTransport
import net.horizonsend.ion.server.features.transport.manager.extractors.ExtractorManager
import net.horizonsend.ion.server.miscellaneous.utils.getSelection
import org.bukkit.entity.Player

@CommandAlias("fixextractors")
@CommandPermission("ion.fixextractors")
object FixExtractorsCommand : SLCommand() {
	@Default
	@Suppress("unused")
	fun onFixExtractors(sender: Player) {
		val maxSelectionVolume = 200000
		val selection = sender.getSelection() ?: return
		if (selection.volume > maxSelectionVolume && !sender.hasPermission("group.dutymode")) {
			sender.userError("Selection too large! The maximum volume is $maxSelectionVolume.")
			return
		}

		if (sender.world.name != selection.world?.name) return

		var count = 0

		for (blockPosition in selection) {
			val x = blockPosition.x()
			val y = blockPosition.y()
			val z = blockPosition.z()

			val block = sender.world.getBlockAt(x, y, z)

			if (!ExtractorManager.isExtractorData(block.blockData)) continue
			if (NewTransport.isExtractor(sender.world, x, y, z)) continue

			count++
			NewTransport.addExtractor(sender.world, x, y, z)
		}

		sender.success("Registered $count new extractors")
	}
}
