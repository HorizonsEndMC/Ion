package net.horizonsend.ion.server.features.multiblock.util

import net.horizonsend.ion.server.features.multiblock.shape.BlockRequirement
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.RelativeFace
import org.bukkit.Material
import org.bukkit.block.BlockFace
import org.bukkit.block.data.Bisected
import org.bukkit.block.data.BlockData
import org.bukkit.block.data.MultipleFacing
import org.bukkit.block.data.type.Slab
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
			pane as MultipleFacing
			face.forEach { pane.setFace(it[multiblockDirection], true) }
		}
	}

//	fun wall(vararg face: RelativeFace): BlockRequirement.() -> Unit = {
//		example = Material.STONE_BRICK_WALL.createBlockData()
//		addPlacementModification { multiblockDirection: BlockFace, pane: BlockData ->
//			pane as MultipleFacing
//			face.forEach { pane.setFace(it[multiblockDirection], true) }
//		}
//	}

	fun slab(type: Slab.Type): BlockRequirement.() -> Unit = {
		example = Material.STONE_BRICK_SLAB.createBlockData()
		addPlacementModification { multiblockDirection: BlockFace, slab: BlockData ->
			slab as Slab
			slab.type = type
		}
	}

	fun blockUpdate(): BlockRequirement.() -> Unit = {
		blockUpdate = true
	}
}
