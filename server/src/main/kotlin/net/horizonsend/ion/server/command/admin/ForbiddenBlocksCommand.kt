package net.horizonsend.ion.server.command.admin

import co.aikar.commands.annotation.CommandAlias
import co.aikar.commands.annotation.CommandPermission
import co.aikar.commands.annotation.Optional
import co.aikar.commands.annotation.Subcommand
import com.sk89q.worldedit.WorldEdit
import com.sk89q.worldedit.bukkit.BukkitAdapter
import com.sk89q.worldedit.extension.input.ParserContext
import com.sk89q.worldedit.function.mask.AbstractExtentMask
import com.sk89q.worldedit.function.mask.Masks
import com.sk89q.worldedit.math.BlockVector3
import it.unimi.dsi.fastutil.longs.LongOpenHashSet
import net.horizonsend.ion.common.extensions.success
import net.horizonsend.ion.common.extensions.userError
import net.horizonsend.ion.server.command.SLCommand
import net.horizonsend.ion.server.features.client.display.ClientDisplayEntities.highlightBlocks
import net.horizonsend.ion.server.features.world.IonWorld.Companion.ion
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.toBlockKey
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.toVec3i
import net.horizonsend.ion.server.miscellaneous.utils.getSelection
import org.bukkit.entity.Player

@CommandPermission("ion.command.admin.forbiddenblocks")
@CommandAlias("forbiddenblocks")
object ForbiddenBlocksCommand : SLCommand() {
	@Subcommand("add")
	fun add(sender: Player, @Optional maskInput: String?) {
		val selection = sender.getSelection() ?: return sender.userError("You must make a selection!")
		val chunk = sender.world.ion
		val blocks = chunk.detectionForbiddenBlocks

		val mask = WorldEdit.getInstance().maskFactory.parseFromInput(maskInput ?: "", ParserContext()) ?: Masks.alwaysTrue()
		(mask as? AbstractExtentMask)?.extent = BukkitAdapter.adapt(sender.world)

		val new = LongOpenHashSet()

		for (blockVector in selection) {
			if (!mask.test(blockVector)) continue
			new.add(toBlockKey(blockVector.x(), blockVector.y(), blockVector.z()))
		}

		blocks.addAll(new)

		chunk.saveForbiddenBlocks()
		sender.success("Added ${new.size} blocks to list")
	}

	@Subcommand("remove")
	fun remove(sender: Player, @Optional maskInput: String?) {
		val selection = sender.getSelection() ?: return sender.userError("You must make a selection!")
		val chunk = sender.world.ion
		val blocks = chunk.detectionForbiddenBlocks

		val old = blocks.size

		val mask = WorldEdit.getInstance().maskFactory.parseFromInput(maskInput ?: "", ParserContext()) ?: Masks.alwaysTrue()
		(mask as? AbstractExtentMask)?.extent = BukkitAdapter.adapt(sender.world)

		blocks.removeAll {
			val pos = toVec3i(it)
			selection.contains(pos.x, pos.y, pos.z) && mask.test(BlockVector3.at(pos.x, pos.y, pos.z))
		}

		val new = blocks.size

		chunk.saveForbiddenBlocks()
		sender.success("Removed ${old - new} blocks from list")
	}

	@Subcommand("clear")
	fun remove(sender: Player, @Optional confirm: Boolean?) {
		if (confirm != true) return sender.userError("You must confirm")

		val chunk = sender.world.ion
		val blocks = chunk.detectionForbiddenBlocks
		val x = blocks.size
		blocks.clear()

		chunk.saveForbiddenBlocks()
		sender.success("Removed $x blocks from list")
	}

	@Subcommand("show all")
	fun showAll(sender: Player) {
		val blocks = sender.world.ion.detectionForbiddenBlocks.map(::toVec3i)
		sender.highlightBlocks(blocks, 40L)
	}

	@Subcommand("show selection")
	fun showSelection(sender: Player) {
		val selection = sender.getSelection() ?: return sender.userError("You must make a selection!")
		val blocks = sender.world.ion.detectionForbiddenBlocks
		val intersect = blocks.filterTo(mutableSetOf()) {
			val (x, y, z) = toVec3i(it)
			selection.contains(x, y, z)
		}.map(::toVec3i)

		sender.highlightBlocks(intersect, 40L)
	}
}
