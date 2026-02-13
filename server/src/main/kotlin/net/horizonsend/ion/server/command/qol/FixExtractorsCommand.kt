package net.horizonsend.ion.server.command.qol

import co.aikar.commands.annotation.CommandAlias
import co.aikar.commands.annotation.CommandPermission
import co.aikar.commands.annotation.Default
import com.sk89q.worldedit.regions.Region
import net.horizonsend.ion.common.extensions.information
import net.horizonsend.ion.common.extensions.success
import net.horizonsend.ion.common.extensions.userError
import net.horizonsend.ion.server.command.SLCommand
import net.horizonsend.ion.server.features.multiblock.MultiblockAccess
import net.horizonsend.ion.server.features.multiblock.MultiblockEntities
import net.horizonsend.ion.server.features.transport.NewTransport
import net.horizonsend.ion.server.features.transport.manager.extractors.ExtractorManager
import net.horizonsend.ion.server.features.world.IonWorld.Companion.hasFlag
import net.horizonsend.ion.server.features.world.WorldFlag
import net.horizonsend.ion.server.miscellaneous.utils.getBlockDataSafe
import net.horizonsend.ion.server.miscellaneous.utils.getBlockTypeSafe
import net.horizonsend.ion.server.miscellaneous.utils.getSelection
import net.horizonsend.ion.server.miscellaneous.utils.isWallSign
import net.kyori.adventure.audience.Audience
import org.bukkit.block.Sign
import org.bukkit.block.data.type.DaylightDetector
import org.bukkit.entity.Player

@CommandPermission("ion.fixextractors")
object FixExtractorsCommand : SLCommand() {
	@CommandAlias("fixextractors")
	@Suppress("unused")
	fun onFixExtractors(sender: Player) {
		val selection = getSelection(sender, 200000) ?: return

		if (sender.world.name != selection.world?.name) return

		var count = 0

		for (blockPosition in selection) {
			val x = blockPosition.x()
			val y = blockPosition.y()
			val z = blockPosition.z()

			val block = sender.world.getBlockAt(x, y, z)

			if (getBlockTypeSafe(sender.world, x, y, z)?.isWallSign == true) {
				tryFixMultiblock(sender, block.state as Sign)
			}

			if (!ExtractorManager.isExtractorData(block.blockData)) continue
			if (NewTransport.isExtractor(sender.world, x, y, z)) continue

			count++
			NewTransport.addExtractor(sender.world, x, y, z)
		}

		sender.success("Registered $count new extractors")
	}

	@CommandAlias("fixsolarpanels")
	@Suppress("unused")
	fun onFixSolarPanels(sender: Player) {
		val selection = getSelection(sender, 200000) ?: return

		if (sender.world.name != selection.world?.name) return

		if (!sender.world.hasFlag(WorldFlag.SPACE_WORLD)) {
			sender.userError("Only inverted daylight detectors in space worlds need to be fixed")
			return
		}

		var count = 0

		for (blockPosition in selection) {
			val x = blockPosition.x()
			val y = blockPosition.y()
			val z = blockPosition.z()

			val block = sender.world.getBlockAt(x, y, z)
			val data = getBlockDataSafe(sender.world, x, y, z) as? DaylightDetector ?: continue

			if (!data.isInverted) continue

			data.power = data.maximumPower
            block.blockData = data

			count++
		}

		sender.success("Fixed $count inverted daylight detectors")
	}

	private fun getSelection(sender: Player, maxSelectionVolume: Int): Region? {
		val selection = sender.getSelection() ?: return null
		if (selection.volume > maxSelectionVolume && !sender.hasPermission("group.dutymode")) {
			sender.userError("Selection too large! The maximum volume is $maxSelectionVolume.")
			return null
		}

		return selection
	}

	fun tryFixMultiblock(user: Audience?, sign: Sign) {
		MultiblockAccess.getStored(sign) ?: return
		if (MultiblockEntities.getMultiblockEntity(sign) != null) return

		MultiblockEntities.loadFromSign(sign)
		user?.information("Attempting to fix multiblock entity")
	}
}
