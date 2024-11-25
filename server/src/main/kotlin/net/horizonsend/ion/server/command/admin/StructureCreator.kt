package net.horizonsend.ion.server.command.admin

import co.aikar.commands.annotation.CommandAlias
import co.aikar.commands.annotation.CommandPermission
import co.aikar.commands.annotation.Default
import net.horizonsend.ion.common.extensions.information
import net.horizonsend.ion.server.command.SLCommand
import net.horizonsend.ion.server.features.custom.blocks.CustomBlocks
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.RelativeFace
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.Vec3i
import net.horizonsend.ion.server.miscellaneous.utils.isConcrete
import net.horizonsend.ion.server.miscellaneous.utils.isGlassPane
import net.horizonsend.ion.server.miscellaneous.utils.isSlab
import net.horizonsend.ion.server.miscellaneous.utils.isStairs
import net.horizonsend.ion.server.miscellaneous.utils.rightFace
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.event.ClickEvent
import org.bukkit.Material
import org.bukkit.block.BlockFace
import org.bukkit.block.data.BlockData
import org.bukkit.block.data.type.GlassPane
import org.bukkit.block.data.type.Slab
import org.bukkit.block.data.type.Stairs
import org.bukkit.entity.Player

@CommandPermission("ion.admin.structurecreator")
@CommandAlias("structurecreator")
object StructureCreator : SLCommand() {
	@Default
	fun onCreate(sender: Player) {
		sender.information("Will attempt to create a multiblock structure using the block in your crosshair as an origin, and the way you are facing as forwards.")

		val forwards = sender.facing
		val right = forwards.rightFace

		val selection = requireSelection(sender)
		val origin = sender.getTargetBlockExact(10) ?: fail { "You're not targeting a block!" }

		val selectionMin = selection.minimumPoint
		val selectionMax = selection.maximumPoint

		val requirements = mutableMapOf<Vec3i, String>()

		for (x in selectionMin.x..selectionMax.x) {
			for (y in selectionMin.y..selectionMax.y) {
				for (z in selectionMin.z..selectionMax.z) {
					val relativeX = x - origin.x
					val relativeY = y - origin.y
					val relativeZ = z - origin.z

					val rightOffset = (right.modX * relativeX) + (right.modZ * relativeZ)
					val forwardOffset = (forwards.modX * relativeX) + (forwards.modZ * relativeZ)

					val data = sender.world.getBlockData(x, y, z)
					if (data.material.isAir) continue
					requirements[Vec3i(rightOffset, relativeY, forwardOffset)] = getBlockRequirement(data, forwards)
				}
			}
		}

		var structure = "override fun MultiblockShape.buildStructure() {"

		for ((z, zEntries) in requirements.entries.groupBy { it.key.z }) {
			val yGrouped = zEntries.groupBy { it.key.y }
			structure += "\n\tz($z) {"

			for ((y, yEntries) in yGrouped) {
				val xGrouped = yEntries.groupBy { it.key.x }
				structure += "\n\t\ty($y) {"

				for ((x, xEntries) in xGrouped) {
					if (xEntries.isEmpty()) continue
					if (xEntries.size > 1) throw IllegalArgumentException("More than 1 placement in multiblock shape at $x, $y, $z")

					val xEntry = xEntries.first()

					structure += "\n\t\t\tx($x)${xEntry.value}"
				}

				structure += "\n\t\t}"
			}

			structure += "\n\t}"
		}

		structure += "\n}"

		sender.sendMessage(Component.text(structure).hoverEvent(Component.text(structure)).clickEvent(ClickEvent.copyToClipboard(structure)))
	}

	private fun getBlockRequirement(data: BlockData, forwards: BlockFace): String {
		val customBlock = CustomBlocks.getByBlockData(data)
		if (customBlock != null) return ".customBlock(CustomBlocks.${customBlock.identifier})"

		return when {
			data.material.isConcrete -> ".carbyne()"
			data.material == Material.FURNACE -> ".machineFurnace()"

			data.material.isStairs -> {
				data as Stairs
				val facing = RelativeFace[forwards, data.facing]
				val half = data.half
				val shape = data.shape

				".anyStairs(PrepackagedPreset.stairs($facing, $half, shape = $shape))"
			}

			data.material.isSlab -> {
				data as Slab
				val type = data.type

				if (type == Slab.Type.DOUBLE) return "PrepackagedPreset.doubleSlab()"

				".anySlab(PrepackagedPreset.slab($type))"
			}

			data.material.isGlassPane -> {
				data as GlassPane
				val faces = data.faces.map { RelativeFace[forwards, it] }
				".anyGlassPane(PrepackagedPreset.pane(${faces.joinToString { it.name }}))"
			}

			else -> ".type(Material.${data.material.name})"
		}
	}
}
