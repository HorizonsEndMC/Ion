package net.horizonsend.ion.server.features.multiblock.util

import net.horizonsend.ion.server.features.multiblock.shape.BlockRequirement
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.RelativeFace
import org.bukkit.Material
import org.bukkit.block.BlockFace
import org.bukkit.block.data.Bisected
import org.bukkit.block.data.BlockData
import org.bukkit.block.data.type.Fence
import org.bukkit.block.data.type.Stairs

object PrepackagedPreset {
	fun stairs(face: RelativeFace, half: Bisected.Half, shape: Stairs.Shape): BlockRequirement.() -> Unit = {
		example = Material.STONE_BRICK_STAIRS.createBlockData()
		addPlacementModification { multiblockDirection: BlockFace, stairs: BlockData ->
			stairs as Stairs
			stairs.half = half
			stairs.shape = shape
			stairs.facing = face[multiblockDirection]
		}
	}

	fun pane(vararg face: RelativeFace): BlockRequirement.() -> Unit = {
		example = Material.GLASS_PANE.createBlockData()
		addPlacementModification { multiblockDirection: BlockFace, pane: BlockData ->
			pane as Fence
			face.forEach { pane.setFace(it[multiblockDirection], true) }
		}
	}
}
